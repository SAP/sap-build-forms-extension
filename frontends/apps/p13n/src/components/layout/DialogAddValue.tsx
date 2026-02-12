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
import { useIntl } from "react-intl"
import { Value } from "../../features/personalizationDefinitions"

interface DialogAddValueProps {
    dialogOpen: boolean
    id: string | undefined
    values: Value[]
    setDialogOpen(o: boolean): void
    setValues(values: Value[]): void
    openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string): void
}

export default function (props: DialogAddValueProps) {
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
    const [newKey, setKey] = useState<string>("")
    const [inputState, setInputState] = useState<{ [key: string]: string }>({})

    const keyNecessary = props.values
        .find((value) => value.locale == "_")
        ?.values.every(
            (item) => item.substring(item.indexOf("(") + 1, item.lastIndexOf(")")).length > 0,
        )

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
                                setKey("")
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
                            (keyNecessary &&
                                (newKey.trim().length < 1 ||
                                    props.values
                                        .filter((value) => value.locale == "_")
                                        .map((e) => e.values)
                                        .flat()
                                        .map((s) =>
                                            s.substring(s.indexOf("(") + 1, s.indexOf(")")).trim(),
                                        )
                                        .includes(newKey.trim()) ||
                                    newKey.includes("(") ||
                                    newKey.includes(")"))) ||
                            Object.keys(inputState).length <
                                props.values
                                    .map((e) => e.locale)
                                    .filter(
                                        (value, index, self) =>
                                            self.indexOf(value) === index &&
                                            value != "_" &&
                                            value != undefined,
                                    ).length ||
                            props.values
                                .filter((value) => value.locale == "_")
                                .map((e) => e.values)
                                .flat()
                                .map((s) =>
                                    s.indexOf("(") > 0
                                        ? s.substring(0, s.indexOf("(")).trim()
                                        : s.trim(),
                                )
                                .includes(newValue.trim()) ||
                            newValue.includes("(") ||
                            newValue.includes(")") ||
                            Object.values(inputState).some(
                                (value) => value.includes(")") || value.includes("("),
                            )
                        }
                        onClick={function _a() {
                            var isExistent = false
                            for (const [key, value] of Object.entries(inputState)) {
                                isExistent = props.values
                                    .filter((value) => value.locale == key)
                                    .map((e) => e.values)
                                    .flat()
                                    .map((s) =>
                                        s.indexOf("(") > 0
                                            ? s.substring(0, s.indexOf("(")).trim()
                                            : s.trim(),
                                    )
                                    .includes(value)
                                if (isExistent == true) {
                                    break
                                }
                            }

                            if (
                                newValue.trim().length > 0 &&
                                Object.keys(inputState).length ==
                                    props.values
                                        .map((e) => e.locale)
                                        .filter(
                                            (value, index, self) =>
                                                self.indexOf(value) === index &&
                                                value != "_" &&
                                                value != undefined,
                                        ).length &&
                                !props.values
                                    .filter((value) => value.locale == "_")
                                    .map((e) => e.values)
                                    .flat()
                                    .map((s) =>
                                        s.indexOf("(") > 0
                                            ? s.substring(0, s.indexOf("(")).trim()
                                            : s.trim(),
                                    )
                                    .includes(newValue.trim()) &&
                                !newValue.includes("(") &&
                                !newValue.includes(")") &&
                                props.id &&
                                !(
                                    keyNecessary &&
                                    (newKey.trim().length < 1 ||
                                        props.values
                                            .filter((value) => value.locale == "_")
                                            .map((e) => e.values)
                                            .flat()
                                            .map((s) =>
                                                s
                                                    .substring(s.indexOf("(") + 1, s.indexOf(")"))
                                                    .trim(),
                                            )
                                            .includes(newKey.trim()) ||
                                        newKey.includes("(") ||
                                        newKey.includes(")"))
                                ) &&
                                !isExistent &&
                                !Object.values(inputState).some(
                                    (value) => value.includes(")") || value.includes("("),
                                )
                            ) {
                                props.setValues(
                                    props.values.length > 0
                                        ? props.values.map((val: Value) => ({
                                              ...val,
                                              values: [
                                                  ...val.values,
                                                  (val.locale == "_"
                                                      ? newValue
                                                      : inputState[val.locale!]) +
                                                      (keyNecessary ? " (" + newKey + ")" : ""),
                                              ],
                                          }))
                                        : [
                                              {
                                                  id: props.id,
                                                  locale: "_",
                                                  values: [
                                                      newValue +
                                                          (newKey.trim().length > 0
                                                              ? " (" + newKey.trim() + ")"
                                                              : ""),
                                                  ],
                                              },
                                          ],
                                )

                                props.setDialogOpen(false)
                                setValue("")
                                setKey("")
                                setInputState({})
                            }
                        }}
                    >
                        {intl.formatMessage({ id: "common_add" })}
                    </Button>
                </Bar>
            }
            headerText={intl.formatMessage({ id: "p13n_button_new_value" })}
            open={props.dialogOpen}
        >
            <Form className={classes.form} layout="S1 M1 L1 XL1" labelSpan="S1 M1 L1 XL1">
                {(keyNecessary || props.values.length == 0) && (
                    <FormItem
                        labelContent={
                            <Label required={props.values.length > 0 ? true : false}>
                                {intl.formatMessage({ id: "p13n_new_setting_key" })}
                            </Label>
                        }
                    >
                        <Input
                            value={newKey}
                            required={props.values.length > 0 ? true : false}
                            valueState={
                                props.values
                                    .filter((value) => value.locale == "_")
                                    .map((e) => e.values)
                                    .flat()
                                    .map((s) =>
                                        s.substring(s.indexOf("(") + 1, s.indexOf(")")).trim(),
                                    )
                                    .includes(newKey.trim()) ||
                                newKey.includes("(") ||
                                newKey.includes(")")
                                    ? "Negative"
                                    : "None"
                            }
                            valueStateMessage={
                                <span>
                                    {intl.formatMessage({
                                        id:
                                            newKey.includes("(") || newKey.includes(")")
                                                ? "p13n_new_value_invalid"
                                                : "p13n_new_setting_key_existent",
                                    })}
                                </span>
                            }
                            onInput={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                setKey(e.target.value.trim())
                            }}
                            onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                setKey(e.target.value.trim())
                            }}
                        />
                    </FormItem>
                )}
                <FormItem
                    labelContent={
                        <Label required>
                            {intl.formatMessage({ id: "p13n_new_setting_value" })}
                        </Label>
                    }
                >
                    <Input
                        value={newValue}
                        required
                        valueState={
                            props.values
                                .filter((value) => value.locale == "_")
                                .map((e) => e.values)
                                .flat()
                                .map((s) =>
                                    s.indexOf("(") > 0
                                        ? s.substring(0, s.indexOf("(")).trim()
                                        : s.trim(),
                                )
                                .includes(newValue.trim()) ||
                            newValue.includes("(") ||
                            newValue.includes(")")
                                ? "Negative"
                                : "None"
                        }
                        valueStateMessage={
                            <span>
                                {intl.formatMessage({
                                    id:
                                        newValue.includes("(") || newValue.includes(")")
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
                {props.values
                    .map((e) => e.locale)
                    .filter(
                        (value, index, self) =>
                            self.indexOf(value) === index && value != "_" && value != undefined,
                    )
                    .filter((value) => value != undefined)
                    .map((locale: string) => {
                        return (
                            <FormItem key={locale} labelContent={<Label required>{locale}</Label>}>
                                <Input
                                    required
                                    value={inputState[locale] || ""}
                                    valueState={
                                        props.values
                                            .filter((value) => value.locale == locale)
                                            .map((e) => e.values)
                                            .flat()
                                            .map((s) => s.substring(0, s.indexOf("(")).trim())
                                            .includes(inputState[locale]) ||
                                        (inputState[locale] &&
                                            (inputState[locale].includes("(") ||
                                                inputState[locale].includes(")")))
                                            ? "Negative"
                                            : "None"
                                    }
                                    valueStateMessage={
                                        <span>
                                            {intl.formatMessage({
                                                id:
                                                    inputState[locale] &&
                                                    (inputState[locale].includes("(") ||
                                                        inputState[locale].includes(")"))
                                                        ? "p13n_new_value_invalid"
                                                        : "p13n_new_value_existent",
                                            })}
                                        </span>
                                    }
                                    onInput={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                        setInputState({
                                            ...inputState,
                                            [locale]: e.target.value.trim(),
                                        })
                                    }}
                                    onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                        setInputState({
                                            ...inputState,
                                            [locale]: e.target.value.trim(),
                                        })
                                    }}
                                />
                            </FormItem>
                        )
                    })}
            </Form>
        </Dialog>
    )
}
