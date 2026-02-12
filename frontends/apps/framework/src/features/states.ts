import { FormDefinition } from "./sessions/definitions"
import { Form } from "./sessions/forms"
import { FrontendJournal } from "./sessions/journal"

/**
 * SessionState holds the state of the current session in the application.
 * It includes the form definition, current form data, header title, session ID,
 * journal for undo/redo functionality, locale, page title, value helps, and a counter
 * for easy state changes.
 * This state is used to manage the current session and provide necessary data to components.
 * It is part of the Redux store and is used to provide session-related data to components.
 */
export interface SessionState {
    def?: FormDefinition
    form: Form
    headerTitle: string
    id?: string
    journal: FrontendJournal
    locale: string
    pageTitle: string
    vhs: Record<string, number>
    ignore: boolean
}

/**
 * ValuehelpsState holds the state of value helps in the application.
 * It includes a record of value helps and a cache for their values.
 * This state is used to manage the loading and retrieval of value helps.
 * It is part of the Redux store and is used to provide value helps to components.
 */
export interface ValuehelpsState {
    vhs: Record<string, boolean>
    cache: Record<string, Record<string, string>>
}

/**
 * EnvironmentState holds information about the current environment, such as screen dimensions and orientation.
 * It is used to adapt the UI based on the device's capabilities.
 */
export interface EnvironmentState {
    screenWidth: number
    screenHeight: number
    isPortraitMode: boolean
}
