import React from "react"
import ReactDOM from "react-dom/client"

import { ThemeProvider } from "@ui5/webcomponents-react"
import "@ui5/webcomponents/dist/Assets"
import "@ui5/webcomponents-fiori/dist/Assets"
import "@ui5/webcomponents-icons/dist/AllIcons.js"

import { ChangableIntlProvider, getLanguage, MessagesProvider } from "commons"

import { getMessages } from "./i18n/utils"

import App from "./App"

const language = getLanguage()
const root = ReactDOM.createRoot(document.getElementById("root") as HTMLElement)

root.render(
    <React.StrictMode>
        <ThemeProvider>
            <ChangableIntlProvider messages={getMessages(language)} locale={language}>
                <MessagesProvider>
                    <App />
                </MessagesProvider>
            </ChangableIntlProvider>
        </ThemeProvider>
    </React.StrictMode>,
)
