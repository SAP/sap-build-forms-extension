import de from "./de"
import en from "./en"

/**
 *
 * @returns
 */
export function getLanguage(): string {
    return (window.navigator as any).userLanguage || window.navigator.language
}

/**
 *
 * @param language
 * @returns
 */
export function getMessages(language: string): Record<string, string> {
    switch (language) {
        case "de":
            return de
        default:
            return en
    }
}
