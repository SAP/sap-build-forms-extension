import { PrimitiveType } from "intl-messageformat"
import { Severity } from "../features/sessions/forms"

/**
 *
 */
export enum MessageOption {
    Ok,
    Retry,
    Yes,
    No,
}

/**
 *
 */
export interface MessageIntf {
    fatal: (key: string, values?: Record<string, PrimitiveType>) => void
    dialog: (
        severity: Severity,
        key: string,
        values?: Record<string, PrimitiveType>,
        options?: MessageOption[],
    ) => Promise<MessageOption>
    toast: (severity: Severity, key: string, values?: Record<string, PrimitiveType>) => void
    block: (show: boolean) => void
}

export type MessageResolver = (value: MessageOption | PromiseLike<MessageOption>) => void
