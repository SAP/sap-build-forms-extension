import { useCallback, useEffect, useRef, useState } from "react"
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
    Input,
    Label,
    List,
    ListItemCustom,
    MessageStrip,
    Option,
    Panel,
    ProgressIndicator,
    Select,
    Tag,
    Text,
    TextArea,
    Ui5CustomEvent,
    UploadCollection,
    UploadCollectionItem,
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
import { elementInfo2ValueState } from "./utils"

/**
 * Convert selection mode from string to UI5 component format
 */
function getselect(mode?: string): "None" | "Single" | "Multiple" {
    if (!mode) return "None"
    switch (mode.toLowerCase()) {
        case "single":
            return "Single"
        case "multiple":
            return "Multiple"
        case "none":
        default:
            return "None"
    }
}

/**
 * Pending file interface with metadata before upload
 */
interface PendingFile {
    file: File
    description: string
    category: string
    progress: number
    id: string
}

/**
 * FileData interface for progress tracking
 */
interface FileData {
    file: File
    value: number
}

// TODO: Backend connection

/**
 * FilesControl component for handling file uploads with progress tracking
 * @param props
 * @returns File upload control
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
    const [files, setFilesState] = useState<FileData[]>([])
    const filesRef = useRef<FileData[]>([])
    const setFiles = (updater: FileData[] | ((prev: FileData[]) => FileData[])) => {
        setFilesState((prev) => {
            const next = typeof updater === "function" ? updater(prev) : updater
            filesRef.current = next
            return next
        })
    }
    const [processing, setProcessing] = useState<boolean>(false)
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const uploadIdxRef = useRef(-1)

    registerFilesRef.current = useCallback((fileList: FileList | null) => {
        if (fileList && fileList.length > 0) {
            setFiles(
                Array.from(fileList).map((file) => ({ file, value: 0 }))
            )
        }
    }, [])

    uploadRef.current = useCallback((description?: string, category?: string) => {
        const onProgress = (e: AxiosProgressEvent) => {
            const idx = uploadIdxRef.current
            setFiles((prev) =>
                prev.map((item, i) => {
                    if (i < idx) return { ...item, value: 100 }
                    if (i === idx) return { ...item, value: e.progress! * 100 }
                    return item
                })
            )

            if (e.loaded === e.total) {
                if (uploadIdxRef.current + 1 < filesRef.current.length) {
                    uploadIdxRef.current++
                    setTimeout(() => {
                        dispatch(
                            uploadAttachment({
                                file: filesRef.current[uploadIdxRef.current].file,
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
                    setProcessing(false)
                    onProcessing(false)
                    setTimeout(() => setShowDialog(false), 2000)
                }
            }
        }

        if (filesRef.current && filesRef.current.length > 0) {
            setProcessing(true)
            onProcessing(true)
            uploadIdxRef.current = 0
            dispatch(
                uploadAttachment({
                    file: filesRef.current[0].file,
                    rowId,
                    key: def.key,
                    onProgress,
                    description,
                    category,
                    messages,
                }),
            )
        }

    }, [rowId, def.key, dispatch, messages, onProcessing, setShowDialog])

    const handleDelete = useCallback((idx: number) => {
        setFiles((prev) => prev.filter((_, i) => i !== idx))
    }, [])

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
 * Upload form data interface
 */
interface UploadFormData {
    files: FileList
    description: string
    category: string
}

/**
 * Extended upload dialog props interface
 * Includes control props and additional functions for dialog management
 */
interface ExtendedUploadDialogProps extends ControlProps {
    setShowDialog: (value: boolean) => void
}

