import { PrimitiveType } from "intl-messageformat"

/**
 *
 */
export enum Severity {
    Error = "e",
    Warning = "w",
    Info = "i",
    Success = "s",
    None = "_",
}

/**
 *
 */
export interface Message {
    style: string
    severity: Severity
    key: string
    params?: Record<string, PrimitiveType>
}

/**
 *
 * @param msg
 * @returns
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
 *
 */
export enum MessageOption {
    Ok,
    Retry,
    Yes,
    No,
    Cancel
}

/**
 *
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
