import { Severity } from "commons"

/**
 *
 */
export interface ElementInfo {
    severity: Severity
    key: string | undefined
    params: Record<string, any>
    text?: string
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
export interface DateRange {
    f: string
    t: string
}

/**
 *
 */
export interface TableInfo {
    sf?: string
    sd?: string
    r?: string[]
    d?: Record<string, ElementMapRow>
    p: number
    ps: number
    s: number
}

/**
 * CurrencyAmount interface
 */
export interface CurrencyAmount {
    amount: number
    currency: string
}

/**
 * Data structure for document form control
 */
export interface DocFormData {
    selectedTab?: string
    docUrl?: string
}

/**
 *
 */
export type DataTypes = string | number | boolean | Date | DateRange | ElementInfo | TableInfo | CurrencyAmount | DocFormData

/**
 *
 */
export type ElementDataType = DataTypes | ElementMap | Attachment[]

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
    msg?: ElementInfo

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
export function isTable(value: ElementDataType): boolean {
    return value != null && typeof (value as any)["r"] !== "undefined"
}

/**
 *
 * @param value
 * @returns
 */
export function isAttachment(value: ElementDataType): boolean {
    return value != null && typeof (value as any)["size"] === "number"
}

/**
 *
 */
export interface Attachment {
    id: string
    p: number
    n: string
    ct?: string
    s: number
    c?: string
    d?: string
    co?: string
    cb?: string
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
            if (value.va && isTable(value.va)) {
                // console.log("__ found mapRow ")
                const table = value.va as TableInfo
                if (table.r) {
                    for (let row of table.r) {
                        // console.log(`__ comparing rowId '${rowId}' with row '${row.id}'`)
                        if (row === rowId) {
                            // console.log("__ SUCCESS: found row with id '${rowId}'")
                            return table.d![row]
                        }
                        const result = FormService.findRowById(rowId, table.d![row])
                        if (result) {
                            return result
                        }
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
     * @param parent
     * @returns
     */
    public static findParentRow(rowId: string, parent: ElementMapRow): ElementMapRow | undefined {
        for (let value of Object.values(parent.values)) {
            if (value.va && isTable(value.va)) {
                const table = value.va as TableInfo
                if (table.r) {
                    for (let row of table.r) {
                        if (row === rowId) {
                            return parent
                        }
                        const result = FormService.findParentRow(rowId, table.d![row])
                        if (result) {
                            return result
                        }
                    }
                }
            }
        }
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

    /**
     *
     * @param rowId
     * @param row
     * @param key
     * @returns
     */
    public static addNewElement(rowId: string, key: string, row: ElementMapRow): Element {
        const r = FormService.findRowById(rowId, row)
        if (r) {
            const e = new Element(key)
            r!.values[key] = e!
            return e
        }
        throw new Error(`Cannot find row {rowId} that is expected to be available`)
    }
}
