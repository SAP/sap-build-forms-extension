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

    /** Renders one combined min/max hint line depending on data type. */
    const renderLimitsHint = () => {
        const hints: React.ReactNode[] = []

        // Fixed hint (string or decimal)
        if (def.limits?.fixedLength !== undefined) {
            const key = def.dataType === DataType.Decimal
                ? "default.msg.validation.hint.fixed.decimal"
                : "default.msg.validation.hint.fixed.string"
            const values: Record<string, string> = { length: def.limits.fixedLength }
            if (def.dataType === DataType.Decimal && def.limits.fixedFractions !== undefined) {
                values.fractions = def.limits.fixedFractions
            }
            hints.push(
                <Text key="fixed" style={{ fontSize: "0.75rem", color: "var(--sapContent_LabelColor)" }}>
                    {intl.formatMessage({ id: key }, values)}
                </Text>
            )
        }

        // Min/max hint
        if (min !== undefined || max !== undefined) {
            let kind: "string" | "number" | "date" | "datetime" | "time" | null = null
            let formatValue: (v: string) => string = (v) => v

            if (isString) {
                kind = "string"
            } else if (isNumeric) {
                kind = "number"
            } else if (def.dataType === DataType.Date) {
                kind = "date"
                formatValue = (v) => intl.formatDate(v, { dateStyle: "medium" })
            } else if (def.dataType === DataType.DateTime) {
                kind = "datetime"
                formatValue = (v) => intl.formatDate(v, { dateStyle: "medium", timeStyle: "medium" })
            } else if (def.dataType === DataType.Time) {
                kind = "time"
                formatValue = (v) => {
                    const [h, m] = v.split(":")
                    const d = new Date()
                    d.setHours(parseInt(h) || 0, parseInt(m) || 0, 0, 0)
                    return intl.formatTime(d, { timeStyle: "short" })
                }
            }

            if (kind) {
                const suffix = min !== undefined && max !== undefined ? "range" : min !== undefined ? "min" : "max"
                const values = {
                    ...(min !== undefined && { min: formatValue(min) }),
                    ...(max !== undefined && { max: formatValue(max) }),
                }
                hints.push(
                    <Text key="minmax" style={{ fontSize: "0.75rem", color: "var(--sapContent_LabelColor)" }}>
                        {intl.formatMessage({ id: `default.msg.validation.hint.${kind}.${suffix}` }, values)}
                    </Text>
                )
            }
        }

        return hints.length > 0 ? <>{hints}</> : null
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
                <>
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
                    {renderLimitsHint()}
                </>
            )
            break
        case DataType.DateTime:
            control = (
                <>
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
                    {renderLimitsHint()}
                </>
            )
            break
        case DataType.Time:
            control = (
                <>
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
                    {renderLimitsHint()}
                </>
            )
            break
        default:
            control = (
                <>
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
                    {renderLimitsHint()}
                </>
            )
    }

    // as default we asume string as data-type, leading to InputText as ui control
    return <ControlContainer {...props}>{control}</ControlContainer>
}
