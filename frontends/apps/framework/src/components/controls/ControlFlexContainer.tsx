import { PropsWithChildren } from "react"

import { FlexBox, FlexBoxJustifyContent, Label } from "@ui5/webcomponents-react"

import { Definition } from "../../features/sessions/definitions"
import { Element, FormService } from "../../features/sessions/forms"
import { useAppSelector } from "../../features/store"
import { getLabel } from "./Control"

/**
 *
 */
interface Props {
    css?: string
    def: Definition
    texts: Record<string, string>
    value?: Element
    asTableCell: boolean
    rowId: string
    justifyContent?:
        | "Start"
        | "Center"
        | "End"
        | FlexBoxJustifyContent
        | "SpaceAround"
        | "SpaceBetween"
        | undefined
}

/**
 *
 * @param props
 * @returns
 */
export default function (props: Props & PropsWithChildren) {
    const { asTableCell, children, def, justifyContent, rowId, texts } = props
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const labelText = getLabel(texts, def)

    return (
        <FlexBox
            direction="Column"
            fitContainer
            alignItems="Stretch"
            justifyContent={justifyContent ?? "Center"}
            style={{ height: "100%" }}
        >
            {!asTableCell && (
                <Label
                    id={"l" + def.key}
                    for={def.key}
                    required={element?.rq}
                    style={def.showLabel === false ? { visibility: "hidden" } : undefined}
                >
                    {def.showLabel !== false ? labelText : ""}
                </Label>
            )}
            {children}
        </FlexBox>
    )
}
