import { create } from "zustand"

import { Process } from "./processes"
import { produce } from "immer"
import { apiOk, handleError, MessageIntf } from "commons"
import { AxiosResponse } from "axios"
import { backend } from "./backend"

type Views = "list" | "details" | "form"
type DetailTabs = "details" | "timeline"

/**
 * 
 */
export interface Profile {
    id: string
    name: string
    selected: boolean
}

/**
 *
 */
export interface Settings {
    language: string
    profiles: Array<Profile>
}

/**
 * 
 */
interface VisualState {
    selectedProcess?: Process,
    view: Views,
    settings?: Settings,
    detailTab: DetailTabs,

    setSelectedProcess: (process?: Process) => void,
    setView: (view: Views) => void,
    setDetailTab: (tab: DetailTabs) => void,

    loadSettings: (messages: MessageIntf, language: string) => Promise<AxiosResponse<Settings> | Error>,

}

export const useVisualStore = create<VisualState>((set) => ({
    selectedProcess: undefined,
    view: "list",
    detailTab: "details",

    setSelectedProcess(process?: Process) {
        set(state => ({ selectedProcess: process }))
    },

    setView(view: Views) {
        set(produce(state => ({ view })))
    },

    setDetailTab(tab: DetailTabs) {
        set(produce(state => ({ detailTab: tab })))
    },

    async loadSettings(messages: MessageIntf, language: string): Promise<AxiosResponse<Settings> | Error> {
        const res = await backend.callDirect(messages, "/v1/processes/settings", "GET", undefined, { params: { language } })
        if (apiOk(res.status)) {
            set(() => ({ settings: res.data as Settings }))
            return Promise.resolve(res)
        }
        return handleError(res, "loadSettings", messages)
    }
}))