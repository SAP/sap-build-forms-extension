import { Backend, RequestInfo } from "commons"

import { SessionResponse } from "./sessions/sessionActions"
import { SessionState } from "./states"

// Create a new backend instance for session management
export const backend = new Backend<SessionResponse>(
    (data: any, request: RequestInfo<SessionResponse>) => {
        data["journal"] = (request.preSendData as SessionState).journal
    },
)

/**
 *  
 */
export interface BackendError {
    error_code: number
    guid: string
    message: string
    user: string
}