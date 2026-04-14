import { Label, Link } from "@ui5/webcomponents-react"

import { FormService } from "../../features/sessions/forms"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { ControlProps, getLabel } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { UserEventType } from "../../features/sessions/definitions"
import { triggerEvent } from "../../features/sessions/sessionActions"
import { useMessages } from "commons"

export default function (props: ControlProps) {
    const { def, rowId, texts, asTableCell } = props
    const showLabel = def.showLabel ?? false
    const dispatch = useAppDispatch()
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const messages = useMessages()

    // Get href and text from element.va (backend LinkData format: { v: text, h: hRef,})
    const href = (typeof element?.va === "object" && (element.va as any).h) || "#"
    const linkTextFromBackend = (typeof element?.va === "object" && (element.va as any).v) || ""

    const linkText = linkTextFromBackend || href

    const linkElement = (
        <Link
            href={href}
            target="_blank"
            onClick={() =>
                dispatch(triggerEvent({ type: UserEventType.Action, def, rowId, messages }))
            }
        >
            {linkText}
        </Link>
    )

    const label = getLabel(texts, def)

    // if there is no label in a table cell link, we can just show the link centered in the cell
    if (!showLabel && asTableCell) {
        return (
            <ControlContainer {...props} asTableCell={true}>
                {linkElement}
            </ControlContainer>
        )
        // if there is a label in a table cell link, we show it directly above the text, since the input fields have no labels in table cells
    } else if (showLabel && asTableCell) {
        return (
            <ControlContainer {...props} asTableCell={true}>
                <Label>{label}</Label>
                {linkElement}
            </ControlContainer>
        )
    } else {
        // when the test is not in the table cell, we space label and link like label and input field 
        return (
            <ControlContainer {...props} asTableCell={false}>
                <div style={{ height: "2.75rem", display: "flex", alignItems: "center" }}>
                    {linkElement}
                </div>
            </ControlContainer>
        )
    }

}