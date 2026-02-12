import { useEffect, useRef, useState } from "react"
import ReactDOM from "react-dom"

import { Controller, useForm } from "react-hook-form"
import { AxiosProgressEvent } from "axios"
import { useIntl } from "react-intl"

import {
    Bar,
    Button,
    Dialog,
    FileUploader,
    FileUploaderDomRef,
    FlexBox,
    Form,
    FormGroup,
    FormItem,
    Icon,
    Label,
    List,
    ListItemCustom,
    Option,
    Panel,
    ProgressIndicator,
    Select,
    Tag,
    Text,
    TextArea,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import { FileUploaderChangeEventDetail } from "@ui5/webcomponents/dist/FileUploader"

import { useMessages } from "commons"

import { ControlProps, getLabel } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { Attachment, FormService } from "../../features/sessions/forms"
import { downloadAttachment } from "../../features/sessions/sessionSlice"
import { Definition } from "../../features/sessions/definitions"
import { ValueName, ValuehelpsService } from "../../features/valuehelps/logic"
import { deleteAttachment, uploadAttachment } from "../../features/sessions/attachmentActions"

/**
 *
 */
interface FileData {
    file: File
    value: number
}

/**
 *
 * @param props
 * @returns
 */
function FilesControl(props: {
    rowId: string
    def: Definition
    showDelete: boolean
    registerFilesRef: React.MutableRefObject<((fileList: FileList | null) => void) | undefined>
    uploadRef: React.MutableRefObject<
        ((description?: string, category?: string) => void) | undefined
    >
    onProcessing: (status: boolean) => void
    setShowDialog: (value: boolean) => void
}) {
    const { def, onProcessing, registerFilesRef, rowId, setShowDialog, showDelete, uploadRef } =
        props
    const [files, setFiles] = useState<FileData[]>([])
    const [processing, setProcessing] = useState<boolean>(false)
    const dispatch = useAppDispatch()
    const messages = useMessages()
    let uploadIdx = -1

    /**
     *
     * @param fileList
     */
    registerFilesRef.current = (fileList: FileList | null) => {
        let f: FileData[] = []
        if (fileList && fileList.length > 0) {
            for (let i = 0; i < fileList.length; i++) {
                f.push({ file: fileList[i], value: 0 })
            }
            setFiles(f)
        }
    }

    /**
     *
     */
    uploadRef.current = (description?: string, category?: string) => {
        // console.log("Starting upload...")
        // files.forEach((file, i) => {
        //     console.log(file.file.name + ", type=" + file.file.type + ", size=" + file.file.size)
        // })

        const onProgress = (e: AxiosProgressEvent) => {
            // console.log("upload: " + JSON.stringify(e))

            let f: FileData[] = []
            files.map((item, i) => {
                if (i < uploadIdx) {
                    f.push({ ...item, value: 100 })
                } else if (i === uploadIdx) {
                    f.push({ ...item, value: e.progress! * 100 })
                } else {
                    f.push(item)
                }
            })
            setFiles(f)

            if (e.loaded == e.total) {
                if (uploadIdx + 1 < files.length) {
                    uploadIdx++
                    // console.log("uploading file: " + files[uploadIdx].file.name)
                    setTimeout(() => {
                        dispatch(
                            uploadAttachment({
                                file: files[0].file,
                                rowId,
                                key: def.key,
                                onProgress,
                                description,
                                category,
                                messages,
                            }),
                        )
                    }, 2000)
                } else {
                    // console.log("file upload is finished!")
                    setTimeout(() => setShowDialog(false), 2000)
                }
            }
        }

        if (files && files.length > 0) {
            setProcessing(true)
            onProcessing(true)
            uploadIdx = 0
            dispatch(
                uploadAttachment({
                    file: files[0].file,
                    rowId,
                    key: def.key,
                    onProgress,
                    description,
                    category,
                    messages,
                }),
            )
        }
    }

    /**
     *
     * @param i
     */
    const handleDelete = (idx: number) => {
        let f: FileData[] = []
        files.map((item, i) => {
            if (i !== idx) {
                f.push(item)
            }
        })
        setFiles(f)
    }

    return (
        <List selectionMode="None" style={{ width: "100%" }}>
            {files.map((file, i) => (
                <ListItemCustom key={i}>
                    <FlexBox justifyContent="Start" alignItems="Center" style={{ width: "100%" }}>
                        <div
                            style={{
                                minWidth: "40%",
                                maxWidth: "50%",
                            }}
                        >
                            {file.file.name}
                        </div>
                        <ProgressIndicator value={file.value} style={{ width: "100%" }} />
                        {showDelete && (
                            <Button
                                icon="delete"
                                design="Transparent"
                                disabled={processing}
                                onClick={() => handleDelete(i)}
                            />
                        )}
                    </FlexBox>
                </ListItemCustom>
            ))}
        </List>
    )
}

/**
 *
 */
interface UploadFormData {
    files: FileList
    description: string
    category: string
}

/**
 *
 */
interface ExtendedUploadDialogProps extends ControlProps {
    setShowDialog: (value: boolean) => void
}

/**
 *
 * @param props
 * @returns
 */
function ExtendedUploadDialog(props: ExtendedUploadDialogProps) {
    const { def, rowId, setShowDialog } = props
    const intl = useIntl()
    const vhs = useAppSelector((state) => state.valuehelps.vhs)
    const locale = useAppSelector((state) => state.session.locale)
    const { control, formState, trigger, getValues, setValue } = useForm<UploadFormData>()
    const uploadRef = useRef<(description?: string, category?: string) => void>(undefined)
    const [processing, setProcessing] = useState<boolean>(false)
    const registerFilesRef = useRef<(fileList: FileList | null) => void>(undefined)
    const [options, setOptions] = useState<ValueName[]>([])
    const [elementDisabled, setElementDisabled] = useState<boolean>(true)

    useEffect(() => {
        if (def.vh && vhs[def.vh.name]) {
            const p = ValuehelpsService.loadFormLocalstore(def.vh.name, locale)
            p.then((values) => {
                const opts = ValuehelpsService.createVHOptions(values, def.vh)
                setOptions(opts)
                if (opts.length > 0) {
                    setValue("category", opts[0].value)
                }
                setElementDisabled(false)
            })
        }
    }, [vhs])

    return (
        <>
            {ReactDOM.createPortal(
                <Dialog
                    open={true}
                    headerText={intl.formatMessage({ id: "upload_dialog_title" })}
                    footer={
                        <Bar
                            endContent={
                                <>
                                    <Button
                                        design="Emphasized"
                                        icon="upload"
                                        disabled={processing}
                                        onClick={() => {
                                            // console.log("starting upload...")
                                            trigger().then((valid: boolean) => {
                                                // console.log(`validation returned ${valid}`)
                                                if (valid) {
                                                    const values = getValues()
                                                    ;(
                                                        uploadRef.current as (
                                                            description?: string,
                                                            category?: string,
                                                        ) => void
                                                    )(values.description, values.category)
                                                }
                                            })
                                        }}
                                    >
                                        {intl.formatMessage({ id: "common_upload" })}
                                    </Button>
                                    <Button
                                        disabled={processing}
                                        onClick={() => setShowDialog(false)}
                                    >
                                        {intl.formatMessage({ id: "common_close" })}
                                    </Button>
                                </>
                            }
                        />
                    }
                    onClose={() => setShowDialog(false)}
                    style={{ width: "80vw", height: "80vh" }}
                >
                    <Form labelSpan="S12 M3 L1 XL1">
                        <FormGroup headerText="Files">
                            <FormItem>
                                <Controller
                                    control={control}
                                    name="files"
                                    rules={{
                                        validate: (files): string | undefined => {
                                            return files && files.length > 0
                                                ? undefined
                                                : "Mindestens eine Datei ausgewählt"
                                        },
                                    }}
                                    render={({ field }) => (
                                        <FlexBox
                                            direction="Column"
                                            alignItems="End"
                                            justifyContent="End"
                                            style={{ width: "100%" }}
                                        >
                                            <FileUploader
                                                onChange={(
                                                    e: Ui5CustomEvent<
                                                        FileUploaderDomRef,
                                                        FileUploaderChangeEventDetail
                                                    >,
                                                ) => {
                                                    field.onChange(e.detail.files)
                                                    registerFilesRef.current!(e.detail.files)
                                                    trigger()
                                                }}
                                                multiple={def.type === "multiple"}
                                                hideInput
                                                disabled={processing}
                                            >
                                                <Button
                                                    disabled={processing}
                                                    design={
                                                        formState.errors?.[field.name]?.message
                                                            ? "Negative"
                                                            : "Default"
                                                    }
                                                >
                                                    {intl.formatMessage({
                                                        id: "common_select_files",
                                                    })}
                                                </Button>
                                            </FileUploader>
                                            <FilesControl
                                                rowId={rowId}
                                                def={def}
                                                showDelete={true}
                                                onProcessing={(status: boolean) => {
                                                    setProcessing(status)
                                                }}
                                                registerFilesRef={registerFilesRef}
                                                uploadRef={uploadRef}
                                                setShowDialog={setShowDialog}
                                            />
                                        </FlexBox>
                                    )}
                                />
                            </FormItem>
                        </FormGroup>
                        <FormGroup headerText="Description Data">
                            {typeof def.vh?.name === "string" && def.vh?.name.length > 0 && (
                                <FormItem labelContent={<Label required>Category</Label>}>
                                    <Controller
                                        control={control}
                                        name="category"
                                        rules={{ required: "Dies ist ein Pflichtfeld" }}
                                        render={({ field }) => (
                                            <Select
                                                style={{ width: "100%" }}
                                                disabled={processing || elementDisabled}
                                                required
                                                valueState={
                                                    formState.errors?.[field.name]?.message
                                                        ? "Negative"
                                                        : "None"
                                                }
                                                valueStateMessage={
                                                    <span>
                                                        {formState.errors?.[field.name]?.message}
                                                    </span>
                                                }
                                                onChange={(e) =>
                                                    field.onChange(e.detail.selectedOption.value)
                                                }
                                                onBlur={() => trigger()}
                                            >
                                                {" "}
                                                {options.map((it, i) => (
                                                    <Option
                                                        key={"s" + i}
                                                        selected={it.value == field.value}
                                                        value={it.value}
                                                    >
                                                        <Text>{it.name}</Text>
                                                    </Option>
                                                ))}
                                            </Select>
                                        )}
                                    />
                                </FormItem>
                            )}
                            {typeof def.hasDescription === "boolean" && def.hasDescription && (
                                <FormItem labelContent={<Label required>Description</Label>}>
                                    <Controller
                                        control={control}
                                        name="description"
                                        rules={{
                                            required: "Dies ist ein Pflichtfeld",
                                            maxLength: {
                                                message: "Maximallänge ist 250",
                                                value: 250,
                                            },
                                        }}
                                        render={({ field }) => (
                                            <TextArea
                                                {...field}
                                                disabled={processing}
                                                required
                                                valueState={
                                                    formState.errors?.[field.name]?.message
                                                        ? "Negative"
                                                        : "None"
                                                }
                                                valueStateMessage={
                                                    <span>
                                                        {formState.errors?.[field.name]?.message}
                                                    </span>
                                                }
                                                onBlur={() => trigger()}
                                            />
                                        )}
                                    />
                                </FormItem>
                            )}
                        </FormGroup>
                    </Form>
                </Dialog>,
                document.body,
            )}
        </>
    )
}

/**
 *
 */
interface ProgressUploadDialogProps extends ExtendedUploadDialogProps {
    registerFilesRef: React.MutableRefObject<((fileList: FileList | null) => void) | undefined>
    uploadRef: React.MutableRefObject<
        ((description?: string, category?: string) => void) | undefined
    >
}

/**
 *
 * @param props
 * @returns
 */
function ProgressUploadDialog(props: ProgressUploadDialogProps) {
    const { def, registerFilesRef, rowId, setShowDialog, uploadRef } = props
    const intl = useIntl()

    return (
        <>
            {ReactDOM.createPortal(
                <Dialog
                    open={true}
                    headerText={intl.formatMessage({ id: "upload_dialog_title" })}
                    onClose={() => setShowDialog(false)}
                    style={{ width: "80vw", height: "80vh" }}
                >
                    {" "}
                    <FilesControl
                        rowId={rowId}
                        def={def}
                        showDelete={false}
                        onProcessing={(status: boolean) => {}}
                        registerFilesRef={registerFilesRef}
                        uploadRef={uploadRef}
                        setShowDialog={setShowDialog}
                    />
                </Dialog>,
                document.body,
            )}
        </>
    )
}

/**
 *
 * @param contentType
 * @returns
 */
function getIconName(contentType?: string): string {
    if (contentType) {
        switch (contentType) {
            case "application/pdf":
                return "pdf-attachment"
            case "application/msword":
            case "application/vnd.openxmlformats-officedocument.wordprocessingml.document":
            case "application/rtf":
                return "doc-attachment"
            case "application/vnd.ms-excel":
            case "text/csv":
            case "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet":
                return "excel-attachment"
            case "application/zip":
            case "application/vnd.rar":
            case "application/x-tar":
            case "application/gzip":
                return "attachment-zip-file"
            case "image/apng":
            case "image/avif":
            case "image/bmp":
            case "image/gif":
            case "image/vnd.microsoft.icon":
            case "image/jpeg":
            case "image/png":
            case "image/svg+xml":
            case "image/tiff":
            case "image/webp":
                return "attachment-photo"
            case "text/html":
                return "attachment-html"
            case "text/plain":
        }
    }

    return "attachment"
}

export default function (props: ControlProps) {
    const { def, rowId, texts } = props
    const intl = useIntl()
    const form = useAppSelector((state) => state.session.form)
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const [showExtendedDialog, setShowExtendedDialog] = useState<boolean>(false)
    const [showProgressDialog, setShowProgressDialog] = useState<boolean>(false)
    const registerFilesRef = useRef<(fileList: FileList | null) => void>(undefined)
    const uploadRef = useRef<(description?: string, category?: string) => void>(undefined)

    const handleUpload = (e: Ui5CustomEvent<FileUploaderDomRef, FileUploaderChangeEventDetail>) => {
        setShowProgressDialog(true)
        setTimeout(() => {
            registerFilesRef.current!(e.detail.files)
            setTimeout(() => {
                ;(
                    uploadRef.current as (
                        fileList?: FileList,
                        description?: string,
                        category?: string,
                    ) => void
                )()
            }, 0)
        }, 0)
    }

    const labelText = getLabel(texts, def)
    const isDialogUpload =
        def.hasDescription || (typeof def.vh?.name === "string" && def.vh?.name.length > 0)

    return (
        <ControlContainer {...props}>
            {typeof labelText === "string" && labelText.length > 0 && (
                <div style={{ paddingTop: "1rem" }}></div>
            )}
            <Panel
                header={
                    <Bar
                        endContent={
                            <>
                                {isDialogUpload && (
                                    <Button icon="add" onClick={() => setShowExtendedDialog(true)}>
                                        {intl.formatMessage({ id: "common_add" })}
                                    </Button>
                                )}
                                {!isDialogUpload && (
                                    <FileUploader
                                        id={def.key}
                                        hideInput
                                        onChange={handleUpload}
                                        multiple={def.type === "multiple"}
                                    >
                                        <Button icon="upload">
                                            {intl.formatMessage({ id: "common_upload" })}
                                        </Button>
                                    </FileUploader>
                                )}
                            </>
                        }
                    />
                }
                style={{
                    marginTop: "3px",
                }}
            >
                <List>
                    {(element?.va as Attachment[]).map((att, i) => (
                        <ListItemCustom key={i}>
                            <FlexBox
                                justifyContent="Start"
                                alignItems="Center"
                                style={{ width: "100%" }}
                            >
                                <div
                                    style={{
                                        minWidth: "80px",
                                        maxWidth: "80px",
                                    }}
                                >
                                    <Icon
                                        name={getIconName(att.ct)}
                                        style={{ width: "1.5rem", height: "1.5rem" }}
                                    />
                                </div>

                                <FlexBox direction="Column" style={{ width: "100%" }}>
                                    <div>
                                        <Text style={{ fontWeight: "bold" }}>
                                            {typeof att["c"] === "string" && att.c.length > 0 && (
                                                <Tag hideStateIcon colorScheme="6">
                                                    {att.c}
                                                </Tag>
                                            )}{" "}
                                            {att.n}
                                        </Text>
                                    </div>
                                    <div style={{ paddingTop: "5px" }}>
                                        <Text>{att.d}</Text>
                                    </div>
                                </FlexBox>
                                <FlexBox
                                    style={{ marginTop: ".5rem", marginBottom: ".5rem" }}
                                    justifyContent="End"
                                >
                                    <Button
                                        icon="download"
                                        design="Transparent"
                                        onClick={() =>
                                            dispatch(
                                                downloadAttachment({ key: def.key, id: att.id }),
                                            )
                                        }
                                    />
                                    <Button
                                        icon="delete"
                                        design="Transparent"
                                        onClick={() =>
                                            dispatch(
                                                deleteAttachment({
                                                    rowId,
                                                    key: def.key,
                                                    id: att.id,
                                                    messages,
                                                }),
                                            )
                                        }
                                    />
                                </FlexBox>
                            </FlexBox>
                        </ListItemCustom>
                    ))}
                </List>
            </Panel>
            {showExtendedDialog && (
                <ExtendedUploadDialog {...props} setShowDialog={setShowExtendedDialog} />
            )}
            {showProgressDialog && (
                <ProgressUploadDialog
                    {...props}
                    setShowDialog={setShowProgressDialog}
                    registerFilesRef={registerFilesRef}
                    uploadRef={uploadRef}
                />
            )}
        </ControlContainer>
    )
}
