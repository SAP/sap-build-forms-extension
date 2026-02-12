import { ReactNode } from "react"

import { Grid } from "@ui5/webcomponents-react"

import Control, { ControlProps } from "./Control"
import { calculateColspan } from "./utils"
import { FormService } from "../../features/sessions/forms"
import { useAppSelector } from "../../features/store"

export default function (props: ControlProps) {
    const { def, rowId } = props
    const form = useAppSelector((state) => state.session.form)

    let children: ReactNode[] = []
    if (def.elements) {
        for (let child of def.elements) {
            // console.log(`  child ${child.id} / ${child.key}`)
            const element = FormService.findElementByRowAndKey(rowId, child.key, form)
            if (element?.vi) {
                children.push(
                    <Control
                        {...props}
                        key={child.key}
                        def={child}
                        data-layout-span={calculateColspan(child)}
                    />,
                )
            }
        }
    }

    return <Grid>{children}</Grid>
}
