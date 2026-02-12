import { UI5WCSlotsNode } from "@ui5/webcomponents-react"
import { FieldError } from "react-hook-form"
import { IntlShape, PrimitiveType } from "react-intl"

/**
 *
 * @param err
 * @returns
 */
export function valueState(
    err?: FieldError,
): "Positive" | "Negative" | "None" | "Critical" | "Information" | undefined {
    if (err) {
        return "Negative"
    }
    return "None"
}

/**
 *
 * @param err
 * @returns
 */
export function valueStateMessage(
    intl: IntlShape,
    err?: FieldError,
    values?: Record<string, PrimitiveType>,
): UI5WCSlotsNode {
    if (err) {
        switch (err.type) {
            case "required":
                return <div>{intl.formatMessage({ id: "common_error_required" }, values)}</div>
            case "maxLength":
                return <div>{intl.formatMessage({ id: "common_error_max" }, values)}</div>
            default:
                return <div>{intl.formatMessage({ id: "common_error_unkown" }, values)}</div>
        }
    }
    return <></>
}
