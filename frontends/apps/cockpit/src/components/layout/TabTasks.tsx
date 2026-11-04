import { Form, FormItem, Label, ObjectStatus, Tab, Text, Title } from "@ui5/webcomponents-react"
import { useIntl } from "react-intl"
import { createUseStyles } from "react-jss"
import { TaskInstance } from "../../state/processes"

const useStyles = createUseStyles({
    container: {
        padding: 30,
        display: "flex",
        flexDirection: "column",
        gap: 24,
    },
    taskCard: {
        borderBottom: "1px solid var(--sapList_BorderColor, #e0e0e0)",
        paddingBottom: 24,
        "&:last-child": {
            borderBottom: "none",
        },
    },
    taskHeader: {
        marginBottom: 12,
    },
    formText: {
        marginLeft: "2px",
    },
    formTextBox: {
        paddingBlock: 6,
        wordBreak: "break-all",
    },
})

const STATUS_STATE: Record<string, "Positive" | "Critical" | "Negative" | "None" | "Information"> = {
    READY: "Positive",
    RESERVED: "Critical",
    COMPLETED: "None",
    CANCELED: "Negative",
}

interface Props {
    tasks: TaskInstance[]
    selected?: boolean
}

export default function TabTasks(props: Props) {
    const intl = useIntl()
    const classes = useStyles()

    function formatDate(dateStr: string | undefined): string {
        if (!dateStr) return ""
        const d = new Date(dateStr)
        return (
            d.toLocaleDateString() +
            " " +
            d.toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" })
        )
    }

    function label(id: string) {
        return <Label>{intl.formatMessage({ id })}</Label>
    }

    function field(value: string | undefined) {
        return (
            <div className={classes.formTextBox}>
                <Text className={classes.formText}>{value ?? ""}</Text>
            </div>
        )
    }

    return (
        <Tab icon="approvals" text={intl.formatMessage({ id: "tab_tasks" })} selected={props.selected} data-tab-id="tasks">
            <div className={classes.container}>
                {props.tasks.length === 0 ? (
                    <Text>{intl.formatMessage({ id: "tasks_no_entries" })}</Text>
                ) : (
                    props.tasks.map((task) => (
                        <div key={task.id} className={classes.taskCard}>
                            <Title level="H5" className={classes.taskHeader}>
                                {task.definitionId ?? task.activityId ?? task.id}
                            </Title>
                            <Form
                                layout="S1 M2 L2 XL2"
                                labelSpan="S10 M4 L4 XL4"
                            >
                                {/* Left column */}
                                <FormItem labelContent={label("tasks_label_name")}>
                                    {field(task.subject)}
                                </FormItem>
                                <FormItem labelContent={label("tasks_label_created_at")}>
                                    {field(formatDate(task.createdAt))}
                                </FormItem>
                                <FormItem labelContent={label("tasks_label_completed_at")}>
                                    {field(formatDate(task.completedAt))}
                                </FormItem>
                                <FormItem labelContent={label("tasks_label_due_date")}>
                                    {field(formatDate(task.dueDate))}
                                </FormItem>
                                <FormItem labelContent={label("tasks_label_processor")}>
                                    {field(task.processor)}
                                </FormItem>
                                <FormItem labelContent={label("tasks_label_potential_owners")}>
                                    {field(task.recipientUsers?.join(", "))}
                                </FormItem>
                                <FormItem labelContent={label("tasks_label_claimed_at")}>
                                    {field(formatDate(task.claimedAt))}
                                </FormItem>
                                <FormItem labelContent={label("tasks_label_id")}>
                                    {field(task.id)}
                                </FormItem>

                                {/* Right column */}
                                <FormItem labelContent={label("tasks_label_priority")}>
                                    {field(task.priority)}
                                </FormItem>
                                <FormItem labelContent={label("tasks_label_status")}>
                                    <div className={classes.formTextBox}>
                                        <ObjectStatus state={STATUS_STATE[task.status ?? ""] ?? "None"}>
                                            {task.status
                                                ? intl.formatMessage({
                                                      id: `tasks_status_${task.status}`,
                                                      defaultMessage: task.status,
                                                  })
                                                : ""}
                                        </ObjectStatus>
                                    </div>
                                </FormItem>
                            </Form>
                        </div>
                    ))
                )}
            </div>
        </Tab>
    )
}
