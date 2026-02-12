import { useEffect } from "react"

import {
    BrowserRouter,
    matchPath,
    Outlet,
    Route,
    Routes,
    useLocation,
    useNavigationType,
} from "react-router-dom"
import { setTheme } from "@ui5/webcomponents-base/dist/config/Theme"

import { NotFound } from "commons"

import Create from "./pages/Create"
import Task from "./pages/Task"
import Show from "./pages/Show"

declare var ROUTER_BASE_NAME: string

/**
 *
 * @returns
 */
function DebugLayout() {
    const location = useLocation()
    const navigationType = useNavigationType()

    useEffect(() => {
        console.log(`The current URL is: `, { ...location })
        console.log(`The last navigation action was '${navigationType}'`)

        {
            const match = matchPath("/" + ROUTER_BASE_NAME + "/create", location.pathname)
            console.log("Next is match for ROUTER_BASE_NAME + '/create':")
            console.log(match)
        }
        {
            const match = matchPath("/" + ROUTER_BASE_NAME + "/create/:state", location.pathname)
            console.log("Next is match for ROUTER_BASE_NAME + '/create:/state':")
            console.log(match)
        }
    }, [location, navigationType])

    return <Outlet />
}

/**
 *
 * @returns
 */
export default function () {
    // TODO(ML) Make this configurable
    setTheme("sap_horizon")

    // return <RouterProvider router={router} />
    return (
        <BrowserRouter basename={ROUTER_BASE_NAME}>
            <Routes>
                {/* <Route element={<DebugLayout />}> */}
                <Route path="/create" element={<Create />} />
                <Route path="/create/:state" element={<Create />} />

                <Route path="/task/:task" element={<Task />} />

                <Route path="/show/:formsId/:state" element={<Show />} />

                <Route path="*" element={<NotFound />} />
            </Routes>
        </BrowserRouter>
    )
}
