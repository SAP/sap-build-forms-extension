import { PayloadAction, createAsyncThunk } from "@reduxjs/toolkit"
import { AxiosResponse } from "axios"

import { apiOk, Backend, getLanguage, Message, MessageIntf } from "commons"

import { Definition, FormDefinition, UIElement, UserEventType } from "./definitions"
import { BackendJournal, JournalService } from "./journal"
import { ElementMap, Form, FormService } from "./forms"
import { SessionState } from "../states"
import { AttachmentRequest } from "./attachmentActions"
import { backend, BackendError } from "../backend"
import { PrimitiveType } from "react-intl"
import { se } from "date-fns/locale"

/**
 *
 */
export interface CreateSessionRequest {
    state?: string
    task?: string
    formsId?: string
    messages: MessageIntf
}

/**
 *  This interface describes the response from the backend when a session is created or updated. It contains 
 *  information about the session, including its definition, header title, ID, journal, locale, page title, values, 
 *  and messages. The dvhs property is optional and may contain dynamic value helps.
 */
export interface SessionResponse {
    def?: FormDefinition
    headerTitle?: string
    id: string
    journal?: BackendJournal
    locale: string
    pageTitle?: string
    values?: ElementMap
    vhs: Record<string, number>
    msg: Message[]
    dvhs?: Record<string, Record<string, string>>
}

/**
 *
 */
export const createSession = createAsyncThunk(
    "session/createSession",
    async (
        { messages, state, task, formsId }: CreateSessionRequest,
        thunkAPI,
    ): Promise<AxiosResponse<SessionResponse | BackendError | string>> => {
        let response = undefined

        try {
            let params: { [key: string]: string } = {}
            if (typeof state === "string") {
                params["state"] = state
            }
            if (typeof task === "string") {
                params["task"] = task
            }
            if (typeof formsId === "string") {
                params["formsId"] = formsId
            }
            response = await backend.callQueued(
                messages,
                "/v1/sessions",
                "POST",
                {
                    ...params,
                    locale: getLanguage(),
                },
                thunkAPI.getState() as any,
            )
        } catch (err) {
            console.error(`Error: ${err}`)
            setTimeout(() => messages.fatal("session_error_creation"), 10)
            return Promise.reject(err)
        }

        if (apiOk(response.status)) {
            return Promise.resolve(response)
        } else {
            console.error(`Error in createSession: ${response.status}:'${response.data}'`)
            let errorInfo: Record<string, PrimitiveType> = {}
            if (typeof response.data === "object") {
                errorInfo["guid"] = (response.data as any as BackendError).guid
            }
            setTimeout(() => messages.fatal("session_error_creation", errorInfo), 10)
            return Promise.reject(Error(response.status + ": " + response.statusText))
        }
    },
)

/**
 *
 * @param type
 * @param def
 * @returns
 */
export function isEventValid(type: UserEventType, def: Definition): boolean {
    switch (type) {
        case UserEventType.Action:
            if (def.uiElement == UIElement.Button || def.uiElement == UIElement.SearchHelp) {
                return true
            }
            return def?.events?.includes(type) ? true : false
        case UserEventType.Sort:
        case UserEventType.Browse:
            return def.uiElement == UIElement.Table
        default:
            return def?.events?.includes(type) ? true : false
    }
}

/**
 *
 * @param type
 * @param session
 * @param rowId
 * @param key
 * @returns
 */
function isEventPrerequisiteFullfilled(
    type: UserEventType,
    session: SessionState,
    def: Definition,
    rowId: string | undefined,
    key: string,
): boolean {
    switch (type) {
        case UserEventType.Action:
        case UserEventType.Open:
            return true
        case UserEventType.Sort:
        case UserEventType.Browse:
            return def.uiElement == UIElement.Table
        default:
            return JournalService.existsElement(session.journal, rowId, key)
    }
}

/**
 *
 */
interface TriggerEventRequest {
    type: UserEventType
    def: Definition
    rowId: string | undefined
    messages: MessageIntf
}

/**
 *
 */
