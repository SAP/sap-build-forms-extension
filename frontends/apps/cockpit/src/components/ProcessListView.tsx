import { useEffect, useState } from "react"
import { useIntl } from "react-intl"
import { createUseStyles } from "react-jss"

import {
    Bar,
    Button,
    FlexBox,
    Icon,
    Input,
    SegmentedButton,
    SegmentedButtonDomRef,
    SegmentedButtonItem,
    Table,
    TableCell,
    TableHeaderCell,
    TableHeaderRow,
    TableRow,
    TableRowAction,
    Tag,
    Text,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import { SegmentedButtonSelectionChangeEventDetail } from "@ui5/webcomponents/dist/SegmentedButton"

import "@ui5/webcomponents-icons/dist/media-rewind.js"
import "@ui5/webcomponents-icons/dist/media-reverse.js"
import "@ui5/webcomponents-icons/dist/media-play.js"
import "@ui5/webcomponents-icons/dist/media-forward.js"

import { formatDate, getLanguage, Margin } from "commons"

import { Process, PROCESS_STATES, useProcessStore } from "../state/processes"
import { useVisualStore } from "../state/visual"

const useStyles = createUseStyles({
    tagAccent1: {
        "&::part(root)": {
            backgroundColor: "var(--sapAccentBackgroundColor1)",
            borderWidth: 0,
        },
    },
    tagAccent2: {
        "&::part(root)": {
            backgroundColor: "var(--sapAccentBackgroundColor2)",
            borderWidth: 0,
        },
    },
    tagAccent3: {
        "&::part(root)": {
            backgroundColor: "var(--sapAccentBackgroundColor3)",
            borderWidth: 0,
        },
    },
    tagAccent4: {
        "&::part(root)": {
            backgroundColor: "var(--sapAccentBackgroundColor4)",
            borderWidth: 0,
        },
    },
    tagAccent5: {
        "&::part(root)": {
            backgroundColor: "var(--sapAccentBackgroundColor5)",
            borderWidth: 0,
        },
    },
    tagAccent6: {
        "&::part(root)": {
            backgroundColor: "var(--sapAccentBackgroundColor6)",
            borderWidth: 0,
        },
    },
    tagAccent7: {
        "&::part(root)": {
            backgroundColor: "var(--sapAccentBackgroundColor7)",
            borderWidth: 0,
        },
    },
    tagAccent8: {
        "&::part(root)": {
            backgroundColor: "var(--sapAccentBackgroundColor8)",
            borderWidth: 0,
        },
    },
    tagAccent9: {
        "&::part(root)": {
            backgroundColor: "var(--sapAccentBackgroundColor9)",
            borderWidth: 0,
        },
    },
    tagAccent10: {
        "&::part(root)": {
            backgroundColor: "var(--sapAccentBackgroundColor10)",
            borderWidth: 0,
        },
    },
})

const PAGE_SIZES = [10, 25, 50, 100]

export default function () {
    const intl = useIntl()
    const processes = useProcessStore((state) => state.processes)
    const setSelectedProcess = useVisualStore((state) => state.setSelectedProcess)
    const setView = useVisualStore((state) => state.setView)
    const classes = useStyles()

    const [page, setPage] = useState(1)
    const [pageSize, setPageSize] = useState(PAGE_SIZES[0])

    useEffect(() => { setPage(1) }, [processes])

    const lastPage = Math.max(Math.ceil(processes.length / pageSize), 1)
    const pageItems = processes.slice((page - 1) * pageSize, page * pageSize)

    const handlePageSizeChange = (
        evt: Ui5CustomEvent<SegmentedButtonDomRef, SegmentedButtonSelectionChangeEventDetail>,
    ) => {
        for (const item of evt.detail.selectedItems) {
            if (item.dataset["key"]) {
                setPageSize(parseInt(item.dataset.key!))
                setPage(1)
                break
            }
        }
    }

    const handlePageInputChange = (evt: any) => {
        const pageNum = parseInt(evt.target.value.trim())
        if (isNaN(pageNum)) return
        const clamped = Math.max(1, Math.min(pageNum, lastPage))
        if (pageNum !== clamped) evt.target.value = "" + clamped
        setPage(clamped)
    }

    const handleCancel = (process: Process) => {
        setSelectedProcess(process)
        console.log("Cancel", process)
    }

    const handleShowForm = (process: Process) => {
        setSelectedProcess(process)
        setView("form")
    }

    const handleShowDetails = (process: Process) => {
        setSelectedProcess(process)
        setView("details")
    }

    return (
        <div>
            <Table
                headerRow={
                    <TableHeaderRow>
                        <TableHeaderCell width="20em">
                            {intl.formatMessage({ id: "label_description" })}
                        </TableHeaderCell>
                        <TableHeaderCell width="10em">
                            {intl.formatMessage({ id: "label_functional_id" })}
                        </TableHeaderCell>
                        <TableHeaderCell minWidth="30em">
                            {intl.formatMessage({ id: "label_status" })}
                        </TableHeaderCell>
                        <TableHeaderCell minWidth="20em">
                            {intl.formatMessage({ id: "label_started_by" })}
                        </TableHeaderCell>
                        <TableHeaderCell minWidth="10em">
                            {intl.formatMessage({ id: "label_started_at" })}
                        </TableHeaderCell>
                        <TableHeaderCell minWidth="10em">
                            {intl.formatMessage({ id: "label_finished_at" })}
                        </TableHeaderCell>
                    </TableHeaderRow>
                }
                noDataText={intl.formatMessage({ id: "common_no_data" })}
                rowActionCount={3}
            >
                {pageItems.map((process) => (
                    <TableRow
                        actions={
                            <>
                                {process.cancelable && (
                                    <TableRowAction
                                        icon="sys-cancel"
                                        text={intl.formatMessage({ id: "common_cancel" })}
                                        onClick={() => handleCancel(process)}
                                    />
                                )}
                                <TableRowAction
                                    icon="form"
                                    text={intl.formatMessage({ id: "common_show_form" })}
                                    onClick={() => handleShowForm(process)}
                                />
                                <TableRowAction
                                    icon="show"
                                    text={intl.formatMessage({ id: "common_show" })}
                                    onClick={() => handleShowDetails(process)}
                                />
                            </>
                        }
                        key={process.id}
                        id={process.id}
                    >
                        <TableCell>{process.description}</TableCell>
                        <TableCell>{process.functionalId}</TableCell>
                        <TableCell>
                            <Tag
                                className={
                                    classes[
                                        ("tagAccent" +
                                            PROCESS_STATES.find((obj) => obj.id === process.state)
                                                ?.color) as keyof typeof classes
                                    ]
                                }
                            >
                                <>
                                    <span slot="icon"></span>
                                    <FlexBox alignItems="Center" justifyContent="Center">
                                        <Icon
                                            name={
                                                PROCESS_STATES.find((obj) => obj.id === process.state)
                                                    ?.icon || "question-mark"
                                            }
                                            style={{
                                                color: `var(--sapAccentColor${
                                                    PROCESS_STATES.find((obj) => obj.id === process.state)?.color
                                                })`,
                                            }}
                                        />
                                        <Text
                                            style={{
                                                color: `var(--sapAccentColor${
                                                    PROCESS_STATES.find((obj) => obj.id === process.state)?.color
                                                })`,
                                                fontWeight: "bold",
                                                margin: Margin.MEDIUM,
                                            }}
                                        >
                                            {process.detailState}
                                        </Text>
                                    </FlexBox>
                                </>
                            </Tag>
                        </TableCell>
                        <TableCell>{process.startedBy}</TableCell>
                        <TableCell>{formatDate(process.startedAt, getLanguage())}</TableCell>
                        <TableCell>{formatDate(process.finishedAt, getLanguage())}</TableCell>
                    </TableRow>
                ))}
            </Table>
            <Bar
                startContent={
                    <SegmentedButton selectionMode="Single" onSelectionChange={handlePageSizeChange}>
                        {PAGE_SIZES.map((size) => (
                            <SegmentedButtonItem
                                key={size}
                                data-key={size}
                                selected={size === pageSize}
                            >
                                {"" + size}
                            </SegmentedButtonItem>
                        ))}
                    </SegmentedButton>
                }
                endContent={
                    <>
                        <Button
                            icon="media-rewind"
                            design="Transparent"
                            disabled={lastPage === 1}
                            onClick={() => setPage(1)}
                        />
                        <Button
                            icon="media-reverse"
                            design="Transparent"
                            disabled={page === 1}
                            onClick={() => setPage((p) => p - 1)}
                        />
                        <Input
                            value={"" + page}
                            style={{ width: "3em" }}
                            onChange={handlePageInputChange}
                        />
                        <Text>&nbsp;/&nbsp;{lastPage}</Text>
                        <Button
                            icon="media-play"
                            design="Transparent"
                            disabled={page === lastPage}
                            onClick={() => setPage((p) => p + 1)}
                        />
                        <Button
                            icon="media-forward"
                            design="Transparent"
                            disabled={lastPage === 1}
                            onClick={() => setPage(lastPage)}
                        />
                    </>
                }
            />
        </div>
    )
}
