import { Backend, RequestInfo } from "commons"

// Create a new backend instance for session management
export const backend = new Backend<any>((data: any, request: RequestInfo<any>) => {})
