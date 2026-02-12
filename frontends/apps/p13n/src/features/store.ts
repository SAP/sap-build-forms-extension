import { configureStore } from "@reduxjs/toolkit"
import { TypedUseSelectorHook, useDispatch, useSelector } from "react-redux"

import axios from "axios"

// for direct axios calls
export const api = axios.create({
    baseURL: "/api",
    timeout: 60000,
    validateStatus: (status: number) => true,
})
if (sessionStorage["accessToken"]) {
    api.defaults.headers["Authorization"] = "Bearer " + sessionStorage.getItem("accessToken")
}

/**
 *
 * @param status
 * @returns
 */
export function apiOk(status: number) {
    return status >= 200 && status < 300
}
