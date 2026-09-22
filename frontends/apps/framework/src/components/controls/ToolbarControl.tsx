import { ReactNode } from "react"

import { Toolbar, ToolbarSpacer } from "@ui5/webcomponents-react"

import { FormService } from "../../features/sessions/forms"
import Control, { ControlProps } from "./Control"
import { useAppSelector } from "../../features/store"

export default function (props: ControlProps) {
    const { def, rowId } = props
    const form = useAppSelector((state) => state.session.form)

    if (!def) return <></>

    const start: ReactNode[] = []
    const center: ReactNode[] = []
    const end: ReactNode[] = []

    // this can occur if toobar-control is called from dialog etc. withat have no toolbar defined. In this
    // case we simply render nothing
    if (def.leftElements) {
        for (const child of def.leftElements) {
            const element = FormService.findElementByRowAndKey(rowId, child.key, form)
            if (element?.vi) {
                start.push(<Control {...props} withContainer={false} insideToolbar key={child.key} def={child} />)
            }
        }
    }

    if (def.elements) {
        for (const child of def.elements) {
            const element = FormService.findElementByRowAndKey(rowId, child.key, form)
            if (element?.vi) {
                center.push(<Control {...props} withContainer={false} insideToolbar key={child.id} def={child} />)
            }
        }
    }

    if (def.rightElements) {
        for (const child of def.rightElements) {
            const element = FormService.findElementByRowAndKey(rowId, child.key, form)
            if (element?.vi) {
                end.push(<Control {...props} withContainer={false} insideToolbar key={child.key} def={child} />)
            }
        }
    }

    if (start.length === 0 && center.length === 0 && end.length === 0) {
        return <></>
    }

    return (
        <Toolbar style={{ width: "100%" }} design="Solid" >
            {start}
            {center}
            <ToolbarSpacer />
            {end}
        </Toolbar>
    )
}
