import { PropsWithChildren, useState } from "react"

import { FlexBox, FlexBoxJustifyContent, Icon, Label, Popover } from "@ui5/webcomponents-react"

import "@ui5/webcomponents-icons/dist/sys-help.js"
import { Definition } from "../../features/sessions/definitions"
import { Element, FormService } from "../../features/sessions/forms"
import { useAppSelector } from "../../features/store"
import { getDoc, getLabel } from "./Control"

/**
 *
 */
interface Props {
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
    const helpIconId = "help-" + def.key
    const [helpOpen, setHelpOpen] = useState(false)

    return (
        <FlexBox
            direction="Column"
            fitContainer
            alignItems="Stretch"
            justifyContent={justifyContent ?? "Center"}
            style={{ height: "100%" }}
        >
            {!asTableCell && (
                <FlexBox alignItems="Center" style={{ gap: "0.25rem" }}>
                    <Label
                        id={"l" + def.key}
                        for={def.key}
                        required={element?.rq}
                        style={def.showLabel === false ? { visibility: "hidden" } : undefined}
                    >
                        {def.showLabel !== false ? labelText : ""}
                    </Label>
                    {def.showHelp && (
                        <>
                            <Icon
                                id={helpIconId}
                                name="sys-help"
                                style={{ cursor: "pointer", fontSize: "0.5rem" }}
                                onClick={() => setHelpOpen(true)}
                            />
                            <Popover
                                opener={helpIconId}
                                open={helpOpen}
                                placement="End"
                                onClose={() => setHelpOpen(false)}
                            >
                                <div style={{ maxWidth: "20rem" }}>
                                    {getDoc(texts, def)}
                                </div>
                            </Popover>
                        </>
                    )}
                </FlexBox>
            )}
            {children}
        </FlexBox>
    )
}
