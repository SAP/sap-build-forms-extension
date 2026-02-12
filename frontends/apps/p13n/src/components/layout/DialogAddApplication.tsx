import {
    Bar,
    Button,
    Dialog,
    Form,
    FormItem,
    Input,
    InputDomRef,
    Label,
    MessageBoxType,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import { JSX, useState } from "react"
import { createUseStyles } from "react-jss"
import { Personalization } from "../../features/personalizationDefinitions"
import { useIntl } from "react-intl"

interface DialogAddApplicationProps {
    dialogOpen: boolean
    username: String | null
    applications: string[]
    setDialogOpen(o: boolean): void
    setApplications(applications: string[]): void
    setPersonalizationsOfUser(personalizations: Personalization[]): void
    openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string): void
}

export default function (props: DialogAddApplicationProps) {
    const useStyles = createUseStyles({
        dialog: {
            paddingTop: 10,
            paddingInline: 3,
        },
        bar: {
            paddingBlock: 3,
        },
        button: {
            marginInline: 2,
        },
        form: {
            padding: 3,
        },
        errorLabel: {
            color: "red",
        },
    })

    const classes = useStyles()
    const intl = useIntl()
    const [newApplicationName, setNewApplicationName] = useState("")

    return (
        <Dialog
            className={classes.dialog}
            footer={
                <Bar
                    design="Footer"
                    className={classes.bar}
                    endContent={
                        <Button
                            onClick={function _a() {
                                props.setDialogOpen(false)
                                setNewApplicationName("")
                            }}
                        >
                            {intl.formatMessage({ id: "common_close" })}
                        </Button>
                    }
                >
                    <Button
                        design="Emphasized"
                        className={classes.button}
                        disabled={
                            newApplicationName.trim().length < 1 ||
                            newApplicationName.trim() == "_" ||
                            props.applications.includes(newApplicationName.trim())
                        }
                        onClick={function _a() {
                            if (
                                newApplicationName.trim().length > 0 &&
                                newApplicationName.trim() != "_" &&
                                !props.applications.includes(newApplicationName.trim())
                            ) {
                                props.setApplications([...props.applications, newApplicationName])
                                props.setDialogOpen(false)
                                setNewApplicationName("")
                            }
                        }}
                    >
                        {intl.formatMessage({ id: "common_add" })}
                    </Button>
                </Bar>
            }
            headerText={intl.formatMessage({ id: "p13n_button_new_application" })}
            open={props.dialogOpen}
        >
            <Form className={classes.form} layout="S1 M1 L1 XL1" labelSpan="S1 M1 L1 XL1">
                <FormItem
                    labelContent={
                        <Label required>
                            {intl.formatMessage({ id: "p13n_new_application_name" })}
                        </Label>
                    }
                >
                    <Input
                        value={newApplicationName}
                        required
                        valueState={
                            props.applications.includes(newApplicationName.trim()) ||
                            newApplicationName.trim() == "_"
                                ? "Negative"
                                : "None"
                        }
                        valueStateMessage={
                            <span>
                                {intl.formatMessage({
                                    id:
                                        newApplicationName.trim() == "_"
                                            ? "p13n_new_application_name_invalid"
                                            : "p13n_new_application_name_existent",
                                })}
                            </span>
                        }
                        onInput={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            setNewApplicationName(e.target.value)
                        }}
                        onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            setNewApplicationName(
                                e.target.attributes.getNamedItem("value")!.nodeValue!,
                            )
                        }}
                    />
                </FormItem>
            </Form>
        </Dialog>
    )
}
