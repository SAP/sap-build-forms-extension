import { create } from "zustand"

import FCLLayout from "@ui5/webcomponents-fiori/dist/types/FCLLayout"
import ListSelectionMode from "@ui5/webcomponents/dist/types/ListSelectionMode"

import { ValueHelpDef, ValueHelpValue } from "./model"

interface ValueHelpUIState {
    // Layout
    layout: FCLLayout
    fullscreen: boolean
    // Selection
    listMode: ListSelectionMode
    selectedDefs: string[]
    // Detail panel
    currentValueHelpDef: ValueHelpDef | undefined
    valueHelpValue: ValueHelpValue | undefined
    updatedValueHelpValues: ValueHelpValue[]
    language: string | undefined
    // Edit mode
    edit: boolean
    // Dialogs
    dialogAddDefOpen: boolean
    isDefIdExistent: boolean
    dialogAddValueOpen: boolean
    dialogUploadFileOpen: boolean
    uploadLoading: boolean
    // Filter bar
    searchId: string
    searchAdapter: string[]

    // Layout actions
    setLayout(l: FCLLayout): void
    setFullscreen(f: boolean): void
    openDetailLayout(): void
    resetDetail(): void

    // List selection
    setListMode(m: ListSelectionMode): void
    toggleListMode(currentDefId?: string): void
    setSelectedDefs(ids: string[]): void
    toggleSelectedDef(id: string): void
    clearSelectedDefs(): void

    // Detail panel
    setCurrentValueHelpDef(def: ValueHelpDef | undefined): void
    setValueHelpValue(v: ValueHelpValue | undefined): void
    setUpdatedValueHelpValues(values: ValueHelpValue[]): void
    changeValueHelpValue(v: ValueHelpValue): void
    setLanguage(lang: string | undefined): void

    // Edit mode
    setEdit(e: boolean): void
    toggleEdit(): void

    // Dialogs
    setDialogAddDefOpen(o: boolean): void
    setIsDefIdExistent(b: boolean): void
    setDialogAddValueOpen(o: boolean): void
    setDialogUploadFileOpen(o: boolean): void

    // Filter bar
    setSearchId(s: string): void
    setSearchAdapter(a: string[]): void
}

export const useValueHelpUIState = create<ValueHelpUIState>((set, get) => ({
    layout: FCLLayout.OneColumn,
    fullscreen: false,
    listMode: ListSelectionMode.Single,
    selectedDefs: [],
    currentValueHelpDef: undefined,
    valueHelpValue: undefined,
    updatedValueHelpValues: [],
    language: undefined,
    edit: false,
    dialogAddDefOpen: false,
    isDefIdExistent: false,
    dialogAddValueOpen: false,
    dialogUploadFileOpen: false,
    uploadLoading: false,
    searchId: "",
    searchAdapter: [],

    setLayout: (l) => set({ layout: l }),
    setFullscreen: (f) => set({ fullscreen: f }),
    openDetailLayout: () => set({ layout: FCLLayout.TwoColumnsMidExpanded, fullscreen: false }),
    resetDetail: () =>
        set({
            layout: FCLLayout.OneColumn,
            currentValueHelpDef: undefined,
            valueHelpValue: undefined,
            language: undefined,
            fullscreen: false,
            edit: false,
            updatedValueHelpValues: [],
        }),

    setListMode: (m) => set({ listMode: m }),
    toggleListMode: (currentDefId) => {
        const { listMode } = get()
        if (listMode === ListSelectionMode.Single) {
            set({
                listMode: ListSelectionMode.Multiple,
                selectedDefs: currentDefId ? [currentDefId] : [],
            })
        } else {
            set({ listMode: ListSelectionMode.Single, selectedDefs: [] })
        }
    },
    setSelectedDefs: (ids) => set({ selectedDefs: ids }),
    toggleSelectedDef: (id) => {
        const { selectedDefs } = get()
        set({
            selectedDefs: selectedDefs.includes(id)
                ? selectedDefs.filter((a) => a !== id)
                : [...selectedDefs, id],
        })
    },
    clearSelectedDefs: () => set({ selectedDefs: [] }),

    setCurrentValueHelpDef: (def) => set({ currentValueHelpDef: def }),
    setValueHelpValue: (v) => set({ valueHelpValue: v }),
    setUpdatedValueHelpValues: (values) => set({ updatedValueHelpValues: values }),
    changeValueHelpValue: (v) => {
        const { updatedValueHelpValues } = get()
        const exists = updatedValueHelpValues.some((c) => c.id === v.id && c.locale === v.locale)
        set({
            valueHelpValue: v,
            updatedValueHelpValues: exists
                ? updatedValueHelpValues.map((c) =>
                      c.id === v.id && c.locale === v.locale ? v : c,
                  )
                : [...updatedValueHelpValues, v],
        })
    },
    setLanguage: (lang) => set({ language: lang }),

    setEdit: (e) => set({ edit: e }),
    toggleEdit: () => set((s) => ({ edit: !s.edit })),

    setDialogAddDefOpen: (o) => set({ dialogAddDefOpen: o }),
    setIsDefIdExistent: (b) => set({ isDefIdExistent: b }),
    setDialogAddValueOpen: (o) => set({ dialogAddValueOpen: o }),
    setDialogUploadFileOpen: (o) => set({ dialogUploadFileOpen: o }),

    setSearchId: (s) => set({ searchId: s }),
    setSearchAdapter: (a) => set({ searchAdapter: a }),
}))
