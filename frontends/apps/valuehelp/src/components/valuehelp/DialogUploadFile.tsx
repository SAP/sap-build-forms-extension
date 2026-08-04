import { useState } from "react"

import { createUseStyles } from "react-jss"
import { useIntl } from "react-intl"
import {
    Bar,
    BusyIndicator,
    Button,
    Dialog,
    FileUploader,
    Form,
    FormItem,
    Label,
    Option,
    Select,
    Switch,
} from "@ui5/webcomponents-react"

/**
 * Dialog for uploading a value help definition file
 */
interface DialogUploadFileProps {
    dialogUploadFileOpen: boolean
    loading: boolean
    setDialogUploadFileOpen(o: boolean): void
    upload(file: File, override: boolean, useTechnicalName: boolean): void
}

const useStyles = createUseStyles({
    dialog: {
        paddingTop: 10,
        paddingInline: 3,
    },
    bar: {
        paddingBlock: 3,
    },
    button: {
        marginInline: 2,
    },
    form: {
        padding: 3,
        width: "100%",
    },
})

export default function (props: DialogUploadFileProps) {
    const classes = useStyles()
    const intl = useIntl()
    const [file, setFile] = useState<File | null>(null)
    const [override, setOverride] = useState<boolean>(true)
    const [useTechnicalName, setUseTechnicalName] = useState<boolean>(false)

    return (
        <Dialog
            className={classes.dialog}
            style={{ minWidth: "50%" }}
            footer={
                <Bar
                    design="Footer"
                    className={classes.bar}
                    endContent={
                        <div>
                            <Button
                                onClick={function _a() {
                                    props.setDialogUploadFileOpen(false)
                                    setFile(null)
                                    setOverride(true)
                                    setUseTechnicalName(false)
                                }}
                            >
                                {intl.formatMessage({ id: "btn_close" })}
                            </Button>
                            <Button
                                design="Emphasized"
                                className={classes.button}
                                disabled={
                                    !file ||
                                    !(
                                        file.type == "text/xml" ||
                                        file.name.endsWith(".hv") ||
                                        file.name.endsWith(".txt")
                                    )
                                }
                                onClick={async function _a() {
                                    if (
                                        file &&
                                        (file.type == "text/xml" ||
                                            file.name.endsWith(".hv") ||
                                            file.name.endsWith(".txt"))
                                    ) {
                                        props.upload(file!, override, useTechnicalName)
                                        setFile(null)
                                        setOverride(true)
                                        setUseTechnicalName(false)
                                    }
                                }}
                            >
                                {intl.formatMessage({ id: "btn_upload" })}
                            </Button>
                        </div>
                    }>
                </Bar>
            }
            headerText={intl.formatMessage({ id: "dlg_upload_title" })}
            open={props.dialogUploadFileOpen}
        >
            <Form className={classes.form} layout="S1 M1 L1 XL1" labelSpan="S4 M3 L2 XL2">
                <FormItem labelContent={<Label>{intl.formatMessage({ id: "dlg_upload_select_file" })}</Label>}>
                    <FileUploader
                        onChange={function _a(e) {
                            if (e.detail.files) {
                                setFile(e.detail.files[0])
                            }
                        }}
                        valueState={
                            file &&
                                !(
                                    file?.type == "text/xml" ||
                                    file.name.endsWith(".hv") ||
                                    file.name.endsWith(".txt")
                                )
                                ? "Negative"
                                : "None"
                        }
                        valueStateMessage={<span>{intl.formatMessage({ id: "dlg_upload_file_type_error" })}</span>}
                        accept={"*.xml, *.hv"}
                        multiple={false}
                    >
                        <Button>{intl.formatMessage({ id: "dlg_upload_select_button" })}</Button>
                    </FileUploader>
                </FormItem>

                <FormItem labelContent={<Label>{intl.formatMessage({ id: "dlg_upload_handle_existing" })}</Label>}>
                    <Select
                        onChange={(e) => {
                            setOverride(e.detail.selectedOption.id === "Override")
                        }}
                    >
                        <Option key={"Override"} id="Override">
                            {intl.formatMessage({ id: "dlg_upload_override" })}
                        </Option>
                        <Option key={"Skip"} id="Skip">
                            {intl.formatMessage({ id: "dlg_upload_skip" })}
                        </Option>
                    </Select>
                </FormItem>

                <FormItem labelContent={<Label>{intl.formatMessage({ id: "dlg_upload_use_technical_name" })}</Label>}>
                    <Switch
                        checked={useTechnicalName}
                        onChange={function _a() {
                            setUseTechnicalName(!useTechnicalName)
                        }}
                    />
                </FormItem>

                <FormItem>
                    <BusyIndicator active={props.loading} delay={1000} size="M" />
                </FormItem>
            </Form>
        </Dialog>
    )
}
