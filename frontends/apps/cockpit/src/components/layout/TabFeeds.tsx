import { useEffect, useState } from "react"
import { useIntl } from "react-intl"
import { createUseStyles } from "react-jss"

import {
    Avatar,
    BusyIndicator,
    Button,
    FlexBox,
    Option,
    Select,
    SelectDomRef,
    Tab,
    Text,
    TextArea,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import { SelectChangeEventDetail } from "@ui5/webcomponents/dist/Select"

import "@ui5/webcomponents-icons/dist/paper-plane.js"
import "@ui5/webcomponents-icons/dist/comment.js"

import { formatDate, getLanguage, useMessages } from "commons"
import { FeedEntry, FeedType, FEED_TYPES, useFeedStore } from "../../state/feeds"
import { Process } from "../../state/processes"

// ─── styles ────────────────────────────────────────────────────────────────
const useStyles = createUseStyles({
    container: {
        display: "flex",
        flexDirection: "column",
        padding: "1rem",
        gap: "1rem",
    },
    inputRow: {
        display: "flex",
        gap: "0.5rem",
        alignItems: "center",
    },
    entryCard: {
        borderLeft: "3px solid var(--sapAccentColor6)",
        paddingLeft: "0.75rem",
        paddingTop: "0.25rem",
        paddingBottom: "0.5rem",
        marginBottom: "0.5rem",
        background: "var(--sapBackgroundColor)",
    },
    replyCard: {
        borderLeft: "3px solid var(--sapAccentColor4)",
        paddingLeft: "0.75rem",
        paddingTop: "0.25rem",
        paddingBottom: "0.5rem",
        marginBottom: "0.25rem",
        marginLeft: "2rem",
        background: "var(--sapBackgroundColor)",
    },
    typeTag: {
        fontSize: "0.75rem",
        fontWeight: "bold",
        color: "var(--sapNeutralColor)",
        marginRight: "0.5rem",
    },
    meta: {
        fontSize: "0.75rem",
        color: "var(--sapNeutralColor)",
    },
    noEntries: {
        padding: "1rem",
        color: "var(--sapNeutralColor)",
    },

    replyInputRow: {
        marginLeft: "2rem",
        marginTop: "0.25rem",
    },
    entryHeader: {
        marginBottom: "0.25rem",
    },
    entryText: {
        paddingLeft: "2rem",
    },
    textarea: {
        flex: 1,
    },
    feedList: {
        width: "100%",
        minHeight: "4rem",
    },
    feedItems: {
        display: "flex",
        flexDirection: "column",
        width: "100%",
    },
    replyButton: {
        marginTop: "0.25rem",
    },
})

// ─── type label ─────────────────────────────────────────────────────────────

function TypeLabel({ type }: { type: string }) {
    const intl = useIntl()
    return <span>{intl.formatMessage({ id: `feeds_type_${type.toLowerCase()}` })}</span>
}

// ─── shared helper ──────────────────────────────────────────────────────────

function parseFeedType(e: Ui5CustomEvent<SelectDomRef, SelectChangeEventDetail>): FeedType | null {
    const val = e.detail.selectedOption.getAttribute("value")
    return val && (FEED_TYPES as readonly string[]).includes(val) ? (val as FeedType) : null
}

// ─── single entry (recursive for threading) ────────────────────────────────

interface EntryProps {
    entry: FeedEntry
    allFeeds: FeedEntry[]
    depth: number
    onReply: (parentId: string, text: string, type: FeedType) => Promise<void>
}

function FeedEntryItem({ entry, allFeeds, depth, onReply }: EntryProps) {
    const intl = useIntl()
    const classes = useStyles()
    const loading = useFeedStore((s) => s.loading)
    const [showReply, setShowReply] = useState(false)
    const [replyText, setReplyText] = useState("")
    const [replyType, setReplyType] = useState<FeedType>("COMMENT")

    const replies = allFeeds.filter((f) => f.parentId === entry.id)

    const handleSendReply = async () => {
        if (!replyText.trim()) return
        await onReply(entry.id, replyText, replyType)
        setReplyText("")
        setShowReply(false)
    }

    const handleTypeChange = (e: Ui5CustomEvent<SelectDomRef, SelectChangeEventDetail>) => {
        const type = parseFeedType(e)
        if (type) setReplyType(type)
    }

    const cardClass = depth === 0 ? classes.entryCard : classes.replyCard

    return (
        <div className={cardClass}>
            <FlexBox gap="0.5rem" alignItems="Center" className={classes.entryHeader}>
                <Avatar size="XS" icon="person-placeholder" />
                <span className={classes.typeTag}>
                    <TypeLabel type={entry.type} />
                </span>
                <Text className={classes.meta}>
                    {entry.userNm} &middot;{" "}
                    {formatDate(entry.ts, getLanguage())}
                    {entry.ts
                        ? ", " +
                        new Date(entry.ts).toLocaleTimeString(getLanguage(), {
                            hour: "2-digit",
                            minute: "2-digit",
                        })
                        : ""}
                </Text>
            </FlexBox>

            <Text className={classes.entryText}>{entry.text}</Text>

            <Button
                design="Transparent"
                icon="comment"
                className={classes.replyButton}
                onClick={() => setShowReply((v) => !v)}
            >
                {intl.formatMessage({ id: "feeds_reply" })}
            </Button>

            {showReply && (
                <div className={`${classes.inputRow} ${classes.replyInputRow}`}>
                    <Select onChange={handleTypeChange}>
                        {FEED_TYPES.map((t) => (
                            <Option key={t} value={t} selected={t === replyType}>
                                {intl.formatMessage({ id: `feeds_type_${t.toLowerCase()}` })}
                            </Option>
                        ))}
                    </Select>
                    <TextArea
                        className={classes.textarea}
                        rows={1}
                        placeholder={intl.formatMessage({ id: "feeds_placeholder" })}
                        value={replyText}
                        onInput={(e) => setReplyText((e.target as HTMLTextAreaElement).value)}
                    />
                    <Button
                        design="Emphasized"
                        icon="paper-plane"
                        disabled={!replyText.trim() || loading}
                        onClick={handleSendReply}
                    />
                </div>
            )}

            {replies.map((reply) => (
                <FeedEntryItem
                    key={reply.id}
                    entry={reply}
                    allFeeds={allFeeds}
                    depth={depth + 1}
                    onReply={onReply}
                />
            ))}
        </div>
    )
}

// ─── tab component ──────────────────────────────────────────────────────────

interface TabFeedsProps {
    selectedProcess?: Process
    selected?: boolean
}

export default function TabFeeds({ selectedProcess, selected }: TabFeedsProps) {
    const intl = useIntl()
    const messages = useMessages()
    const classes = useStyles()

    const feeds = useFeedStore((s) => s.feeds)
    const loadFeeds = useFeedStore((s) => s.loadFeeds)
    const addFeed = useFeedStore((s) => s.addFeed)
    const clearFeeds = useFeedStore((s) => s.clearFeeds)
    const loading = useFeedStore((s) => s.loading)

    const [newText, setNewText] = useState("")
    const [newType, setNewType] = useState<FeedType>("COMMENT")

    // Load when tab becomes active or the selected process changes.
    useEffect(() => {
        if (!selected) return
        if (selectedProcess?.id) {
            loadFeeds(messages, selectedProcess.id)
        } else {
            clearFeeds()
        }
    }, [selected, selectedProcess?.id])

    const handleTypeChange = (e: Ui5CustomEvent<SelectDomRef, SelectChangeEventDetail>) => {
        const type = parseFeedType(e)
        if (type) setNewType(type)
    }

    const handleSend = async () => {
        if (!newText.trim() || !selectedProcess?.id) return
        await addFeed(messages, selectedProcess.id, newText, newType)
        setNewText("")
    }

    const handleReply = async (parentId: string, text: string, type: FeedType) => {
        if (!selectedProcess?.id) return
        await addFeed(messages, selectedProcess.id, text, type, parentId)
    }

    const topLevelFeeds = feeds.filter((f) => f.parentId === null)

    return (
        <Tab icon="discussion-2" text={intl.formatMessage({ id: "tab_feeds" })} selected={selected} data-tab-id="feeds">
            <div className={classes.container}>
                <div className={classes.inputRow}>
                    <Select onChange={handleTypeChange}>

                        {FEED_TYPES.map((t) => (
                            <Option key={t} value={t} selected={t === newType}>
                                {intl.formatMessage({ id: `feeds_type_${t.toLowerCase()}` })}
                            </Option>
                        ))}
                    </Select>
                    <TextArea
                        className={classes.textarea}
                        rows={1}
                        placeholder={intl.formatMessage({ id: "feeds_placeholder" })}
                        value={newText}
                        onInput={(e) => setNewText((e.target as HTMLTextAreaElement).value)}
                    />
                    <Button
                        design="Emphasized"
                        icon="paper-plane"
                        disabled={!newText.trim() || loading}
                        onClick={handleSend}
                    >
                        {intl.formatMessage({ id: "feeds_send" })}
                    </Button>
                </div>

                <BusyIndicator active={loading} className={classes.feedList}>
                    <div className={classes.feedItems}>
                        {topLevelFeeds.length === 0 ? (
                            <Text className={classes.noEntries}>
                                {intl.formatMessage({ id: "feeds_no_entries" })}
                            </Text>
                        ) : (
                            topLevelFeeds.map((entry) => (
                                <FeedEntryItem
                                    key={entry.id}
                                    entry={entry}
                                    allFeeds={feeds}
                                    depth={0}
                                    onReply={handleReply}
                                />
                            ))
                        )}
                    </div>
                </BusyIndicator>
            </div>
        </Tab>
    )
}
