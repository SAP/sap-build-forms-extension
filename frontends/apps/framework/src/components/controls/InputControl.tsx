import { useIntl } from "react-intl"
import { useRef, useEffect } from "react"

import { DatePicker, DateTimePicker, Input, TimePicker } from "@ui5/webcomponents-react"

import { getLanguage, useMessages } from "commons"

import { DataType } from "../../features/sessions/definitions"
import { Element, FormService } from "../../features/sessions/forms"
import { useAppDispatch, useAppSelector } from "../../features/store"
import {
    toInternalDate,
    toInternalDateTime,
    toInternalTime,
    fromInternalDateTime,
    fromInternalTime
} from "../../utils/DataFormatUtils"

import { ControlProps, handleChange, handleEnterFocus, handleLeaveFocus, getPlaceholder } from "./Control"
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

    // Refs to access Date values directly from UI5 components
    const datePickerRef = useRef<any>(null)
    const dateTimePickerRef = useRef<any>(null)
    const timePickerRef = useRef<any>(null)

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
                    ref={datePickerRef}
                    id={def.key}
                    value={(element?.va as string) ?? ""}
                    displayFormat="short"
                    value-format='yyyy-MM-dd'
                    placeholder={getPlaceholder(texts, def)}
                    onChange={(e) => {
                        const dateValue = (datePickerRef.current as any)?.dateValue
                        if (dateValue) {
                            handleChange(
                                dispatch,
                                def,
                                rowId,
                                messages,
                                toInternalDate(dateValue, getLanguage()!),
                            )
                        }
                    }}
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
                    ref={dateTimePickerRef}
                    id={def.key}
                    value={element?.va ? fromInternalDateTime((element.va as string), getLanguage()!) : ""}
                    displayFormat="short"
                    valueFormat='yyyy-MM-ddTHH:mm:ss'
                    placeholder={getPlaceholder(texts, def)}
                    onChange={(e) => {
                        console.log(`DateTimePicker onChange triggered with value: ${e.target.value}`)
                        const dateValue = (dateTimePickerRef.current as any)?.dateValue
                        if (dateValue) {
                            handleChange(
                                dispatch,
                                def,
                                rowId,
                                messages,
                                toInternalDateTime(dateValue, getLanguage()!),
                            )
                        }
                    }}
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
                    ref={timePickerRef}
                    id={def.key}
                    value={element?.va ? fromInternalTime((element.va as string), getLanguage()!) : ""}
                    displayFormat="medium"
                    valueFormat="HH:mm:ss"
                    placeholder={getPlaceholder(texts, def)}
                    onChange={(e) => {
                        const timeValue = (timePickerRef.current as any)?.dateValue
                        if (timeValue) {
                            handleChange(
                                dispatch,
                                def,
                                rowId,
                                messages,
                                toInternalTime(timeValue, getLanguage()!),
                            )
                        } else if (e.target.value.length === 0) {
                            handleChange(dispatch, def, rowId, messages, undefined)
                        }
                    }}
                    onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                    onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                    readonly={!element?.ed || !globalEd}
                    required={element?.rq}
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
                    placeholder={getPlaceholder(texts, def)}
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
                            : def.inputType === "password"
                              ? "Password"
                              : def.inputType === "email"
                                ? "Email"
                                : def.inputType === "number"
                                  ? "Number"
                                  : def.inputType === "telephone"
                                    ? "Tel"
                                    : "Text"
                    }
                />
            )
    }

    // as default we asume string as data-type, leading to InputText as ui control
    return <ControlContainer {...props}>{control}</ControlContainer>
}
