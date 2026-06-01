import { useState } from "react"

import { createUseStyles } from "react-jss"
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
        width: 400,
    },
})

export default function (props: DialogUploadFileProps) {
    const classes = useStyles()
    const [file, setFile] = useState<File | null>(null)
    const [override, setOverride] = useState<boolean>(true)
    const [useTechnicalName, setUseTechnicalName] = useState<boolean>(false)

    return (
        <Dialog
            className={classes.dialog}
            footer={
                <Bar
                    design="Footer"
                    className={classes.bar}
                    endContent={
                        <Button
                            onClick={function _a() {
                                props.setDialogUploadFileOpen(false)
                                setFile(null)
                                setOverride(true)
                                setUseTechnicalName(false)
                            }}
                        >
                            Close
                        </Button>
                    }
                >
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
                        Upload
                    </Button>
                </Bar>
            }
            headerText="Upload value help definition file"
            open={true}
        >
            <Form className={classes.form} layout="S1 M1 L1 XL1" labelSpan="S1 M1 L1 XL1">
                <FormItem labelContent={<Label>Select file</Label>}>
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
                        valueStateMessage={<span>Please choose file type xml</span>}
                        accept={"*.xml, *.hv"}
                        multiple={false}
                    >
                        <Button>Select xml file</Button>
                    </FileUploader>
                </FormItem>

                <FormItem labelContent={<Label>Handle existing entries</Label>}>
                    <Select
                        onChange={(e) => {
                            if (e.detail.selectedOption.innerText == "Override") {
                                setOverride(true)
                            } else {
                                setOverride(false)
                            }
                        }}
                    >
                        <Option key={"Override"} id="Override">
                            Override
                        </Option>
                        <Option key={"Skip"} id="Skip">
                            Skip
                        </Option>
                    </Select>
                </FormItem>

                <FormItem labelContent={<Label>Use technical Name</Label>}>
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
