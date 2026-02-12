import {
    AttachmentDesignType,
    DesignValue,
    Elem,
    ElemForTable,
    InputValue,
    leafNodes,
    Message,
    SelectValue,
    SeverityValue,
    StyleValue,
    UploadType,
} from "./scenarioDefinitions"

/**
 * Generates a unique identifier for form elements
 * This ensures new elements are treated the same as saved elements
 */
export function generateUniqueId(): string {
    return `${Date.now()}-${Math.random().toString(36).substring(2, 9)}`
}

export function calculateCol(
    col: string,
    newValue: String,
    currentField: "xs" | "sm" | "md" | "lg" | "xl",
) {
    var newCol = ""

    if (currentField == "sm" && newValue != "") {
        newCol += "sm:" + newValue + " "
    } else if (currentField != "sm" && /sm:\d+/.exec(col)) {
        newCol += "sm:" + /sm:\d+/.exec(col)?.toString().split(":")[1] + " "
    }

    if (currentField == "md" && newValue != "") {
        newCol += "md:" + newValue + " "
    } else if (currentField != "md" && /md:\d+/.exec(col)) {
        newCol += "md:" + /md:\d+/.exec(col)?.toString().split(":")[1] + " "
    }

    if (currentField == "lg" && newValue != "") {
        newCol += "lg:" + newValue + " "
    } else if (currentField != "lg" && /lg:\d+/.exec(col)) {
        newCol += "lg:" + /lg:\d+/.exec(col)?.toString().split(":")[1] + " "
    }

    if (currentField == "xl" && newValue != "") {
        newCol += "xl:" + newValue + " "
    } else if (currentField != "xl" && /xl:\d+/.exec(col)) {
        newCol += "xl:" + /xl:\d+/.exec(col)?.toString().split(":")[1] + " "
    }

    return newCol.trim()
}

export function haveChildrenError(child: Elem, messages: Message[], isFirstEl: boolean): boolean {
    if (
        !isFirstEl &&
        messages.filter(
            (a: any) => a.elementId == child.name.charAt(0).toUpperCase() + child.name.slice(1),
        ).length > 0
    ) {
        return true
    } else {
        if (
            child.elements.length > 0 ||
            child.toolbar ||
            child.footer ||
            child.leftElements ||
            child.rightElements
        ) {
            const has_search_child: boolean = child.elements.some((childElement) =>
                haveChildrenError(childElement, messages, false),
            )
            const has_toolbar_child: boolean = child.toolbar
                ? haveChildrenError(child.toolbar, messages, false)
                : false
            const has_footer_child: boolean = child.footer
                ? haveChildrenError(child.footer, messages, false)
                : false

            const has_error_left: boolean = child.leftElements
                ? child.leftElements.some((childElement) =>
                      haveChildrenError(childElement, messages, false),
                  )
                : false
            const has_error_right: boolean = child.rightElements
                ? child.rightElements.some((childElement) =>
                      haveChildrenError(childElement, messages, false),
                  )
                : false

            return (
                has_search_child ||
                has_toolbar_child ||
                has_footer_child ||
                has_error_left ||
                has_error_right
            )
        }
        return false
    }
}

export function changeName(element: Elem, prefix: string, postfix: string) {
    var newElement = { ...element, name: prefix.trim() + element.name.trim() + postfix.trim() }

    if (element.elements && element.elements.length > 0) {
        var newElements: Elem[] = []
        element.elements.forEach((child) => {
            newElements.push(changeName(child, prefix, postfix))
        })
        newElement = { ...newElement, elements: newElements }
    }
    if (element.toolbar) {
        newElement = { ...newElement, toolbar: changeName(element.toolbar, prefix, postfix) }
    }
    if (element.footer) {
        newElement = { ...newElement, footer: changeName(element.footer, prefix, postfix) }
    }
    if (element.leftElements) {
        var newElements: Elem[] = []
        element.leftElements.forEach((child) => {
            newElements.push(changeName(child, prefix, postfix))
        })
        newElement = { ...newElement, leftElements: newElements }
    }
    if (element.rightElements) {
        var newElements: Elem[] = []
        element.rightElements.forEach((child) => {
            newElements.push(changeName(child, prefix, postfix))
        })
        newElement = { ...newElement, rightElements: newElements }
    }

    return newElement
}

