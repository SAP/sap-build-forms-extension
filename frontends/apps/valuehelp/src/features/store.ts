import { create } from "zustand"
import { AxiosResponse } from "axios"

import { apiOk, copyAndReplace, handleError, MessageIntf } from "commons"

import { ValueHelpDef } from "./definitions"
import { backend } from "./backend"


/**
 * ValueHelpState interface defines the structure of the state managed by Zustand.  
 * It includes an array of value help definitions, adapters, and languages.
 * This state is used to manage value helps in the application.
 * It is part of the Zustand store and is used to provide value helps to components.
 */
interface ValueHelpState {
    defs: ValueHelpDef[]
    adapters: string[]
    languages: string[]

    /**
     * Function to clear all value help definitions locally from the state.
     */
    clearDefs(): void

    /**
     *  Function to find all value help definitions.
     *  
     * @param messages 
     * @param requestParams 
     */
    findDefs(messages: MessageIntf, requestParams?: object): Promise<AxiosResponse<ValueHelpDef[]> | Error>

    /**
     *  Function to add a new value help definition.
     * 
     * @param messages 
     * @param def 
     */
    addDef(messages: MessageIntf, def: ValueHelpDef): Promise<AxiosResponse<ValueHelpDef> | Error>

    /**
     * Function to update an existing value help definition.
     * 
     * @param messages 
     * @param def 
     */
    updateDef(messages: MessageIntf, def: ValueHelpDef): Promise<AxiosResponse<ValueHelpDef> | Error>

    /**
     *  Function to delete a value help definition.
     * 
     * @param messages 
     * @param def 
     */
    deleteDef(messages: MessageIntf, def: ValueHelpDef): Promise<AxiosResponse<void> | Error>

    /**
     *  Function to delete multiple value help definitions.
     * 
     * @param messages 
     * @param defs 
     */
    deleteDefs(messages: MessageIntf, defs: string[]): Promise<AxiosResponse<void> | Error>

    /**
     *  Function to export value help definitions based on request parameters.
     * 
     * @param messages 
     * @param requestParam 
     */
    findDefExport(messages: MessageIntf, requestParams: any): Promise<AxiosResponse<any> | Error>

    /**
     * Function to find all available adapters.
     * 
     * @param messages 
     */
    findAdapters(messages: MessageIntf): Promise<AxiosResponse<string[]> | Error>

    /**
     * Function to find all available languages.
     * 
     * @param messages 
     */
    findLanguages(messages: MessageIntf): Promise<AxiosResponse<string[]> | Error>

    /**
     *  Function to find the latest values for a given value help definition and locale.
     * 
     * @param messages 
     * @param defId 
     * @param locale 
     */
    findLatestValues(messages: MessageIntf, defId: string, locale: string): Promise<AxiosResponse<any> | Error>
}

/**
 * Zustand store for value help state management.
 * This store holds the definitions, adapters, and languages related to value helps.    
 */
