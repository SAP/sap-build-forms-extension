import { useEffect, useState } from "react"

import { useIntl } from "react-intl"

import { Bar, FlexibleColumnLayout, Title } from "@ui5/webcomponents-react"
import { ThemingParameters } from "@ui5/webcomponents-react-base"
import FCLLayout from "@ui5/webcomponents-fiori/dist/types/FCLLayout"
import ListSelectionMode from "@ui5/webcomponents/dist/types/ListSelectionMode"

import { apiOk, MessageOption, Severity, useMessages } from "commons"

import { ValueHelpDef, ValueHelpValue } from "../features/model"
import { useValueHelpState } from "../features/valuehelpstate"
import ValueHelpListColumn from "./valuehelp/ValueHelpListColumn"
import ValueHelpDetailColumn from "./valuehelp/ValueHelpDetailColumn"
import DialogAddValueHelpDefinition from "./valuehelp/DialogAddValueHelpDefinition"
import DialogAddValueHelpValue from "./valuehelp/DialogAddValueHelpValue"
import DialogUploadFile from "./valuehelp/DialogUploadFile"

export default function () {
    const intl = useIntl()
    const messages = useMessages()
    const state = useValueHelpState()

    const [listMode, setListMode] = useState<ListSelectionMode>(ListSelectionMode.Single)
    const [selectedValueHelpDefs, setSelectedValueHelpDefs] = useState<string[]>([])

    const [currentValueHelpDef, setCurrentValueHelpDef] = useState<ValueHelpDef | undefined>()
    const [valueHelpValue, setValueHelpValue] = useState<ValueHelpValue | undefined>(undefined)
    const [updatedValueHelpValues, setUpdatedValueHelpValues] = useState<ValueHelpValue[]>([])

    const [searchId, setSearchId] = useState<string>("")
    const [searchAdapter, setSearchAdapter] = useState<string[]>([])

    const [language, setLanguage] = useState<string>()

    const [layout, setLayout] = useState(FCLLayout.OneColumn)
    const [fullscreen, setFullscreen] = useState<boolean>(false)
    const [edit, setEdit] = useState<boolean>(false)

    const [dialogAddDefOpen, setDialogAddDefOpen] = useState(false)
    const [isDefIdExistent, setIsDefIdExistent] = useState(false)
    const [dialogAddValueOpen, setDialogAddValueOpen] = useState(false)

    const [dialogUploadFileOpen, setDialogUploadFileOpen] = useState(false)
    const [uploadLoading] = useState(false)

    /**
     * Initial data loading and event listener setup.
     */
    useEffect(() => {
        // load definitions
        refresh()
        // async load adapters
        state.findAdapters(messages)
        // async load languages
        state.findLanguages(messages)
    }, [])

    /**
     *  Refreshes the ValueHelp definitions based on optional request parameters.
     *
     * @param requestParams
     */
    function refresh(requestParams?: object) {
        state.clearDefs()
        state.findDefs(messages, requestParams).then((action: any) => {
            if (apiOk(action.status)) {
                messages.toast(Severity.Success, "msg_valuehelpdefs_loaded")
            }
        })
    }

    /**
     * Filters the ValueHelp definitions based on search criteria.
     */
    function filter() {
        filterVH(searchId, searchAdapter)
    }

    /**
     * Filters the ValueHelp definitions based on search criteria.
     *
     * @param s
     * @param sAdapter
     */
    function filterVH(s: string | undefined, sAdapter: string[] | undefined) {
        const effectiveS = s ?? searchId
        const effectiveAdapter = sAdapter ?? searchAdapter
        if (effectiveS || effectiveAdapter.length > 0) {
            if (effectiveS && effectiveAdapter.length > 0) {
                refresh({ search: effectiveS, adapter: effectiveAdapter })
            } else if (effectiveS) {
                refresh({ search: effectiveS })
            } else {
                refresh({ adapter: effectiveAdapter.join(",") })
            }
        } else {
            refresh(undefined)
        }
        if (listMode === ListSelectionMode.Multiple) {
            setSelectedValueHelpDefs(
                selectedValueHelpDefs.filter((id) =>
                    state.defs.some((v: ValueHelpDef) => v.id === id),
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
        if (v.languages.length === 0) {
            if (language !== "_" || valueHelpValue?.locale !== "_" || valueHelpValue.id !== v.id) {
                changeLanguage("_", v)
            }
        } else {
            if (
                language !== v.languages[0] ||
                valueHelpValue?.locale !== v.languages[0] ||
                valueHelpValue.id !== v.id
            ) {
                changeLanguage(v.languages[0], v)
            }
        }
    }

    /**
     *  Handles changing the current language and fetching corresponding ValueHelp values.
     *
     * @param lang
     * @param def
     */
    function changeLanguage(lang: string, def: ValueHelpDef) {
        setLanguage(lang)

        // we only show values of local valuehelps
        if (def.adapter !== "local") {
            setValueHelpValue(undefined)
            return
        }

        const existingValueHelpValue = updatedValueHelpValues.find(
            (vh: ValueHelpValue) => vh.id === def.id && vh.locale === lang,
        )
        if (existingValueHelpValue) {
            setValueHelpValue(existingValueHelpValue)
        } else {
            state
                .findLatestValues(messages, def.id, lang)
                .then((action: any) => {
                    if (apiOk(action.status)) {
                        setValueHelpValue(action.data)
                    } else if (action.status === 404) {
                        setValueHelpValue({
                            id: def.id,
                            version: -1,
                            locale: lang,
                            values: [],
                        })
                    }
                })
                .catch(() => {})
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
     */
    function addValueHelpDef(def: ValueHelpDef) {
        state.addDef(messages, def).then((action: any) => {
            if (apiOk(action.status)) {
                console.log(`ValueHelp definition ${def?.id} has been created successfully`)
                if (listMode === ListSelectionMode.Multiple) {
                    setSelectedValueHelpDefs([...selectedValueHelpDefs, def.id])
                }
                openValueHelpDef(action.data)
                filter()
                messages.toast(Severity.Success, "msg_valuehelpdef_created", { id: def.id })
            } else if (action.status === 409) {
                console.log("Definition ID is already existent.")
                messages.dialog(Severity.Error, "err_valuehelpdef_id_existent", { id: def.id }, [
                    MessageOption.Ok,
                ])
            }
        })
    }

    /**
     * Deletes a ValueHelp definition after confirmation.
     *
     * @param def
     */
    async function deleteValueHelpDef(def: ValueHelpDef) {
        const result = await messages.dialog(
            Severity.Warning,
            "confirm_delete_vh",
            { id: def.id },
            [MessageOption.Ok, MessageOption.Cancel],
        )
        if (result !== MessageOption.Ok) return

        state.deleteDef(messages, def).then((action: any) => {
            if (apiOk(action.status)) {
                console.log(
                    `ValueHelp definition ${currentValueHelpDef?.id} has been deleted successfully`,
                )
                filter()
                toListView()
                messages.toast(Severity.Success, "msg_valuehelpdef_deleted", { id: def.id })
            }
        })
    }

    /**
     * Deletes the selected ValueHelp definitions after confirmation.
     */
    async function deleteSelectedValueHelps() {
        const result = await messages.dialog(
            Severity.Warning,
            "confirm_delete_selected_vh",
            { count: selectedValueHelpDefs.length },
            [MessageOption.Ok, MessageOption.Cancel],
        )
        if (result !== MessageOption.Ok) return

        state.deleteDefs(messages, selectedValueHelpDefs).then((action: any) => {
            if (action.status === 204) {
                console.log(`Selected value help definitions have been deleted successfully`)
                if (selectedValueHelpDefs.includes(currentValueHelpDef?.id!)) {
                    toListView()
                }
                filter()
                messages.toast(Severity.Success, "msg_valuehelpdefs_deleted")
                setSelectedValueHelpDefs([])
            }
        })
    }

    /**
     * Updates the current ValueHelp definition.
     */
    function updateCurrentValueHelp() {
        state.updateDef(messages, currentValueHelpDef!).then((action: any) => {
            if (apiOk(action.status)) {
                console.log(
                    `ValueHelp definition ${currentValueHelpDef?.id} has been updated successfully`,
                )
                if (listMode === ListSelectionMode.Multiple) {
                    setSelectedValueHelpDefs([...selectedValueHelpDefs, currentValueHelpDef!.id])
                }
                openValueHelpDef(action.data)
                filter()
                messages.toast(Severity.Success, "msg_valuehelpdef_updated", {
                    id: currentValueHelpDef!.id,
                })
            } else if (action.status === 409) {
                console.log("Definition ID is already existent.")
                messages.dialog(
                    Severity.Error,
                    "err_valuehelpdef_id_existent",
                    { id: currentValueHelpDef!.id },
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
        const existing = updatedValueHelpValues.find(
            (obj) =>
                obj.id === changedValueHelpValue.id && obj.locale === changedValueHelpValue.locale,
        )
        if (existing) {
            setUpdatedValueHelpValues(
                updatedValueHelpValues.map((c) =>
                    c.id === changedValueHelpValue.id && c.locale === changedValueHelpValue.locale
                        ? changedValueHelpValue
                        : c,
                ),
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
        if (listMode === ListSelectionMode.Multiple) {
            if (
                state.defs.every(
                    (objekt: ValueHelpDef) =>
                        selectedValueHelpDefs.includes(objekt.id) ||
                        selectedValueHelpDefs.length < 1,
                )
            ) {
                // download all displayed
                requestParams = { search: searchId, adapter: searchAdapter }
            } else {
                // download selected
                requestParams = { ids: selectedValueHelpDefs }
            }
        } else {
            requestParams = { search: searchId, adapter: searchAdapter }
        }
        state.findDefExport(messages, requestParams).then((action: any) => {
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
            setCurrentValueHelpDef(v)
            setUpdatedValueHelpValues([])
            changeLanguages(v)
            setLayout(FCLLayout.TwoColumnsMidExpanded)
            setFullscreen(false)
        }
    }

    /**
     *  Switches the layout to the list view.
     */
    function toListView() {
        setLayout(FCLLayout.OneColumn)
        setCurrentValueHelpDef(undefined)
        setValueHelpValue(undefined)
        setLanguage(undefined)
        setFullscreen(false)
    }

    function handleToggleListMode() {
        if (listMode === ListSelectionMode.Single) {
            setListMode(ListSelectionMode.Multiple)
            if (currentValueHelpDef) {
                setSelectedValueHelpDefs([currentValueHelpDef.id])
            } else {
                setSelectedValueHelpDefs([])
            }
        } else {
            setListMode(ListSelectionMode.Single)
            setSelectedValueHelpDefs([])
        }
    }

    function handleSelectAll() {
        if (state.defs.every((def: ValueHelpDef) => selectedValueHelpDefs.includes(def.id))) {
            setSelectedValueHelpDefs([])
        } else {
            setSelectedValueHelpDefs(state.defs.map((v: ValueHelpDef) => v.id))
        }
    }

    function handleToggleSelectedDef(id: string) {
        if (selectedValueHelpDefs.includes(id)) {
            setSelectedValueHelpDefs(selectedValueHelpDefs.filter((a) => a !== id))
        } else {
            setSelectedValueHelpDefs([...selectedValueHelpDefs, id])
        }
    }

    return (
        <div style={{ height: "100vh" }}>
            <Bar>
                <Title level="H1" style={{ fontSize: ThemingParameters.sapFontHeader3Size }}>
                    {intl.formatMessage({ id: "app_title" })}
                </Title>
            </Bar>
            <FlexibleColumnLayout
                layout={layout}
                style={{ paddingTop: "1em", height: "calc(100% - 44px - 1em)" }}
                startColumn={
                    <ValueHelpListColumn
                        defs={state.defs}
                        adapters={state.adapters}
                        searchId={searchId}
                        searchAdapter={searchAdapter}
                        listMode={listMode}
                        selectedDefs={selectedValueHelpDefs}
                        currentDefId={currentValueHelpDef?.id}
                        onSearchIdChange={setSearchId}
                        onSearchAdapterChange={setSearchAdapter}
                        onFilter={filter}
                        onClearFilter={() => {
                            setSearchId("")
                            setSearchAdapter([])
                            filterVH("", [])
                        }}
                        onSelectItem={openValueHelpDef}
                        onToggleListMode={handleToggleListMode}
                        onToggleSelectedDef={handleToggleSelectedDef}
                        onSelectAll={handleSelectAll}
                        onAdd={() => setDialogAddDefOpen(true)}
                        onDeleteSelected={deleteSelectedValueHelps}
                        onDownload={download}
                        onUpload={() => setDialogUploadFileOpen(true)}
                    />
                }
                midColumn={
                    <ValueHelpDetailColumn
                        currentValueHelpDef={currentValueHelpDef}
                        valueHelpValue={valueHelpValue}
                        language={language}
                        availableLanguages={state.languages}
                        edit={edit}
                        fullscreen={fullscreen}
                        onEdit={() => setEdit(!edit)}
                        onSave={updateCurrentValueHelp}
                        onDelete={() =>
                            currentValueHelpDef && deleteValueHelpDef(currentValueHelpDef)
                        }
                        onClose={toListView}
                        onFullscreen={() => {
                            setLayout(
                                fullscreen
                                    ? FCLLayout.TwoColumnsMidExpanded
                                    : FCLLayout.MidColumnFullScreen,
                            )
                            setFullscreen(!fullscreen)
                        }}
                        onChangeLanguage={changeLanguage}
                        onChangeValueHelpValue={changeValueHelpValue}
                        onSetDialogAddValueOpen={setDialogAddValueOpen}
                        onChangeLanguages={changeLanguages}
                        onSetCurrentValueHelpDef={setCurrentValueHelpDef}
                    />
                }
            />
            <DialogAddValueHelpDefinition
                dialogAddDefOpen={dialogAddDefOpen}
                setDialogAddDefOpen={setDialogAddDefOpen}
                addValueHelpDef={addValueHelpDef}
                isIdExistent={isDefIdExistent}
                setIsIdExistent={setIsDefIdExistent}
                availableLanguages={state.languages}
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
                upload={() => {}}
                loading={uploadLoading}
            />
        </div>
    )
}
