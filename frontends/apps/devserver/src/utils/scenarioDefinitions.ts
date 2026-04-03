export type Scenario = {
    accessObject?: string
    active?: boolean
    basePackage?: string
    defaultLanguage?: string
    elements?: Elem[]
    name?: string
    root?: string
    texts?: string[]
    version?: number
}

export type Mixin = {
    name?: string
    version?: number
    accessObject?: string
    basePackage?: string
    elements?: Elem[]
    texts?: string[]
}

export type Elem = {
    text?: string
    name: string
    type: string
    id?: string
    defaultValue?: string
    linkText?: string
    linkHRef?: string
    col?: string
    dataType?: DataTypeValue
    sort?: number
    valueHelp?: ValueHelpOptions
    visible?: string
    editable?: string
    required?: string
    css?: string
    showLabel?: boolean
    showHelp?: boolean
    showAsColumn?: boolean
    lineBreak?: boolean
    design?: DesignValue | AttachmentDesignType
    icon?: string
    tooltip?: string
    dialogKey?: string
    select?: SelectValue
    style?: StyleValue
    pageSize?: number
    inputType?: InputValue
    path?: string
    mixinName?: string
    version?: number
    adapter?: string
    hasDescription?: boolean
    elements: Elem[]
    footer?: Elem
    headerSegment?: Elem
    toolbar?: Elem
    leftElements?: Elem[]
    rightElements?: Elem[]
    wizardFormatOptions?: WizardFormatOptions
    columnOptions?: ColumnOptions
    size?: Dimension
    fileTypes?: string
    cardinality?: UploadType
    categories?: CategoryOption[]
    validationRules?: Validation[]
}

export type ElemForTable = Elem & {
    parent: ElemForTable | undefined
    index: string
    subRows?: ElemForTable[]
}

export type Validation = {
    type: string | undefined
    severity: string | undefined
    messageKey: string | undefined
}

export type MinMaxValidation = Validation & {
    limit: string | undefined
    inclusive: boolean
}

export type FixedValidation = Validation & {
    length: string | undefined
    fractions: string | undefined
}

export type RegexValidation = Validation & {
    pattern: string | undefined
}

export type SpELValidation = Validation & {
    expression: string | undefined
}

export type BeanValidation = Validation & {
    beanName: string | undefined
}

export type Severity = {
    text: string
    id: string
    icon: string
}

export type Tab = {
    text: string
    icon: string
}

export enum DataTypeValue {
    Auto = "auto",
    String = "string",
    Int = "int",
    Decimal = "decimal",
    Date = "date",
    Time = "time",
    Datetime = "datetime",
    Boolean = "boolean",
    Collection = "collection",
}

export enum DesignValue {
    Default = "default",
    Emphasized = "emphasized",
    Positive = "positive",
    Negative = "negative",
    Transparent = "transparent",
    Attention = "attention",
    Warn = "warn",
    Info = "info",
}

export enum SelectValue {
    None = "none",
    Single = "single",
    Multiple = "multiple",
}

export enum UploadType {
    Single = "single",
    Multiple = "multiple",
}

export enum AttachmentDesignType {
    FileUploader = "fileUploader",
    UploadCollection = "uploadCollection",
}

export enum StyleValue {
    Dialog = "dialog",
    Inline = "inline",
    AnalyticTable = "gid",
}

export enum InputValue {
    Text = "text",
    Password = "password",
    Numeric = "numeric",
    Email = "email",
}

export const tabs: Tab[] = [
    { text: "Structure", icon: "tree" },
    { text: "Languages", icon: "text" },
]

export const leafNodes: string[] = [
    "alert",
    "autocomplete",
    "button",
    "checkbox",
    "currency",
    "daterangepicker",
    "dummy",
    "edit",
    "icon",
    "image",
    "information",
    "input",
    "mixin",
    "multiselect",
    "radio",
    "select",
    "text",
    "upload",
]

export const severities: Severity[] = [
    { text: "Info", id: "i", icon: "message-information" },
    { text: "Warning", id: "w", icon: "message-warning" },
    { text: "Error", id: "e", icon: "message-error" },
]

export type Message = {
    defName: string
    defVersion: number
    severity: SeverityValue
    elementId: string
    message: string
    elementPart: ElementPart
}

export enum SeverityValue {
    Negative = "e",
    Positive = "s",
    None = "_",
    Critical = "w",
    Information = "i",
}

export enum ElementPart {
    Version = "v",
    Name = "n",
    UiElementType = "u",
    DataType = "d",
    None = "_",
}

export type Parent = {
    elem: Elem
    index: string
}

export enum TextPostfix {
    short = ".short",
    long = ".long",
    title = ".title",
    doc = ".doc",
}

export type WizardFormatOptions = {
    skipInSummary: boolean
    skipInForm: boolean
}

export type ColumnOptions = {
    minColumnWidth: string
    maxColumnWidth: string
}

export type Dimension = {
    height: string
    width: string
}

export type ValueHelpOptions = {
    name: string
    validate: boolean
    emptySelection: boolean
    displayFormat: string
}

export type CategoryOption = {
    label: string
    hvOpt: ValueHelpOptions
}