export const useValueHelpState = create<ValueHelpState>((set, get) => ({
    defs: [],
    adapters: [],
    languages: [],

    clearDefs: () => set({ defs: [] }),

    findDefs: async (messages: MessageIntf, requestParams?: object): Promise<AxiosResponse<ValueHelpDef[]> | Error> => {
        try {
            const response = await backend.callDirect(messages, "/v1/valuehelpdefs", "GET", undefined, { params: requestParams })
            if (apiOk(response.status)) {
                set({ defs: response.data as ValueHelpDef[] })
                return Promise.resolve(response)
            } else {
                return handleError(response, "findDefs", messages)
            }
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => messages.fatal("common_error_backend"), 10)
            return Promise.reject(err)
        }
    },

    addDef: async (messages: MessageIntf, def: ValueHelpDef): Promise<AxiosResponse<ValueHelpDef> | Error> => {
        try {
            const response = await backend.callDirect(
                messages,
                `/v1/valuehelpdefs/${encodeURIComponent(def.id)}`,
                "POST",
                def,
            )
            if (apiOk(response.status)) {
                set((state) => ({ defs: copyAndReplace(state.defs, def).sort((a, b) => a.id.localeCompare(b.id)) }))
                return Promise.resolve(response)
            } else if (response.status === 409) {
                return Promise.resolve(response)
            } else {
                return handleError(response, "addDef", messages)
            }
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => messages.fatal("common_error_backend"), 10)
            return Promise.reject(err)
        }
    },

    updateDef: async (messages: MessageIntf, def: ValueHelpDef): Promise<AxiosResponse<ValueHelpDef> | Error> => {
        try {
            const response = await backend.callDirect(
                messages,
                `/v1/valuehelpdefs/${encodeURIComponent(def.id)}`,
                "PUT",
                def,
            )
            if (apiOk(response.status)) {
                set((state) => ({ defs: copyAndReplace(state.defs, def).sort((a, b) => a.id.localeCompare(b.id)) }))
                return Promise.resolve(response)
            } else {
                return handleError(response, "updateDef", messages)
            }
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => messages.fatal("common_error_backend"), 10)
            return Promise.reject(err)
        }
    },

    deleteDef: async (messages: MessageIntf, def: ValueHelpDef): Promise<AxiosResponse<void> | Error> => {
        try {
            const response = await backend.callDirect(
                messages,
                `/v1/valuehelpdefs/${encodeURIComponent(def.id)}`,
                "DELETE",
            )
            if (apiOk(response.status)) {
                const defs = get().defs
                const pos = defs.findIndex((it) => it.id === def.id)
                set({ defs: defs.slice(0, pos).concat(defs.slice(pos + 1)) })
                return Promise.resolve(response)
            } else {
                return handleError(response, "deleteDef", messages)
            }
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => messages.fatal("common_error_backend"), 10)
            return Promise.reject(err)
        }
    },

    deleteDefs: async (messages: MessageIntf, defs: string[]): Promise<AxiosResponse<void> | Error> => {
        try {
            const response = await backend.callDirect(messages, `/v1/valuehelpdefs`, "DELETE", { ids: defs })
            if (apiOk(response.status)) {
                let currentDefs = get().defs
                defs.forEach((def) => {
                    const pos = currentDefs.findIndex((it) => it.id === def)
                    currentDefs = currentDefs.slice(0, pos).concat(currentDefs.slice(pos + 1))
                })
                set({ defs: currentDefs })
                return Promise.resolve(response)
            }

            return handleError(response, "deleteDefs", messages)
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => messages.fatal("common_error_backend"), 10)
            return Promise.reject(err)
        }
    },

    findDefExport: async (messages: MessageIntf, requestParams: any): Promise<AxiosResponse<any> | Error> => {
        try {
            const response = await backend.callDirect(messages, `/v1/valuehelpdefs/export`, "GET", undefined, { params: requestParams })
            if (apiOk(response.status)) {
                return Promise.resolve(response)
            }
            return handleError(response, "findDefExport", messages)
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => messages.fatal("common_error_backend"), 10)
            return Promise.reject(err)
        }
    },

    findAdapters: async (messages: MessageIntf): Promise<AxiosResponse<string[]> | Error> => {
        try {
            const response = await backend.callDirect(messages, "/v1/valuehelpdefs/definedAdapter", "GET")
            if (apiOk(response.status)) {
                set({ adapters: response.data as string[] })
                return Promise.resolve(response)
            } else {
                return handleError(response, "findAdapters", messages)
            }
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => messages.fatal("common_error_backend"), 10)
            return Promise.reject(err)
        }
    },

    findLanguages: async (messages: MessageIntf): Promise<AxiosResponse<string[]> | Error> => {
        try {
            const response = await backend.callDirect(messages, "/v1/valuehelpdefs/locales", "GET")
            if (apiOk(response.status)) {
                set({ languages: response.data as string[] })
                return Promise.resolve(response)
            } else {
                return handleError(response, "findLanguages", messages)
            }
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => messages.fatal("common_error_backend"), 10)
            return Promise.reject(err)
        }
    },

    findLatestValues: async (messages: MessageIntf, defId: string, locale: string): Promise<AxiosResponse<any> | Error> => {
        try {
            const response = await backend.callDirect(
                messages,
                `/v1/valuehelpvalues/${encodeURIComponent(defId)}/${encodeURIComponent(locale)}/latest`,
                "GET",
            )
            if (apiOk(response.status) || response.status === 404) {
                return Promise.resolve(response)
            } else {
                return handleError(response, "findLatestValues", messages)
            }
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => messages.fatal("common_error_backend"), 10)
            return Promise.reject(err)
        }
    }
}))
