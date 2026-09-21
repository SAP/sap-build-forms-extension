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
        state.findDefs(messages).then((action: any) => {
            if (apiOk(action.status)) {
                messages.toast(Severity.Success, "msg_valuehelpdefs_loaded")
            }
        })
        // async load adapters
        state.findAdapters(messages)
        // async load languages
        state.findLanguages(messages)
    }, [])

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
        let requestParams: object | undefined
        if (effectiveS || effectiveAdapter.length > 0) {
            if (effectiveS && effectiveAdapter.length > 0) {
                requestParams = { search: effectiveS, adapter: effectiveAdapter.join(",") }
            } else if (effectiveS) {
                requestParams = { search: effectiveS }
            } else {
                requestParams = { adapter: effectiveAdapter.join(",") }
            }
        }
        state.clearDefs()
        state.findDefs(messages, requestParams).then((action: any) => {
            if (apiOk(action.status)) {
                messages.toast(Severity.Success, "msg_valuehelpdefs_loaded")
                if (ui.listMode === ListSelectionMode.Multiple) {
                    const loadedIds = new Set((action.data as ValueHelpDef[]).map((v) => v.id))
                    ui.setSelectedDefs(ui.selectedDefs.filter((id) => loadedIds.has(id)))
                }
            }
        })
    }

    /**
     *  Handles language changes for the ValueHelp definition.
     *
     * @param v
     */
    function changeLanguages(v: ValueHelpDef) {
        if (!v.languages || v.languages.length === 0) {
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
                .catch(() => {
                    console.error("Unexpected Error occured!")
                })
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
                ui.setDialogAddDefOpen(false)
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
            [MessageOption.Yes, MessageOption.Cancel],
        )
        if (result !== MessageOption.Yes) return

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
            [MessageOption.Yes, MessageOption.Cancel],
        )
        if (result !== MessageOption.Yes) return

        state.deleteDefs(messages, ui.selectedDefs).then((action: any) => {
            if (apiOk(action.status)) {
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
     * Updates the current ValueHelp definition and saves pending value changes.
     *
     * @param def - The form values submitted by react-hook-form.
     */
    function updateCurrentValueHelp(def: ValueHelpDef) {
        const previousDef = ui.currentValueHelpDef
        state.updateDef(messages, def).then((action: any) => {
            if (apiOk(action.status)) {
                console.log(
                    `ValueHelp definition ${def?.id} has been updated successfully`,
                )
                if (ui.listMode === ListSelectionMode.Multiple) {
                    ui.setSelectedDefs([...ui.selectedDefs, def.id])
                }

                // Strip columns for any value-keys that were removed
                const removedKeys = (previousDef?.valueKeys ?? []).filter(
                    (k) => !(def.valueKeys ?? []).includes(k),
                )
                if (removedKeys.length > 0 && def.adapter === "local") {
                    const langs = def.languages.length > 0 ? def.languages : ["_"]
                    langs.forEach((locale) => {
                        state.findLatestValues(messages, def.id, locale).then((valAction: any) => {
                            if (apiOk(valAction.status) && valAction.data?.values?.length > 0) {
                                const cleaned = {
                                    ...valAction.data,
                                    values: valAction.data.values.map((row: Record<string, string>) => {
                                        const newRow = { ...row }
                                        removedKeys.forEach((k) => delete newRow[k])
                                        return newRow
                                    }),
                                }
                                state.updateValues(messages, cleaned)
                            }
                        })
                    })
                }

                openValueHelpDef(action.data)
                filter()
                messages.toast(Severity.Success, "msg_valuehelpdef_updated", {
                    id: def.id,
                })
            } else if (action.status === 409) {
                console.log("Definition ID is already existent.")
                messages.dialog(
                    Severity.Error,
                    "err_valuehelpdef_id_existent",
                    { id: def.id },
                    [MessageOption.Ok],
                )
            }
        })
    }

    /**
     * Persists all pending value edits (create / update / delete) to the backend.
     * Accepts the latest def so locale validation is not stale when called alongside updateCurrentValueHelp.
     */
    function saveValues(currentDef?: ValueHelpDef) {
        const defForValidation = currentDef ?? ui.currentValueHelpDef
        const pending = ui.updatedValueHelpValues
        const requests = pending.flatMap((c) => {
            const validLocale =
                defForValidation?.languages.includes(c.locale) || c.locale === "_"
            if (!validLocale) return []

            if (c.version === -1 && c.values.length > 0) {
                return [state.addValues(messages, c).then((action: any) => {
                    if (apiOk(action.status)) ui.setValueHelpValue(action.data)
                })]
            } else if (c.version >= 0 && c.values.length > 0) {
                return [state.updateValues(messages, c).then((action: any) => {
                    if (apiOk(action.status)) ui.setValueHelpValue(action.data)
                })]
            } else if (c.version >= 0 && c.values.length === 0) {
                return [state.deleteValues(messages, c.id, c.locale).then((action: any) => {
                    if (apiOk(action.status)) ui.setValueHelpValue(undefined)
                })]
            }
            return []
        })
        Promise.all(requests).then(() => {
            ui.setUpdatedValueHelpValues([])
        })
    }

    /**
     * Downloads the ValueHelp definitions as an XML file.
     */
    function download(): void {
        let requestParams: URLSearchParams | undefined
        if (ui.listMode === ListSelectionMode.Multiple) {
            const allSelected = state.defs.every((def: ValueHelpDef) =>
                ui.selectedDefs.includes(def.id),
            )
            if (!allSelected && ui.selectedDefs.length > 0) {
                // download selected ids
                requestParams = new URLSearchParams()
                ui.selectedDefs.forEach((id) => requestParams!.append("ids", id))
            }
        }
        if (!requestParams) {
            // download all displayed (apply current search filters)
            requestParams = new URLSearchParams()
            if (ui.searchId) requestParams.append("search", ui.searchId)
            ui.searchAdapter.forEach((a) => requestParams!.append("adapter", a))
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
                document.body.removeChild(a)
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
        ui.setUploadLoading(true)
        state.importDefs(messages, file, override, useTechnicalName).then((action: any) => {
            ui.setUploadLoading(false)
            ui.setDialogUploadFileOpen(false)
            if (apiOk(action.status)) {
                ui.resetDetail()
                filter()
                if (action.data) {
                    messages.dialog(Severity.Warning, "msg_upload_warnings", undefined, [MessageOption.Ok])
                } else {
                    messages.toast(Severity.Success, "msg_upload_success")
                }
            } else {
                messages.dialog(Severity.Error, "err_upload_failed", undefined, [MessageOption.Ok])
            }
        })
    }

    /**
     * Copies the selected definitions (multi-select) or the current definition (single mode).
     */
    function copyDef() {
        if (ui.listMode === ListSelectionMode.Multiple) {
            ui.setCopiedDefs(state.defs.filter((d: ValueHelpDef) => ui.selectedDefs.includes(d.id)))
        } else if (ui.currentValueHelpDef) {
            ui.setCopiedDefs([ui.currentValueHelpDef])
        }
    }

    /**
     * Pastes copied definitions with unique IDs, allocating all new IDs before adding any.
     */
    function pasteDef() {
        if (ui.copiedDefs.length === 0) return
        const taken = new Set(state.defs.map((d: ValueHelpDef) => d.id))
        ui.copiedDefs.forEach((source) => {
            const base = source.id.replace(/ \(\d+\)$/, "")
            let candidate = base
            let n = 1
            while (taken.has(candidate)) {
                candidate = `${base} (${n})`
                n++
            }
            taken.add(candidate)
            addValueHelpDef({ ...source, id: candidate })
        })
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
                        adapters={["local", ...state.adapters]}
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
                        onCopy={copyDef}
                        onPaste={pasteDef}
                        hasCopiedDef={ui.copiedDefs.length > 0}
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
                        onSave={(def) => {
                            updateCurrentValueHelp(def)
                            saveValues(def)
                        }}
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
                availableAdapters={state.adapters}
                existingIds={state.defs.map((d) => d.id)}
            />
            <DialogAddValueHelpValue
                dialogAddValueOpen={ui.dialogAddValueOpen}
                currentValueHelpDef={ui.currentValueHelpDef}
                valueHelpValue={ui.valueHelpValue}
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
