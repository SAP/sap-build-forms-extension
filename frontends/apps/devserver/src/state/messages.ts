import { create } from "zustand"
import { immer } from "zustand/middleware/immer"
import { Message, ElementPart } from "../utils/scenarioDefinitions"

type State = {
    messages: Message[]
}

type Action = {
    insertMessages: (payload: Record<string, Message> | Message[]) => void
    deleteMessages: () => void
    deleteElementMessages: (elementId: string, elementPart?: ElementPart) => void
}

const useMessagesStore = create<State & Action>()(
    immer((set) => ({
        messages: [],
        insertMessages: (payload: Record<string, Message> | Message[]) => {
            const array: Message[] = Array.isArray(payload)
                ? payload
                : Object.keys(payload).map((k) => (payload as Record<string, Message>)[k])
            set((state) => {
                for (let i = 0; i < array.length; i++) {
                    const item = array[i]
                    const exists = state.messages.find((element) =>
                        element.defName === item.defName
                        && element.defVersion === item.defVersion
                        && element.elementId === item.elementId
                        && element.message === item.message)
                    if (!exists) state.messages.push(item)
                }
            })
        },
        deleteMessages: () => set((state) => { state.messages = [] }),
        deleteElementMessages: (elementId: string, elementPart?: ElementPart) => set((state) => {
            state.messages = state.messages.filter(msg => {
                if (msg.elementId !== elementId) return true
                if (elementPart && msg.elementPart !== elementPart) return true
                return false
            })
        })
    }))
)

export default useMessagesStore