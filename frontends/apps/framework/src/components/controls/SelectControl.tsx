import { useEffect, useState } from "react"

import { useIntl } from "react-intl"

import { Option, Select, Text } from "@ui5/webcomponents-react"

import { useMessages } from "commons"

import {
    ControlProps,
    handleChange,
    handleEnterFocus,
    handleLeaveFocus,
    getPlaceholder,
    handleDynamicValueHelp,
    handleValueHelp,
} from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { FormService } from "../../features/sessions/forms"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { ValueName } from "../../features/valuehelps/logic"
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
    const vhs = useAppSelector((state) => state.valuehelps.vhs)
    const dvhs = useAppSelector((state) => state.session.dvhs)
    const locale = useAppSelector((state) => state.session.locale)
    const intl = useIntl()
    const [options, setOptions] = useState<ValueName[]>([])
    const [elementDisabled, setElementDisabled] = useState<boolean>(true)
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const emptySelection = def.vh?.emptySelection ?? false

    // the following useEffects handle "static" and "dynamic" value-helps.
    useEffect(() => {
        handleDynamicValueHelp(def, dvhs, setOptions, setElementDisabled)
    }, [dvhs])
    useEffect(() => {
        handleValueHelp(def, vhs, locale, setOptions, setElementDisabled)
    }, [vhs])

    useEffect(() => {
        const hasValue = typeof element?.va === "string" && element.va.length > 0
        if (emptySelection || hasValue || options.length === 0 || !element?.ed || !globalEd) {
            return
        }
        void handleChange(dispatch, def, rowId, messages, options[0].value)
    }, [
        dispatch,
        def,
        rowId,
        messages,
        options,
        emptySelection,
        element?.va,
        element?.ed,
        globalEd,
    ])

    // console.log(`Element ${def.id} has value-help ${def.vh}`)

    return (
        <ControlContainer {...props}>
            <Select
                id={def.key}
                disabled={elementDisabled || !element?.ed || !globalEd}
                required={element?.rq}
                onChange={(e) =>
                    handleChange(
                        dispatch,
                        def,
                        rowId,
                        messages,
                        e.detail.selectedOption.dataset.id ?? "",
                    )
                }
                onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                valueState={elementInfo2ValueState(element?.msg)}
                valueStateMessage={elementInfo2ValueStateText(intl, element?.msg)}
                style={{
                    width: "100%",
                }}
            >
                {emptySelection && (
                    <Option
                        selected={typeof element?.va !== "string" || element.va === ""}
                        data-id=""
                    >
                        {getPlaceholder(texts, def) ?? ""}
                    </Option>
                )}
                {options.map((it, i) => (
                    <Option key={"s" + i} selected={it.value == element?.va} data-id={it.value}>
                        <Text>{it.name}</Text>
                    </Option>
                ))}
            </Select>
        </ControlContainer>
    )
}
