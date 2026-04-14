import { Label, Text } from "@ui5/webcomponents-react"

import { FormService } from "../../features/sessions/forms"
import { useAppSelector } from "../../features/store"
import { ControlProps, getDoc, getLabel } from "./Control"
import ControlContainer from "./ControlFlexContainer"

export default function (props: ControlProps) {
    const { def, rowId, texts, asTableCell } = props
    const showLabel = def.showLabel ?? false
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)    

    let text = (element?.va as string) ?? ""

    const label = getLabel(texts, def)
    
    // if there is no label in a table cell text, we can just show the text centered in the cell
    if (!showLabel && asTableCell) {
        return (
            <ControlContainer {...props} asTableCell={true}>
                <Text>{text}</Text>
            </ControlContainer>
        )
    // if there is a label in a table cell text, we show it directly above the text, since the input fields have no labels in table cells
    } else if (showLabel && asTableCell) {
        return (
            <ControlContainer {...props} asTableCell={true}>
                <Label>{label}</Label>
                <Text>{text}</Text>
            </ControlContainer>
        )
    } else { 
        // when the test is not in the table cell, we space label and text like label and input field 
        return (
            <ControlContainer {...props} asTableCell={false}>
                <div style={{ height: "2.75rem", display: "flex", alignItems: "center" }}>
                    <Text>{text}</Text>
                </div>
            </ControlContainer>
        )  
    }
}
