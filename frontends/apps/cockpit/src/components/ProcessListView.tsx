import { useIntl } from "react-intl"
import { createUseStyles } from "react-jss"

import {
    FlexBox,
    Icon,
    Table,
    TableCell,
    TableHeaderCell,
    TableHeaderRow,
    TableRow,
    TableRowAction,
    Tag,
    Text,
} from "@ui5/webcomponents-react"

import { formatDate, getLanguage, Margin } from "commons"

import { Process, PROCESS_STATES, useProcessStore } from "../state/processes"
import { useVisualStore } from "../state/visual"
import { set } from "date-fns"
import { use } from "react"

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
    const processes = useProcessStore((state) => state.processes)
    const setSelectedProcess = useVisualStore((state) => state.setSelectedProcess)
    const setView = useVisualStore((state) => state.setView)
    const classes = useStyles()

    const handleCancel = (process: Process) => {
        setSelectedProcess(process)
        console.log("Cancel", process)
    }

    const handleShowForm = (process: Process) => {
        // console.log("Show form", process)
        setSelectedProcess(process)
        setView("form")
    }

    const handleShowDetails = (process: Process) => {
        // console.log("Show details", process)
        setSelectedProcess(process)
        setView("details")
    }

    return (
        <Table
            headerRow={
                <TableHeaderRow>
                    <TableHeaderCell width="20em">
                        {intl.formatMessage({ id: "label_description" })}
                    </TableHeaderCell>
                    <TableHeaderCell width="10em">
                        {intl.formatMessage({
                            id: "label_functional_id",
                        })}
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
            rowActionCount={3}
        >
            {processes.map((process) => (
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
                                                PROCESS_STATES.find(
                                                    (obj) => obj.id === process.state,
                                                )?.color
                                            })`,
                                        }}
                                    />

                                    <Text
                                        style={{
                                            color: `var(--sapAccentColor${
                                                PROCESS_STATES.find(
                                                    (obj) => obj.id === process.state,
                                                )?.color
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
    )
}
