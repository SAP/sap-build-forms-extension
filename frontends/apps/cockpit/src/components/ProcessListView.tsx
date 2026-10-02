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

import { formatDate, getLanguage, Margin, useMessages } from "commons"

import { PAGE_SIZES, Process, PROCESS_STATES, useProcessStore } from "../state/processes"
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

export default function () {
    const intl = useIntl()
    const messages = useMessages()
    const processes = useProcessStore((state) => state.processes)
    const totalCount = useProcessStore((state) => state.totalCount)
    const page = useProcessStore((state) => state.page)
    const pageSize = useProcessStore((state) => state.pageSize)
    const setPage = useProcessStore((state) => state.setPage)
    const setPageSize = useProcessStore((state) => state.setPageSize)
    const setSelectedProcess = useVisualStore((state) => state.setSelectedProcess)
    const setView = useVisualStore((state) => state.setView)
    const classes = useStyles()

    const lastPage = Math.max(Math.ceil(totalCount / pageSize), 1)

    const handlePageSizeChange = (
        evt: Ui5CustomEvent<SegmentedButtonDomRef, SegmentedButtonSelectionChangeEventDetail>,
    ) => {
        for (const item of evt.detail.selectedItems) {
            if (item.dataset["key"]) {
                setPageSize(messages, parseInt(item.dataset.key!))
                break
            }
        }
    }

    const handlePageInputChange = (evt: any) => {
        const pageNum = parseInt(evt.target.value.trim())
        if (isNaN(pageNum)) return
        const clamped = Math.max(1, Math.min(pageNum, lastPage))
        if (pageNum !== clamped) evt.target.value = "" + clamped
        setPage(messages, clamped)
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
                        <TableHeaderCell minWidth="10em">
                            {intl.formatMessage({ id: "label_status" })}
                        </TableHeaderCell>
                        <TableHeaderCell minWidth="30em">
                            {intl.formatMessage({ id: "label_additional_information" })}
                        </TableHeaderCell>
                        <TableHeaderCell minWidth="20em">
                            {intl.formatMessage({ id: "label_started_by" })}
                        </TableHeaderCell>
                        <TableHeaderCell minWidth="10em">
                            {intl.formatMessage({ id: "label_started_at" })}
                        </TableHeaderCell>
                    </TableHeaderRow>
                }
                noDataText={intl.formatMessage({ id: "common_no_data" })}
                rowActionCount={3}
            >
                {processes.map((process) => (
                    <TableRow
                        actions={
                            <>
                                <TableRowAction
                                    icon="form"
                                    text={intl.formatMessage({ id: "show_form" })}
                                    onClick={() => handleShowForm(process)}
                                />
                                <TableRowAction
                                    icon="show"
                                    text={intl.formatMessage({ id: "show_details" })}
                                    onClick={() => handleShowDetails(process)}
                                />
                            </>
                        }
                        key={process.id}
                        id={process.id}
                    >
                        <TableCell>
                            <FlexBox direction="Column">
                                <Text style={{ fontWeight: "bold" }}>{process.scenarioName}</Text>
                                <Text>{process.description}</Text>
                            </FlexBox>
                        </TableCell>
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
                        <TableCell>{process.additionalInformation}</TableCell>
                        <TableCell>{process.startedBy}</TableCell>
                        <TableCell>{formatDate(process.startedAt, getLanguage())}{process.startedAt ? ", " + new Date(process.startedAt).toLocaleTimeString(getLanguage(), { hour: "2-digit", minute: "2-digit" }) : ""}</TableCell>
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
                            onClick={() => setPage(messages, 1)}
                        />
                        <Button
                            icon="media-reverse"
                            design="Transparent"
                            disabled={page === 1}
                            onClick={() => setPage(messages, page - 1)}
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
                            onClick={() => setPage(messages, page + 1)}
                        />
                        <Button
                            icon="media-forward"
                            design="Transparent"
                            disabled={lastPage === 1}
                            onClick={() => setPage(messages, lastPage)}
                        />
                    </>
                }
            />
        </div>
    )
}
