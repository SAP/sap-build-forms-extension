import { MutableRefObject, useRef } from "react"

import { useIntl } from "react-intl"

import { DateRangePicker, DateRangePickerDomRef } from "@ui5/webcomponents-react"

import { getLanguage, useMessages } from "commons"

import { ControlProps, handleChange, handleEnterFocus, handleLeaveFocus } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { DateRange, FormService } from "../../features/sessions/forms"
import { elementInfo2ValueState, elementInfo2ValueStateText } from "./utils"
import { formatDate, fromInternalDate, toInternalDate } from "../../utils/DataFormatUtils"

/**
 *
 * @param value
 * @returns
 */
function formatValue(value: DateRange): string {
    if (value) {
        const l = getLanguage()
        const fd = fromInternalDate(value.f, l)
        const td = fromInternalDate(value.t, l)
        return formatDate(fd, l) + " - " + formatDate(td, l)
    }
    return ""
}

/**
 *
 * @param ref
 * @returns
 */
function parseValue(
    ref: MutableRefObject<DateRangePickerDomRef | undefined>,
): DateRange | undefined {
    const f = toInternalDate(ref.current!.startDateValue!, "")
    const t = toInternalDate(ref.current!.endDateValue!, "")

    if (!f || !t) {
        return undefined
    }
    return { f, t }
}

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { def, globalEd, rowId } = props
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const intl = useIntl()
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const ref = useRef<DateRangePickerDomRef>(undefined)

    // console.log(`DateRangePicker with value ${element!.va as string}`)
    // console.log(element!.va as string)

    return (
        <ControlContainer {...props}>
            <DateRangePicker
                ref={ref as MutableRefObject<DateRangePickerDomRef | null>}
                id={def.key}
                value={formatValue(element!.va as DateRange)}
                readonly={!element?.ed || !globalEd}
                required={element?.rq}
                onChange={(e) => handleChange(dispatch, def, rowId, messages, parseValue(ref))}
                onInput={(e) => handleChange(dispatch, def, rowId, messages, parseValue(ref))}
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
