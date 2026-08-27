/**
 *
 */
export enum UIElement {
    Alert = "alert",
    Attachment = "attachment",
    Autocomplete = "autocomplete",
    Button = "button",
    Checkbox = "checkbox",
    Currency = "currency",
    DateRange = "daterange",
    Dialog = "dialog",
    DocForm = "docform",
    Dummy = "dummy",
    Editor = "edit",
    Form = "form",
    Group = "group",
    Icon = "icon",
    Image = "image",
    Input = "input",
    Link = "link",
    MultiSelect = "multiselect",
    RadioButtons = "radio",
    SearchHelp = "searchhelp",
    Segment = "segment",
    Select = "select",
    Table = "table",
    Text = "text",
    Toolbar = "toolbar",
    Wizard = "wizard",
}

/**
 *
 */
export enum DataType {
    String = "string",
    Int = "int",
    Decimal = "decimal",
    Date = "date",
    Time = "time",
    DateTime = "datetime",
    Collection = "coll",
    Boolean = "bool",
}

/**
 *
 */
export enum UserEventType {
    Change = "change",
    Action = "action",
    Open = "open",
    EnterFocus = "enterFocus",
    LeaveFocus = "leaveFocus",
    Sort = "sort",
    Browse = "browse",
}

/**
 *
 */
export class Limitations {
    min?: string
    max?: string
    match?: string
    fixedLength?: string
    fixedFractions?: string
}

export interface CategoryOption {
    label: string
    hvOpt: {
        name: string
        validate: boolean
        emptySelection: boolean
        displayFormat: string
    }
}

/**
 *  ValueName is used to represent a value and its display name in select options.
 *  It is used in select controls and value helps.
 */
export interface ValueHelpInfo {
    displayFormat?: string
    emptySelection: boolean
    name: string
    validate: boolean
}

/**
 * In frontend we don't use dedicated classes for each ui-element-type but put all
 * attributes into this definition. this makes things easier
 */
export interface Definition {
    id: string
    key: string
    uiElement: UIElement
    // xs sm md lg xl
    col?: string
    design?: string
    dataType?: DataType
    editable?: string | boolean
    elements?: Definition[]
    events?: string[]
    icon?: string
    showAsColumn: boolean
    visible?: string | boolean
    required?: string | boolean
    value?: string | number | boolean
    limits: Limitations
    vh?: ValueHelpInfo
    type?: string
    select?: "none" | "multiple" | "single"
    toolbar?: Definition
    tooltip?: string
    footer?: Definition
    leftElements?: Definition[]
    rightElements?: Definition[]
    header?: Definition
    hasDescription?: boolean
    el?: boolean
    showLabel?: boolean
    showHelp?: boolean
    lineBreak?: boolean
    pageSize: number
    linkText?: string
    linkHRef?: string
    fileTypes?: string
    selectionMode?: "none" | "single" | "multiple"
    categories?: CategoryOption[]
    columnOptions?: { minColumnWidth?: string; maxColumnWidth?: string }
    size?: { height: string; width: string }
    inputType?: string
    shortcut?: string
}

/**
 *
 */
export class FormDefinition {
    id: string
    version: string
    texts: Record<string, string>
    root: string
    elements: Definition[]
    locale?: string

    constructor(
        id: string,
        version: string,
        texts: Record<string, string>,
        root: string,
        elements: Definition[],
    ) {
        this.id = id
        this.version = version
        this.texts = texts
        this.root = root
        this.elements = elements
    }
}

/**
 *
 */
export type DefinitionMap = Map<string, Definition>

/**
 *
 * @returns
 */
export function findRoot(def: FormDefinition): Definition | undefined {
    return def.elements.find((element) => element.id === def.root)
}

/**
 *
 * @returns
 */
export function hasCollection(def: Definition) {
    return def.uiElement === UIElement.Table
}

/**
 *
 * @returns
 */
export function hasChildren(def: Definition) {
    return (
        def.uiElement === UIElement.Form ||
        def.uiElement === UIElement.Group ||
        def.uiElement === UIElement.Segment ||
        def.uiElement === UIElement.Wizard
    )
}

/**
 *
 * @param key
 * @param map
 * @returns
 */
export function findByKey(key: string, map: DefinitionMap): Definition | undefined {
    return map.get(key)
}

/**
 *
 */
export const CURRENT = "current"
