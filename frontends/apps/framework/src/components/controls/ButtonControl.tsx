import { useEffect } from "react"
import { Button, Label } from "@ui5/webcomponents-react"

import { Severity, useMessages } from "commons"

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
    const { def, globalEd, rowId, texts, withContainer, onAfterAction } = props
    const dispatch = useAppDispatch()
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const messages = useMessages()
    const { toast } = messages

    useEffect(() => {
        if (!def.shortcut) return
        const handler = async (e: KeyboardEvent) => {
            const shortcut = def.shortcut!
            const parts = shortcut.toLowerCase().split("+")
            const expectedKey = parts[parts.length - 1]
            const needsCtrl = parts.includes("ctrl")
            const needsAlt = parts.includes("alt")
            const needsShift = parts.includes("shift")
            // e.code gives the physical key 
            // Handle "KeyS" → "s", "Digit1" → "1", "F4" → "f4"
            let physicalKey: string
            if (e.code.startsWith("Key")) {
                physicalKey = e.code.slice(3).toLowerCase()
            } else if (e.code.startsWith("Digit")) {
                physicalKey = e.code.slice(5).toLowerCase()
            } else {
                physicalKey = e.code.toLowerCase()
            }
            if (physicalKey !== expectedKey) return
            // AltGr on Windows sends ctrlKey+altKey without metaKey; skip to avoid
            // false-triggering ctrl+alt shortcuts when the user types AltGr characters
            if (e.ctrlKey && e.altKey && !e.metaKey) return
            if (needsCtrl !== (e.ctrlKey || e.metaKey)) return
            if (needsAlt !== e.altKey) return
            if (needsShift !== e.shiftKey) return
            if (!element?.ed || !globalEd) return
            e.preventDefault()
            await dispatch(triggerEvent({ type: UserEventType.Action, def, rowId, messages }))
            toast(Severity.None, "button_shortcut_triggered", { label: getLabel(texts, def) ?? def.key })
            if (onAfterAction) await onAfterAction()
        }
        document.addEventListener("keydown", handler, true)
        return () => document.removeEventListener("keydown", handler, true)
    }, [def, element?.ed, globalEd, rowId, messages, onAfterAction, dispatch])

    const button = (
        <Button
            id={def.key}
            design={mapDesign(def.design)}
            disabled={!element?.ed || !globalEd}
            icon={def.icon}
            tooltip={def.tooltip}
            onClick={async (e: any) => {

                if (def.linkHRef && def.linkHRef.trim() !== "") {
                    e.preventDefault?.()
                    window.open(def.linkHRef, "_blank")
                    return
                }
                await dispatch(triggerEvent({ type: UserEventType.Action, def, rowId, messages }))
                if (onAfterAction) {
                    await onAfterAction()
                }
            }}
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
                <Label></Label>
                <>{button}</>
            </ControlContainer>
        )
    }

    return <>{button}</>
}
