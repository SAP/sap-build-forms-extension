import { AxiosResponse } from "axios"
import { create } from "zustand"

import { apiOk, handleError, MessageIntf } from "commons"

import { backend } from "./backend"
import { Settings } from "./visual"

/**
 * 
 */
export interface Process {
    id: string,
    refId: string,
    description: string,
    functionalId: string,
    state: string,
    detailState: string,
    startedBy?: string,
    startedAt?: Date,
    finishedAt?: Date,
    scenarioName: string,
    scenarioVersion: number,
    scenarioUrl: string,
    version: number,
    cancelable: boolean,
    showState: string
}

/**
 * 
 */
export interface ProcessStatePresentation {
    id: string,
    color: "1" | "2" | "3" | "4" | "5" | "6" | "7" | "8" | "9" | "10" | "Placeholder" | undefined,
    icon: string
}

/**
 * 
 */
export const PROCESS_STATES: Array<ProcessStatePresentation> = [
    {
        id: "0",
        color: "10",
        icon: "to-be-reviewed"
    },
    {
        id: "10",
        color: "9",
        icon: "status-inactive"
    },
    {
        id: "20",
        color: "6",
        icon: "media-play"
    },
    {
        id: "90",
        color: "3",
        icon: "status-error"
    },
    {
        id: "100",
        color: "8",
        icon: "status-completed"
    },
]

/**
 * 
 */
export const FILTER_INPUI_TYPES = ["equals", "contains", "begins_with", "ends_with"]

/**
 * Filter parameters interface
 */
export type FilterParams = {
    profiles?: string[],
    descriptionType?: string,
    descriptionValue?: string,
    functionalIdType?: string,
    functionalIdValue?: string,
    status?: string[],
    additionalInformationType?: string,
    additionalInformationValue?: string,
    user?: string,
    roleUser?: string[],
    startedBy?: string,
    endedOn?: string,
    scenario?: string
}

/**
 * Process state interface 
 */
interface ProcessState {
    processes: Process[],
    filter: FilterParams,

    initFilter: (settings: Settings) => void,
    setFilter: (filter: FilterParams) => void,

    findProcesses: (messages: MessageIntf, filter: FilterParams) => Promise<AxiosResponse<Process[]> | Error>,
}

/**
 * 
 */
export const useProcessStore = create<ProcessState>((set) => ({
    processes: [],
    filter: { profiles: ["my_applications"] },

    initFilter(settings: Settings) {
        const f: FilterParams = {}

        settings.profiles.forEach(p => {
            if (p.selected) {
                f.profiles = f.profiles ? f.profiles : []
                f.profiles?.push(p.id)
            }
        })
        set(() => ({ filter: f }))

        console.log(f)
    },

    setFilter(filter: FilterParams) {
        set(() => ({ filter }))
    },

    async findProcesses(messages: MessageIntf, filter: FilterParams): Promise<AxiosResponse<Process[]> | Error> {
        const res = await backend.callDirect(messages, "/v1/processes", "GET", undefined, {
            params: filter, paramsSerializer: { indexes: null }
        })
        if (apiOk(res.status)) {
            set(() => ({ processes: res.data as Process[] }))
            return Promise.resolve(res)
        }
        return handleError(res, "findProcesses", messages)
    }

}))