export function isInsertAllowed(
    parentType: string | undefined,
    elemType: string,
    lastIndex: string | undefined,
) {
    if (elemType == "mixin") {
        return true
    }

    if (elemType == "segment") {
        if (parentType == "form" || parentType == "wizard") {
            return true
        } else {
            return false
        }
    }
    if (elemType == "group") {
        if (parentType == "segment") {
            return true
        } else {
            return false
        }
    }

    if (elemType == "dialog") {
        if (parentType == "segment" || parentType == "group") {
            return true
        } else {
            return false
        }
    }

    if (elemType == "toolbar") {
        if (parentType == "form" || parentType == "table") {
            return true
        } else {
            return false
        }
    }

    if (elemType == "table") {
        if (parentType && ["segment", "group", "table", "dialog"].includes(parentType)) {
            return true
        } else {
            return false
        }
    }

    if (
        [
            "alert",
            "autocomplete",
            "attachment",
            "checkbox",
            "currency",
            "daterangepicker",
            "edit",
            "icon",
            "image",
            "input",
            "multiselect",
            "radio",
            "select",
            "text",
            "dummy",
        ].includes(elemType)
    ) {
        if (
            parentType &&
            ["segment", "group", "table", "searchhelp", "dialog"].includes(parentType)
        ) {
            return true
        } else {
            return false
        }
    }

    if (elemType == "button") {
        if (
            (parentType &&
                ["segment", "group", "table", "searchhelp", "toolbar", "dialog"].includes(
                    parentType,
                )) ||
            (lastIndex && ["l", "r"].includes(lastIndex))
        ) {
            return true
        } else {
            return false
        }
    }

    if (elemType == "searchhelp") {
        if (parentType && ["segment", "group", "table", "dialog"].includes(parentType)) {
            return true
        } else {
            return false
        }
    }

    return false
}

export function getChildrenMessageSeverities(
    child: Elem,
    messages: Message[],
    isFirstEl: boolean,
): SeverityValue[] {
    var severityError: SeverityValue[] = []

    !isFirstEl &&
        severityError.push(
            ...messages
                .filter(
                    (a: any) =>
                        a.elementId == child.name.charAt(0).toUpperCase() + child.name.slice(1),
                )
                .map((e) => e.severity),
        )
    child.elements &&
        child.elements.forEach((childElement) => {
            severityError = severityError.concat(
                getChildrenMessageSeverities(childElement, messages, false),
            )
        })
    child.leftElements &&
        child.leftElements.forEach((childElement) => {
            severityError = severityError.concat(
                getChildrenMessageSeverities(childElement, messages, false),
            )
        })
    child.rightElements &&
        child.rightElements.forEach((childElement) => {
            severityError = severityError.concat(
                getChildrenMessageSeverities(childElement, messages, false),
            )
        })

    if (child.toolbar) {
        severityError = severityError.concat(
            getChildrenMessageSeverities(child.toolbar, messages, false),
        )
    }

    if (child.footer) {
        severityError = severityError.concat(
            getChildrenMessageSeverities(child.footer, messages, false),
        )
    }

    return severityError
}

export function elementInfo2ValueState(
    severity: SeverityValue,
): "Critical" | "Information" | "Negative" | "Positive" | "None" {
    switch (severity) {
        case SeverityValue.Critical:
            return "Critical"
        case SeverityValue.Information:
            return "Information"
        case SeverityValue.Negative:
            return "Negative"
        case SeverityValue.Positive:
            return "Positive"
    }
    return "None"
}

