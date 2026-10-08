import { useState } from "react"
import ReactDOM from "react-dom"
import { useIntl } from "react-intl"

import {
    Bar,
    BusyIndicator,
    Button,
    Dialog,
    FlexBox,
    Input,
    InputDomRef,
    Table,
    TableCell,
    TableHeaderCell,
    TableHeaderRow,
    TableRow,
    TableSelectionSingle,
    TableSelectionSingleDomRef,
    Text,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"

import "@ui5/webcomponents-icons/dist/media-reverse.js"
import "@ui5/webcomponents-icons/dist/media-play.js"

export interface SearchDialogProps {
    title: string
    onSearch: (search: string) => Promise<string[]>
    onSelect: (values: string[]) => void
    onClose: () => void
}

const PAGE_SIZE = 20

export default function SearchDialog({ title, onSearch, onSelect, onClose }: SearchDialogProps) {
    const intl = useIntl()

    const [searchInput, setSearchInput] = useState("")
    const [suggestions, setSuggestions] = useState<string[]>([])
    const [loading, setLoading] = useState(false)
    const [page, setPage] = useState(1)
    const [selected, setSelected] = useState<number | null>(null)

    async function triggerSearch(term: string) {
        setLoading(true)
        setPage(1)
        setSelected(null)
        const results = await onSearch(term)
        setSuggestions(results)
        setLoading(false)
    }

    const lastPage = Math.max(Math.ceil(suggestions.length / PAGE_SIZE), 1)
    const pageItems = suggestions.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE)
    const selectedStr = selected !== null ? String(selected) : ""

    function handleConfirm() {
        const value = selected !== null ? suggestions[selected] : undefined
        if (value) onSelect([value])
        onClose()
    }

    return ReactDOM.createPortal(
        <Dialog
            open={true}
            headerText={title}
            footer={
                <Bar
                    design="Footer"
                    endContent={
                        <>
                            <Button
                                design="Emphasized"
                                disabled={selected === null}
                                onClick={handleConfirm}
                            >
                                {intl.formatMessage({ id: "button_select" })}
                            </Button>
                            <Button onClick={onClose}>
                                {intl.formatMessage({ id: "button_close" })}
                            </Button>
                        </>
                    }
                />
            }
            onClose={onClose}
            style={{ width: "50vw", height: "50vh" }}
        >
            <FlexBox direction="Column" style={{ padding: "0.5rem 1rem 0", gap: "0.5rem", height: "100%" }}>
                <FlexBox style={{ gap: "0.5rem" }}>
                    <Input
                        style={{ flex: 1 }}
                        placeholder={intl.formatMessage({ id: "button_search" })}
                        value={searchInput}
                        onInput={(e: Ui5CustomEvent<InputDomRef>) => setSearchInput(e.target.value)}
                        onKeyDown={(e) => { if (e.key === "Enter") triggerSearch(searchInput) }}
                    />
                    <Button onClick={() => triggerSearch(searchInput)}>
                        {intl.formatMessage({ id: "button_search" })}
                    </Button>
                </FlexBox>
                <BusyIndicator active={loading} style={{ width: "100%" }}>
                    <Table
                        features={
                            <TableSelectionSingle
                                selected={selectedStr}
                                onChange={(e: Ui5CustomEvent<TableSelectionSingleDomRef>) =>
                                    setSelected(Number(e.target.selected))
                                }
                            />
                        }
                        headerRow={
                            <TableHeaderRow sticky>
                                <TableHeaderCell>{title}</TableHeaderCell>
                            </TableHeaderRow>
                        }
                        noDataText={intl.formatMessage({ id: "common_no_data" })}
                        overflowMode="Scroll"
                        style={{ width: "100%" }}
                    >
                        {pageItems.map((s, i) => {
                            const globalIdx = (page - 1) * PAGE_SIZE + i
                            return (
                                <TableRow key={globalIdx} row-key={String(globalIdx)}>
                                    <TableCell>{s}</TableCell>
                                </TableRow>
                            )
                        })}
                    </Table>
                </BusyIndicator>
                <Bar
                    endContent={
                        <>
                            <Button
                                icon="media-reverse"
                                design="Transparent"
                                disabled={page === 1}
                                onClick={() => setPage((p) => p - 1)}
                            />
                            <Text>{page}&nbsp;/&nbsp;{lastPage}</Text>
                            <Button
                                icon="media-play"
                                design="Transparent"
                                disabled={page === lastPage}
                                onClick={() => setPage((p) => p + 1)}
                            />
                        </>
                    }
                />
            </FlexBox>
        </Dialog>,
        document.body,
    )
}
