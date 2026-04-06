import { useIntl } from "react-intl"

import { DatePicker, DateTimePicker, Input, TimePicker } from "@ui5/webcomponents-react"

import { getLanguage, useMessages } from "commons"

import { DataType } from "../../features/sessions/definitions"
import { Element, FormService } from "../../features/sessions/forms"
import { useAppDispatch, useAppSelector } from "../../features/store"
import {
    fromInternalDateTime,
    toInternalDate,
    toInternalDateTime,
} from "../../utils/DataFormatUtils"

import { ControlProps, handleChange, handleEnterFocus, handleLeaveFocus } from "./Control"
import ControlContainer from "./ControlFlexContainer"
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

    // if (element?.er && typeof element?.er === "object") {
    //     console.log(`InputControl for ${def.id} with info ${(element?.er as ElementInfo).severity}`)
    // }

    const convertInput = (e?: Element): string => {
        if (typeof element?.va === "string") {
            return element.va
        }
        if (element?.va === undefined || element?.va === null) {
            return ""
        }
        return element.va.toString()
    }

    let control = <></>
    switch (def.dataType) {
        case DataType.Date:
            control = (
                <DatePicker
                    id={def.key}
                    value={(element?.va as string) ?? ""}
                    onChange={(e) =>
                        handleChange(
                            dispatch,
                            def,
                            rowId,
                            messages,
                            toInternalDate(e.target.value ?? "", getLanguage()!),
                        )
                    }
                    onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                    onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                    readonly={!element?.ed || !globalEd}
                    required={element?.rq}
                    valueState={elementInfo2ValueState(element?.msg)}
                    valueStateMessage={elementInfo2ValueStateText(intl, element?.msg)}
                    primaryCalendarType="Gregorian"
                    style={{ width: "100%" }}
                />
            )
            break
        case DataType.DateTime:
            // console.log(`date-time`)
            control = (
                <DateTimePicker
                    id={def.key}
                    value={fromInternalDateTime((element?.va as string) ?? "", getLanguage()!)}
                    onChange={(e) =>
                        handleChange(
                            dispatch,
                            def,
                            rowId,
                            messages,
                            toInternalDateTime(e.target.value ?? "", getLanguage()!),
                        )
                    }
                    onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                    onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                    readonly={!element?.ed || !globalEd}
                    required={element?.rq}
                    valueState={elementInfo2ValueState(element?.msg)}
                    valueStateMessage={elementInfo2ValueStateText(intl, element?.msg)}
                    onInput={function () {}}
                    primaryCalendarType="Gregorian"
                    style={{ width: "100%" }}
                />
            )
            break
        case DataType.Time:
            control = (
                <TimePicker
                    id={def.key}
                    value={(element?.va as string) ?? ""}
                    onChange={(e) => {
                        handleChange(
                            dispatch,
                            def,
                            rowId,
                            messages,
                            e.target.value.length == 0 ? undefined : e.target.value,
                        )
                    }}
                    onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                    onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                    readonly={!element?.ed || !globalEd}
                    // required={element?.rq}
                    valueState={elementInfo2ValueState(element?.msg)}
                    valueStateMessage={elementInfo2ValueStateText(intl, element?.msg)}
                    onInput={function ka() {}}
                    style={{ width: "100%" }}
                />
            )
            break
        default:
            control = (
                <Input
                    id={def.key}
                    value={convertInput(element)}
                    onChange={(e) => {
                        let value: string | number = e.target.value ?? ""
                        if (def.dataType === DataType.Int) {
                            value = parseInt(value)
                        } else if (def.dataType === DataType.Decimal) {
                            value = parseFloat(value)
                        }
                        handleChange(dispatch, def, rowId, messages, value)
                    }}
                    onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                    onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                    readonly={!element?.ed || !globalEd}
                    required={element?.rq}
                    valueState={elementInfo2ValueState(element?.msg)}
                    valueStateMessage={elementInfo2ValueStateText(intl, element?.msg)}
                    style={{ width: "100%" }}
                    type={
                        def.dataType === DataType.Int || def.dataType === DataType.Decimal
                            ? "Number"
                            : "Text"
                    }
                />
            )
    }

    // as default we asume string as data-type, leading to InputText as ui control
    return <ControlContainer {...props}>{control}</ControlContainer>
}
