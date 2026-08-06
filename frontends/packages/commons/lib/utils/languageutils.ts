import de from "../i18n/de"
import en from "../i18n/en"

// Default locale for date formatting
export const DEFAULT_LOCALE = "en"

/**
 *  Returns the language of the browser.
 *  @returns {string} The language code, e.g., "en-US" or "de-DE".
 */
export function getLanguage(): string {
    const language = (window.navigator as any).userLanguage || window.navigator.language
    return language.split("-")[0]
}

/**
 * Checks the given locale and if not one of the support locales it returns "en",
 * as the default locale
 *
 * @param locale
 * @returns
 */
export function checkLocale(locale: string): string {
    switch (locale) {
        case "de":
            return locale
        default:
            return DEFAULT_LOCALE
    }
}

/**
 * Class to manage messages for different languages.
 * It initializes with default messages and allows adding additional messages.
 */
class Messages {
    private processedMessages: Record<string, Record<string, string>> = {}

    /**
     * Returns the messages for the specified language.
     * If the language is not found, it returns the default messages.
     *
     * @param language
     * @param addMessages
     * @returns
     */
    getMessages(language: string, addMessages: Record<string, string>): Record<string, string> {
        if (this.processedMessages[language] === undefined) {
            // init and load default messages
            let defaultMessages = {}
            if (language === "de") {
                defaultMessages = de
            } else if (language === "en") {
                defaultMessages = en
            }
            // create and language entry with default and additional messages
            this.processedMessages[language] = Object.assign(defaultMessages, addMessages)
        }
        return this.processedMessages[language]
    }
}

export const messages = new Messages()
