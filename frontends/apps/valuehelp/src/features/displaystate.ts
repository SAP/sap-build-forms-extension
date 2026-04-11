import {create} from "zustand"

import FCLLayout from "@ui5/webcomponents-fiori/dist/types/FCLLayout"
import ListSelectionMode from "@ui5/webcomponents/dist/types/ListSelectionMode"

/**
 *
 */
interface DisplayState {
    layout: FCLLayout
    fullscreen: boolean
    edit: boolean
    language?: string
    searchAdapter: string[]
    searchId: string
    listMode: ListSelectionMode
    selectedValueHelpDefs: string[]

    /**
     *
     * @param layout
     */
    setLayout(layout: FCLLayout): void

    setFullscreen(fullscreen: boolean): void

    setEdit(edit: boolean): void

    setLanguage(language?: string): void

    setSearchAdapter(searchAdapter: string[]): void

    setSearchId(searchId: string): void

    setListMode(listMode: ListSelectionMode): void

    setSelectedValueHelpDefs(selectedValueHelpDefs: string[]): void
}

/**
 *
 */
export const useDisplayState = create<DisplayState>((set, get) => ({
    layout: FCLLayout.OneColumn,
    fullscreen: false,
    edit: false,
    language: undefined,
    searchAdapter: [],
    searchId: "",
    listMode: ListSelectionMode.Single,
    selectedValueHelpDefs: [],

    setLayout: (layout: FCLLayout) => set({layout}),

    setFullscreen: (fullscreen: boolean) => set({fullscreen}),

    setEdit: (edit: boolean) => set({edit}),

    setLanguage: (language?: string) => set({language}),

    setSearchAdapter: (searchAdapter: string[]) => set({searchAdapter}),

    setSearchId: (searchId: string) => set({searchId}),

    setListMode: (listMode: ListSelectionMode) => set({listMode}),

    setSelectedValueHelpDefs: (selectedValueHelpDefs: string[]) => set({selectedValueHelpDefs}),
}))
