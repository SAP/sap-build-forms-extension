import { Icon, Label } from "@ui5/webcomponents-react"

import { FormService } from "../../features/sessions/forms"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { ControlProps } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { useMessages } from "commons"
import { UserEventType } from "../../features/sessions/definitions"
import { triggerEvent } from "../../features/sessions/sessionActions"

/**
 * Convert PascalCase to kebab-case
 * e.g., "AddCoursebook" -> "add-coursebook"
 */
function toKebabCase(str: string): string {
    if (!str) return str
    return str
        .replace(/([a-z0-9])([A-Z])/g, "$1-$2")
        .replace(/([A-Z])([A-Z][a-z])/g, "$1-$2")
        .toLowerCase()
}

export default function (props: ControlProps) {
    const { def, rowId, withContainer} = props
    const dispatch = useAppDispatch()
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const messages = useMessages()
    

    let iconName = def.icon || (element?.nm as string) || (element?.va as string) ||""

    iconName = toKebabCase(iconName)

    const icon = (
        <Icon 
            name={iconName}
            onClick={() =>
                dispatch(triggerEvent({ type: UserEventType.Action, def, rowId, messages }))
            }
            style={{ height: "100%" }}
        />
    )

    if (withContainer) {
        return (
            <ControlContainer {...props} asTableCell={true} justifyContent="Center">
                    <Label></Label>
                    <>{icon}</>
            </ControlContainer>
        )
    }

    return <>{icon}</>
}
