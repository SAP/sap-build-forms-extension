import { messages } from "commons"

import de from "./de"
import en from "./en"

/**
 *
 * @param language
 * @returns
 */
export function getMessages(language: string): Record<string, string> {
    const lang = language.split("-")[0]
    switch (lang) {
        case "de":
            return messages.getMessages("de", de)
        default:
            return messages.getMessages("en", en)
    }
}
