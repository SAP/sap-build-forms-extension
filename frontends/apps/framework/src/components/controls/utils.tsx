import { IntlShape } from "react-intl"

import { Icon, Text, UI5WCSlotsNode } from "@ui5/webcomponents-react"
import { ThemingParameters } from "@ui5/webcomponents-react-base"

import { Severity } from "commons"

import { Definition } from "../../features/sessions/definitions"
import { ElementInfo } from "../../features/sessions/forms"

/**
 *
 * @param col
 */
export function calculateColspan(def: Definition): string {
    if (typeof def.col === "string" && def.col.length > 0) {
        // console.log(`Non-default col with ${def.col}`)

        let sm = -1,
            md = -1,
            lg = -1,
            xl = -1

        // parse the given definition and assign the according values
        for (let value of def.col.split(" ")) {
            value = value.toLowerCase().trim()
            const intValue = parseInt(value.replace(/\D/g, ""), 10)

            if (value.startsWith("sm")) {
                sm = intValue
            } else if (value.startsWith("md")) {
                md = intValue
            } else if (value.startsWith("lg")) {
                lg = intValue
            } else if (value.startsWith("xl")) {
                xl = intValue
            }
        }

        // check dependencies if some values aren't defined
        if (sm < 0) {
            sm = 12
        }
        if (md < 0) {
            md = sm
        }
        if (lg < 0) {
            lg = md
        }
        if (xl < 0) {
            xl = lg
        }

        // console.log(`Log for ${def.id} is "${def.col}" -> "XL${xl} L${lg} M${md} S${sm}"`)
        return `XL${xl} L${lg} M${md} S${sm}`
    }

    // console.log(`Default Log for ${def.id} is "${def.col}"`)
    return "XL12 L12 M12 S12"
}

interface SeverityIconProps {
    ei: ElementInfo | boolean | undefined
}

/**
 *
 * @param severity
 * @returns
 */
export function SeverityIcon({ ei }: SeverityIconProps) {
    let severity: Severity | undefined = undefined

    if (typeof ei === "boolean" && ei) {
        severity = Severity.Warning
    } else if (typeof ei === "object") {
        severity = ei.severity
    }

    switch (severity) {
        case Severity.Error:
            return (
                <Icon
                    name="error"
                    style={{
                        color: ThemingParameters.sapErrorColor,
                        marginRight: "sap-margin-tiny",
                    }}
                />
            )
        case Severity.Warning:
            return (
                <Icon
                    name="alert"
                    style={{
                        color: ThemingParameters.sapWarningColor,
                        marginRight: "sap-margin-tiny",
                    }}
                />
            )
        case Severity.Info:
            return (
                <Icon
                    name="information"
                    style={{
                        color: ThemingParameters.sapInformationColor,
                        marginRight: "sap-margin-tiny",
                    }}
                />
            )
        case Severity.Success:
            return (
                <Icon
                    name="message-success"
                    style={{
                        color: ThemingParameters.sapSuccessColor,
                        marginRight: "sap-margin-tiny",
                    }}
                />
            )
    }

    return <></>
}

export function elementInfo2ValueState(
    msg?: ElementInfo,
): "Negative" | "None" | "Positive" | "Critical" | "Information" {
    if (msg && typeof msg === "object") {
        switch (msg.severity) {
            case Severity.Error:
                return "Negative"
            case Severity.Info:
                return "Information"
            case Severity.Success:
                return "Positive"
            case Severity.Warning:
                return "Critical"
        }
    }
    return "None"
}

/**
 *
 * @param intl
 * @param ei
 * @returns
 */
export function elementInfo2ValueStateText(
    intl: IntlShape,
    ei?: boolean | ElementInfo,
): UI5WCSlotsNode {
    if (ei && typeof ei === "object" && ei.key && typeof ei.key === "string") {
        return <Text>{intl.formatMessage({ id: ei.key }, ei?.params)}</Text>
    }
    return <></>
}

// /**
//  *
//  * @param text
//  * @param params
//  * @returns
//  */
// export function formatMsg(text: string, params: Record<string, any>): string {
//     return text.replace(/\{.*?\}/g, (placeholder) => {
//         return params[placeholder.substring(1, placeholder.length - 1)] || "N/A"
//     })
// }

export function resolveParams(
    params: Record<string, string>,
    texts: Record<string, string>,
): Record<string, string> {
    let result: Record<string, string> = {}

    if (typeof params !== "undefined") {
        for (const key in params) {
            const value = texts[params[key]]
            result[key] = typeof value === "undefined" ? params[key] : value
        }
    }

    return result
}
