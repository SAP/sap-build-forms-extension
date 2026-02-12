import { CheckBox, CheckBoxDomRef, Ui5CustomEvent } from "@ui5/webcomponents-react"

import { useMessages } from "commons"

import { useAppDispatch, useAppSelector } from "../../features/store"
import { ControlProps, getLong, handleChange, handleEnterFocus, handleLeaveFocus } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { FormService } from "../../features/sessions/forms"
import { elementInfo2ValueState } from "./utils"

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { asTableCell, def, globalEd, rowId, texts } = props
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    return (
        <ControlContainer {...props}>
            <CheckBox
                id={def.key}
                checked={element!.va as boolean}
                required={element?.rq}
                readonly={!element?.ed || !globalEd}
                onChange={(e: Ui5CustomEvent<CheckBoxDomRef, never>) =>
                    handleChange(dispatch, def, rowId, messages, e.target.checked ?? false)
                }
                onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                text={asTableCell ? "" : getLong(texts, def)}
                valueState={elementInfo2ValueState(element?.msg)}
            />
        </ControlContainer>
    )
}
