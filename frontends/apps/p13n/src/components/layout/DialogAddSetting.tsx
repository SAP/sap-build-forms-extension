import {
    Bar,
    Button,
    CheckBox,
    Dialog,
    Form,
    FormItem,
    Input,
    InputDomRef,
    Label,
    MessageBoxType,
    Option,
    Select,
    TextArea,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import { JSX, useState } from "react"
import { createUseStyles } from "react-jss"
import { Personalization } from "../../features/personalizationDefinitions"
import { useIntl } from "react-intl"
import { backendDispatch } from "../../utils/backend"

interface DialogAddSettingProps {
    dialogOpen: boolean
    username: String | null
    application: string
    personalizationsOfUser: Personalization[]
    setDialogOpen(o: boolean): void
    setPersonalizationsOfUser(personalizations: Personalization[]): void
    openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string): void
}

export default function (props: DialogAddSettingProps) {
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
    const [settingKey, setSettingKey] = useState("")
    const [encoding, setEncoding] = useState("t")
    const [value, setValue] = useState("")
    const [editable, setEditable] = useState(true)
    const [visible, setVisible] = useState(true)

    function resetValues() {
        setSettingKey("")
        setEncoding("t")
        setValue("")
        setVisible(true)
        setEditable(true)
    }

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
                                resetValues()
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
                            settingKey.trim().length < 1 ||
                            settingKey.trim().startsWith("_") ||
                            value.trim().length < 1 ||
                            props.personalizationsOfUser.some((p) => p.key === settingKey.trim())
                        }
                        onClick={function _a() {
                            if (
                                settingKey.trim().length > 0 &&
                                !settingKey.trim().startsWith("_") &&
                                value.trim().length > 0 &&
                                !props.personalizationsOfUser.some(
                                    (p) => p.key === settingKey.trim(),
                                )
                            ) {
                                const p = backendDispatch(
                                    `/v1/p13n`,
                                    "POST",
                                    {
                                        id: null,
                                        user: props.username,
                                        app: props.application,
                                        key: settingKey,
                                        encoding: encoding,
                                        value: value,
                                        editable: editable,
                                        visible: visible,
                                    },
                                    undefined,
                                )
                                p.then((action: any) => {
                                    if (action.status == 201) {
                                        props.setPersonalizationsOfUser([
                                            ...props.personalizationsOfUser,
                                            action.data,
                                        ])
                                        props.openMessageBox(
                                            MessageBoxType.Success,
                                            <>
                                                {intl.formatMessage({
                                                    id: "p13n_create_success",
                                                })}
                                            </>,
                                            "",
                                        )
                                    } else {
                                        props.openMessageBox(
                                            MessageBoxType.Error,
                                            <>
                                                {intl.formatMessage({
                                                    id: "p13n_create_error",
                                                })}
                                            </>,
                                            "",
                                        )
                                    }
                                })
                                props.setDialogOpen(false)
                                resetValues()
                            }
                        }}
                    >
                        {intl.formatMessage({ id: "common_add" })}
                    </Button>
                </Bar>
            }
            headerText={intl.formatMessage({ id: "p13n_button_add_setting" })}
            open={props.dialogOpen}
        >
            <Form className={classes.form} layout="S1 M1 L1 XL1" labelSpan="S1 M1 L1 XL1">
                <FormItem
                    labelContent={
                        <Label required>{intl.formatMessage({ id: "p13n_new_setting_key" })}</Label>
                    }
                >
                    <Input
                        value={settingKey}
                        required
                        valueState={
                            settingKey.trim().startsWith("_") ||
                            props.personalizationsOfUser.some((p) => p.key === settingKey.trim())
                                ? "Negative"
                                : "None"
                        }
                        valueStateMessage={
                            <span>
                                {intl.formatMessage(
                                    settingKey.trim().startsWith("_")
                                        ? {
                                              id: "p13n_new_setting_key_invalid",
                                          }
                                        : {
                                              id: "p13n_new_setting_key_existent",
                                          },
                                )}
                            </span>
                        }
                        onInput={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            setSettingKey(e.target.value)
                        }}
                        onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            setSettingKey(e.target.attributes.getNamedItem("value")!.nodeValue!)
                        }}
                    />
                </FormItem>
                <FormItem
                    labelContent={
                        <Label>{intl.formatMessage({ id: "p13n_new_setting_encoding" })}</Label>
                    }
                >
                    <Select
                        onChange={(e) => {
                            setEncoding(e.detail.selectedOption.id)
                        }}
                        valueState="None"
                    >
                        <Option key={"t"} id={"t"} selected={encoding == "t"}>
                            {intl.formatMessage({ id: "p13n_new_setting_encoding_plain" })}
                        </Option>
                        <Option key={"j"} id={"j"} selected={encoding == "j"}>
                            {intl.formatMessage({ id: "p13n_new_setting_encoding_json" })}
                        </Option>
                        <Option key={"b"} id={"b"} selected={encoding == "b"}>
                            {intl.formatMessage({ id: "p13n_new_setting_encoding_binary" })}
                        </Option>
                    </Select>
                </FormItem>
                <FormItem
                    labelContent={
                        <Label required>
                            {intl.formatMessage({ id: "p13n_new_setting_value" })}
                        </Label>
                    }
                >
                    <TextArea
                        style={{
                            height: value.length > 50 ? 150 : 50,
                        }}
                        onInput={(e) => {
                            setValue(e.target.value)
                        }}
                        valueState="None"
                        value={value}
                    />
                </FormItem>
                <FormItem
                    labelContent={<Label>{intl.formatMessage({ id: "form_visible" })}</Label>}
                >
                    <CheckBox
                        onChange={(e) => {
                            setVisible(e.target.checked)
                        }}
                        valueState="None"
                        checked={visible}
                    />
                </FormItem>
                <FormItem
                    labelContent={<Label>{intl.formatMessage({ id: "form_editable" })}</Label>}
                >
                    <CheckBox
                        onChange={(e) => {
                            setEditable(e.target.checked)
                        }}
                        valueState="None"
                        checked={editable}
                    />
                </FormItem>
            </Form>
        </Dialog>
    )
}
