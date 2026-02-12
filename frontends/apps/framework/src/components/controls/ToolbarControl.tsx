import { ReactNode } from "react"

import { Bar } from "@ui5/webcomponents-react"
import BarDesign from "@ui5/webcomponents/dist/types/BarDesign"

import { FormService } from "../../features/sessions/forms"
import Control, { ControlProps } from "./Control"
import { useAppSelector } from "../../features/store"

export default function (props: ControlProps) {
    const { def, rowId } = props
    const form = useAppSelector((state) => state.session.form)

    // console.log(`Toolbar for ${def.id}`)

    let children: ReactNode[] = []
    let start: ReactNode[] = []
    let end: ReactNode[] = []
    if (def.elements) {
        for (let child of def.elements) {
            const element = FormService.findElementByRowAndKey(rowId, child.key, form)
            if (element?.vi) {
                children.push(
                    <Control {...props} withContainer={false} key={child.id} def={child} />,
                )
            }
        }
    }
    if (children.length === 0) {
        children.push(<div key="_"></div>)
    }
    if (def.leftElements) {
        for (let child of def.leftElements) {
            const element = FormService.findElementByRowAndKey(rowId, child.key, form)
            if (element?.vi) {
                start.push(<Control {...props} withContainer={false} key={child.key} def={child} />)
            }
        }
    }
    if (def.rightElements) {
        for (let child of def.rightElements) {
            const element = FormService.findElementByRowAndKey(rowId, child.key, form)
            if (element?.vi) {
                end.push(<Control {...props} withContainer={false} key={child.key} def={child} />)
            }
        }
    }

    return (
        <Bar
            {...props}
            design={props.design ? (props.design as BarDesign) : "Header"}
            startContent={
                <span
                    style={{
                        display: "flex",
                        gap: ".5rem",
                    }}
                >
                    {start}
                </span>
            }
            endContent={
                <span
                    style={{
                        display: "flex",
                        gap: ".5rem",
                    }}
                >
                    {end}
                </span>
            }
            style={{ width: "100%" }}
        >
            <span
                style={{
                    display: "flex",
                    gap: ".5rem",
                }}
            >
                {children}
            </span>
        </Bar>
    )
}
