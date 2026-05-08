import { JSX, useEffect, useRef, useState } from "react"

import { useIntl } from "react-intl"

import {
    Bar,
    Button,
    CheckBox,
    DynamicPage,
    DynamicPageHeader,
    DynamicPageTitle,
    FilterBar,
    FilterGroupItem,
    FlexBox,
    FlexibleColumnLayout,
    Input,
    InputDomRef,
    Label,
    List,
    ListDomRef,
    MessageBox,
    MessageBoxType,
    MultiComboBox,
    MultiComboBoxItem,
    TabContainer,
    Text,
    Title,
    Ui5CustomEvent,
    Switch,
    ListItemStandard,
    Toolbar,
    ToolbarButton,
    MessageBoxAction,
    Tab,
} from "@ui5/webcomponents-react"
import { ThemingParameters } from "@ui5/webcomponents-react-base"
import { ListItemClickEventDetail } from "@ui5/webcomponents/dist/List"
import FCLLayout from "@ui5/webcomponents-fiori/dist/types/FCLLayout"
import ButtonDesign from "@ui5/webcomponents/dist/types/ButtonDesign"
import ListSelectionMode from "@ui5/webcomponents/dist/types/ListSelectionMode"

import { apiOk, Margin, MessageOption, Severity, useMessages } from "commons"

import { ValueHelpDef, ValueHelpValue } from "../features/model"
import DialogAddValueHelpValue from "./valuehelp/DialogAddValueHelpValue"
import DialogAddValueHelpDefinition from "./valuehelp/DialogAddValueHelpDefinition"
import CurrentValuesTab from "./valuehelp/CurrentValuesTab"
import DialogUploadFile from "./valuehelp/DialogUploadFile"
import { useValueHelpState } from "../features/valuehelpstate"
import { useDisplayState } from "../features/displaystate"
import ValueHelpDefinitionForm from "./valuehelp/ValueHelpDefinitionForm"

