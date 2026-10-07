import { Form, FormItem, Label, Tab, Text } from "@ui5/webcomponents-react"
import { useIntl } from "react-intl"
import { createUseStyles } from "react-jss"
import { Process } from "../../state/processes"

const useStyles = createUseStyles({
    formTextBox: {
        paddingBlock: 6,
        wordBreak: "break-all",
    },
    formText: {
        marginLeft: "2px",
    },
})

interface TabDetailsProps {
    selectedProcess: Process | undefined
    selected?: boolean
}

export default function TabDetails(props: TabDetailsProps) {
    const intl = useIntl()
    const classes = useStyles()

    function formatDate(date: Date) {
        function prepend0(number: Number) {
            return number.toString().padStart(2, "0")
        }
        return (
            prepend0(date.getDate()) +
            "." +
            prepend0(date.getMonth() + 1) +
            "." +
            date.getFullYear() +
            " " +
            date.getHours() +
            ":" +
            prepend0(date.getMinutes())
        )
    }

    return (
        <Tab icon="detail-view" text={intl.formatMessage({ id: "tab_details" })} selected={props.selected} data-tab-id="details">
            <Form
                layout="S1 M1 L1 XL1"
                labelSpan="S10 M4 L2 XL2"
                style={{
                    alignItems: "center",
                    padding: 30,
                }}
            >
                <>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "label_id" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.id}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "label_ref_id" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.refId}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "label_status" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.state}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "label_detail_state" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.detailState}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "label_started_by" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.startedBy}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "label_started_at" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            {props.selectedProcess?.startedAt && (
                                <Text className={classes.formText}>
                                    {formatDate(new Date(props.selectedProcess.startedAt))}
                                </Text>
                            )}
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "label_finished_at" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            {props.selectedProcess?.finishedAt && (
                                <Text className={classes.formText}>
                                    {formatDate(new Date(props.selectedProcess.finishedAt))}
                                </Text>
                            )}
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "label_scenario" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.scenarioName}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "label_scenario_version" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.scenarioVersion}
                            </Text>
                        </div>
                    </FormItem>
                </>
            </Form>
        </Tab>
    )
}
