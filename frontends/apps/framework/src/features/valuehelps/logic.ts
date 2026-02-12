import { api, apiOk } from "commons"
import { localstore } from "../localstore"
import { ValueHelpInfo } from "../sessions/definitions"

interface ValueHelpVersion {
    name: string
    version: number
    values: Record<string, string>
}

interface LoadValueHelpResponse {
    name: string
    locale: string
    version: number
    values: string
}

export interface ValueName {
    value: string
    name: string
}

/**
 *
 */
export class ValuehelpsService {
    /**
     * check all value-helps and identify value-helps that needed to be loaded
     *
     * @param vhs
     * @param sessionId
     * @param locale
     * @returns
     */
    public static async check(
        vhs: Record<string, number>,
        locale: string,
    ): Promise<Record<string, boolean>> {
        let result: Record<string, boolean> = {}

        for (const vh in vhs) {
            // retrieve the number of records from local storage
            const count = await localstore.valuehelps
                .where({ name: vh, locale, version: vhs[vh] })
                .count()
            if (count > 1) {
                throw Error(`value-help '${vh}' has more that 1 instances for locale '${locale}'`)
            }
            result[vh] = count == 1
        }

        return result
    }

    /**
     *
     * @param name
     * @param sessionId
     * @param locale
     * @returns
     */
    public static async load(
        name: string,
        sessionId: string,
        locale: string,
    ): Promise<Error | string> {
        // console.log(`load value-help '${name}' for locale '${locale}' from backend service`)

        // trigger load of not current valuehelps from backend service
        const response = await api.get<LoadValueHelpResponse>(
            `v1/valuehelpvalues/${name}/${locale}`,
            {
                headers: { session: sessionId },
            },
        )

        // if response is ok then go through all returned value-helps and store it in localstore
        if (!apiOk(response.status)) {
            console.error(`Error in valuehelpvalues: ${response.status} :'${response.data}'`)
            return Error(response.status + ": " + response.statusText)
        }

        const data = response.data
        await localstore.valuehelps.where({ name, locale }).delete()
        await localstore.valuehelps.add({
            name,
            locale,
            version: data.version,
            values: JSON.parse(data.values),
        })

        return name
    }

    /**
     *
     * @param name
     * @param locale
     * @returns
     */
    public static async loadFormLocalstore(
        name: string,
        locale: string,
    ): Promise<Record<string, string>> {
        // console.log(`load value-help '${name}' for locale '${locale}' from localstore`)

        const data = await localstore.valuehelps.where({ name, locale }).first()
        if (data) {
            return data.values
        }

        console.error(`cannot find value-help '${name}+${locale}'`)
        return {}
    }

    /**
     *
     * @param name
     * @param locale
     * @param values
     * @returns
     */
    public static async loadMultipleFromLocalstore(
        names: Array<string>,
        locale: string,
    ): Promise<Record<string, Record<string, string>>> {
        let search: Array<Array<string>> = []
        for (const name of names) {
            search.push([name, locale])
        }

        const data = await localstore.valuehelps.where("[name+locale]").anyOf(search).toArray()

        let result: Record<string, Record<string, string>> = {}
        for (const d of data) {
            result[d.name] = d.values
        }

        return result
    }

    /**
     * formatVHOption is a utility function to format a value help option based on the value help info.
     * It takes a key, value, and an optional value help info object, returning a formatted string.
     * If the value help info has a display format, it replaces placeholders with the key and value.
     * If no value help info is provided, it simply returns the value.
     *
     * @param key
     * @param value
     * @param vh
     * @returns
     */
    public static formatVHOption(key: string, value: string, vh?: ValueHelpInfo): string {
        let name = value
        if (vh && vh.displayFormat) {
            name = vh.displayFormat.replace("{key}", key).replace("{value}", name)
        }
        return name
    }

    /**
     * createVHOptions is a utility function to format value names based on the value help info.
     * It takes a value help info object and a record of values, returning an array of
     * ValueName objects with formatted names.
     *
     * @param values
     * @param vh
     * @returns
     */
    public static createVHOptions(values: Record<string, string>, vh?: ValueHelpInfo): ValueName[] {
        let opts: ValueName[] = []
        for (const key in values) {
            opts.push({ value: key, name: this.formatVHOption(key, values[key], vh) })
        }
        return opts
    }

    /**
     * createVHOptionsAsRecord is a utility function to format value names based on the value help info.
     * It takes a value help info object and a record of values, returning a record of
     * key-value pairs where the key is the value and the value is the formatted name.
     *
     * @param values
     * @param vh
     * @returns
     */
    public static createVHOptionsAsRecord(
        values: Record<string, string>,
        vh?: ValueHelpInfo,
    ): Record<string, string> {
        let opts: Record<string, string> = {}
        for (const key in values) {
            opts[key] = this.formatVHOption(key, values[key], vh)
        }
        return opts
    }
}