export function getHighestSeverity(severities: SeverityValue[]) {
    if (severities.includes(SeverityValue.Critical)) {
        return SeverityValue.Critical
    } else if (severities.includes(SeverityValue.Negative)) {
        return SeverityValue.Negative
    } else if (severities.includes(SeverityValue.Information)) {
        return SeverityValue.Information
    } else if (severities.includes(SeverityValue.Positive)) {
        return SeverityValue.Positive
    } else {
        return SeverityValue.None
    }
}

export function changeElAtTypeChangeTable(oldEl: ElemForTable, newType: string) {
    var newEl: ElemForTable = {
        ...oldEl,
        type: newType,
    }
    //delete fields
    if (oldEl.type == "button" && newType != "button") {
        delete newEl.design
        delete newEl.icon
        delete newEl.tooltip
    } else if (oldEl.type == "alert" && newType != "alert") {
        delete newEl.design
        delete newEl.icon
    } else if (oldEl.type == "input" && newType != "input") {
        delete newEl.inputType
    } else if (oldEl.type == "attachment" && newType != "attachment") {
        delete newEl.fileTypes
        delete newEl.cardinality
        delete newEl.design
        delete newEl.categories
        delete newEl.adapter
        delete newEl.hasDescription
    } else if (oldEl.type == "mixin" && newType != "mixin") {
        delete newEl.path
        delete newEl.mixinName
        delete newEl.version
    } else if (oldEl.type == "form" && newType != "form") {
        delete newEl.headerSegment
    } else if (oldEl.type == "searchhelp" && newType != "searchhelp") {
        delete newEl.dialogKey
    } else if (oldEl.type == "table" && newType != "table") {
        delete newEl.select
        delete newEl.style
        delete newEl.toolbar
    }

    if (
        ["currency", "multiselect", "select", "radio"].includes(oldEl.type) &&
        !["currency", "multiselect", "select", "radio"].includes(newType)
    ) {
        delete newEl.valueHelp
    }
    if (
        ["dialog", "form", "searchhelp"].includes(oldEl.type) &&
        !["dialog", "form", "searchhelp"].includes(newType)
    ) {
        delete newEl.footer
    }
    if (
        ["image", "dialog", "searchhelp"].includes(oldEl.type) &&
        !["image", "dialog", "searchhelp"].includes(newType)
    ) {
        delete newEl.size
    }

    //add fields
    if (newType == "button" && oldEl.type != "button") {
        newEl = {
            ...newEl,
            icon: "",
            tooltip: "",
            design: DesignValue.Default,
        }
    } else if (newType == "alert" && oldEl.type != "alert") {
        newEl = {
            ...newEl,
            icon: "",
            design: DesignValue.Positive,
        }
    } else if (newType == "input" && oldEl.type != "input") {
        newEl = {
            ...newEl,
            inputType: InputValue.Text,
        }
    } else if (newType == "mixin" && oldEl.type != "mixin") {
        newEl = {
            ...newEl,
            path: "",
            mixinName: "",
            version: 0,
        }
    } else if (newType == "searchhelp" && oldEl.type != "searchhelp") {
        newEl = { ...newEl, dialogKey: "" }
    } else if (newType == "attachment" && oldEl.type != "attachment") {
        newEl = {
            ...newEl,
            fileTypes: "",
            cardinality: UploadType.Single,
            design: AttachmentDesignType.FileUploader,
            categories: [],
            adapter: "",
            hasDescription: false,
        }
    } else if (newType == "table" && oldEl.type != "table") {
        newEl = {
            ...newEl,
            select: SelectValue.Single,
            style: StyleValue.Inline,
        }
    }

    if (
        ["currency", "multiselect", "select", "radio"].includes(newType) &&
        !["currency", "multiselect", "select", "radio"].includes(oldEl.type)
    ) {
        newEl = {
            ...newEl,
            valueHelp: {
                name: "",
                validate: false,
                emptySelection: false,
                displayFormat: "",
            },
        }
    }
    if (
        ["image", "dialog", "searchhelp"].includes(newType) &&
        !["image", "dialog", "searchhelp"].includes(oldEl.type)
    ) {
        newEl = { ...newEl, size: { height: "", width: "" } }
    }

    if (leafNodes.includes(newType)) {
        newEl = { ...newEl, elements: [] }
    }
    return newEl
}

