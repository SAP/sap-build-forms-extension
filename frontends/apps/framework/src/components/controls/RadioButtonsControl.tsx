import { useEffect, useState } from "react"

import { RadioButton } from "@ui5/webcomponents-react"

import { useMessages, Placeholder } from "commons"

import {
    ControlProps,
    handleChange,
    handleDynamicValueHelp,
    handleEnterFocus,
    handleLeaveFocus,
    handleValueHelp,
} from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { ValueName } from "../../features/valuehelps/logic"
import { FormService } from "../../features/sessions/forms"
import { elementInfo2ValueState } from "./utils"

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { def, rowId } = props
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const vhs = useAppSelector((state) => state.valuehelps.vhs)
    const dvhs = useAppSelector((state) => state.session.dvhs)
    const locale = useAppSelector((state) => state.session.locale)
    const [options, setOptions] = useState<ValueName[]>([])
    const [elementDisabled, setElementDisabled] = useState<boolean>(true)
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    // the following useEffects handle "static" and "dynamic" value-helps.
    useEffect(() => {
        handleDynamicValueHelp(def, dvhs, setOptions, setElementDisabled)
    }, [dvhs])
    useEffect(() => {
        handleValueHelp(def, vhs, locale, setOptions, setElementDisabled)
    }, [vhs])

    return (
        <ControlContainer {...props}>
            {options.length === 0 && <Placeholder />}
            {options.length > 0 && (
                <div>
                    {options.map((it, i) => (
                        <RadioButton
                            key={def.key + "_" + i}
                            name={def.key}
                            value={it.value}
                            onChange={() => handleChange(dispatch, def, rowId, messages, it.value)}
                            onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                            onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                            checked={(element?.va as string) === it.value}
                            text={it.name}
                            valueState={elementInfo2ValueState(element?.msg)}
                            disabled={elementDisabled || !element?.ed || !props.globalEd}
                        />
                    ))}
                </div>
            )}
        </ControlContainer>
    )
}
