import { createContext, ReactNode, useState } from "react"
import { IntlProvider } from "react-intl"

import { DEFAULT_LOCALE } from "../utils/DataFormatUtils"

/**
 *
 */
interface IntlProviderData {
    change: (locale: string, message: Record<string, string>) => void
}

/**
 *
 */
const Context = createContext<IntlProviderData>({
    change: (locale: string, message: Record<string, string>) => {},
})

/**
 *
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
