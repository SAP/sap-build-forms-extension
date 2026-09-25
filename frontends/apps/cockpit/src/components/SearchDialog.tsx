import { useState } from "react"
import ReactDOM from "react-dom"
import { useIntl } from "react-intl"

import {
    Bar,
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
    TableRowAction,
    Text,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"

import "@ui5/webcomponents-icons/dist/media-reverse.js"
import "@ui5/webcomponents-icons/dist/media-play.js"
import "@ui5/webcomponents-icons/dist/accept.js"

export interface SearchDialogProps {
    title: string
    suggestions: string[]
    onSelect: (v: string) => void
    onClose: () => void
}

const PAGE_SIZE = 20

export default function SearchDialog({ title, suggestions, onSelect, onClose }: SearchDialogProps) {
    const intl = useIntl()
    const [searchInput, setSearchInput] = useState("")
    const [search, setSearch] = useState("")
    const [page, setPage] = useState(1)
    const [selectedValue, setSelectedValue] = useState("")

    const allFiltered = [...new Set(suggestions.filter(Boolean))]
        .filter((s) => s.toLowerCase().includes(search.toLowerCase()))
        .sort()

    const lastPage = Math.max(Math.ceil(allFiltered.length / PAGE_SIZE), 1)
    const pageItems = allFiltered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE)

    const handleSearch = () => {
        setSearch(searchInput)
        setPage(1)
        setSelectedValue("")
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
                                disabled={!selectedValue}
                                onClick={() => { onSelect(selectedValue); onClose() }}
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
                        onKeyDown={(e) => { if (e.key === "Enter") handleSearch() }}
                    />
                    <Button onClick={handleSearch}>
                        {intl.formatMessage({ id: "button_search" })}
                    </Button>
                </FlexBox>
                <Table
                    headerRow={
                        <TableHeaderRow sticky>
                            <TableHeaderCell>{title}</TableHeaderCell>
                        </TableHeaderRow>
                    }
                    noDataText={intl.formatMessage({ id: "common_no_data" })}
                    overflowMode="Scroll"
                    rowActionCount={1}
                    style={{ width: "100%" }}
                >
                    {pageItems.map((s) => (
                        <TableRow
                            key={s}
                            row-key={s}
                            interactive={false}
                            style={{
                                backgroundColor:
                                    s === selectedValue
                                        ? "var(--sapList_SelectionBackgroundColor)"
                                        : undefined,
                            }}
                            onClick={() => setSelectedValue(s)}
                        >
                            <TableCell>{s}</TableCell>
                        </TableRow>
                    ))}
                </Table>
                <Bar
                    endContent={
                        <>
                            <Button
                                icon="media-reverse"
                                design="Transparent"
                                disabled={page === 1}
                                onClick={() => { setPage((p) => p - 1); setSelectedValue("") }}
                            />
                            <Text>{page}&nbsp;/&nbsp;{lastPage}</Text>
                            <Button
                                icon="media-play"
                                design="Transparent"
                                disabled={page === lastPage}
                                onClick={() => { setPage((p) => p + 1); setSelectedValue("") }}
                            />
                        </>
                    }
                />
            </FlexBox>
        </Dialog>,
        document.body,
    )
}