export const triggerEvent = createAsyncThunk(
    "session/triggerEvent",
    async (
        { type, def, rowId, messages }: TriggerEventRequest,
        thunkAPI,
    ): Promise<AxiosResponse<SessionResponse | BackendError | string> | undefined> => {
        console.log(`triggerEvent is called on '${def.id}' with type for '${type}'`)

        if (isEventValid(type, def)) {
            // console.log(`Event found ${def.events[type]}`)
            // go through changes in journal. Only if we find one for the element then we trigger the event
            const session = (thunkAPI.getState() as any).session as SessionState
            // console.log(session.journal)
            if (isEventPrerequisiteFullfilled(type, session, def, rowId, def.key)) {
                // console.log("Change found ==> calling backend...")
                let response = undefined

                try {
                    response = await backend.callQueued(
                        messages,
                        "/v1/sessions",
                        "PATCH",
                        {
                            id: session.id,
                            command: type,
                            srcRow: rowId,
                            srcKey: def.key,
                        },
                        session,
                    )
                } catch (err) {
                    console.error(`Error: ${err}`)
                    setTimeout(() => messages.fatal("session_error_generic"), 10)
                    return Promise.reject(err)
                }

                if (apiOk(response.status)) {
                    return Promise.resolve(response)
                } else {
                    console.error(`Error in trigger-event: ${response.status}:'${response.data}'`)
                    let errorInfo: Record<string, PrimitiveType> = {}
                    if (typeof response.data === "object") {
                        errorInfo = { guid: (response.data as any as BackendError).guid }
                    }
                    setTimeout(() => messages.fatal("session_error_generic", errorInfo), 10)
                    return Promise.reject(Error(response.status + ": " + response.statusText))
                }
            }
        }

        return Promise.resolve(undefined)
    },
)

/**
 *
 */
interface DeleteRowEventRequest {
    def: Definition
    rowId: string
    deleteRowId: string
    messages: MessageIntf
}

/**
 *
 */
export const deleteRow = createAsyncThunk(
    "session/deleteRow",
    async (
        { def, deleteRowId, rowId, messages }: DeleteRowEventRequest,
        thunkAPI,
    ): Promise<AxiosResponse<SessionResponse | BackendError | string> | undefined> => {
        console.log(`deleteRow is called on '${def.id}'`)

        // console.log(`Event found ${def.events[type]}`)
        const session = (thunkAPI.getState() as any).session as SessionState
        // console.log(session.journal)
        const element = FormService.findElementByRowAndKey(
            rowId,
            def.key,
            session.form,
        )
        if (element) {
            JournalService.addDeleted(
                session.journal,
                rowId,
                def.key,
                deleteRowId,
            )

            try {
                let response = await backend.callQueued(
                    messages,
                    "/v1/sessions",
                    "PATCH",
                    {
                        id: session.id,
                        command: "delete",
                        srcRow: rowId,
                        srcKey: def.key,
                    },
                    session,
                )

                if (apiOk(response.status)) {
                    return Promise.resolve(response)
                } else {
                    console.error(`Error in trigger-event: ${response.status}:'${response.data}'`)
                    let errorInfo: Record<string, PrimitiveType> = {}
                    if (typeof response.data === "object") {
                        errorInfo = { guid: (response.data as any as BackendError).guid }
                    }
                    setTimeout(() => messages.fatal("session_error_generic", errorInfo), 10)
                    return Promise.reject(Error(response.status + ": " + response.statusText))
                }
            } catch (err) {
                console.error(`Error: ${err}`)
                setTimeout(() => messages.fatal("session_error_generic"), 10)
                return Promise.reject(err)
            }
        }
    },
)

/**
 *  
 * @param state
 * @param action
 */
export function handleSessionResponse(
    state: SessionState,
    action: PayloadAction<
        AxiosResponse<string | BackendError | SessionResponse, any> | undefined,
        string,
        {
            arg: TriggerEventRequest | AttachmentRequest | CreateSessionRequest
            requestId: string
            requestStatus: "fulfilled"
        },
        never
    >,
    initSession: boolean,
) {
    // console.log(action.payload)
    if (action.payload) {
        if (action.payload.status == 410) {
            throw new Error("Session is Gone!")
        }

        const data: SessionResponse = action.payload.data as SessionResponse
        if (!initSession && data.id !== state.id) {
            throw new Error("Session-Id does not match!")
        }

        if (initSession) {
            state.id = data.id
            state.def = data.def
            state.locale = data.locale
            if (data.values) {
                state.form.values = data.values
            }
            state.vhs = data.vhs
        } else {
            if (data.journal) {
                JournalService.apply(state.form, data.journal)
            }
            state.form = new Form("", state.form.values)
        }

        // frontend journal needs to be fresh, all data to be resetted
        JournalService.init(state.journal)

        // handling of page title and header title
        if (data.pageTitle) {
            state.pageTitle = data.pageTitle
        }
        if (data.headerTitle) {
            state.headerTitle = data.headerTitle
        }

        // handling of messages from backend
        if (data.msg && data.msg.length > 0) {
            // console.log("Message found in server return")
            switch (data.msg[0].style) {
                case "Toast":
                    // console.log("Toast found in server return")
                    action.meta.arg.messages.toast(data.msg[0].severity, data.msg[0].key, data.msg[0].params)
                    break
                case "Dialog":
                    // console.log("Dialog found in server return")
                    action.meta.arg.messages.dialog(data.msg[0].severity, data.msg[0].key, data.msg[0].params)
                    break
                default:
                    console.warn(`Unsupported message style ${data.msg[0].style}`)
            }
        }

        // handling of dynamic value-helps
        if (data.dvhs) {
            state.dvhs = data.dvhs
        }
    }
}
