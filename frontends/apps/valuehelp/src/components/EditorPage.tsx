import { useEffect } from "react"

import { useIntl } from "react-intl"

import { Bar, FlexibleColumnLayout, Title } from "@ui5/webcomponents-react"
import { ThemingParameters } from "@ui5/webcomponents-react-base"
import FCLLayout from "@ui5/webcomponents-fiori/dist/types/FCLLayout"
import ListSelectionMode from "@ui5/webcomponents/dist/types/ListSelectionMode"

import { apiOk, MessageOption, Severity, useMessages } from "commons"

import { ValueHelpDef, ValueHelpValue } from "../features/model"
import { useValueHelpState } from "../features/valuehelpstate"
import { useValueHelpUIState } from "../features/valuehelpUIState"
import ValueHelpListColumn from "./valuehelp/ValueHelpListColumn"
import ValueHelpDetailColumn from "./valuehelp/ValueHelpDetailColumn"
import DialogAddValueHelpDefinition from "./valuehelp/DialogAddValueHelpDefinition"
import DialogAddValueHelpValue from "./valuehelp/DialogAddValueHelpValue"
import DialogUploadFile from "./valuehelp/DialogUploadFile"

export default function () {
    const intl = useIntl()
    const messages = useMessages()
    const state = useValueHelpState()
    const ui = useValueHelpUIState()

    /**
     * Initial data loading.
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
        filterVH(ui.searchId, ui.searchAdapter)
    }

    /**
     * Filters the ValueHelp definitions based on search criteria.
     *
     * @param s
     * @param sAdapter
     */
    function filterVH(s: string | undefined, sAdapter: string[] | undefined) {
        const effectiveS = s ?? ui.searchId
        const effectiveAdapter = sAdapter ?? ui.searchAdapter
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
        if (ui.listMode === ListSelectionMode.Multiple) {
            ui.setSelectedDefs(
                ui.selectedDefs.filter((id) => state.defs.some((v: ValueHelpDef) => v.id === id)),
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
            if (
                ui.language !== "_" ||
                ui.valueHelpValue?.locale !== "_" ||
                ui.valueHelpValue.id !== v.id
            ) {
                changeLanguage("_", v)
            }
        } else {
            if (
                ui.language !== v.languages[0] ||
                ui.valueHelpValue?.locale !== v.languages[0] ||
                ui.valueHelpValue.id !== v.id
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
        ui.setLanguage(lang)

        // we only show values of local valuehelps
        if (def.adapter !== "local") {
            ui.setValueHelpValue(undefined)
            return
        }

        const existingValueHelpValue = ui.updatedValueHelpValues.find(
            (vh: ValueHelpValue) => vh.id === def.id && vh.locale === lang,
        )
        if (existingValueHelpValue) {
            ui.setValueHelpValue(existingValueHelpValue)
        } else {
            state
                .findLatestValues(messages, def.id, lang)
                .then((action: any) => {
                    if (apiOk(action.status)) {
                        ui.setValueHelpValue(action.data)
                    } else if (action.status === 404) {
                        ui.setValueHelpValue({
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

    /**
     * Adds a new ValueHelp definition.
     *
     * @param def - The ValueHelp definition to add.
     */
    function addValueHelpDef(def: ValueHelpDef) {
        state.addDef(messages, def).then((action: any) => {
            if (apiOk(action.status)) {
                console.log(`ValueHelp definition ${def?.id} has been created successfully`)
                if (ui.listMode === ListSelectionMode.Multiple) {
                    ui.setSelectedDefs([...ui.selectedDefs, def.id])
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
                    `ValueHelp definition ${ui.currentValueHelpDef?.id} has been deleted successfully`,
                )
                filter()
                ui.resetDetail()
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
            { count: ui.selectedDefs.length },
            [MessageOption.Ok, MessageOption.Cancel],
        )
        if (result !== MessageOption.Ok) return

        state.deleteDefs(messages, ui.selectedDefs).then((action: any) => {
            if (action.status === 204) {
                console.log(`Selected value help definitions have been deleted successfully`)
                if (ui.selectedDefs.includes(ui.currentValueHelpDef?.id!)) {
                    ui.resetDetail()
                }
                filter()
                messages.toast(Severity.Success, "msg_valuehelpdefs_deleted")
                ui.clearSelectedDefs()
            }
        })
    }

    /**
     * Updates the current ValueHelp definition.
     */
    function updateCurrentValueHelp() {
        state.updateDef(messages, ui.currentValueHelpDef!).then((action: any) => {
            if (apiOk(action.status)) {
                console.log(
                    `ValueHelp definition ${ui.currentValueHelpDef?.id} has been updated successfully`,
                )
                if (ui.listMode === ListSelectionMode.Multiple) {
                    ui.setSelectedDefs([...ui.selectedDefs, ui.currentValueHelpDef!.id])
                }
                openValueHelpDef(action.data)
                filter()
                messages.toast(Severity.Success, "msg_valuehelpdef_updated", {
                    id: ui.currentValueHelpDef!.id,
                })
            } else if (action.status === 409) {
                console.log("Definition ID is already existent.")
                messages.dialog(
                    Severity.Error,
                    "err_valuehelpdef_id_existent",
                    { id: ui.currentValueHelpDef!.id },
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
    //                     ui.setValueHelpValue(data)
    //                 } else {
    //                     console.log(`Error while creating valueHelpValue ${c?.id}`)
    //                     messages.dialog(Severity.Error, "err_valuehelpvalue_create", undefined, [MessageOption.Ok])
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
    //                         ui.setValueHelpValue(data)
    //                     } else if (action.status == 409) {
    //                         console.log(`Error while updating valueHelpValue ${c?.id}`)
    //                         messages.dialog(Severity.Error, "err_valuehelpvalue_update", undefined, [MessageOption.Ok])
    //                     } else {
    //                         console.log(`Error while updating valueHelpValue ${c?.id}`)
    //                         messages.dialog(Severity.Error, "err_valuehelpvalue_update_version", undefined, [MessageOption.Ok])
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
    //                         ui.setValueHelpValue(undefined)
    //                     } else {
    //                         console.log(`Error while deleting valueHelpValue ${c?.id}`)
    //                         messages.dialog(Severity.Error, "err_valuehelpvalue_delete", undefined, [MessageOption.Ok])
    //                     }
    //                 })
    //             }
    //         }
    //     })
    //     ui.setUpdatedValueHelpValues([])
    // }

    /**
     * Downloads the ValueHelp definitions as an XML file.
     */
    function download(): void {
        let requestParams = {}
        if (ui.listMode === ListSelectionMode.Multiple) {
            if (
                state.defs.every(
                    (objekt: ValueHelpDef) =>
                        ui.selectedDefs.includes(objekt.id) || ui.selectedDefs.length < 1,
                )
            ) {
                // download all displayed
                requestParams = { search: ui.searchId, adapter: ui.searchAdapter }
            } else {
                // download selected
                requestParams = { ids: ui.selectedDefs }
            }
        } else {
            requestParams = { search: ui.searchId, adapter: ui.searchAdapter }
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
    function upload(_file: File, _override: boolean, _useTechnicalName: boolean): void {
        // setUploadLoading(true)
        // const formData = new FormData()
        // formData.append("file", file)
        // const p = backendDispatch("/v1/valuehelpdefs/import", "POST", formData, {
        //     override: override,
        //     useTechnicalName: useTechnicalName,
        // })
        // p.then((action: any) => {
        //     ui.setUploadLoading(false)
        //     ui.setDialogUploadFileOpen(false)
        //     if (action.status == 200) {
        //         ui.resetDetail()
        //         filter()
        //         if (action.data) {
        //             messages.dialog(Severity.Warning, "msg_upload_warnings", undefined, [MessageOption.Ok])
        //         } else {
        //             messages.toast(Severity.Success, "msg_upload_success")
        //         }
        //     } else {
        //         messages.dialog(Severity.Error, "err_upload_failed", undefined, [MessageOption.Ok])
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
            ui.setCurrentValueHelpDef(v)
            ui.setUpdatedValueHelpValues([])
            changeLanguages(v)
            ui.openDetailLayout()
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
                layout={ui.layout}
                style={{ paddingTop: "1em", height: "calc(100% - 44px - 1em)" }}
                startColumn={
                    <ValueHelpListColumn
                        defs={state.defs}
                        adapters={state.adapters}
                        searchId={ui.searchId}
                        searchAdapter={ui.searchAdapter}
                        listMode={ui.listMode}
                        selectedDefs={ui.selectedDefs}
                        currentDefId={ui.currentValueHelpDef?.id}
                        onSearchIdChange={ui.setSearchId}
                        onSearchAdapterChange={ui.setSearchAdapter}
                        onFilter={filter}
                        onClearFilter={() => {
                            ui.setSearchId("")
                            ui.setSearchAdapter([])
                            filterVH("", [])
                        }}
                        onSelectItem={openValueHelpDef}
                        onToggleListMode={() =>
                            ui.toggleListMode(ui.currentValueHelpDef?.id)
                        }
                        onToggleSelectedDef={ui.toggleSelectedDef}
                        onSelectAll={() => {
                            if (
                                state.defs.every((def: ValueHelpDef) =>
                                    ui.selectedDefs.includes(def.id),
                                )
                            ) {
                                ui.clearSelectedDefs()
                            } else {
                                ui.setSelectedDefs(state.defs.map((v: ValueHelpDef) => v.id))
                            }
                        }}
                        onAdd={() => ui.setDialogAddDefOpen(true)}
                        onDeleteSelected={deleteSelectedValueHelps}
                        onDownload={download}
                        onUpload={() => ui.setDialogUploadFileOpen(true)}
                    />
                }
                midColumn={
                    <ValueHelpDetailColumn
                        currentValueHelpDef={ui.currentValueHelpDef}
                        valueHelpValue={ui.valueHelpValue}
                        language={ui.language}
                        availableLanguages={state.languages}
                        edit={ui.edit}
                        fullscreen={ui.fullscreen}
                        onEdit={ui.toggleEdit}
                        onSave={updateCurrentValueHelp}
                        onDelete={() =>
                            ui.currentValueHelpDef && deleteValueHelpDef(ui.currentValueHelpDef)
                        }
                        onClose={ui.resetDetail}
                        onFullscreen={() => {
                            ui.setLayout(
                                ui.fullscreen
                                    ? FCLLayout.TwoColumnsMidExpanded
                                    : FCLLayout.MidColumnFullScreen,
                            )
                            ui.setFullscreen(!ui.fullscreen)
                        }}
                        onChangeLanguage={changeLanguage}
                        onChangeValueHelpValue={ui.changeValueHelpValue}
                        onSetDialogAddValueOpen={ui.setDialogAddValueOpen}
                        onChangeLanguages={changeLanguages}
                        onSetCurrentValueHelpDef={ui.setCurrentValueHelpDef}
                    />
                }
            />
            <DialogAddValueHelpDefinition
                dialogAddDefOpen={ui.dialogAddDefOpen}
                setDialogAddDefOpen={ui.setDialogAddDefOpen}
                addValueHelpDef={addValueHelpDef}
                isIdExistent={ui.isDefIdExistent}
                setIsIdExistent={ui.setIsDefIdExistent}
                availableLanguages={state.languages}
            />
            <DialogAddValueHelpValue
                dialogAddValueOpen={ui.dialogAddValueOpen}
                valueHelpValue={ui.valueHelpValue!}
                setDialogAddValueOpen={ui.setDialogAddValueOpen}
                changeValueHelpValue={ui.changeValueHelpValue}
            />
            <DialogUploadFile
                dialogUploadFileOpen={ui.dialogUploadFileOpen}
                setDialogUploadFileOpen={ui.setDialogUploadFileOpen}
                upload={upload}
                loading={ui.uploadLoading}
            />
        </div>
    )
}
