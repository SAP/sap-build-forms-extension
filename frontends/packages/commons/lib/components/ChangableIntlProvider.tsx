import { createContext, ReactNode, useState } from "react"

import { IntlProvider } from "react-intl"

// @ts-ignore
import { setLanguage } from "@ui5/webcomponents-base/dist/config/Language"

import { DEFAULT_LOCALE } from "../utils/languageutils"

/**
 * Interface for the data provided by the ChangableIntlProvider context.
 * It includes a change function to update the locale and messages.
 */
interface IntlProviderData {
    change: (locale: string, message: Record<string, string>) => void
}

/**
 * Context for the ChangableIntlProvider.
 * It provides a way to change the locale and messages dynamically.
 */
const Context = createContext<IntlProviderData>({
    // @ts-ignore
    change: (locale: string, message: Record<string, string>) => {},
})

/**
 * ChangableIntlProvider component that wraps the IntlProvider.
 * It allows changing the locale and messages dynamically.
 * It uses React's state to manage the current locale and messages.
 *
 * @param props - The properties passed to the component.
 *                It includes locale, messages, and children.
 * @returns JSX.Element
 *          The rendered component with the IntlProvider and context.
 */
function ChangableIntlProvider(props: {
    locale: string
    messages: Record<string, string>
    children: ReactNode
}) {
    const [locale, setLocale] = useState<string>(props.locale)
    const [messages, setMessages] = useState<Record<string, string>>(props.messages)

    const change = (l: string, m: Record<string, string>) => {
        setLocale(l)
        setMessages(m)
        // for ui5, Datepicker/Timepicker etc. don't work otherwise
        setLanguage(l)

        console.log(`Locale set to '${l}'`)
    }

    return (
        <Context.Provider value={{ change }}>
            <IntlProvider messages={messages} locale={locale} defaultLocale={DEFAULT_LOCALE}>
                {props.children}
            </IntlProvider>
        </Context.Provider>
    )
}

export { ChangableIntlProvider, Context as ChangableIntlContext }
