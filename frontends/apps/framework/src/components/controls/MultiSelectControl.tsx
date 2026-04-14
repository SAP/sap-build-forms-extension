import { useEffect, useState } from "react"

import { useIntl } from "react-intl"

import { MultiComboBox, MultiComboBoxItem } from "@ui5/webcomponents-react"

import { useMessages } from "commons"

import { useAppDispatch, useAppSelector } from "../../features/store"
import { ValuehelpsService } from "../../features/valuehelps/logic"

import { ControlProps, handleChange, handleEnterFocus, handleLeaveFocus } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { elementInfo2ValueState, elementInfo2ValueStateText } from "./utils"
import { FormService } from "../../features/sessions/forms"

/**
 *
 */
interface ValueName {
    value: string
    name: string
}

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { def, globalEd, rowId } = props
    const intl = useIntl()
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const vhs = useAppSelector((state) => state.valuehelps.vhs)
    const locale = useAppSelector((state) => state.session.locale)
    const [options, setOptions] = useState<ValueName[]>([])
    const [elementDisabled, setElementDisabled] = useState<boolean>(true)
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const emptySelection = def.vh?.emptySelection ?? false

    useEffect(() => {
        if (def.vh && vhs[def.vh.name]) {
            const p = ValuehelpsService.loadFormLocalstore(def.vh.name, locale)
            p.then((values) => {
                setOptions(ValuehelpsService.createVHOptions(values, def.vh))
                setElementDisabled(false)
            })
        }
    }, [vhs])

    return (
        <ControlContainer {...props}>
            <MultiComboBox
                id={def.key}
                style={{ width: "100%" }}
                readonly={elementDisabled || !element?.ed || !globalEd}
                required={element?.rq}
                onOpen={() => console.log("onOpen")}
                onSelectionChange={(e) => {
                    if (e.detail.items) {
                        let v = ""
                        e.detail.items.forEach((item) => (v += item.dataset.id + ";"))
                        handleChange(dispatch, def, rowId, messages, v)
                    }
                }}
                onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                valueState={elementInfo2ValueState(element?.msg)}
                valueStateMessage={elementInfo2ValueStateText(intl, element?.msg)}
            >
                {options.map((it) => {
                    let isSelected = false
                    if (!emptySelection && element?.va && typeof element.va === "string") {
                        isSelected = element.va.search(it.value + ";") > -1
                    }
                    return (
                        <MultiComboBoxItem
                            key={it.value}
                            text={it.name}
                            data-id={it.value}
                            selected={isSelected}
                        />
                    )
                })}
            </MultiComboBox>
        </ControlContainer>
    )
}
