import { Text } from "@ui5/webcomponents-react"

import { FormService } from "../../features/sessions/forms"
import { useAppSelector } from "../../features/store"
import { ControlProps, getDoc } from "./Control"
import ControlContainer from "./ControlFlexContainer"

export default function (props: ControlProps) {
    const { def, rowId, texts } = props
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    let text = element!.va as string
    if (!text || text.length === 0) {
        text = getDoc(texts, def)
    }

    return (
        <ControlContainer {...props} asTableCell={true} justifyContent="Center">
            <Text>{text}</Text>
        </ControlContainer>
    )
}