export default function () {
    const intl = useIntl()
    const messages = useMessages()
    const valueHelpState = useValueHelpState()
    const displayState = useDisplayState()

    const [valueHelpValue, setValueHelpValue] = useState<ValueHelpValue | undefined>(undefined)
    const [updatedValueHelpValues, setUpdatedValueHelpValues] = useState<ValueHelpValue[]>([])

    const [messageBoxOpen, setMessageBoxOpen] = useState(false)
    const [messageBoxId, setMessageBoxId] = useState("")
    const [messageBoxType, setMessageBoxType] = useState<MessageBoxType>()
    const [messageBoxText, setMessageBoxText] = useState<JSX.Element>(<></>)

    const [dialogAddDefOpen, setDialogAddDefOpen] = useState(false)
    const [isDefIdExistent, setIsDefIdExistent] = useState(false)
    const [dialogAddValueOpen, setDialogAddValueOpen] = useState(false)

    const [dialogUploadFileOpen, setDialogUploadFileOpen] = useState(false)
    const [uploadLoading, setUploadLoading] = useState(false)

    const refSelectedValueHelpDef = useRef<ValueHelpDef | undefined>(undefined)

    /**
     * Initial data loading and event listener setup.
     */
    useEffect(() => {
        // load definitions
        refresh()
        // async load adpaters
        valueHelpState.findAdapters(messages)
        // async load languages
        valueHelpState.findLanguages(messages)
    }, [])

    /**
     *  Refreshes the ValueHelp definitions based on optional request parameters.
     *
     * @param requestParams
     */
    function refresh(requestParams?: object) {
        valueHelpState.clearDefs()
        valueHelpState.findDefs(messages, requestParams).then((action: any) => {
            if (apiOk(action.status)) {
                messages.toast(Severity.Success, "msg_valuehelpdefs_loaded")
            }
        })
    }

    /**
     * Filters the ValueHelp definitions based on search criteria.
     */
    function filter() {
        filterVH(displayState.searchId, displayState.searchAdapter)
    }

    /**
     * Filters the ValueHelp definitions based on search criteria.
     *
     * @param s
     * @param sAdapter
     */
    function filterVH(s: string | undefined, sAdapter: string[] | undefined) {
        if (s == undefined) {
            s = displayState.searchId
        }
        if (sAdapter == undefined) {
            sAdapter = displayState.searchAdapter
        }
        if ((s != "" && s != undefined) || sAdapter.length > 0) {
            if (s != "" && s != undefined && sAdapter.length > 0) {
                refresh({ search: s, adapter: sAdapter })
            } else if (s != "" && s != undefined) {
                refresh({ search: s })
            } else {
                refresh({ adapter: sAdapter.join(",") })
            }
        } else {
            refresh(undefined)
        }
        if (displayState.listMode == ListSelectionMode.Multiple) {
            displayState.setSelectedValueHelpDefs(
                displayState.selectedValueHelpDefs.filter((id) =>
                    valueHelpState.defs.some((v: ValueHelpDef) => v.id === id),
                ),
            )
        }
    }

    /**
     *  Handles language changes for the ValueHelp definition.
     *
     * @param v
     */
    function changeLanguages(v: ValueHelpDef) {
        if (v.languages.length == 0) {
            if (
                displayState.language != "_" ||
                valueHelpValue?.locale != "_" ||
                valueHelpValue.id != v.id
            ) {
                changeLanguage("_", v)
            }
        } else {
            if (
                displayState.language != v.languages[0] ||
                valueHelpValue?.locale != v.languages[0] ||
                valueHelpValue.id != v.id
            ) {
                changeLanguage(v.languages[0], v)
            }
        }
    }

    /**
     *  Handles changing the current language and fetching corresponding ValueHelp values.
     *
     * @param language
     * @param def
     */
    function changeLanguage(language: string, def: ValueHelpDef) {
        console.log(language)
        displayState.setLanguage(language)

        // we only show value of local valuehelps
        if (def.adapter != "local") {
            setValueHelpValue(undefined)
            return
        }

        var existingValueHelpValue = updatedValueHelpValues.find(
            (valueHelp: ValueHelpValue) =>
                valueHelp.id === valueHelpValue!.id && valueHelp.locale === language,
        )
        if (existingValueHelpValue) {
            setValueHelpValue(existingValueHelpValue)
        } else {
            valueHelpState
                .findLatestValues(messages, def.id, language)
                .then((action: any) => {
                    if (apiOk(action.status)) {
                        setValueHelpValue(action.data)
                    } else if (action.status == 404) {
                        setValueHelpValue({
                            id: def.id,
                            version: -1,
                            locale: language,
                            values: [],
                        })
                    }
                })
                .catch(() => {
                    setValueHelpValue
                })
        }
    }

    // function getAdapter() {
    //     const p = backendDispatch(`/v1/valuehelpdefs/adapter`, "GET", undefined, undefined)
    //     p.then((action: any) => {
    //         if (action.status == 200) {
    //             const data: string[] = action.data
    //             setAdapter(data)
    //         } else {
    //             setAdapter([])
    //         }
    //     })
    // }

    /**
     * Adds a new ValueHelp definition.
     *
     * @param def - The ValueHelp definition to add.
     * @returns A promise that resolves when the definition is added.
     * @param def
     */
    function addValueHelpDef(def: ValueHelpDef) {
        valueHelpState.addDef(messages, def).then((action: any) => {
            if (apiOk(action.status)) {
                console.log(`ValueHelp definition ${def?.id} has been created successfully`)
                if (displayState.listMode == ListSelectionMode.Multiple) {
                    displayState.setSelectedValueHelpDefs([
                        ...displayState.selectedValueHelpDefs,
                        def.id,
                    ])
                }
                openValueHelpDef(action.data)
                filter()
                messages.toast(Severity.Success, "valuehelpdef_created", { id: def.id })
            } else if (action.status == 409) {
                console.log("Definition ID is already existent.")
                messages.dialog(Severity.Error, "err_valuehelpdef_id_existent", { id: def.id }, [
                    MessageOption.Ok,
                ])
            }
        })
    }

    /**
     * Deletes a ValueHelp definition.
     */
    function deleteValueHelpDef(def: ValueHelpDef) {
        valueHelpState.deleteDef(messages, def).then((action: any) => {
            if (apiOk(action.status)) {
                console.log(
                    `ValueHelp definition ${refSelectedValueHelpDef.current?.id} has been deleted successfully`,
                )
                filter()
                toListView()
                messages.toast(Severity.Success, "msg_valuehelpdef_deleted", { id: def.id })
            }
        })
    }

    /**
     * Deletes the selected ValueHelp definitions.
     *
     */
    function deleteSelectedValueHelps() {
        valueHelpState
            .deleteDefs(messages, displayState.selectedValueHelpDefs)
            .then((action: any) => {
                if (action.status == 204) {
                    console.log(`Selected value help definitions have been deleted successfully`)
                    if (
                        displayState.selectedValueHelpDefs.includes(
                            refSelectedValueHelpDef.current?.id!,
                        )
                    ) {
                        toListView()
                    }
                    filter()
                    // getAdapter()
                    messages.toast(Severity.Success, "msg_valuehelpdefs_deleted")
                    displayState.setSelectedValueHelpDefs([])
                }
            })
    }

    /**
     * Updates the current ValueHelp definition.
     */
    function updateCurrentValueHelp() {
        valueHelpState.updateDef(messages, refSelectedValueHelpDef.current!).then((action: any) => {
            if (apiOk(action.status)) {
                console.log(
                    `ValueHelp definition ${refSelectedValueHelpDef.current?.id} has been updated successfully`,
                )
                if (displayState.listMode == ListSelectionMode.Multiple) {
                    displayState.setSelectedValueHelpDefs([
                        ...displayState.selectedValueHelpDefs,
                        refSelectedValueHelpDef.current!.id,
                    ])
                }
                openValueHelpDef(action.data)
                filter()
                messages.toast(Severity.Success, "msg_valuehelpdef_updated", {
                    id: refSelectedValueHelpDef.current!.id,
                })
            } else if (action.status == 409) {
                console.log("Definition ID is already existent.")
                messages.dialog(
                    Severity.Error,
                    "err_valuehelpdef_id_existent",
                    { id: refSelectedValueHelpDef.current!.id },
                    [MessageOption.Ok],
                )
            }
        })
    }

    // function updateValueHelpValues() {
    //     updatedValueHelpValues!.map((c) => {
    //         if (
    //             c.version == -1 &&
    //             Object.keys(c.values).length > 0 &&
    //             (currentValueHelpDef?.languages.includes(c.locale) || c.locale == "_")
    //         ) {
    //             const p = backendDispatch(
    //                 `/v1/valuehelpvalues/${encodeURIComponent(c?.id)}/${encodeURIComponent(
    //                     c.locale,
    //                 )}`,
    //                 "POST",
    //                 { ...c, version: 0 },
    //                 undefined,
    //             )
    //             p.then((action: any) => {
    //                 if (action.status == 201) {
    //                     const data = action.data
    //                     setValueHelpValue(data)
    //                 } else {
    //                     console.log(`Error while creating valueHelpValue ${c?.id}`)
    //                     openMessageBox(
    //                         MessageBoxType.Error,
    //                         <p>ValueHelp value could not be created. Please try again.</p>,
    //                         "",
    //                     )
    //                 }
    //             })
    //         } else if (c.version >= 0) {
    //             if (
    //                 Object.keys(c.values).length > 0 &&
    //                 (currentValueHelpDef?.languages.includes(c.locale) || c.locale == "_")
    //             ) {
    //                 const p = backendDispatch(
    //                     `/v1/valuehelpvalues/${encodeURIComponent(c?.id)}/${encodeURIComponent(
    //                         c.locale,
    //                     )}`,
    //                     "PUT",
    //                     c,
    //                     undefined,
    //                 )
    //                 p.then((action: any) => {
    //                     if (action.status == 200) {
    //                         const data = action.data
    //                         setValueHelpValue(data)
    //                     } else if (action.status == 409) {
    //                         console.log(`Error while updating valueHelpValue ${c?.id}`)
    //                         openMessageBox(
    //                             MessageBoxType.Error,
    //                             <p>ValueHelp value could not be updated. Please try again.</p>,
    //                             "",
    //                         )
    //                     } else {
    //                         console.log(`Error while updating valueHelpValue ${c?.id}`)
    //                         openMessageBox(
    //                             MessageBoxType.Error,
    //                             <p>
    //                                 ValueHelp value could not be updated, becuase it has not got the
    //                                 most recent version. Please refresh and try again.
    //                             </p>,
    //                             "",
    //                         )
    //                     }
    //                 })
    //             } else {
    //                 const p = backendDispatch(
    //                     `/v1/valuehelpvalues/${encodeURIComponent(c?.id)}/${encodeURIComponent(
    //                         c.locale,
    //                     )}`,
    //                     "DELETE",
    //                     undefined,
    //                     undefined,
    //                 )
    //                 p.then((action: any) => {
    //                     if (action.status == 204) {
    //                         setValueHelpValue(undefined)
    //                     } else {
    //                         console.log(`Error while deleting valueHelpValue ${c?.id}`)
    //                         openMessageBox(
    //                             MessageBoxType.Error,
    //                             <p>ValueHelp value could not be deleted. Please try again.</p>,
    //                             "",
    //                         )
    //                     }
    //                 })
    //             }
    //         }
    //     })
    //     setUpdatedValueHelpValues([])
    // }

    /**
     *  Handles changes to the ValueHelp values.
     *
     * @param changedValueHelpValue
     */
    function changeValueHelpValue(changedValueHelpValue: ValueHelpValue) {
        setValueHelpValue(changedValueHelpValue)
        var existing = updatedValueHelpValues.find((obj) => {
            return obj.id == changedValueHelpValue.id && obj.locale == changedValueHelpValue.locale
        })
        if (existing) {
            setUpdatedValueHelpValues(
                updatedValueHelpValues!.map((c) => {
                    if (
                        c.id == changedValueHelpValue.id &&
                        c.locale == changedValueHelpValue.locale
                    ) {
                        return changedValueHelpValue
                    } else {
                        return c
                    }
                }),
            )
        } else {
            setUpdatedValueHelpValues([...updatedValueHelpValues, changedValueHelpValue])
        }
    }

    /**
     * Downloads the ValueHelp definitions as an XML file.
     */
    function download(): void {
        let requestParams = {}
        if (displayState.listMode == ListSelectionMode.Multiple) {
            if (
                valueHelpState.defs.every(
                    (objekt: ValueHelpDef) =>
                        displayState.selectedValueHelpDefs.includes(objekt.id) ||
                        displayState.selectedValueHelpDefs.length < 1,
                )
            ) {
                //download all displayed
                requestParams = {
                    search: displayState.searchId,
                    adapter: displayState.searchAdapter,
                }
            } else {
                //download selected
                requestParams = { ids: displayState.selectedValueHelpDefs }
            }
        } else {
            requestParams = { search: displayState.searchId, adapter: displayState.searchAdapter }
        }
        valueHelpState.findDefExport(messages, requestParams).then((action: any) => {
            if (apiOk(action.status)) {
                const url = window.URL.createObjectURL(
                    new Blob([action.data], { type: "application/xml" }),
                )
                const a = document.createElement("a")
                a.href = url
                a.download = "valueHelpDefinitions.xml"
                document.body.appendChild(a)
                a.click()
                window.URL.revokeObjectURL(url)
                messages.toast(Severity.Success, "msg_valuehelpdefs_exported")
            }
        })
    }

    /**
     *  Uploads a ValueHelp definitions XML file.
     *
     * @param file
     * @param override
     * @param useTechnicalName
     */
    function upload(file: File, override: boolean, useTechnicalName: boolean): void {
        // setUploadLoading(true)
        // const formData = new FormData()
        // formData.append("file", file)
        // const p = backendDispatch("/v1/valuehelpdefs/import", "POST", formData, {
        //     override: override,
        //     useTechnicalName: useTechnicalName,
        // })
        // p.then((action: any) => {
        //     setUploadLoading(false)
        //     setDialogUploadFileOpen(false)
        //     if (action.status == 200) {
        //         toListView()
        //         if (action.data) {
        //             openMessageBox(
        //                 MessageBoxType.Warning,
        //                 <p style={{ whiteSpace: "pre-line" }}>{action.data}</p>,
        //                 "",
        //             )
        //         } else {
        //             openMessageBox(MessageBoxType.Success, <p>Upload successful!</p>, "")
        //         }
        //         setCurrentValueHelpDef(undefined)
        //         setLanguage(undefined)
        //         setValueHelpValue(undefined)
        //         filter()
        //     } else {
        //         openMessageBox(
        //             MessageBoxType.Error,
        //             <p>Upload failed. Please check file and try again</p>,
        //             "",
        //         )
        //     }
        // })
    }

    /**
     * Opens the ValueHelp definition in the editor.
     *
     * @param v
     */
    function openValueHelpDef(v: ValueHelpDef | undefined) {
        if (v) {
            refSelectedValueHelpDef.current = v
            setUpdatedValueHelpValues([])
            changeLanguages(v)
            displayState.setLayout(FCLLayout.TwoColumnsMidExpanded)
            displayState.setFullscreen(false)
        }
    }

    /**
     * Opens a message box with the given parameters.
     *
     * @param mBoxType
     * @param mBoxText
     * @param mBoxId
     */
    function openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string) {
        setMessageBoxType(mBoxType)
        setMessageBoxId(mBoxId)
        setMessageBoxText(mBoxText)
        setMessageBoxOpen(true)
    }

    /**
     *  Switches the layout to the list view.
     */
    function toListView() {
        displayState.setLayout(FCLLayout.OneColumn)
        refSelectedValueHelpDef.current = undefined
        setValueHelpValue(undefined)
        displayState.setLanguage(undefined)
        displayState.setFullscreen(false)
    }

    return (
        <div style={{ height: "100vh" }}>
            <Bar>
                <Title level="H1" style={{ fontSize: ThemingParameters.sapFontHeader3Size }}>
                    {intl.formatMessage({ id: "app_title" })}
                </Title>
            </Bar>
            <FlexibleColumnLayout
                layout={displayState.layout}
                style={{ paddingTop: "1em", height: "calc(100% - 44px - 1em)" }}
                startColumn={
                    <>
                        <FilterBar
                            onClear={() => {
                                displayState.setSearchId("")
                                displayState.setSearchAdapter([])
                            }}
                            onGo={() => {
                                filter()
                            }}
                            search={
                                <Input
                                    value={displayState.searchId}
                                    onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                        displayState.setSearchId(
                                            e.target.attributes.getNamedItem("value")!.nodeValue!,
                                        )
                                    }}
                                    onKeyDown={(e) => {
                                        if (e.key === "Enter") {
                                            displayState.setSearchId(
                                                // @ts-expect-error
                                                e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                            )
                                            filterVH(
                                                // @ts-expect-error
                                                e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                                undefined,
                                            )
                                        }
                                    }}
                                />
                            }
                            showResetButton
                            hideFilterConfiguration
                            showClearOnFB
                            showGoOnFB
                        >
                            <FilterGroupItem label="Adapter" filterKey="1">
                                <MultiComboBox
                                    onSelectionChange={(e) => {
                                        displayState.setSearchAdapter(
                                            e.detail.items.map((a) => a.id),
                                        )
                                    }}
                                >
                                    {valueHelpState.adapters.map((item: string) => (
                                        <MultiComboBoxItem
                                            text={item}
                                            key={item}
                                            id={item}
                                            selected={displayState.searchAdapter.includes(item)}
                                        />
                                    ))}
                                </MultiComboBox>
                            </FilterGroupItem>
                        </FilterBar>

                        <FlexBox direction="Row" justifyContent="SpaceBetween">
                            <FlexBox alignItems="Center">
                                {displayState.listMode == ListSelectionMode.Multiple && (
                                    <CheckBox
                                        text={"Select all"}
                                        onChange={function () {
                                            if (
                                                valueHelpState.defs.every((def: ValueHelpDef) =>
                                                    displayState.selectedValueHelpDefs.includes(
                                                        def.id,
                                                    ),
                                                )
                                            ) {
                                                displayState.setSelectedValueHelpDefs([])
                                            } else {
                                                valueHelpState.defs.map((e: ValueHelpDef) => {
                                                    if (
                                                        !displayState.selectedValueHelpDefs.includes(
                                                            e.id,
                                                        )
                                                    ) {
                                                        displayState.setSelectedValueHelpDefs(
                                                            valueHelpState.defs.map(
                                                                (v: ValueHelpDef) => v.id,
                                                            ),
                                                        )
                                                    }
                                                })
                                            }
                                        }}
                                        checked={valueHelpState.defs.every((objekt: ValueHelpDef) =>
                                            displayState.selectedValueHelpDefs.includes(objekt.id),
                                        )}
                                    />
                                )}
                                <Switch
                                    onChange={function () {
                                        if (displayState.listMode == ListSelectionMode.Single) {
                                            displayState.setListMode(ListSelectionMode.Multiple)
                                            if (refSelectedValueHelpDef.current) {
                                                displayState.setSelectedValueHelpDefs([
                                                    refSelectedValueHelpDef.current.id,
                                                ])
                                            } else {
                                                displayState.setSelectedValueHelpDefs([])
                                            }
                                        } else {
                                            displayState.setListMode(ListSelectionMode.Single)
                                            displayState.setSelectedValueHelpDefs([])
                                        }
                                    }}
                                    checked={displayState.listMode == ListSelectionMode.Multiple}
                                    style={{ marginLeft: Margin.SMALL }}
                                />
                                <Text style={{ marginLeft: Margin.SMALL }}>Multiselect</Text>
                            </FlexBox>

                            <FlexBox alignItems="Center" wrap="Wrap">
                                <Button
                                    design="Transparent"
                                    icon="upload"
                                    onClick={() => {
                                        setDialogUploadFileOpen(true)
                                    }}
                                >
                                    Upload File
                                </Button>
                                <Button
                                    design="Transparent"
                                    icon="download"
                                    style={{ marginLeft: Margin.SMALL }}
                                    disabled={
                                        displayState.listMode == ListSelectionMode.Multiple &&
                                        displayState.selectedValueHelpDefs.length < 1
                                    }
                                    onClick={download}
                                >
                                    {displayState.listMode == ListSelectionMode.Single
                                        ? "Download"
                                        : "Download selected"}
                                </Button>
                                <Button
                                    design="Transparent"
                                    icon="add"
                                    onClick={() => {
                                        setDialogAddDefOpen(true)
                                    }}
                                >
                                    New Value Help
                                </Button>
                                {displayState.listMode == ListSelectionMode.Multiple && (
                                    <Button
                                        design="Transparent"
                                        icon="delete"
                                        style={{ marginLeft: Margin.SMALL }}
                                        disabled={displayState.selectedValueHelpDefs.length < 1}
                                        onClick={() => {
                                            openMessageBox(
                                                MessageBoxType.Confirm,
                                                displayState.selectedValueHelpDefs.length > 5 ? (
                                                    <p>
                                                        Delete{" "}
                                                        <i>
                                                            <b>
                                                                {
                                                                    displayState
                                                                        .selectedValueHelpDefs
                                                                        .length
                                                                }
                                                            </b>
                                                        </i>{" "}
                                                        selected ValueHelp definitions?
                                                    </p>
                                                ) : (
                                                    <p>
                                                        Delete ValueHelp definitions{" "}
                                                        <i>
                                                            <b>
                                                                {displayState.selectedValueHelpDefs.join(
                                                                    ", ",
                                                                )}
                                                            </b>
                                                        </i>{" "}
                                                        ?
                                                    </p>
                                                ),
                                                "DeleteSelectedValueHelpDefs",
                                            )
                                        }}
                                    >
                                        Delete selected
                                    </Button>
                                )}
                            </FlexBox>
                        </FlexBox>

                        <List
                            headerText="Value Helps"
                            selectionMode={displayState.listMode}
                            id="valueHelpList"
                            onItemClick={(
                                e: Ui5CustomEvent<ListDomRef, ListItemClickEventDetail>,
                            ) => {
                                if (displayState.listMode == ListSelectionMode.Multiple) {
                                    if (
                                        displayState.selectedValueHelpDefs.includes(
                                            e.detail.item.id,
                                        )
                                    ) {
                                        //remove
                                        displayState.setSelectedValueHelpDefs(
                                            displayState.selectedValueHelpDefs.filter(
                                                (a) => a !== e.detail.item.id,
                                            ),
                                        )
                                    } else {
                                        //add
                                        displayState.setSelectedValueHelpDefs([
                                            ...displayState.selectedValueHelpDefs,
                                            e.detail.item.id,
                                        ])
                                    }
                                }

                                openValueHelpDef(
                                    valueHelpState.defs.find(
                                        (valueHelp: ValueHelpDef) =>
                                            valueHelp.id === e.detail.item.id,
                                    ),
                                )
                            }}
                        >
                            {valueHelpState.defs.map((item: ValueHelpDef) => (
                                <ListItemStandard
                                    description={
                                        item.description
                                            ? item.adapter
                                                ? item.description + " | " + item.adapter
                                                : item.description
                                            : item.adapter
                                    }
                                    key={item.id}
                                    id={item.id}
                                    icon={"navigation-right-arrow"}
                                    iconEnd={true}
                                    navigated={refSelectedValueHelpDef.current?.id == item.id}
                                    selected={displayState.selectedValueHelpDefs.includes(item.id)}
                                >
                                    {item.id}
                                </ListItemStandard>
                            ))}
                        </List>
                    </>
                }
                midColumn={
                    <DynamicPage
                        headerArea={
                            <DynamicPageHeader>
                                <FlexBox wrap="Wrap">
                                    <FlexBox direction="Column">
                                        <>
                                            <FlexBox style={{ paddingBlock: 2 }}>
                                                <Label>Description:</Label>
                                                <Text
                                                    style={{
                                                        marginLeft: "2px",
                                                        wordBreak: "break-all",
                                                    }}
                                                >
                                                    {refSelectedValueHelpDef.current?.description}
                                                </Text>
                                            </FlexBox>
                                            <FlexBox style={{ paddingBlock: 2 }}>
                                                <Label>TTL:</Label>
                                                {refSelectedValueHelpDef.current?.ttl == -1 && (
                                                    <Text
                                                        style={{
                                                            marginLeft: "2px",
                                                            wordBreak: "break-all",
                                                        }}
                                                    >
                                                        static
                                                    </Text>
                                                )}
                                                {refSelectedValueHelpDef.current?.ttl == 0 && (
                                                    <Text
                                                        style={{
                                                            marginLeft: "2px",
                                                            wordBreak: "break-all",
                                                        }}
                                                    >
                                                        refresh
                                                    </Text>
                                                )}
                                                {refSelectedValueHelpDef.current &&
                                                    refSelectedValueHelpDef.current?.ttl > 0 && (
                                                        <Text
                                                            style={{
                                                                marginLeft: "2px",
                                                                wordBreak: "break-all",
                                                            }}
                                                        >
                                                            {refSelectedValueHelpDef.current?.ttl}{" "}
                                                            min
                                                        </Text>
                                                    )}
                                            </FlexBox>
                                            <FlexBox style={{ paddingBlock: 2 }}>
                                                <Label>Adapter:</Label>
                                                <Text
                                                    style={{
                                                        marginLeft: "2px",
                                                        wordBreak: "break-all",
                                                    }}
                                                >
                                                    {refSelectedValueHelpDef.current?.adapter}
                                                </Text>
                                            </FlexBox>
                                        </>
                                    </FlexBox>
                                </FlexBox>
                            </DynamicPageHeader>
                        }
                        titleArea={
                            <DynamicPageTitle
                                actionsBar={
                                    <Toolbar design="Transparent">
                                        <ToolbarButton
                                            design="Transparent"
                                            onClick={() => {
                                                displayState.setEdit(!displayState.edit)
                                            }}
                                            text={intl.formatMessage({
                                                id: displayState.edit
                                                    ? "common_show"
                                                    : "common_edit",
                                            })}
                                        />
                                        <ToolbarButton
                                            design="Transparent"
                                            onClick={() => {
                                                openMessageBox(
                                                    MessageBoxType.Confirm,
                                                    <div>
                                                        Delete value help definition{" "}
                                                        <b>
                                                            <i>
                                                                {
                                                                    refSelectedValueHelpDef.current
                                                                        ?.id
                                                                }
                                                            </i>
                                                        </b>
                                                        ?
                                                    </div>,
                                                    "DeleteValueHelpDef",
                                                )
                                            }}
                                            text={intl.formatMessage({ id: "common_delete" })}
                                        />
                                        <ToolbarButton
                                            icon="save"
                                            onClick={updateCurrentValueHelp}
                                            design="Emphasized"
                                            text={intl.formatMessage({ id: "common_save" })}
                                        />
                                    </Toolbar>
                                }
                                heading={<Title>{refSelectedValueHelpDef.current?.id}</Title>}
                                snappedHeading={
                                    <Title>{refSelectedValueHelpDef.current?.id}</Title>
                                }
                                navigationBar={
                                    <Toolbar design="Transparent">
                                        <ToolbarButton
                                            icon={
                                                displayState.fullscreen
                                                    ? "exit-full-screen"
                                                    : "full-screen"
                                            }
                                            design={ButtonDesign.Transparent}
                                            onClick={() => {
                                                displayState.setLayout(
                                                    displayState.fullscreen
                                                        ? FCLLayout.TwoColumnsMidExpanded
                                                        : FCLLayout.MidColumnFullScreen,
                                                )
                                                displayState.setFullscreen(!displayState.fullscreen)
                                            }}
                                        />
                                        <ToolbarButton
                                            icon="decline"
                                            design={ButtonDesign.Transparent}
                                            onClick={() => {
                                                toListView()
                                            }}
                                        />
                                    </Toolbar>
                                }
                            />
                        }
                    >
                        <TabContainer
                            contentBackgroundDesign="Solid"
                            headerBackgroundDesign="Solid"
                            style={{ width: "100%" }}
                            tabLayout="Standard"
                        >
                            <Tab icon="settings" selected text="Config">
                                <ValueHelpDefinitionForm
                                    edit={displayState.edit}
                                    availableLanguages={valueHelpState.languages}
                                    refValueHelpDef={refSelectedValueHelpDef}
                                    changeLanguages={changeLanguages}
                                />
                            </Tab>
                            {/* {refSelectedValueHelpDef.current?.adapter === "local" && (
                                <CurrentValuesTab
                                    edit={displayState.edit}
                                    currentValueHelpDef={refSelectedValueHelpDef}
                                    valueHelpValue={valueHelpValue}
                                    language={displayState.language}
                                    changeValueHelpValue={changeValueHelpValue}
                                    changeLanguage={changeLanguage}
                                    setDialogAddValueOpen={setDialogAddValueOpen}
                                />
                            )} */}
                        </TabContainer>
                    </DynamicPage>
                }
            />
            <MessageBox
                onClose={(action, escPressed) => {
                    if (
                        messageBoxType === MessageBoxType.Confirm &&
                        action === MessageBoxAction.OK
                    ) {
                        if (messageBoxId == "DeleteValueHelpDef") {
                            console.log("delete value help " + refSelectedValueHelpDef.current?.id)
                            deleteValueHelpDef(refSelectedValueHelpDef.current!)
                        } else if (messageBoxId == "DeleteSelectedValueHelpDefs") {
                            deleteSelectedValueHelps()
                        }
                    }
                    setMessageBoxOpen(false)
                }}
                type={messageBoxType}
                open={messageBoxOpen}
            >
                {messageBoxText}
            </MessageBox>
            <DialogAddValueHelpDefinition
                dialogAddDefOpen={dialogAddDefOpen}
                setDialogAddDefOpen={setDialogAddDefOpen}
                addValueHelpDef={addValueHelpDef}
                isIdExistent={isDefIdExistent}
                setIsIdExistent={setIsDefIdExistent}
                availableLanguages={valueHelpState.languages}
            />
            <DialogAddValueHelpValue
                dialogAddValueOpen={dialogAddValueOpen}
                valueHelpValue={valueHelpValue!}
                setDialogAddValueOpen={setDialogAddValueOpen}
                changeValueHelpValue={changeValueHelpValue}
            />
            <DialogUploadFile
                dialogUploadFileOpen={dialogUploadFileOpen}
                setDialogUploadFileOpen={setDialogUploadFileOpen}
                upload={upload}
                loading={uploadLoading}
            />
        </div>
    )
}
