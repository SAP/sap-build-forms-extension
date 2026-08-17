import { useIntl } from "react-intl"

import { TextArea } from "@ui5/webcomponents-react"

import { useMessages } from "commons"

import { ControlProps, handleChange, handleEnterFocus, handleLeaveFocus, getPlaceholder } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { FormService } from "../../features/sessions/forms"
import { elementInfo2ValueState, elementInfo2ValueStateText } from "./utils"

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { def, globalEd, rowId, texts } = props
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const intl = useIntl()
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    return (
        <ControlContainer {...props}>
            <TextArea
                id={def.key}
                value={(element?.va as string) ?? ""}
                placeholder={getPlaceholder(texts, def)}
                readonly={!element?.ed || !globalEd}
                required={element?.rq}
                onInput={(e) => handleChange(dispatch, def, rowId, messages, e.target.value ?? "")}
                onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                valueState={elementInfo2ValueState(element?.msg)}
                valueStateMessage={elementInfo2ValueStateText(intl, element?.msg)}
                style={{
                    width: "100%",
                }}
            />
            <div style={{ minHeight: "2rem" }}></div>
        </ControlContainer>
    )
}
