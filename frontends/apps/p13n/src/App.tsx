import {
    createBrowserRouter,
    createRoutesFromElements,
    Route,
    RouterProvider,
} from "react-router-dom"

import { setTheme } from "@ui5/webcomponents-base/dist/config/Theme"

import NotFound from "./pages/NotFound"
import PersonalizationAdmin from "./pages/PersonalizationAdmin"
import PersonalizationUser from "./pages/PersonalizationUser"
import Values from "./pages/Values"

function App() {
    const router = createBrowserRouter(
        createRoutesFromElements(
            <>
                <Route path="/user" element={<PersonalizationUser />} />
                <Route path="/admin" element={<PersonalizationAdmin />} />
                <Route path="/admin/values" element={<Values />} />
                <Route path="*" element={<NotFound />} />
            </>,
        ),
    )

    // TODO(ML) Make this configurable
    setTheme("sap_horizon")

    return <RouterProvider router={router} />
}

export default App
