import { Icon } from "@ui5/webcomponents-react"
import { ThemingParameters } from "@ui5/webcomponents-react-base"

import { Severity } from "../../utils/messages"

/**
 *
 */
interface SeverityIconProps {
    severity?: Severity
}

/**
 *
 * @param severity
 * @returns
 */
export function SeverityIcon(props: SeverityIconProps) {
    let severity: Severity | undefined = undefined

    if (typeof props.severity === "boolean" && props.severity) {
        severity = Severity.Warning
    } else if (props.severity) {
        severity = props.severity
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
        default:
            return <> </>
    }
}
