import { useEffect, useState } from "react"

import { RadioButton } from "@ui5/webcomponents-react"

import { useMessages, Placeholder } from "commons"

import { ControlProps, handleChange, handleEnterFocus, handleLeaveFocus } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { ValuehelpsService } from "../../features/valuehelps/logic"
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
    const locale = useAppSelector((state) => state.session.locale)
    const [options, setOptions] = useState<Record<string, string>>()
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    useEffect(() => {
        if (def.vh && vhs[def.vh.name]) {
            const p = ValuehelpsService.loadFormLocalstore(def.vh.name, locale)
            p.then((values) => {
                setOptions(ValuehelpsService.createVHOptionsAsRecord(values, def.vh))
            })
        }
    }, [vhs])

    return (
        <ControlContainer {...props}>
            {!options && <Placeholder />}
            {options && (
                <div>
                    {Object.getOwnPropertyNames(options).map((key, i) => (
                        <RadioButton
                            key={def.key + "_" + i}
                            name={def.key}
                            value={key}
                            onChange={() => handleChange(dispatch, def, rowId, messages, key)}
                            onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                            onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                            checked={(element?.va as string) === key}
                            text={options[key]}
                            valueState={elementInfo2ValueState(element?.msg)}
                        />
                    ))}
                </div>
            )}
        </ControlContainer>
    )
}
