import {
    Bar,
    Button,
    Dialog,
    Form,
    FormItem,
    Input,
    InputDomRef,
    Label,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import { useState } from "react"
import { useIntl } from "react-intl"
import { createUseStyles } from "react-jss"
import { Scenario } from "../../utils/scenarioDefinitions"
import useElementsStore from "../../state/elements"

interface DialogAddLanguageProps {
    dialogAddLanguageOpen: boolean
    treeItemsShown: Scenario | null | undefined
    languages: string[]
    language: string | undefined
    version: number
    scenarioMixinName: string
    update: number
    setDialogAddLanguageOpen(o: boolean): void
    setLanguages(l: string[]): void
    setLanguage(l: string): void
    setUpdate: (e: any) => void
}

export default function (props: DialogAddLanguageProps) {
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
    const editTexts = useElementsStore((state) => state.editTexts)
    const [newLanguageName, setNewLanguageName] = useState<string>("")
    const [isLocale, setIsLocale] = useState(true)

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
                                props.setDialogAddLanguageOpen(false)
                                setNewLanguageName("")
                                setIsLocale(true)
                            }}
                        >
                            {intl.formatMessage({ id: "add_language_dialog_button_close" })}
                        </Button>
                    }
                >
                    <Button
                        design="Emphasized"
                        className={classes.button}
                        onClick={function _a() {
                            try {
                                var v = new Intl.Locale(newLanguageName)
                                if (v == undefined) {
                                    setIsLocale(false)
                                }
                            } catch (e) {
                                setIsLocale(false)
                            }

                            if (isLocale) {
                                if (
                                    newLanguageName.trim().length > 0 &&
                                    !props.languages.includes(newLanguageName.trim()) &&
                                    new Intl.Locale(newLanguageName) != undefined
                                ) {
                                    var texts: any = JSON.parse(
                                        JSON.stringify(props.treeItemsShown?.texts!),
                                    )

                                    var textsOfDefaultLanguage: { [key: string]: string }
                                    if (props.treeItemsShown?.defaultLanguage) {
                                        textsOfDefaultLanguage = {
                                            ...texts[props.treeItemsShown?.defaultLanguage],
                                        }
                                    } else {
                                        textsOfDefaultLanguage = {
                                            ...texts[Object.keys(texts).sort()[0]],
                                        }
                                    }

                                    Object.keys(textsOfDefaultLanguage).forEach((key) => {
                                        textsOfDefaultLanguage[key] = ""
                                    })

                                    texts[newLanguageName] = textsOfDefaultLanguage

                                    editTexts({
                                        version: props.version,
                                        texts: texts,
                                        scenarioMixinName: props.scenarioMixinName,
                                    })
                                    props.setLanguage(newLanguageName)
                                    props.setUpdate(props.update + 1)
                                    setNewLanguageName("")
                                    setIsLocale(true)
                                    props.setDialogAddLanguageOpen(false)
                                }
                            }
                        }}
                    >
                        {intl.formatMessage({ id: "add_language_dialog_button_add" })}
                    </Button>
                </Bar>
            }
            headerText={intl.formatMessage({ id: "add_language_dialog_title" })}
            open={props.dialogAddLanguageOpen}
            onClose={() => {
                props.setDialogAddLanguageOpen(false)
            }}
        >
            <Form className={classes.form} layout="S1 M1 L1 XL1" labelSpan="S1 M1 L1 XL1">
                <FormItem labelContent={<Label required>{intl.formatMessage({ id: "add_language_dialog_label_name" })}</Label>}>
                    <Input
                        value={newLanguageName}
                        required
                        valueState={
                            props.languages.includes(newLanguageName.trim()) || !isLocale
                                ? "Negative"
                                : "None"
                        }
                        valueStateMessage={
                            !isLocale ? (
                                <span>{intl.formatMessage({ id: "add_language_dialog_invalid_locale" })}</span>
                            ) : (
                                <span>{intl.formatMessage({ id: "add_language_dialog_already_exists" })}</span>
                            )
                        }
                        onInput={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            setNewLanguageName(e.target.value!)
                            setIsLocale(true)
                        }}
                        onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            setNewLanguageName(
                                e.target.attributes.getNamedItem("value")!.nodeValue!,
                            )
                        }}
                    />
                </FormItem>
            </Form>
        </Dialog>
    )
}
