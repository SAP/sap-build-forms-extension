import { MessageBoxType } from "@ui5/webcomponents-react"
import { JSX } from "react"

export interface Personalization {
    id: string | null
    user: string
    key: string
    app: string
    encoding: string
    value: string
    editable: boolean
    visible: boolean
}

export interface Value {
    id: string
    locale: string
    values: string[]
}

export type MessageBoxParams = {
    type: MessageBoxType | undefined
    id: string
    text: JSX.Element
}
