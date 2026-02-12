import {
    Bar,
    Button,
    Dialog,
    Form,
    FormGroup,
    FormItem,
    Input,
    InputDomRef,
    Label,
    MessageBoxType,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import { JSX, useState } from "react"
import { createUseStyles } from "react-jss"
import { useIntl } from "react-intl"
import { Value } from "../../features/personalizationDefinitions"

interface DialogAddLocaleProps {
    dialogOpen: boolean
    id: string | undefined
    values: Value[]
    setDialogOpen(o: boolean): void
    setValues(values: Value[]): void
    setLocale(locale: string): void
    openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string): void
}

export default function (props: DialogAddLocaleProps) {
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
    const [newValue, setValue] = useState<string>("")
    const [inputState, setInputState] = useState<{ [key: string]: string }>({})

    if (
        (props.values.find((value) => value.locale == "_")?.values || []).length > 0 &&
        props.values
            .find((value) => value.locale == "_")
            ?.values.every(
                (item) => item.substring(item.indexOf("(") + 1, item.lastIndexOf(")")).length > 0,
            )
    ) {
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
                                    setValue("")
                                    setInputState({})
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
                                newValue.trim().length < 1 ||
                                Object.keys(inputState).length <
                                    props.values.find((value) => value.locale == "_")!.values
                                        .length ||
                                Object.values(inputState).some((value) => value.trim().length < 1)
                            }
                            onClick={function _a() {
                                if (
                                    newValue.trim().length > 0 &&
                                    Object.keys(inputState).length ==
                                        props.values.find((value) => value.locale == "_")!.values
                                            .length &&
                                    !Object.values(inputState).some(
                                        (value) => value.trim().length < 1,
                                    ) &&
                                    props.id
                                ) {
                                    var values: string[] = []
                                    for (const [key, value] of Object.entries(inputState)) {
                                        values.push(value + " (" + key + ")")
                                    }

                                    props.setValues([
                                        ...props.values,
                                        {
                                            locale: newValue,
                                            id: props.id,
                                            values: values,
                                        },
                                    ])

                                    props.setLocale(newValue)
                                    props.setDialogOpen(false)
                                    setValue("")
                                    setInputState({})
                                }
                            }}
                        >
                            {intl.formatMessage({ id: "common_add" })}
                        </Button>
                    </Bar>
                }
                headerText={intl.formatMessage({ id: "p13n_button_new_locale" })}
                open={props.dialogOpen}
            >
                <Form className={classes.form} layout="S1 M1 L1 XL1" labelSpan="S1 M1 L1 XL1">
                    <FormGroup
                        headerText={intl.formatMessage({
                            id: "p13n_form_group_locale",
                        })}
                    >
                        <FormItem
                            labelContent={
                                <Label required>
                                    {intl.formatMessage({ id: "p13n_new_locale" })}
                                </Label>
                            }
                        >
                            <Input
                                value={newValue}
                                required
                                valueState={
                                    props.values
                                        .filter((v) => {
                                            return v.locale != "_"
                                        })
                                        .map((a) => {
                                            return a.locale
                                        })
                                        .includes(newValue.trim()) || newValue.trim() == "_"
                                        ? "Negative"
                                        : "None"
                                }
                                valueStateMessage={
                                    <span>
                                        {intl.formatMessage({
                                            id:
                                                newValue.trim() == "_"
                                                    ? "p13n_new_value_invalid"
                                                    : "p13n_new_value_existent",
                                        })}
                                    </span>
                                }
                                onInput={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                    setValue(e.target.value.trim())
                                }}
                                onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                    setValue(e.target.value.trim())
                                }}
                            />
                        </FormItem>
                    </FormGroup>
                    <FormGroup
                        headerText={intl.formatMessage({
                            id: "p13n_form_group_values",
                        })}
                    >
                        {props.values
                            .find((value) => value.locale == "_")
                            ?.values.map((item: string) => {
                                var key = item.substring(
                                    item.indexOf("(") + 1,
                                    item.lastIndexOf(")"),
                                )
                                if (key != undefined && key != null && key.length > 0) {
                                    return (
                                        <FormItem
                                            key={key}
                                            labelContent={<Label required>{key}</Label>}
                                        >
                                            <Input
                                                required
                                                value={inputState[key] || ""}
                                                onInput={(
                                                    e: Ui5CustomEvent<InputDomRef, never>,
                                                ) => {
                                                    setInputState({
                                                        ...inputState,
                                                        [key]: e.target.value.trim(),
                                                    })
                                                }}
                                                onChange={(
                                                    e: Ui5CustomEvent<InputDomRef, never>,
                                                ) => {
                                                    setInputState({
                                                        ...inputState,
                                                        [key]: e.target.value.trim(),
                                                    })
                                                }}
                                            />
                                        </FormItem>
                                    )
                                } else {
                                    return <></>
                                }
                            })}
                    </FormGroup>
                </Form>
            </Dialog>
        )
    }
}