export function changeElAtTypeChange(oldEl: Elem, newType: string) {
    var newEl: Elem = {
        ...oldEl,
        type: newType,
    }
    //delete fields
    if (oldEl.type == "button" && newType != "button") {
        delete newEl.design
        delete newEl.icon
        delete newEl.tooltip
    } else if (oldEl.type == "alert" && newType != "alert") {
        delete newEl.design
        delete newEl.icon
    } else if (oldEl.type == "input" && newType != "input") {
        delete newEl.inputType
    } else if (oldEl.type == "attachment" && newType != "attachment") {
        delete newEl.fileTypes
        delete newEl.cardinality
        delete newEl.design
        delete newEl.categories
        delete newEl.adapter
        delete newEl.hasDescription
    } else if (oldEl.type == "mixin" && newType != "mixin") {
        delete newEl.path
        delete newEl.mixinName
        delete newEl.version
    } else if (oldEl.type == "form" && newType != "form") {
        delete newEl.headerSegment
    } else if (oldEl.type == "searchhelp" && newType != "searchhelp") {
        delete newEl.dialogKey
    } else if (oldEl.type == "table" && newType != "table") {
        delete newEl.select
        delete newEl.style
        delete newEl.toolbar
    }

    if (
        ["currency", "multiselect", "select", "radio"].includes(oldEl.type) &&
        !["currency", "multiselect", "select", "radio"].includes(newType)
    ) {
        delete newEl.valueHelp
    }
    if (
        ["dialog", "form", "wizard", "searchhelp"].includes(oldEl.type) &&
        !["dialog", "form", "wizard", "searchhelp"].includes(newType)
    ) {
        delete newEl.footer
    }

    //add fields
    if (newType == "button" && oldEl.type != "button") {
        newEl = {
            ...newEl,
            icon: "",
            tooltip: "",
            design: DesignValue.Default,
        }
    } else if (newType == "alert" && oldEl.type != "alert") {
        newEl = {
            ...newEl,
            icon: "",
            design: DesignValue.Positive,
        }
    } else if (newType == "input" && oldEl.type != "input") {
        newEl = {
            ...newEl,
            inputType: InputValue.Text,
        }
    } else if (newType == "mixin" && oldEl.type != "mixin") {
        newEl = {
            ...newEl,
            path: "",
            mixinName: "",
            version: 0,
        }
    } else if (newType == "searchhelp" && oldEl.type != "searchhelp") {
        newEl = { ...newEl, dialogKey: "" }
    } else if (newType == "attachment" && oldEl.type != "attachment") {
        newEl = {
            ...newEl,
            fileTypes: "",
            cardinality: UploadType.Single,
            design: AttachmentDesignType.FileUploader,
            categories: [],
            adapter: "",
            hasDescription: false,
        }
    } else if (newType == "table" && oldEl.type != "table") {
        newEl = {
            ...newEl,
            select: SelectValue.Single,
            style: StyleValue.Inline,
        }
    }

    if (
        ["currency", "multiselect", "select", "radio"].includes(newType) &&
        !["currency", "multiselect", "select", "radio"].includes(oldEl.type)
    ) {
        newEl = {
            ...newEl,
            valueHelp: {
                name: "",
                validate: false,
                emptySelection: false,
                displayFormat: "",
            },
        }
    }

    if (
        ["image", "dialog", "searchhelp"].includes(newType) &&
        !["image", "dialog", "searchhelp"].includes(oldEl.type)
    ) {
        newEl = { ...newEl, size: { height: "", width: "" } }
    }

    if (leafNodes.includes(newType)) {
        newEl = { ...newEl, elements: [] }
    }
    return newEl
}
