import { Label } from "@ui5/webcomponents-react"
import { FormService } from "../../features/sessions/forms"
import { useAppSelector } from "../../features/store"
import { ControlProps } from "./Control"
import ControlContainer from "./ControlFlexContainer"

export default function (props: ControlProps) {
    const { def, rowId, withContainer } = props
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    const imageSrc = (element?.va as string) || ""
    
    const height = def.size?.height || undefined
    const width = def.size?.width || undefined

    // priority: coloumn width (maxWidth) - height and width - 100% - no stretching
    const imageElement = (
        <img
            src={imageSrc}
            alt={def.id}
            style={{
                height: height || "auto",
                width: width || "100%",
                objectFit: "contain",
                maxWidth: "100%",
            }}
        />
    )

    if (withContainer) {
        return (
            <ControlContainer {...props} asTableCell={true} justifyContent="End">
                <Label></Label>
                {imageElement}
            </ControlContainer>
        )
    }

    return imageElement
}
