import { Definition } from "./definitions"
import {
    DataTypes,
    Element,
    ElementInfo,
    ElementMapRow,
    Form,
    FormService,
    isTable,
    TableInfo,
} from "./forms"

/**
 *
 */
export enum ElementProp {
    Value = "va",
    Visible = "vi",
    Editable = "ed",
    Required = "rq",
    Selected = "s",
    Message = "msg",
    Position = "p",
    PageSize = "ps",
    SortField = "sf",
    SortOrder = "so",
}

/**
 *
 */
interface Change {
    p: ElementProp
    va: DataTypes | undefined
}

/**
 *
 */
interface UpdatedInfo {
    key: string
    rowId?: string
    changes: Change[]
}

/**
 *
 */
interface DeletedInfo {
    rowId: string
    key?: string
    ids?: string[]
}

/**
 *
 */
export class FrontendJournal {
    updated: UpdatedInfo[]
    deleted: DeletedInfo[]

    constructor() {
        this.updated = []
        this.deleted = []
    }
}

/**
 *
 */
export type BackendJournal = Record<string, Record<string, Record<string, any>>>

/**
 *
 */
export class JournalService {
    /**
     *
     */
    public static init(journal: FrontendJournal) {
        journal.updated = []
        journal.deleted = []
    }

    /**
     *
     * @param element
     * @param value
     * @param rowId
     */
    public static update(
        journal: FrontendJournal,
        def: Definition,
        element: Element,
        value: DataTypes | undefined,
        rowId?: string,
        forced?: boolean,
        prop?: ElementProp,
    ) {
        // console.log(`Update value ${element.va} to ${value}`)
        // only update value if new value !== old value or if forced!!
        if (forced || element.va !== value) {
            JournalService.addUpdated(journal, def, rowId, prop ?? ElementProp.Value, value)
            // apply the change also to the element
            switch (prop) {
                case ElementProp.Position:
                    ; (element.va as TableInfo).p = value as number
                    break
                case ElementProp.SortField:
                    ; (element.va as TableInfo).sf = value as string
                    break
                case ElementProp.SortOrder:
                    ; (element.va as TableInfo).sd = value as string
                    break
                case ElementProp.PageSize:
                    ; (element.va as TableInfo).ps = value as number
                    break
                default:
                    element.va = value
            }
        }
    }

    /**
     *
     * @param element
     * @param value
     * @param rowId
     */
    public static updateVisible(
        journal: FrontendJournal,
        def: Definition,
        element: Element,
        value: boolean,
        rowId?: string,
    ) {
        // only if new value !== old value!
        // console.log(`Update value ${element.va} to ${value}`)
        if (element.vi !== value) {
            JournalService.addUpdated(journal, def, rowId, ElementProp.Visible, value)
            // apply the change also to the element
            element.vi = value
        }
    }

    /**
     *
     * @param element
     * @param value
     * @param rowId
     */
    public static updateSelected(
        journal: FrontendJournal,
        def: Definition,
        element: Element,
        selectedRowIds: Array<string>,
    ) {
        for (let rowId in (element.va as TableInfo).d!) {
            const row = (element.va as TableInfo).d![rowId]
            const oldSelValue = row.sel
            // assume the row isn't selected
            row.sel = false
            // if row is contained in selectedRowIds then it's selected
            if (selectedRowIds.indexOf(row.id) > -1) {
                row.sel = true
            }
            if (oldSelValue != row.sel) {
                JournalService.addUpdated(journal, def, row.id, ElementProp.Selected, row.sel)
            }
        }
    }

    /**
     *
     * @param rowId
     * @param key
     * @returns
     */
    public static existsElement(
        journal: FrontendJournal,
        rowId: string | undefined,
        key: string,
    ): boolean {
        if (journal.updated.find((it) => it.rowId === rowId && it.key === key)) {
            return true
        }
        // if (journal.added.find((it) => it.rowId === rowId && it.key === key)) {
        //     return true
        // }
        // if (journal.deleted.find((it) => it.rowId === rowId && it.key === key)) {
        //     return true
        // }

        // TODO(ML) include added and deleted

        return false
    }

