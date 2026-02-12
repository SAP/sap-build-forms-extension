/**
 *
 */
export enum Severity {
    Error = 4,
    Warning = 3,
    Info = 2,
    Success = 1,
    None = 0,
}

/**
 *
 */
export interface ElementInfo {
    severity: Severity
    key: string | undefined
    params: Record<string, any>
}

/**
 *
 */
export const ROOT_ROW = "_"

/**
 *
 */
export type ElementMap = Record<string, Element>

/**
 *
­­ */
export interface ElementMapRow {
    id: string
    values: ElementMap
    sel: boolean
}

/**
 *
 */
export type DataTypes = string | number | boolean | Date | ElementInfo

/**
 *
 */
export type ElementDataType = DataTypes | ElementMap | ElementMapRow[]

/**
 *
 */
export class Element {
    key: string
    nm?: string
    va?: ElementDataType
    vi?: boolean
    ed?: boolean
    rq?: boolean
    msg?: boolean | ElementInfo

    constructor(key: string, name?: string) {
        this.key = key

        if (typeof name === "string") {
            this.nm = name
        }
    }
}

/**
 *
 * @param value
 * @returns
 */
export function isElementMap(value: ElementDataType): boolean {
    return (
        typeof value === "object" &&
        Array.isArray(value) === false &&
        typeof (value as any)["getMonth"] === "undefined"
    )
}

/**
 *
 * @param value
 * @returns
 */
export function isElementMapRows(value: ElementDataType): boolean {
    return value != null && Array.isArray(value) === true
}

/**
 *
 */
export class Form implements ElementMapRow {
    id: string = "_"
    sel: boolean = false
    version: string
    values: ElementMap

    /**
     *
     * @param values
     */
    constructor(version: string, values: ElementMap) {
        this.version = version
        this.values = values
    }
}

/**
 *
 */
export class FormService {
    /**
     *
     * @param values
     * @param key
     * @returns
     */
    public static findElementByKey(key: string, values: ElementMap): Element | undefined {
        if (typeof values[key] !== "undefined") {
            return values[key]
        }

        return undefined
    }

    /**
     *
     * @param rowId
     * @param values
     * @returns
     */
    public static findRowById(rowId: string, row: ElementMapRow): ElementMapRow | undefined {
        if (rowId == row.id) {
            return row
        }

        for (let value of Object.values(row.values)) {
            // console.log(
            //     `__ checking '${value.key}, type='${typeof value.va}', array=${Array.isArray(
            //         value.va,
            //     )}`,
            // )
            if (value.va && isElementMapRows(value.va)) {
                // console.log("__ found mapRow ")
                for (let row of value.va as ElementMapRow[]) {
                    // console.log(`__ comparing rowId '${rowId}' with row '${row.id}'`)
                    if (row.id === rowId) {
                        // console.log("__ SUCCESS: found row with id '${rowId}'")
                        return row
                    }
                    const result = FormService.findRowById(rowId, row)
                    if (result) {
                        return result
                    }
                }
            }
        }

        // console.log(`Cannot find row '${rowId}'`)
        return undefined
    }

    /**
     *
     * @param rowId
     * @param key
     * @param row
     * @returns
     */
    public static findElementByRowAndKey(
        rowId: string | undefined,
        key: string,
        row: ElementMapRow,
    ): Element | undefined {
        if (typeof rowId === "string") {
            const v = FormService.findRowById(rowId, row)
            if (v) {
                row = v
            }
        }

        return FormService.findElementByKey(key, row.values)
    }
}
