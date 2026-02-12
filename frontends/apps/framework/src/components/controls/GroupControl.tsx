import { FlexBox, Panel, Title } from "@ui5/webcomponents-react"
import { ThemingParameters } from "@ui5/webcomponents-react-base"

import { SeverityIcon, useMessages } from "commons"

import { FormService } from "../../features/sessions/forms"
import { ControlProps, getLabel, handleChange } from "./Control"
import { useAppDispatch, useAppSelector } from "../../features/store"
import ControlGridContainer from "./ControlGridContainer"

/**
 *
 */
interface GroupControlProps extends ControlProps {
    showPanel?: boolean
}

/**
 *
 * @param props
 * @returns
 */
export default function (props: GroupControlProps) {
    const { def, rowId, showPanel, slot, texts } = props
    const dispatch = useAppDispatch()
    const form = useAppSelector((state) => state.session.form)
    const messages = useMessages()

    // console.log(`Group for '${def.id}' on row '${rowId}'`)

    // determine if panel is collapsed or not
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const isCollapsed = typeof element?.va === "undefined" ? false : (element?.va as boolean)

    if (typeof showPanel === "undefined" || showPanel) {
        return (
            <Panel
                header={
                    <FlexBox slot={slot}>
                        <SeverityIcon severity={element?.msg?.severity} />
                        <Title
                            level="H3"
                            style={{ fontSize: ThemingParameters.sapFontHeader5Size }}
                        >
                            {getLabel(texts, def)}
                        </Title>
                    </FlexBox>
                }
                collapsed={isCollapsed}
                style={{ width: "100%" }}
                onToggle={() => handleChange(dispatch, def, rowId, messages, !isCollapsed)}
            >
                <ControlGridContainer {...props} />
            </Panel>
        )
    }

    return <ControlGridContainer {...props} />
}