/**
 * Extended upload dialog component
 * Provides file selection, description, and category inputs before upload
 * @param props
 * @returns Extended upload dialog with file selection, description and category inputs before upload
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
    const messages = useMessages()
    const allowedExtensionsDialog = parseFileTypes(def.fileTypes)
    const [setRejected, rejectedStrip] = useRejectedFilesMessage()

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
    }, [vhs, def.vh, locale, setValue])

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
                                            trigger().then((valid: boolean) => {
                                                if (valid) {
                                                    const values = getValues()
                                                        ; (
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
                        <FormGroup headerText={intl.formatMessage({ id: "attachment_files_group" })}>
                            <FormItem>
                                <Controller
                                    control={control}
                                    name="files"
                                    rules={{
                                        validate: (files): string | undefined => {
                                            return files && files.length > 0
                                                ? undefined
                                                : intl.formatMessage({ id: "attachment_file_required" })
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
                                                    const files = e.detail.files
                                                        ? Array.from(e.detail.files)
                                                        : []
                                                    const rejected = files.filter(
                                                        (f) => !isFileAccepted(f, allowedExtensionsDialog),
                                                    )
                                                    if (rejected.length > 0) {
                                                        setRejected(
                                                            rejected.map((f) => f.name).join(", "),
                                                            def.fileTypes ?? "",
                                                        )
                                                        return
                                                    }
                                                    field.onChange(e.detail.files)
                                                    registerFilesRef.current!(e.detail.files)
                                                    trigger()
                                                }}
                                                multiple={def.type === "multiple"}
                                                accept={buildAcceptAttr(allowedExtensionsDialog)}
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
                                            {rejectedStrip}
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
                        <FormGroup headerText={intl.formatMessage({ id: "attachment_desc_group" })}>
                            {typeof def.vh?.name === "string" && def.vh?.name.length > 0 && (
                                <FormItem labelContent={<Label required>{intl.formatMessage({ id: "attachment_category" })}</Label>}>
                                    <Controller
                                        control={control}
                                        name="category"
                                        rules={{ required: intl.formatMessage({ id: "attachment_field_required" }) }}
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
                                <FormItem labelContent={<Label required>{intl.formatMessage({ id: "attachment_description" })}</Label>}>
                                    <Controller
                                        control={control}
                                        name="description"
                                        rules={{
                                            required: intl.formatMessage({ id: "attachment_field_required" }),
                                            maxLength: {
                                                message: intl.formatMessage({ id: "attachment_description_maxlength" }),
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
 * Props Interface for ProgressUploadDialog
 */
interface ProgressUploadDialogProps extends ExtendedUploadDialogProps {
    registerFilesRef: React.MutableRefObject<((fileList: FileList | null) => void) | undefined>
    uploadRef: React.MutableRefObject<
        ((description?: string, category?: string) => void) | undefined
    >
}

/**
 * Progress upload dialog component without description input
 * @param props
 * @returns Progress dialog
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
                        onProcessing={(status: boolean) => { }}
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
 * Get icon name for given content type
 * @param contentType
 * @returns icon name for given content type
 */
function getIconName(contentType?: string): string {
    if (contentType) {
        switch (contentType) {
            case "application/pdf":
                return "pdf-attachment"
            case "application/msword":
            case "application/rtf":
                return "doc-attachment"
            case "application/vnd.ms-excel":
            case "text/csv":
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
            case "image/jpeg":
            case "image/png":
            case "image/svg+xml":
            case "image/tiff":
            case "image/webp":
                return "attachment-photo"
            case "text/html":
                return "attachment-html"
        }
    }
    return "attachment"
}

/**
 * Parse a free-form fileTypes string (e.g. ".pdf, jpg .PNG") into a normalised
 * lowercase extension set without leading dots (e.g. ["pdf", "jpg", "png"]).
 * @param fileTypes
 * @return array of normalised file extensions without leading dots, or empty array if no valid types found
 */
function parseFileTypes(fileTypes?: string): string[] {
    if (!fileTypes || fileTypes.trim().length === 0) return []
    return fileTypes
        .split(/[\s,;|]+/)
        .map((t) => t.replace(/^\./, "").toLowerCase().trim())
        .filter((t) => t.length > 0)
}

/**
 * Checks if a given file is accepted based on allowed extensions
 * @param file
 * @param allowedExtensions array of normalised file extensions without leading dots, e.g. ["pdf", "jpg", "png"]
 * @return true if file is accepted, false if rejected
 */
