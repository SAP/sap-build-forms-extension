import {
    Button,
    FlexBox,
    Form,
    FormItem,
    Icon,
    Input,
    Label,
    MessageBoxType,
    Option,
    Select,
    Table,
    TableCell,
    TableHeaderCell,
    TableHeaderRow,
    TableRow,
} from "@ui5/webcomponents-react"
import { useEffect, useState } from "react"
import { useIntl } from "react-intl"
import { Scenario } from "../../utils/scenarioDefinitions"
import TextEdit from "./TextEdit"
import useElementsStore from "../../state/elements"
import { Severity, useMessages } from "commons"

interface Props {
    defaultLanguage: string | undefined
    treeItemsShown: Scenario | null
    update: number
    language: string
    version: number
    scenarioMixinName: string
    setUpdate: (e: any) => void
    setDialogAddLanguageOpen: (o: boolean) => void
    setLanguage: (e: any) => void
    openMessageBox: (e1: any, e2: any, e3: any, onConfirm?: () => void) => void
}

export default function LanguagesTab(props: Props) {
    const editBaseData = useElementsStore((state) => state.editBaseData)
    const editTexts = useElementsStore((state) => state.editTexts)
    const [searchValue, setSearchValue] = useState<string>("")
    const { toast } = useMessages()
    const intl = useIntl()

    useEffect(() => {
        if (props.defaultLanguage) {
            props.setLanguage(props.defaultLanguage)
        } else {
            props.setLanguage(Object.keys(props.treeItemsShown?.texts!).sort()[0])
        }
    }, [props.defaultLanguage])

    return (
        <>
            <Form
                layout="S1 M2 L2 XL2"
                labelSpan="S12 M12 L12 XL12"
                style={{ paddingBottom: "1rem" }}
            >
                <FormItem labelContent={<Label>{intl.formatMessage({ id: "languages_tab_label_management" })}</Label>}>
                    <FlexBox direction="Row" style={{ gap: "0.5rem", alignItems: "center", flexWrap: "wrap" }}>
                        <Select
                            onChange={function Ta(e) {
                                props.setLanguage(e.detail.selectedOption.textContent!.toString())
                            }}
                            style={{ minWidth: "150px" }}
                        >
                            {props.treeItemsShown?.defaultLanguage != undefined &&
                                !(
                                    Object.keys(props.treeItemsShown?.texts!) as Array<string>
                                ).includes(props.treeItemsShown.defaultLanguage) && (
                                    <Option
                                        selected={true}
                                        key={props.treeItemsShown.defaultLanguage}
                                    >
                                        {props.treeItemsShown.defaultLanguage}
                                    </Option>
                                )}
                            {Object.keys(props.treeItemsShown?.texts!)
                                .sort()
                                .map((key) => {
                                    return (
                                        <Option
                                            selected={
                                                key == props.language ||
                                                (key == "en" &&
                                                    !Object.keys(
                                                        props.treeItemsShown?.texts!,
                                                    ).includes(props.language))
                                            }
                                            key={key}
                                        >
                                            {key}
                                        </Option>
                                    )
                                })}
                        </Select>

                        <Button
                            design="Transparent"
                            disabled={Object.keys(props.treeItemsShown?.texts!).length < 2}
                            onClick={() => {
                                props.openMessageBox(
                                    MessageBoxType.Confirm,
                                    intl.formatMessage({ id: "languages_tab_confirm_remove_title" }),
                                    <>
                                        {intl.formatMessage(
                                            { id: "languages_tab_confirm_remove_text" },
                                            { language: <b><i>{props.language}</i></b> },
                                        )}
                                    </>,
                                    () => {
                                        var texts: any = JSON.parse(
                                            JSON.stringify(props.treeItemsShown?.texts!),
                                        )

                                        if (Object.keys(texts).length > 1) {
                                            var newLanguage
                                            if (props.defaultLanguage) {
                                                if (props.language == props.defaultLanguage) {
                                                    let i = 0
                                                    while (true) {
                                                        if (
                                                            Object.keys(texts).sort()[i] !=
                                                            props.defaultLanguage
                                                        ) {
                                                            break
                                                        }
                                                        i = i + 1
                                                    }
                                                    newLanguage = Object.keys(texts).sort()[i]
                                                    editBaseData({
                                                        scenarioMixinName: props.scenarioMixinName,
                                                        version: props.version,
                                                        defaultLanguage: newLanguage,
                                                    })
                                                } else {
                                                    newLanguage = props.defaultLanguage
                                                }
                                            } else {
                                                let i = 0
                                                while (true) {
                                                    if (Object.keys(texts).sort()[i] != props.language) {
                                                        break
                                                    }
                                                    i = i + 1
                                                }
                                                newLanguage = Object.keys(texts).sort()[i]
                                            }

                                            delete texts[props.language]

                                            editTexts({
                                                version: props.treeItemsShown?.version,
                                                texts: texts,
                                                scenarioMixinName: props.scenarioMixinName,
                                            })
                                            props.setLanguage(newLanguage)
                                            props.setUpdate(props.update + 1)

                                            // Show toast notification
                                            toast(Severity.None, "element_deleted")
                                        }
                                    }
                                )
                            }}
                        >
                            {intl.formatMessage({ id: "languages_tab_button_remove_language" })}
                        </Button>

                        <Button
                            design="Transparent"
                            onClick={() => {
                                props.setDialogAddLanguageOpen(true)
                            }}
                        >
                            {intl.formatMessage({ id: "languages_tab_button_add_language" })}
                        </Button>
                    </FlexBox>
                </FormItem>

                <FormItem labelContent={<Label>{intl.formatMessage({ id: "languages_tab_label_search" })}</Label>}>
                    <Input
                        style={{ width: "100%" }}
                        icon={<Icon name="search" />}
                        onChange={function _s() { }}
                        onInput={function _s(e: any) {
                            setSearchValue(e.target.attributes.getNamedItem("value")!.nodeValue!)
                        }}
                        value={searchValue}
                        type="Text"
                        valueState="None"
                        showClearIcon={true}
                    />
                </FormItem>
            </Form>

            <Table
                headerRow={
                    <TableHeaderRow>
                        <TableHeaderCell width="20rem">
                            <span>{intl.formatMessage({ id: "languages_tab_col_key" })}</span>
                        </TableHeaderCell>
                        <TableHeaderCell>
                            <span>{intl.formatMessage({ id: "languages_tab_col_value" })}</span>
                        </TableHeaderCell>
                        <TableHeaderCell>
                            <span></span>
                        </TableHeaderCell>
                    </TableHeaderRow>
                }
            >
                {props.treeItemsShown &&
                    props?.treeItemsShown?.texts &&
                    props.language &&
                    props.treeItemsShown.texts![props.language as any] &&
                    (
                        Object.keys(props?.treeItemsShown?.texts![props.language as any])
                            .filter((v) => {
                                if (searchValue.trim().length > 0) {
                                    return v.toLowerCase().includes(searchValue.toLowerCase())
                                } else {
                                    return true
                                }
                            })
                            .sort() as Array<string>
                    ).map((key) => {
                        function setValue(newValue: any) {
                            var texts: any = JSON.parse(
                                JSON.stringify(props.treeItemsShown?.texts!),
                            )
                            var languageString: string = props.language!
                            texts[languageString][key] = newValue
                            editTexts({
                                version: props.treeItemsShown?.version,
                                texts: texts,
                                scenarioMixinName: props.scenarioMixinName,
                            })
                            props.setUpdate(props.update + 1)
                        }
                        return (
                            <TableRow key={key}>
                                <TableCell>
                                    <span>
                                        <b>{key}</b>
                                    </span>
                                </TableCell>
                                <TextEdit
                                    value={
                                        props?.treeItemsShown?.texts![props.language as any][
                                        key as any
                                        ]!
                                    }
                                    setValue={setValue}
                                />
                            </TableRow>
                        )
                    })}
            </Table>
        </>
    )
}
