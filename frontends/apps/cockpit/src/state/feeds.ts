import { create } from "zustand"
import { apiOk, handleError, MessageIntf } from "commons"
import { backend } from "./backend"

export const FEED_TYPES = ["COMMENT", "INFO", "QUESTION", "ANSWER"] as const
export type FeedType = (typeof FEED_TYPES)[number]

export interface FeedEntry {
    id: string
    parentId: string | null
    formId: string
    userNm: string
    type: FeedType
    pos: number
    ts: string
    text: string
}

/**
 * Feed store state and actions
 */
interface FeedState {
    feeds: FeedEntry[]
    loading: boolean

    loadFeeds: (messages: MessageIntf, formId: string) => Promise<void>
    addFeed: (
        messages: MessageIntf,
        formId: string,
        text: string,
        type: FeedType,
        parentId?: string,
    ) => Promise<FeedEntry | null>
    clearFeeds: () => void
}

export const useFeedStore = create<FeedState>((set, get) => ({
    feeds: [],
    loading: false,

    async loadFeeds(messages, formId) {
        set(() => ({ loading: true }))
        const res = await backend.callDirect(
            messages,
            `/v1/processes/${formId}/feeds`,
            "GET",
            undefined,
        )
        if (apiOk(res.status)) {
            set(() => ({ feeds: Array.isArray(res.data) ? (res.data as FeedEntry[]) : [], loading: false }))
        } else {
            set(() => ({ loading: false }))
            handleError(res, "loadFeeds", messages)
        }
    },

    async addFeed(messages, formId, text, type, parentId) {
        set(() => ({ loading: true }))
        const res = await backend.callDirect(
            messages,
            `/v1/processes/${formId}/feeds`,
            "POST",
            { text, type, parentId: parentId ?? null },
        )
        if (apiOk(res.status)) {
            const newEntry = res.data as FeedEntry
            set((state) => ({ feeds: [...state.feeds, newEntry], loading: false }))
            return newEntry
        }
        set(() => ({ loading: false }))
        handleError(res, "addFeed", messages)
        return null
    },

    clearFeeds() {
        set(() => ({ feeds: [], loading: false }))
    },
}))