function isFileAccepted(file: File, allowedExtensions: string[]): boolean {
    if (allowedExtensions.length === 0) return true
    const ext = file.name.includes(".")
        ? file.name.split(".").pop()!.toLowerCase()
        : ""
    return allowedExtensions.includes(ext)
}

/**
 * Build the "accept" attribute string for <input type="file"> / <FileUploader>.
 * @param allowedExtensions array of normalised file extensions without leading dots, e.g. ["pdf", "jpg", "png"]
 * @return string suitable for the "accept" attribute, or undefined if no restrictions
 */
function buildAcceptAttr(allowedExtensions: string[]): string | undefined {
    if (allowedExtensions.length === 0) return undefined
    return allowedExtensions.map((e) => `.${e}`).join(",")
}

/**
 * Hook that provides a dismissable rejected-files message strip.
 * Returns the message text setter and the MessageStrip element to render.
 */
function useRejectedFilesMessage(): [
    (rejectedNames: string, allowedTypes: string) => void,
    React.ReactElement | null,
] {
    const intl = useIntl()
    const [rejectedMessage, setRejectedMessage] = useState<{
        files: string
        types: string
    } | null>(null)

    const setRejected = (rejectedNames: string, allowedTypes: string) => {
        setRejectedMessage({ files: rejectedNames, types: allowedTypes })
    }

    const strip = rejectedMessage ? (
        <MessageStrip
            design="Negative"
            onClose={() => setRejectedMessage(null)}
        >
            {intl.formatMessage(
                { id: "attachment_filetype_rejected" },
                { files: rejectedMessage.files, types: rejectedMessage.types },
            )}
        </MessageStrip>
    ) : null

    return [setRejected, strip]
}

/**
 * FileUploader Control
 * Renders a file uploader with optional description and category inputs, and displays upload progress
 * @param props 
 * @returns FileUploader control with extended upload dialog and progress tracking
 */
function FileUploaderControl(props: ControlProps) {
    const { def, globalEd, rowId, texts } = props
    const intl = useIntl()
    const form = useAppSelector((state) => state.session.form)
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const [showExtendedDialog, setShowExtendedDialog] = useState<boolean>(false)
    const [showProgressDialog, setShowProgressDialog] = useState<boolean>(false)
    const registerFilesRef = useRef<(fileList: FileList | null) => void>(undefined)
    const uploadRef = useRef<(description?: string, category?: string) => void>(undefined)
    // Holds the FileList that should trigger an upload once the dialog has mounted
    const [pendingUploadFiles, setPendingUploadFiles] = useState<FileList | null>(null)
    const [setRejectedFU, rejectedStripFU] = useRejectedFilesMessage()
    const hasAttachmentError = elementInfo2ValueState(element?.msg) === "Negative"

    // Start the upload once the progress dialog is open and refs are wired up
    useEffect(() => {
        if (showProgressDialog && pendingUploadFiles !== null) {
            registerFilesRef.current?.(pendingUploadFiles)
            uploadRef.current?.()
            setPendingUploadFiles(null)
        }
    }, [showProgressDialog, pendingUploadFiles])

    const handleUpload = (e: Ui5CustomEvent<FileUploaderDomRef, FileUploaderChangeEventDetail>) => {
        const allowedExtensions = parseFileTypes(def.fileTypes)
        const files = e.detail.files ? Array.from(e.detail.files) : []
        const rejected = files.filter((f) => !isFileAccepted(f, allowedExtensions))
        if (rejected.length > 0) {
            setRejectedFU(rejected.map((f) => f.name).join(", "), def.fileTypes ?? "")
            return
        }
        setPendingUploadFiles(e.detail.files)
        setShowProgressDialog(true)
    }

    const labelText = getLabel(texts, def)
    const isDialogUpload =
        def.hasDescription || (typeof def.vh?.name === "string" && def.vh?.name.length > 0)
    // TODO: get existing attachments from backend for the case site is saved and reloaded.
    const existingAttachmentsFU = (element?.va as Attachment[]) || []
    const isSingleFU = def.type !== "multiple"
    const atCapacityFU = isSingleFU && existingAttachmentsFU.length > 0
    const [stripDismissedFU, setStripDismissedFU] = useState(false)

    // Reset dismissed state whenever capacity is freed so the message reappears next time
    useEffect(() => {
        if (!atCapacityFU) setStripDismissedFU(false)
    }, [atCapacityFU])

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
                                    <Button
                                        icon="add"
                                        disabled={atCapacityFU || !element?.ed || !globalEd}
                                        onClick={() => setShowExtendedDialog(true)}
                                    >
                                        {intl.formatMessage({ id: "common_add" })}
                                    </Button>
                                )}
                                {!isDialogUpload && (
                                    <FileUploader
                                        id={def.key}
                                        hideInput
                                        onChange={handleUpload}
                                        multiple={def.type === "multiple"}
                                        disabled={atCapacityFU || !element?.ed || !globalEd}
                                        accept={buildAcceptAttr(parseFileTypes(def.fileTypes))}
                                    >
                                        <Button icon="upload" disabled={atCapacityFU || !element?.ed || !globalEd}>
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
                    border: hasAttachmentError ? "0.125rem solid var(--sapField_InvalidColor)" : undefined,
                    backgroundColor: hasAttachmentError ? "var(--sapField_InvalidBackground)" : undefined,
                    borderRadius: hasAttachmentError ? "0.25rem" : undefined,
                }}
            >

                <List
                    selectionMode={existingAttachmentsFU.length > 0 ? getselect(def.select) : "None"}
                >
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
                {atCapacityFU && !stripDismissedFU && (
                    <MessageStrip
                        design="Information"
                        onClose={() => setStripDismissedFU(true)}
                    >
                        {intl.formatMessage({ id: "attachment_single_only" })}
                    </MessageStrip>
                )}
                {rejectedStripFU}
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


