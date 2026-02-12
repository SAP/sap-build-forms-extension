import {
    createBrowserRouter,
    createRoutesFromElements,
    Route,
    RouterProvider,
} from "react-router-dom"

import { setTheme } from "@ui5/webcomponents-base/dist/config/Theme"

import { NotFound } from "commons"
import Editor from "./components/Editor"

function App() {
    const router = createBrowserRouter(
        createRoutesFromElements(
            <>
                <Route path="/" element={<Editor />} />
                <Route path="editor" element={<Editor />} />
                <Route path="*" element={<NotFound />} />
            </>,
        ),
    )

    // TODO(ML) Make this configurable
    setTheme("sap_horizon")

    return <RouterProvider router={router} />
}

export default App
