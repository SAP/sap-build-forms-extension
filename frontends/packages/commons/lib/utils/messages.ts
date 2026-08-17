import { PrimitiveType } from "intl-messageformat"

/**
 * Defines the severity levels for messages. Each severity level is represented by a single character:
 * - "e" for Error
 * - "w" for Warning
 * - "i" for Info
 * - "s" for Success
 * - "_" for None
 */
export enum Severity {
    Error = "e",
    Warning = "w",
    Info = "i",
    Success = "s",
    None = "_",
}

/**
 * Defines the structure of a message object. Each message has a style, severity, key, and optional parameters.
 * - style: A string representing the style of the message (e.g., "MessageBox").
 * - severity: The severity level of the message, defined by the Severity enum.
 * - key: A string key that can be used to look up the message text from a resource bundle or similar.
 * - params: An optional object containing parameters that can be used to replace placeholders in the message text.
 */
export interface Message {
    style: string
    severity: Severity
    key: string
    params?: Record<string, PrimitiveType>
}

/**
 *  Determines the highest severity level from an array of messages. The severity levels are ordered as follows:
 *  None < Success < Info < Warning < Error. The function iterates through the messages and keeps track of the 
 *  highest severity level found.    
 * 
 * @param msg An array of message objects to evaluate.
 * @returns The highest severity level found among the messages.
 */
export function highestSeverity(msg: Message[]): Severity {
    const severityOrd: Record<Severity, number> = { _: 0, s: 1, i: 2, w: 3, e: 4 }
    var h = 0
    for (var m of msg) {
        if (severityOrd[m.severity] > h) {
            h = severityOrd[m.severity]
        }
    }
    switch (h) {
        case 1:
            return Severity.Success
        case 2:
            return Severity.Info
        case 3:
            return Severity.Warning
        case 4:
            return Severity.Error
    }
    return Severity.None
}

/**
 * Defines the options available for message dialogs. These options represent the buttons that can be displayed in a 
 * message box:
 * 
 * - Ok: A button for acknowledging the message.
 * - Retry: A button for retrying an action that may have failed.
 * - Yes: A button for confirming an action.
 * - No: A button for rejecting an action.
 * - Cancel: A button for canceling an action.
 */
export enum MessageOption {
    Ok,
    Retry,
    Yes,
    No,
    Cancel
}

/**
 * Defines the interface for a message handling system. This interface includes methods for displaying messages in 
 * different formats (fatal, dialog, toast) and for blocking user interaction when necessary.
 * 
 * - fatal: A method for displaying a fatal error message. It takes a message key and optional parameters.
 * - dialog: A method for displaying a message in a dialog box. It takes the severity, message key, optional 
 * parameters, and options for the dialog buttons. It returns a promise that resolves with the user's action.
 * - toast: A method for displaying a message as a toast notification. It takes the severity, message key, and 
 * optional parameters.
 * - block: A method for blocking or unblocking user interaction. It takes a boolean value indicating whether to 
 * block (true) or unblock (false) interaction.   
 */
export interface MessageIntf {
    fatal: (key: string, values?: Record<string, PrimitiveType>) => void
    dialog: (
        severity: Severity,
        key: string,
        params?: Record<string, PrimitiveType>,
        options?: MessageOption[],
    ) => Promise<MessageOption>
    toast: (severity: Severity, key: string, params?: Record<string, PrimitiveType>) => void
    block: (show: boolean) => void
}

export type MessageResolver = (value: MessageOption | PromiseLike<MessageOption>) => void
