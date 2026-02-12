import { AxiosResponse } from "axios"
import { Backend, MessageIntf } from "commons"

// Create a new backend instance for DevServer
export const backend = new Backend<any>((data: any, request: any) => {
    // Add any DevServer-specific pre-processing here if needed
})

// Message interface stub for backend calls
const messageStub: MessageIntf = {
    block: () => {},
    fatal: () => {},
    dialog: () => Promise.resolve("OK" as any),
    toast: () => {},
}

/**
 * DevServer-specific wrapper for commons Backend functionality
 * @param url API endpoint URL
 * @param method HTTP method
 * @param params Request parameters
 * @returns Promise with response data
 */
export async function backendDispatch(
    url: string,
    method: "GET" | "POST" | "PATCH" | "DELETE" | "PUT",
    params?: object,
): Promise<AxiosResponse<any>> {
    return backend.callQueued(messageStub, url, method, params)
}
