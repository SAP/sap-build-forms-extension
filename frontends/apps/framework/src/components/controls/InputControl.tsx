import React from "react"
import { useIntl } from "react-intl"
import { useRef, useState } from "react"

import { DatePicker, DateTimePicker, Input, Text, TimePicker } from "@ui5/webcomponents-react"

import { getLanguage, useMessages } from "commons"

import { DataType } from "../../features/sessions/definitions"
import { FormService } from "../../features/sessions/forms"
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

export default function (props: ControlProps) {
    const { def, globalEd, rowId, texts } = props
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const intl = useIntl()
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    const pickerRef = useRef<any>(null)
    const [localViolation, setLocalViolation] = useState<string | undefined>()

    const isString = def.dataType === DataType.String || def.dataType === undefined
    const isNumeric = def.dataType === DataType.Int || def.dataType === DataType.Decimal
    const min = def.limits?.min
    const max = def.limits?.max
    const readonly = !element?.ed || !globalEd

    const valueState = element?.msg ? elementInfo2ValueState(element.msg) : localViolation ? "Negative" : "None"
    const valueStateMessage = element?.msg
        ? elementInfo2ValueStateText(intl, element.msg)
        : localViolation ? <Text>{localViolation}</Text> : <></>

    const inputValue =
        typeof element?.va === "string" ? element.va :
        element?.va == null ? "" : String(element.va)

    const onFocus = () => handleEnterFocus(dispatch, def, rowId, messages)
    const onBlur = () => {
        handleLeaveFocus(dispatch, def, rowId, messages)
    }

    // Resolves the correct UI5 Input `type` attribute.
    const inputType = (() => {
        if (isNumeric) return "Number"
        switch (def.inputType) {
            case "password":  return "Password"
            case "email":     return "Email"
            case "number":    return "Number"
            case "telephone": return "Tel"
            default:          return "Text"
        }
    })()

    // Common props shared by all controls
    const commonProps = {
        id: def.key,
        placeholder: getPlaceholder(texts, def),
        onFocus,
        onBlur,
        readonly,
        required: element?.rq,
        valueState,
        valueStateMessage,
        style: { width: "100%" },
    }

    let control = <></>
    switch (def.dataType) {
        case DataType.Date:
            control = (
                <DatePicker
                    {...commonProps}
                    ref={pickerRef}
                    value={(element?.va as string) ?? ""}
                    displayFormat="short"
                    value-format="yyyy-MM-dd"
                    primaryCalendarType="Gregorian"
                    minDate={min}
                    maxDate={max}
                    onChange={() => {
                        const dateValue = pickerRef.current?.dateValue
                        if (dateValue) handleChange(dispatch, def, rowId, messages, toInternalDate(dateValue, getLanguage()!))
                    }}
                />
            )
            break
        case DataType.DateTime:
            control = (
                <DateTimePicker
                    {...commonProps}
                    ref={pickerRef}
                    value={element?.va ? fromInternalDateTime(element.va as string, getLanguage()!) : ""}
                    displayFormat="short"
                    valueFormat="yyyy-MM-ddTHH:mm:ss"
                    primaryCalendarType="Gregorian"
                    minDate={min}
                    maxDate={max}
                    onChange={() => {
                        const dateValue = pickerRef.current?.dateValue
                        if (dateValue) handleChange(dispatch, def, rowId, messages, toInternalDateTime(dateValue, getLanguage()!))
                    }}
                />
            )
            break
        case DataType.Time:
            control = (
                <TimePicker
                    {...commonProps}
                    ref={pickerRef}
                    value={element?.va ? fromInternalTime(element.va as string, getLanguage()!) : ""}
                    displayFormat="medium"
                    valueFormat="HH:mm:ss"
                    onChange={(e) => {
                        const timeValue = pickerRef.current?.dateValue
                        if (timeValue) {
                            handleChange(dispatch, def, rowId, messages, toInternalTime(timeValue, getLanguage()!))
                        } else if (e.target.value.length === 0) {
                            handleChange(dispatch, def, rowId, messages, undefined)
                        }
                    }}
                />
            )
            break
        default:
            control = (
                <Input
                    {...commonProps}
                    value={inputValue}
                    type={inputType}
                    maxlength={isString && max ? parseInt(max) : undefined}
                    min={isNumeric ? min : undefined}
                    max={isNumeric ? max : undefined}
                    onChange={(e) => {
                        const raw = e.target.value ?? ""
                        const value =
                            def.dataType === DataType.Int ? parseInt(raw) :
                            def.dataType === DataType.Decimal ? parseFloat(raw) :
                            raw
                        handleChange(dispatch, def, rowId, messages, value)
                    }}
                />
            )
    }

    // as default we asume string as data-type, leading to InputText as ui control
    return <ControlContainer {...props}>{control}</ControlContainer>
}
