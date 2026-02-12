import { BrowserRouter, Route, Routes } from "react-router-dom"

import { setTheme } from "@ui5/webcomponents-base/dist/config/Theme"

import { NotFound } from "commons"

import Editor from "./components/EditorPage"

declare var ROUTER_BASE_NAME: string

export default function () {
    // TODO(ML) Make this configurable
    setTheme("sap_horizon")

    return (
        <BrowserRouter basename={ROUTER_BASE_NAME}>
            <Routes>
                <Route path="" element={<Editor />} />
                <Route path="/editor" element={<Editor />} />

                <Route path="*" element={<NotFound />} />
            </Routes>
        </BrowserRouter>
    )
}
