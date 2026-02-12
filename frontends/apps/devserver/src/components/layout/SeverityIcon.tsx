import { Icon } from "@ui5/webcomponents-react"
import { ThemingParameters } from "@ui5/webcomponents-react-base"

import { SeverityValue } from "../../utils/scenarioDefinitions"

/**
 *
 */
interface SeverityIconProps {
    severity: SeverityValue
    message: string
    isMainElement: boolean
}

/**
 *
 * @param severity
 * @returns
 */
export default function ({ severity, message, isMainElement }: SeverityIconProps) {
    switch (severity) {
        case SeverityValue.Critical:
            return (
                <Icon
                    name="error"
                    style={{
                        color: isMainElement ? ThemingParameters.sapErrorColor : "grey",
                        marginInlineEnd: 10,
                    }}
                    showTooltip={message != ""}
                    accessibleName={message}
                />
            )
        case SeverityValue.Negative:
            return (
                <Icon
                    name="error"
                    style={{
                        color: isMainElement ? ThemingParameters.sapErrorColor : "grey",
                        marginInlineEnd: 10,
                    }}
                    showTooltip={message != ""}
                    accessibleName={message}
                />
            )
        case SeverityValue.Information:
            return (
                <Icon
                    name="alert"
                    style={{
                        color: isMainElement ? ThemingParameters.sapWarningColor : "grey",
                        marginInlineEnd: 10,
                    }}
                    showTooltip={message != ""}
                    accessibleName={message}
                />
            )
        case SeverityValue.Positive:
            return (
                <Icon
                    name="message-success"
                    style={{
                        color: isMainElement ? ThemingParameters.sapSuccessColor : "grey",
                        marginInlineEnd: 10,
                    }}
                    showTooltip={message != ""}
                    accessibleName={message}
                />
            )
    }

    return <></>
}
