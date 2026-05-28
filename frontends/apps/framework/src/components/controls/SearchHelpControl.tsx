import { useState } from "react"

import ReactDOM from "react-dom"
import { useIntl } from "react-intl"

import { Bar, Button, Dialog, Icon, Input } from "@ui5/webcomponents-react"

import { useMessages } from "commons"

import {
    ControlProps,
    getLabel,
    handleAction,
    handleChange,
    handleEnterFocus,
    handleLeaveFocus,
    handleOpen,
} from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { FormService } from "../../features/sessions/forms"
import { elementInfo2ValueState, elementInfo2ValueStateText } from "./utils"
import ControlGridContainer from "./ControlGridContainer"

/**
 *
 */
// const useStyles = createUseStyles({
//     actuator: {
//         color: "var(--_ui5-v1-24-0_input_icon_color)",
//         "&:hover": {
//             background: "var(--sapField_Hover_Background)",
//             borderColor: "var(--sapField_Hover_BorderColor)",
//         },
//     },
// })

/**
 *
 */
interface DetailDialogProps extends ControlProps {
    setVisible: (value: boolean) => void
}

/**
 *
 * @param props
 * @returns
 */
function SearchDialog(props: DetailDialogProps) {
    const { def, rowId, setVisible, texts } = props
    const intl = useIntl()
    const messages = useMessages()
    const dispatch = useAppDispatch()

    return (
        <>
            {ReactDOM.createPortal(
                <Dialog
                    open={true}
                    headerText={intl.formatMessage(
                        { id: "sh_dialog_title" },
                        { name: getLabel(texts, def) },
                    )}
                    footer={
                        <Bar
                            design="Footer"
                            endContent={
                                <>
                                    <Button onClick={() => setVisible(false)}>
                                        {intl.formatMessage({ id: "sh_dialog_close" })}
                                    </Button>
                                    <Button
                                        design="Emphasized"
                                        onClick={() => {
                                            const p = handleAction(dispatch, def, rowId, messages)
                                            p.finally(() => setVisible(false))
                                        }}
                                    >
                                        {intl.formatMessage({ id: "sh_dialog_select" })}
                                    </Button>
                                </>
                            }
                        />
                    }
                    onClose={() => setVisible(false)}
                    stretch={!def.size?.height && !def.size?.width}
                    style={def.size?.height || def.size?.width ? { height: def.size?.height, width: def.size?.width } : undefined}
                >
                    <ControlGridContainer {...props} asTableCell={false} />
                </Dialog>,
                document.body,
            )}
        </>
    )
}

export default function (props: ControlProps) {
    const { def, globalEd, rowId } = props
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const intl = useIntl()
    const [showDialog, setShowDialog] = useState<boolean>(false)
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const [isHovered, setHovered] = useState<boolean>(false)

    // if (value?.er && typeof value?.er === "object") {
    //     console.log(`InputControl for ${def.id} with info ${(value?.er as ElementInfo).severity}`)
    // }

    const handleShowDialog = async () => {
        await handleOpen(dispatch, def, rowId, messages)
        setShowDialog(true)
    }

    return (
        <ControlContainer {...props}>
            <Input
                id={def.key}
                value={(element?.va as string) ?? ""}
                onChange={(e) => handleChange(dispatch, def, rowId, messages, e.target.value ?? "")}
                onFocus={() => handleEnterFocus(dispatch, def, rowId, messages)}
                onBlur={() => handleLeaveFocus(dispatch, def, rowId, messages)}
                readonly={!element?.ed || !globalEd}
                required={element?.rq}
                valueState={elementInfo2ValueState(element?.msg)}
                valueStateMessage={elementInfo2ValueStateText(intl, element?.msg)}
                style={{ width: "100%" }}
                icon={
                    <Icon
                        name="border"
                        onClick={handleShowDialog}
                        style={{
                            boxShadow: isHovered ? "var(--sapField_Hover_Shadow)" : "none",
                        }}
                    />
                }
                onMouseOver={() => setHovered(true)}
                onMouseLeave={() => setHovered(false)}
                onKeyDown={(event) => {
                    if (event.key === "F4") {
                        event.preventDefault()
                        handleShowDialog()
                    }
                }}
            />
            {showDialog && <SearchDialog {...props} setVisible={setShowDialog} />}
        </ControlContainer>
    )
}
