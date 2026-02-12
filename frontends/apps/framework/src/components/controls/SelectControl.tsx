import { useEffect, useState } from "react"

import { useIntl } from "react-intl"

import { Option, Select, Text } from "@ui5/webcomponents-react"

import { useMessages } from "commons"

import { ControlProps, handleChange, handleEnterFocus, handleLeaveFocus } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { FormService } from "../../features/sessions/forms"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { ValueName, ValuehelpsService } from "../../features/valuehelps/logic"
import { elementInfo2ValueState, elementInfo2ValueStateText } from "./utils"

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { def, globalEd, rowId } = props
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const vhs = useAppSelector((state) => state.valuehelps.vhs)
    const locale = useAppSelector((state) => state.session.locale)
    const intl = useIntl()
    const [options, setOptions] = useState<ValueName[]>([])
    const [elementDisabled, setElementDisabled] = useState<boolean>(true)
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    /**
     *
     */
    useEffect(() => {
        // console.log(`SelectControl: def=${def.id} with vh=${def.vh?.name} and locale=${locale}`)
        if (def.vh && vhs[def.vh.name]) {
            const p = ValuehelpsService.loadFormLocalstore(def.vh.name, locale)
            p.then((values) => {
                setOptions(ValuehelpsService.createVHOptions(values, def.vh))
                setElementDisabled(false)
            })
        }
    }, [vhs])

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
                {def.el && !element?.rq && (
                    <Option
                        selected={element?.va !== "string" || (element?.va as string) === ""}
                    ></Option>
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
