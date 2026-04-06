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
    let currentColPosition = 0

    if (def.elements) {
        for (let child of def.elements) {
            // console.log(`  child ${child.id} / ${child.key}`)
            const element = FormService.findElementByRowAndKey(rowId, child.key, form)
            if (element?.vi) {
                // Parse colspan (assuming format like "XL6 L6 M6 S12")
                const colspan = calculateColspan(child)
                const colspanValue = parseInt(colspan.split(" ")[0].replace("XL", "")) || 6

                console.log(`Child ${child.id} has colspan ${colspan} (calculated value: ${colspanValue})`)

                // Calculate indent needed to push to new row
                let indent: string | undefined = undefined
                if (child.lineBreak && currentColPosition > 0) {
                    const remainingCols = 12 - currentColPosition
                    indent = `XL${remainingCols} L${remainingCols} M${remainingCols} S${remainingCols}`
                    currentColPosition = 0
                }

                children.push(
                    <div
                        key={child.key}
                        data-layout-span={colspan}
                        data-layout-indent={indent}
                    >
                        <Control
                            {...props}
                            def={child}
                        />
                    </div>
                )
                currentColPosition = (currentColPosition + colspanValue) % 12
            }
        }
    }

    return <Grid>{children}</Grid>
}