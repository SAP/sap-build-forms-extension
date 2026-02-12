import { Button } from "@ui5/webcomponents-react"

import { useMessages } from "commons"

import { ControlProps, getLabel } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { UserEventType } from "../../features/sessions/definitions"
import { FormService } from "../../features/sessions/forms"
import { triggerEvent } from "../../features/sessions/sessionActions"

/**
 *
 * @param input
 * @returns
 */
function mapDesign(
    input?: string,
): "Default" | "Positive" | "Negative" | "Transparent" | "Emphasized" | "Attention" {
    switch (input) {
        case "emphasized":
            return "Emphasized"
        case "positive":
            return "Positive"
        case "negative":
            return "Negative"
        case "transparent":
            return "Transparent"
        case "attention":
            return "Attention"
        default:
            return "Default"
    }
}

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { def, globalEd, rowId, texts, withContainer } = props
    const dispatch = useAppDispatch()
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const messages = useMessages()

    const button = (
        <Button
            id={def.key}
            design={mapDesign(def.design)}
            disabled={!element?.ed || !globalEd}
            icon={def.icon}
            onClick={() =>
                dispatch(triggerEvent({ type: UserEventType.Action, def, rowId, messages }))
            }
            style={{
                width: "100%",
            }}
        >
            {getLabel(texts, def)}
        </Button>
    )

    if (withContainer) {
        return (
            <ControlContainer {...props} asTableCell={true}>
                <div style={{ opacity: 0 }}>.</div>
                <>{button}</>
            </ControlContainer>
        )
    }

    return <>{button}</>
}
