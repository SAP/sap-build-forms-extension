import React from "react"
import ReactDOM from "react-dom/client"

import { Provider } from "react-redux"

import { ThemeProvider } from "@ui5/webcomponents-react"

import "@ui5/webcomponents-react/dist/Assets"
import "@ui5/webcomponents/dist/Assets"
import "@ui5/webcomponents-fiori/dist/Assets"
import "@ui5/webcomponents-icons/dist/Assets"
import "@ui5/webcomponents-icons/dist/AllIcons.js"

import { ChangableIntlProvider, getLanguage, MessagesProvider } from "commons"

import store from "./features/store"
import { getMessages } from "./i18n/utils"
import App from "./App"
import "./main.css"

const language = getLanguage()
const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement)

root.render(
    <React.StrictMode>
        <Provider store={store}>
            <ChangableIntlProvider messages={getMessages(language)} locale={language}>
                <ThemeProvider>
                    <MessagesProvider>
                        <App />
                    </MessagesProvider>
                </ThemeProvider>
            </ChangableIntlProvider>
        </Provider>
    </React.StrictMode>,
)
