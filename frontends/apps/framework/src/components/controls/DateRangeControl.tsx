import { useRef } from "react"

import { useIntl } from "react-intl"

import { DateRangePicker, DateRangePickerDomRef } from "@ui5/webcomponents-react"

import { getLanguage, useMessages } from "commons"

import { ControlProps, handleChange, handleEnterFocus, handleLeaveFocus, getPlaceholder } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { DateRange, FormService } from "../../features/sessions/forms"
import { elementInfo2ValueState, elementInfo2ValueStateText } from "./utils"
import { toInternalDateRange, fromInternalDateRange } from "../../utils/DataFormatUtils"

/**
 * Format a date range value for display
 *
 * @param value
 * @returns
 */
function formatValue(value: DateRange | undefined): string {
    if (value) {
        const l = getLanguage()
        return fromInternalDateRange(value, l)
    }
    return ""
}

/**
 * Parse date range from DateRangePickerDomRef to internal format
 *
 * @param ref
 * @returns
 */
function parseValue(
    ref: React.RefObject<DateRangePickerDomRef | undefined>,
): DateRange | undefined {
    const l = getLanguage()
    const result = toInternalDateRange(ref.current!.startDateValue!, ref.current!.endDateValue!, l)
    return result
}

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
    const ref = useRef<DateRangePickerDomRef | null>(null)

    return (
        <ControlContainer {...props}>
            <DateRangePicker
                ref={ref}
                id={def.key}
                value={formatValue(element?.va as DateRange | undefined)}
                displayFormat="short"
                valueFormat="short"
                placeholder={getPlaceholder(texts, def)}
                readonly={!element?.ed || !globalEd}
                required={element?.rq}
                onChange={(e) => handleChange(dispatch, def, rowId, messages, parseValue(ref as React.RefObject<DateRangePickerDomRef | undefined>))}
                onInput={(e) => handleChange(dispatch, def, rowId, messages, parseValue(ref as React.RefObject<DateRangePickerDomRef | undefined>))}
                onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                primaryCalendarType="Gregorian"
                onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                valueState={elementInfo2ValueState(element?.msg)}
                valueStateMessage={elementInfo2ValueStateText(intl, element?.msg)}
                style={{ width: "100%" }}
            />
        </ControlContainer>
    )
}
