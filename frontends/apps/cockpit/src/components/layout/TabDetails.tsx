import { Form, FormItem, Label, Link, Tab, Text } from "@ui5/webcomponents-react"
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
}

export default function (props: TabDetailsProps) {
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
        <Tab icon="detail-view" text={intl.formatMessage({ id: "tab_details" })} selected>
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
                        labelContent={<Label>{intl.formatMessage({ id: "process_id" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.processId}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "process_state" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            {props.selectedProcess && (
                                <Text className={classes.formText}>
                                    {intl.formatMessage({
                                        id: "process_state_" + props.selectedProcess?.state,
                                    })}
                                </Text>
                            )}
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "process_flow" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            <Link
                                design="Default"
                                className={classes.formText}
                                onClick={function _a() {}}
                            >
                                {intl.formatMessage({ id: "click_to_open" })}
                            </Link>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={
                            <Label>{intl.formatMessage({ id: "technical_bo_name" })}</Label>
                        }
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.technicalBoName}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "bo_version" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.boVersion}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={
                            <Label>
                                {intl.formatMessage({ id: "bpm_process_instance_id_short" })}
                            </Label>
                        }
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.bpmProcessInstanceIdShort}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "started_by" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            <Text className={classes.formText}>
                                {props.selectedProcess?.startedBy}
                            </Text>
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "started_on" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            {props.selectedProcess && (
                                <Text className={classes.formText}>
                                    {formatDate(new Date(props.selectedProcess.startedOn))}
                                </Text>
                            )}
                        </div>
                    </FormItem>
                    <FormItem
                        labelContent={<Label>{intl.formatMessage({ id: "ended_on" })}</Label>}
                    >
                        <div className={classes.formTextBox}>
                            {props.selectedProcess && props.selectedProcess.endedOn && (
                                <Text className={classes.formText}>
                                    {formatDate(new Date(props.selectedProcess.endedOn))}
                                </Text>
                            )}
                        </div>
                    </FormItem>
                </>
            </Form>
        </Tab>
    )
}
