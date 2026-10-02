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
    additionalInformation?: string
    startedBy?: string,
    startedAt?: Date,
    finishedAt?: Date,
    scenarioName: string,
    scenarioVersion: number,
    scenarioUrl: string,
    version: number,
    cancelable: boolean,
    templatable: boolean,
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

export const PAGE_SIZES = [10, 25, 50, 100]

/**
 * Filter parameters interface
 */
export type FilterParams = {
    profiles?: string[],
    descriptionValue?: string[],
    functionalIdValue?: string[],
    status?: string[],
    additionalInformationValue?: string[],
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
    totalCount: number,
    page: number,
    pageSize: number,
    filter: FilterParams,

    initFilter: (settings: Settings) => void,
    setFilter: (filter: FilterParams) => void,
    mergeFilter: (partial: Partial<FilterParams>) => void,
    setPage: (messages: MessageIntf, page: number) => void,
    setPageSize: (messages: MessageIntf, pageSize: number) => void,

    findProcesses: (messages: MessageIntf, filter: FilterParams, page?: number, pageSize?: number) => Promise<AxiosResponse | Error>,
    loadSuggestions: (messages: MessageIntf, field: string, search: string) => Promise<string[]>,
    cancelProcess: (messages: MessageIntf, processId: string) => Promise<AxiosResponse | Error>,
    useAsTemplate: (messages: MessageIntf, processId: string) => Promise<AxiosResponse | Error>,
}

/**
 *
 */
export const useProcessStore = create<ProcessState>((set, get) => ({
    processes: [],
    totalCount: 0,
    page: 1,
    pageSize: PAGE_SIZES[0],
    filter: { profiles: ["my_requests"] },

    initFilter(settings: Settings) {
        const f: FilterParams = {}

        settings.profiles.forEach(p => {
            if (p.selected) {
                f.profiles = f.profiles ? f.profiles : []
                f.profiles?.push(p.id)
            }
        })
        set(() => ({ filter: f }))
    },

    setFilter(filter: FilterParams) {
        set(() => ({ filter }))
    },

    mergeFilter(partial: Partial<FilterParams>) {
        set((state) => ({ filter: { ...state.filter, ...partial } }))
    },

    setPage(messages: MessageIntf, page: number) {
        set(() => ({ page }))
        const { filter, pageSize, findProcesses } = get()
        findProcesses(messages, filter, page, pageSize)
    },

    setPageSize(messages: MessageIntf, pageSize: number) {
        set(() => ({ pageSize, page: 1 }))
        const { filter, findProcesses } = get()
        findProcesses(messages, filter, 1, pageSize)
    },

    async findProcesses(messages: MessageIntf, filter: FilterParams, page?: number, pageSize?: number): Promise<AxiosResponse | Error> {
        const params: Record<string, unknown> = { ...filter }

        if (!filter.descriptionValue?.length) {
            delete params.descriptionValue
        } else {
            params.descriptionType = "contains"
        }

        if (!filter.functionalIdValue?.length) {
            delete params.functionalIdValue
        } else {
            params.functionalIdType = "contains"
        }

        if (!filter.additionalInformationValue?.length) {
            delete params.additionalInformationValue
        } else {
            params.additionalInformationType = "contains"
        }

        if (!filter.user?.trim()) delete params.user
        if (!filter.startedBy?.trim()) delete params.startedBy
        if (!filter.endedOn?.trim()) delete params.endedOn
        if (!filter.scenario?.trim() || filter.scenario === "...") delete params.scenario
        if (!filter.roleUser?.length) delete params.roleUser
        if (!filter.status?.length) delete params.status

        // When called without an explicit page (e.g. Go button after filter change), reset to page 1
        const currentPage = page ?? 1
        const currentPageSize = pageSize ?? get().pageSize
        params.page = currentPage
        params.pageSize = currentPageSize

        const res = await backend.callDirect(messages, "/v1/processes", "GET", undefined, {
            params, paramsSerializer: { indexes: null }
        })
        if (apiOk(res.status)) {
            const data = res.data as { items?: Process[], totalCount?: number }
            set(() => ({
                processes: data.items ?? [],
                totalCount: data.totalCount ?? 0,
                page: currentPage,
                pageSize: currentPageSize,
            }))
            return Promise.resolve(res)
        }
        return handleError(res, "findProcesses", messages)
    },

    async loadSuggestions(messages: MessageIntf, field: string, search: string): Promise<string[]> {
        const res = await backend.callDirect(messages, "/v1/processes/suggestions", "GET", undefined, {
            params: { field, search }
        })
        if (apiOk(res.status)) {
            return res.data as string[]
        }
        handleError(res, "loadSuggestions", messages)
        return []
    },

    async cancelProcess(messages: MessageIntf, processId: string): Promise<AxiosResponse | Error> {
        const res = await backend.callDirect(messages, `/v1/processes/${processId}/cancel`, "POST", undefined)
        if (apiOk(res.status)) {
            return Promise.resolve(res)
        }
        return handleError(res, "cancelProcess", messages)
    },

    async useAsTemplate(messages: MessageIntf, processId: string): Promise<AxiosResponse | Error> {
        const res = await backend.callDirect(messages, `/v1/processes/${processId}/template`, "POST", undefined)
        if (apiOk(res.status)) {
            return Promise.resolve(res)
        }
        return handleError(res, "useAsTemplate", messages)
    },

}))