/**
 * UploadCollection Control
 * @param props 
 * @returns Upload Collection with drag-and-drop, description, and category inputs, and progress tracking
 */
function UploadCollectionControl(props: ControlProps) {
    const { def, rowId, texts, globalEd } = props
    const intl = useIntl()
    const form = useAppSelector((state) => state.session.form)
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const vhs = useAppSelector((state) => state.valuehelps.vhs)
    const locale = useAppSelector((state) => state.session.locale)

    const [pendingFiles, setPendingFiles] = useState<PendingFile[]>([])
    const [categoryOptions, setCategoryOptions] = useState<ValueName[]>([])
    const [dragOver, setDragOver] = useState(false)
    const [uploadAttempted, setUploadAttempted] = useState(false)
    const [stripDismissed, setStripDismissed] = useState(false)
    const dragCounterRef = useRef(0)
    const fileInputRef = useRef<HTMLInputElement>(null)
    const [setRejectedUC, rejectedStripUC] = useRejectedFilesMessage()
    const hasAttachmentError = elementInfo2ValueState(element?.msg) === "Negative"


    // TODO: Get existing attachments from backend
    const existingAttachments = (element?.va as Attachment[]) || []
    const isSingle = def.type !== "multiple"
    const atCapacity = isSingle && (existingAttachments.length > 0 || pendingFiles.length > 0)

    // Reset dismissed state whenever capacity is freed so the message reappears next time
    useEffect(() => {
        if (!atCapacity) setStripDismissed(false)
    }, [atCapacity])

    // Load category options if value help is configured
    useEffect(() => {
        if (def.vh && vhs[def.vh.name]) {
            const p = ValuehelpsService.loadFormLocalstore(def.vh.name, locale)
            p.then((values) => {
                const opts = ValuehelpsService.createVHOptions(values, def.vh)
                setCategoryOptions(opts)
            })
        }
    }, [vhs, def.vh, locale])

    // Handle file drop
    const handleDrop = (e: React.DragEvent) => {
        e.preventDefault()
        e.stopPropagation()
        dragCounterRef.current = 0
        setDragOver(false)

        const files = Array.from(e.dataTransfer.files)
        addPendingFiles(files)
    }

    // Introduces a counter to track drag enter/leave events and avoid flickering when dragging over child elements
    const handleDragEnter = (e: React.DragEvent) => {
        e.preventDefault()
        e.stopPropagation()
        dragCounterRef.current++
        if (dragCounterRef.current === 1 && !atCapacity && element?.ed && globalEd) {
            setDragOver(true)
        }
    }

    const handleDragOver = (e: React.DragEvent) => {
        e.preventDefault()
        e.stopPropagation()
    }

    const handleDragLeave = (e: React.DragEvent) => {
        e.preventDefault()
        e.stopPropagation()
        dragCounterRef.current--
        if (dragCounterRef.current === 0) {
            setDragOver(false)
        }
    }

    // Add files to pending list
    const addPendingFiles = (files: File[]) => {
        if (atCapacity || !element?.ed || !globalEd) return
        const allowedExtensions = parseFileTypes(def.fileTypes)
        const rejected = files.filter((f) => !isFileAccepted(f, allowedExtensions))
        const accepted = files.filter((f) => isFileAccepted(f, allowedExtensions))
        if (rejected.length > 0) {
            setRejectedUC(rejected.map((f) => f.name).join(", "), def.fileTypes ?? "")
        }
        if (accepted.length === 0) return
        // Single cardinality: only one file
        const filesToAdd = isSingle ? accepted.slice(0, 1) : accepted
        const newPending = filesToAdd.map(file => ({
            file,
            description: "",
            category: categoryOptions.length > 0 ? categoryOptions[0].value : "",
            progress: 0,
            id: `${Date.now()}-${Math.random()}`,
        }))
        setPendingFiles(prev => isSingle ? newPending : [...prev, ...newPending])
        setUploadAttempted(false)
    }

    // Handle file selection from button
    const handleFileSelect = (e: React.ChangeEvent<HTMLInputElement>) => {
        if (e.target.files) {
            addPendingFiles(Array.from(e.target.files))
            e.target.value = "" 
        }
    }

    // Update pending file metadata
    const updatePendingFile = (id: string, updates: Partial<PendingFile>) => {
        setPendingFiles(prev =>
            prev.map(pf => pf.id === id ? { ...pf, ...updates } : pf)
        )
    }

    // Remove pending file
    const removePendingFile = (id: string) => {
        setPendingFiles(prev => prev.filter(pf => pf.id !== id))
    }

    // Validate that a document that needs a description has one before upload
    const isValid = () => {
        if (pendingFiles.length === 0) return false
        if (def.hasDescription) {
            return pendingFiles.every((pf) => pf.description.trim().length > 0)
        }
        return true
    }

    // Handle upload button click
    const handleUploadClick = async () => {
        setUploadAttempted(true)
        if (!isValid()) return

        let anyFailed = false
        for (const pending of pendingFiles) {
            const onProgress = (e: AxiosProgressEvent) => {
                updatePendingFile(pending.id, { progress: e.progress ? e.progress * 100 : 0 })
            }
            try {
                await dispatch(
                    uploadAttachment({
                        file: pending.file,
                        rowId,
                        key: def.key,
                        onProgress,
                        description: pending.description,
                        category: pending.category,
                        messages,
                    }),
                ).unwrap()
                removePendingFile(pending.id)
            } catch (error) {
                anyFailed = true
                console.error("Upload failed:", error)
            }
        }
        if (!anyFailed) {
            setUploadAttempted(false)
        }
    }

    return (
        <ControlContainer {...props}>
            {typeof getLabel(texts, def) === "string" && getLabel(texts, def).length > 0 && (
                <div style={{ paddingTop: "1rem" }}></div>
            )}

            <div
                onDragEnter={handleDragEnter}
                onDragOver={handleDragOver}
                onDragLeave={handleDragLeave}
                onDrop={handleDrop}
                style={{ position: "relative" }}
            >
                {/* Custom drag overlay */}
                {dragOver && (
                    <div
                        style={{
                            position: "absolute",
                            inset: 0,
                            zIndex: 10,
                            display: "flex",
                            flexDirection: "column",
                            alignItems: "center",
                            justifyContent: "center",
                            background: "color-mix(in srgb, var(--sapButton_Emphasized_Background) 10%, white)",
                            border: "2px dashed var(--sapButton_Emphasized_BorderColor)",
                            borderRadius: "0.5rem",
                            pointerEvents: "none",
                            gap: "0.5rem",
                        }}
                    >
                        <Icon
                            name="upload-to-cloud"
                            style={{
                                width: "3rem",
                                height: "3rem",
                                color: "var(--sapButton_Emphasized_BorderColor)",
                            }}
                        />
                        <Text style={{ fontSize: "1rem", fontWeight: "bold", color: "var(--sapButton_Emphasized_BorderColor)" }}>
                            {intl.formatMessage({ id: "attachment_drag_drop_hint" })}
                        </Text>
                    </div>
                )}

                <UploadCollection
                    hideDragOverlay
                    style={{
                    border: hasAttachmentError ? "0.125rem solid var(--sapField_InvalidColor)" : undefined,
                    backgroundColor: hasAttachmentError ? "var(--sapField_InvalidBackground)" : undefined,
                    borderRadius: hasAttachmentError ? "0.25rem" : undefined,
                }}
                    noDataText={intl.formatMessage({ id: "attachment_drag_drop_hint" })}
                    noDataDescription={intl.formatMessage({ id: "attachment_or_click_add" })}
                    selectionMode={existingAttachments.length > 0 || pendingFiles.length > 0 ? getselect(def.select) : "None"}
                    header={
                        <Bar
                            endContent={
                                <>
                                    <input
                                        ref={fileInputRef}
                                        type="file"
                                        multiple={def.type === "multiple"}
                                        accept={buildAcceptAttr(parseFileTypes(def.fileTypes))}
                                        onChange={handleFileSelect}
                                        style={{ display: "none" }}
                                    />
                                    <Button
                                        icon="add"
                                        disabled={atCapacity || !element?.ed || !globalEd}
                                        onClick={() => fileInputRef.current?.click()}
                                    >
                                        {intl.formatMessage({ id: "common_add" })}
                                    </Button>
                                    {pendingFiles.length > 0 && (
                                        <Button
                                            icon="upload"
                                            design="Emphasized"
                                            disabled={!element?.ed || !globalEd}
                                            onClick={handleUploadClick}
                                        >
                                            {intl.formatMessage({ id: "common_upload" })} ({pendingFiles.length})
                                        </Button>
                                    )}
                                </>
                            }
                        />
                    }
                >
                    {/* Existing uploaded attachments */}
                    {existingAttachments.map((att, i) => (
                        <UploadCollectionItem
                            key={`existing-${i}`}
                            fileName={att.n}
                            uploadState="Complete"
                            thumbnail={<Icon slot="thumbnail" name={getIconName(att.ct)} />}
                            fileNameClickable
                            onFileNameClick={() =>
                                dispatch(downloadAttachment({ key: def.key, id: att.id }))
                            }
                            deleteButton={
                                <>
                                    <Button
                                        icon="download"
                                        design="Transparent"
                                        tooltip={intl.formatMessage({ id: "common_show" })}
                                        onClick={() =>
                                            dispatch(downloadAttachment({ key: def.key, id: att.id }))
                                        }
                                    />
                                    <Button
                                        icon="delete"
                                        design="Transparent"
                                        tooltip={intl.formatMessage({ id: "common_delete" })}
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
                                </>
                            }
                        >
                            {/* Description shown right below the file name */}
                            {att.d && (
                                <Text style={{ color: "var(--sapContent_LabelColor)", fontSize: "0.875rem" }}>
                                    {att.d}
                                </Text>
                            )}
                            {/* Category tag + metadata row */}
                            <FlexBox alignItems="Center" style={{ gap: "1rem", marginTop: att.d ? "0.25rem" : undefined }}>
                                {typeof att.c === "string" && att.c.length > 0 && (
                                    <Tag hideStateIcon colorScheme="6" style={{ flexShrink: 0 }}>
                                        {att.c}
                                    </Tag>
                                )}
                                
                            </FlexBox>
                        </UploadCollectionItem>
                    ))}

                    {/* Pending files awaiting upload */}
                    {pendingFiles.map((pending) => {
                        const isUploading = pending.progress > 0
                        const descriptionInvalid =
                            uploadAttempted && def.hasDescription && pending.description.trim().length === 0
                        const hasVh = typeof def.vh?.name === "string" && def.vh.name.length > 0
                        return (
                            <UploadCollectionItem
                                key={pending.id}
                                fileName={pending.file.name}
                                uploadState={isUploading ? "Uploading" : "Complete"}
                                progress={pending.progress}
                                thumbnail={<Icon slot="thumbnail" name={getIconName(pending.file.type)} />}
                                highlight="Critical"
                                hideTerminateButton
                                hideRetryButton
                                hideDeleteButton
                            >
                                {/* Single row: status tag | file size | category | description | delete */}
                                <FlexBox alignItems="Center" style={{ width: "100%", gap: "0.5rem" }}>
                                    <Tag
                                        hideStateIcon
                                        colorScheme="2"
                                        style={{ flexShrink: 0 }}
                                    >
                                        {intl.formatMessage({ id: "attachment_not_uploaded" })}
                                    </Tag>

                                    {hasVh && (
                                        <Select
                                            style={{ flex: "1 1 8rem", minWidth: 0 }}
                                            disabled={isUploading}
                                            onChange={(e) =>
                                                updatePendingFile(pending.id, {
                                                    category: e.detail.selectedOption.value,
                                                })
                                            }
                                        >
                                            {categoryOptions.map((opt, i) => (
                                                <Option
                                                    key={i}
                                                    value={opt.value}
                                                    selected={opt.value === pending.category}
                                                >
                                                    {opt.name}
                                                </Option>
                                            ))}
                                        </Select>
                                    )}

                                    {typeof def.hasDescription === "boolean" && def.hasDescription && (
                                        <Input
                                            value={pending.description}
                                            onInput={(e) =>
                                                updatePendingFile(pending.id, {
                                                    description: e.target.value || "",
                                                })
                                            }
                                            maxlength={250}
                                            disabled={isUploading}
                                            required
                                            placeholder={intl.formatMessage({ id: "attachment_description" })}
                                            style={{ flex: "2 1 10rem", minWidth: 0 }}
                                            valueState={descriptionInvalid ? "Negative" : "None"}
                                            valueStateMessage={
                                                <span>
                                                    {intl.formatMessage({ id: "attachment_description_required" })}
                                                </span>
                                            }
                                        />
                                    )}

                                    {!hasVh && !(typeof def.hasDescription === "boolean" && def.hasDescription) && (
                                        <div style={{ flex: 1 }} />
                                    )}

                                    {!isUploading && (
                                        <Button
                                            icon="decline"
                                            design="Transparent"
                                            style={{ flexShrink: 0 }}
                                            tooltip={intl.formatMessage({ id: "common_delete" })}
                                            onClick={() => removePendingFile(pending.id)}
                                        />
                                    )}
                                </FlexBox>
                            </UploadCollectionItem>
                        )
                    })}
                </UploadCollection>
            </div>

            {atCapacity && !stripDismissed && (
                <MessageStrip
                    design="Information"
                    onClose={() => setStripDismissed(true)}
                >
                    {intl.formatMessage({ id: "attachment_single_only" })}
                </MessageStrip>
            )}
            {rejectedStripUC}

        </ControlContainer>
    )
}

/**
 * Renders a file uploader or upload collection control based on the selected design
 * @param props ControlProps containing the definition and other properties
 * @returns FileUploader or UploadCollection control depending on the selected design
 */
export default function (props: ControlProps) {
    const { def } = props

    // Check if UploadCollection design is selected
    if (def.design === "uploadCollection") {
        return <UploadCollectionControl {...props} />
    }
    // If not, default to FileUploader design
    return <FileUploaderControl {...props} />
}