    /**
     *
     * @param journal
     * @param rowId
     * @param key
     * @param id
     */
    public static addDeleted(journal: FrontendJournal, rowId: string, key: string, id: string) {
        // First step, add to deleted rows
        const deletedElement = journal.deleted.find(
            (item) => item.rowId === rowId && item.key === key,
        )
        if (!deletedElement) {
            journal.deleted.push({ rowId, key, ids: [id] })
        } else {
            const deletedItem = deletedElement.ids?.find((item) => item === id)
            if (!deletedItem) {
                deletedElement.ids?.push(id)
            }
        }
        // Second step, check if there are updates on this deleted row. In this case we can
        // throw these changes away as we remove the row anyway
        const updatedInfoIdx = journal.updated.findIndex((info) => info.rowId === id)
        if (updatedInfoIdx > -1) {
            journal.updated.splice(updatedInfoIdx, 1)
        }
        // We don't need to check added because adding rows (for tables) will always be done
        // server side!
    }

    /**
     *
     * @param form
     * @param journal
     */
    public static apply(form: Form, journal: BackendJournal) {
        for (const rowId in journal) {
            const row = FormService.findRowById(rowId, form)
            if (row == null) {
                console.error(`Error applying backend changes, cannot find row!!`)
                return
            }
            // console.log(`Found row ${rowId}`)
            for (const key in journal[rowId]) {
                const element = row.values[key]
                if (element == null) {
                    console.warn(
                        `Error applying backend changes, cannot find element (row='${rowId}', key='${key}')!`,
                    )
                    continue
                }
                // console.log(`Found element ${key}`)
                for (const prop in journal[rowId][key]) {
                    const value = journal[rowId][key][prop]
                    // console.log(`Applied ${prop} = ${value}`)
                    switch (prop) {
                        case ElementProp.Editable:
                            element.ed = value as boolean
                            break
                        case ElementProp.Message:
                            element.msg = value as ElementInfo
                            break
                        case ElementProp.Required:
                            element.rq = value as boolean
                            break
                        case ElementProp.Value:
                            if (isTable(value)) {
                                const table = element.va as TableInfo

                                // copy basic values from backend result
                                table.p = value.p
                                table.ps = value.ps
                                table.r = value.r
                                table.s = value.s
                                table.sf = value.sf
                                table.sd = value.sd

                                // create a new data object by merging it from backend and already existing elements
                                let newData: Record<string, ElementMapRow> = {}
                                value.r.forEach((rowId: string) => {
                                    if (value.d[rowId]) {
                                        newData[rowId] = value.d[rowId]
                                    } else if (table.d![rowId]) {
                                        newData[rowId] = table.d![rowId]
                                    }
                                })
                                table.d = newData
                            } else {
                                element.va = value
                            }
                            break
                        case ElementProp.Visible:
                            element.vi = value as boolean
                            break
                        case ElementProp.Selected:
                            row!.sel = value as boolean
                            break
                    }
                }
            }
        }
    }

    /**
     *
     * @param element
     * @param attributeType
     * @param value
     * @param rowId
     */
    private static addUpdated(
        journal: FrontendJournal,
        def: Definition,
        rowId: string | undefined,
        elementProp: ElementProp,
        value: DataTypes | undefined,
    ) {
        // find or create new (update) info object
        let info = journal.updated.find((it) => it.key === def.key && it.rowId === rowId)
        if (!info) {
            info = {
                key: def.key,
                rowId,
                changes: [],
            }
            journal.updated.push(info)
        }

        // either update the according change info or create a new one
        const change = info.changes.find((it) => it.p === elementProp)
        if (change) {
            change.va = value
        } else {
            info.changes.push({ p: elementProp, va: value })
        }
    }
}
