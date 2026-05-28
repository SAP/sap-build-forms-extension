import { createPortal } from "react-dom"

import { Dialog } from "@ui5/webcomponents-react"

import { useAppDispatch, useAppSelector } from "../../features/store"
import { ControlProps, getLabel } from "./Control"
import { FormService } from "../../features/sessions/forms"
import { update } from "../../features/sessions/sessionSlice"
import { ElementProp } from "../../features/sessions/journal"
import ControlGridContainer from "./ControlGridContainer"
import ToolbarControl from "./ToolbarControl"

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { def, rowId, texts } = props
    const dispatch = useAppDispatch()
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    // console.log(`DialogControl: ${def.key} - ${element?.vi}`)

    let headerText = element?.va
    if (typeof headerText !== "string" || headerText.length === 0) {
        headerText = getLabel(texts, def)
    }

    const hasSize = def.size?.height || def.size?.width

    return (
        <>
            {element?.vi &&
                createPortal(
                    <Dialog
                        headerText={headerText}
                        open={true}
                        stretch={!hasSize}
                        style={hasSize ? { height: def.size?.height, width: def.size?.width } : undefined}
                        footer={<ToolbarControl {...props} def={def.footer!} />}
                        onClose={() =>
                            dispatch(
                                update({
                                    def,
                                    rowId,
                                    prop: ElementProp.Visible,
                                    value: false,
                                }),
                            )
                        }
                    >
                        <ControlGridContainer {...props} />
                    </Dialog>,
                    document.body,
                )}
        </>
    )
}
