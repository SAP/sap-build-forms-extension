import { createAsyncThunk, createSlice } from "@reduxjs/toolkit"

import { ValuehelpsState } from "../states"
import { ValuehelpsService } from "./logic"

/**
 *
 */
export interface CheckRequest {
    vhs: Record<string, number>
    locale: string
}

/**
 *
 */
export const checkValueHelps = createAsyncThunk(
    "valuehelps/checkvaluehelps",
    async (params: CheckRequest, thunkAPI): Promise<Record<string, boolean>> => {
        return ValuehelpsService.check(params.vhs, params.locale)
    },
)

export interface LoadValueHelpRequest {
    name: string
    sessionId: string
    locale: string
}

/**
 *
 */
export const loadValueHelp = createAsyncThunk(
    "valuehelps/loadvaluehelp",
    async (params: LoadValueHelpRequest, thunkAPI): Promise<Error | string> => {
        return ValuehelpsService.load(params.name, params.sessionId, params.locale)
    },
)

export interface LoadIntoCacheRequest {
    names: Array<string>
    locale: string
}

export const loadIntoCache = createAsyncThunk(
    "valuehelps/loadIntoCache",
    async (
        params: LoadIntoCacheRequest,
        thunkAPI,
    ): Promise<Error | Record<string, Record<string, string>>> => {
        return ValuehelpsService.loadMultipleFromLocalstore(params.names, params.locale)
    },
)

const initialState: ValuehelpsState = {
    vhs: {},
    cache: {},
}

export const valuehelpsSlice = createSlice({
    name: "valuehelps",
    initialState,
    reducers: {},
    extraReducers: (builder) => {
        builder.addCase(checkValueHelps.fulfilled, (state, action) => {
            state.vhs = action.payload
        })
        builder.addCase(loadValueHelp.fulfilled, (state, action) => {
            const result = action.payload
            if (typeof result === "string") {
                // valuehelp is loaded from server and stored in localstore => can be used
                state.vhs[result] = true
            }
        })
        builder.addCase(loadIntoCache.fulfilled, (state, action) => {
            const result = action.payload
            if (!(result instanceof Error)) {
                state.cache = action.payload as Record<string, Record<string, string>>
            }
        })
    },
})

export const { } = valuehelpsSlice.actions
