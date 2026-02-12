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
import { backendDispatch } from "../../utils/backend"
import { Personalization } from "../../features/personalizationDefinitions"
import { useIntl } from "react-intl"
import FCLLayout from "@ui5/webcomponents-fiori/dist/types/FCLLayout"

interface DialogAddUserProps {
    dialogOpen: boolean
    username: String | null
    users: string[]
    setDialogOpen(o: boolean): void
    setUsername(username: string): void
    setUsers(users: string[]): void
    setPersonalizationsOfUser(personalizations: Personalization[]): void
    setApplication(application: string): void
    openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string): void
    setLayout(layout: FCLLayout): void
}

export default function (props: DialogAddUserProps) {
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
    const [newUsername, setNewUsername] = useState("")

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
                                setNewUsername("")
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
                            newUsername.trim().length < 1 ||
                            newUsername.trim() == "_" ||
                            props.users.includes(newUsername.trim())
                        }
                        onClick={function _a() {
                            if (
                                newUsername.trim().length > 0 &&
                                newUsername.trim() != "_" &&
                                !props.users.includes(newUsername.trim())
                            ) {
                                props.setUsername(newUsername.trim())
                                props.setUsers([...props.users, newUsername])
                                const p = backendDispatch(
                                    `/v1/p13n/user/${newUsername}`,
                                    "GET",
                                    undefined,
                                    undefined,
                                )
                                p.then((action: any) => {
                                    if (action.status == 200) {
                                        const data = action.data
                                        props.setPersonalizationsOfUser(data)
                                    } else {
                                        props.openMessageBox(
                                            MessageBoxType.Error,
                                            <>
                                                {intl.formatMessage({
                                                    id: "p13n_load_error",
                                                })}
                                            </>,
                                            "",
                                        )
                                    }
                                })
                                props.setApplication("_")
                                props.setLayout(FCLLayout.TwoColumnsMidExpanded)
                                props.setDialogOpen(false)
                                setNewUsername("")
                            }
                        }}
                    >
                        {intl.formatMessage({ id: "common_add" })}
                    </Button>
                </Bar>
            }
            headerText="Add User"
            open={props.dialogOpen}
        >
            <Form className={classes.form} layout="S1 M1 L1 XL1" labelSpan="S1 M1 L1 XL1">
                <FormItem
                    labelContent={
                        <Label required>{intl.formatMessage({ id: "p13n_new_user_name" })}</Label>
                    }
                >
                    <Input
                        value={newUsername}
                        required
                        valueState={
                            props.users.includes(newUsername.trim()) || newUsername.trim() == "_"
                                ? "Negative"
                                : "None"
                        }
                        valueStateMessage={
                            <span>
                                {intl.formatMessage({
                                    id:
                                        newUsername.trim() == "_"
                                            ? "p13n_new_user_name_invalid"
                                            : "p13n_new_user_name_existent",
                                })}
                            </span>
                        }
                        onInput={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            setNewUsername(e.target.value)
                        }}
                        onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            setNewUsername(e.target.attributes.getNamedItem("value")!.nodeValue!)
                        }}
                    />
                </FormItem>
            </Form>
        </Dialog>
    )
}
