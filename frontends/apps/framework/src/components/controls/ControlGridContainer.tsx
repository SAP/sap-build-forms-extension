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

                // console.log(
                //     `Child ${child.id} has colspan ${colspan} (calculated value: ${colspanValue})`,
                // )

                // Calculate indent needed to push to new row
                let indent: string | undefined = undefined
                let className = ""
                if (child.lineBreak && currentColPosition > 0) {
                    // const remainingCols = 12 - currentColPosition
                    // indent = `XL${remainingCols} L${remainingCols} M${remainingCols} S${remainingCols}`
                    // currentColPosition = 0

                    // debugger

                    // col: "sm:12 md:6 lg:4 xl:4"
                    // let span = ""
                    // if (child.col) {
                    //     child.col.split(" ").forEach((col) => {
                    //         const [t, value] = col.split(":")
                    //         const v = 12 - parseInt(value)
                    //         switch (t) {
                    //             case "xl":
                    //                 span += ` XL${v}`
                    //                 break
                    //             case "lg":
                    //                 span += ` L${v}`
                    //                 break
                    //             case "md":
                    //                 span += ` M${v}`
                    //                 break
                    //             case "sm":
                    //                 span += ` S${v}`
                    //                 break
                    //         }
                    //     })
                    // }
                    // children.push(<div key={child.key + "_b"} data-layout-span={span}></div>)
                    className = "forms_newline"
                }

                children.push(
                    <div
                        key={child.key}
                        data-layout-span={colspan}
                        data-layout-indent={indent}
                        className={className}
                    >
                        <Control {...props} def={child} />
                    </div>,
                )
                currentColPosition = (currentColPosition + colspanValue) % 12
            }
        }
    }

    return <Grid className="forms_grid">{children}</Grid>
}
