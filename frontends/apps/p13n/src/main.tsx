import React from "react"
import ReactDOM from "react-dom/client"

import { Provider } from "react-redux"

import { ThemeProvider } from "@ui5/webcomponents-react"
import "@ui5/webcomponents-react/dist/Assets"
import "@ui5/webcomponents/dist/Assets"
import "@ui5/webcomponents-fiori/dist/Assets"
import "@ui5/webcomponents-icons/dist/Assets"
import "@ui5/webcomponents-icons/dist/AllIcons"

import { ChangableIntlProvider } from "./components/ChangableIntlProvider"
import { getLanguage, getMessages } from "./i18n/utils"

import { MessagesProvider } from "./components/layout/Messages"
import App from "./App"

const language = getLanguage()
const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement)

root.render(
    <React.StrictMode>
        <ChangableIntlProvider messages={getMessages(language)} locale={language}>
            <ThemeProvider>
                <MessagesProvider>
                    <App />
                </MessagesProvider>
            </ThemeProvider>
        </ChangableIntlProvider>
    </React.StrictMode>,
)
