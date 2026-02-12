import { RouterProvider, RouterProviderProps } from "react-router-dom"

// @ts-ignore
import { setTheme } from "@ui5/webcomponents-base/dist/config/Theme"

/**
 *
 */
export interface BaseAppProps extends RouterProviderProps {}

/**
 *
 * @param param0
 * @returns
 */
export function BaseApp({ router }: BaseAppProps) {
    // ML(TODO): This isn't working, no matter what I specify in setTheme the framework doesn't care ...
    // const url = new URL(window.location.href)
    // let sapUiTheme = url.searchParams.get("sap-ui-theme")
    // console.log(`URL-Parameter for UI5 theme is ${sapUiTheme}`)
    // switch (sapUiTheme) {
    //     default:
    //         sapUiTheme = "sap_horizon"
    // }
    // console.log(`Setting UI5 theme to ${sapUiTheme}`)
    // // url.searchParams.set("sap-ui-theme", sapUiTheme)
    // setTheme(sap)

    return <RouterProvider router={router} />
}
