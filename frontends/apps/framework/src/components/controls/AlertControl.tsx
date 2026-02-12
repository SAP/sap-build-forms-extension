import { Icon, MessageStrip } from "@ui5/webcomponents-react"

import { ControlProps } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { useAppSelector } from "../../features/store"
import { FormService } from "../../features/sessions/forms"

function mapDesign(input?: string): "Critical" | "Positive" | "Negative" | "Information" {
    switch (input) {
        case "positive":
            return "Positive"
        case "negative":
            return "Negative"
        case "warn":
            return "Critical"
        default:
            return "Information"
    }
}

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { def, rowId, texts, withContainer } = props
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    let text = element?.va as string
    if (typeof text !== "string" || text.length === 0) {
        text = texts[def.id + ".doc"]
    }
    if (typeof text !== "string" || text.length === 0) {
        text = texts[def.id + ".long"]
    }
    if (typeof text !== "string" || text.length === 0) {
        text = def.id
    }

    if (withContainer) {
        return (
            <ControlContainer {...props} asTableCell={true}>
                <MessageStrip
                    id={def.key}
                    design={mapDesign(def.design)}
                    icon={def.icon ? <Icon name={def.icon} /> : <></>}
                    hideCloseButton
                    style={{ width: "100%" }}
                >
                    {text}
                </MessageStrip>
            </ControlContainer>
        )
    }

    return <>{text}</>
}
