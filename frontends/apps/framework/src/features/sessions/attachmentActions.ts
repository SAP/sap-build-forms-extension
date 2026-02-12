import { createAsyncThunk } from "@reduxjs/toolkit"
import { AxiosProgressEvent, AxiosResponse } from "axios"

import { SessionResponse } from "./sessionActions"
import { SessionState } from "../states"
import { apiOk, MessageIntf } from "commons"
import { backend } from "../backend"

/**
 *
 */
export interface AttachmentRequest {
    file?: File
    rowId: string
    key: string
    category?: string
    description?: string
    id?: string
    onProgress?: (e: AxiosProgressEvent) => void
    messages: MessageIntf
}

/**
 *
 */
export interface DownloadAttachmentInput {
    key: string
    id: string
}

/**
 *
 */
export const uploadAttachment = createAsyncThunk(
    "session/uploadAttachment",
    async (params: AttachmentRequest, thunkAPI): Promise<AxiosResponse<SessionResponse>> => {
        const form = new FormData()
        form.append("file", params.file!)
        form.append("row", params.rowId)
        form.append("key", params.key)
        if (params.category) {
            form.append("cat", params.category)
        }
        if (params.description) {
            form.append("desc", params.description)
        }
        form.append("sessionId", ((thunkAPI.getState() as any).session as SessionState).id!)

        let response = undefined

        try {
            response = await backend.callDirect(params.messages, "/v1/attachments", "POST", form, {
                onUploadProgress(e: any) {
                    if (params.onProgress) {
                        params.onProgress(e)
                    }
                },
            })
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => params.messages.fatal("session_error_generic"), 10)
            return Promise.reject(err)
        }

        if (apiOk(response.status)) {
            return Promise.resolve(response)
        } else {
            console.error(`Error in file upload: ${response.status}:'${response.data}'`)
            setTimeout(() => params.messages.fatal("session_error_generic"), 10)
            return Promise.reject(Error(response.status + ": " + response.statusText))
        }
    },
)

/**
 *
 */
export const deleteAttachment = createAsyncThunk(
    "session/deleteAttachment",
    async (params: AttachmentRequest, thunkAPI): Promise<AxiosResponse<SessionResponse>> => {
        let response = undefined

        try {
            response = await backend.callDirect(
                params.messages,
                "/v1/attachments/" +
                    ((thunkAPI.getState() as any).session as SessionState).id! +
                    "/" +
                    params.rowId +
                    "/" +
                    params.key +
                    "/" +
                    params.id,
                "DELETE",
            )
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => params.messages.fatal("session_error_generic"), 10)
            return Promise.reject(err)
        } finally {
            params.messages.block(false)
        }

        if (apiOk(response.status)) {
            return Promise.resolve(response)
        } else {
            console.error(`Error in deleteAttachment: ${response.status} :'${response.data}'`)
            setTimeout(() => params.messages.fatal("session_error_generic"), 10)
            return Promise.reject(Error(response.status + ": " + response.statusText))
        }
    },
)
