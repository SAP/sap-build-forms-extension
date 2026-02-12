import { createContext, ReactNode, useContext, useRef, useState } from "react"
import ReactDOM from "react-dom"

import { useIntl } from "react-intl"
import { PrimitiveType } from "intl-messageformat"

import {
    BusyIndicator,
    Dialog,
    IllustratedMessage,
    MessageBox,
    MessageBoxAction,
    MessageStrip,
    Toast,
} from "@ui5/webcomponents-react"
import "@ui5/webcomponents-fiori/dist/illustrations/SimpleError"

import { Severity } from "../../features/sessions/forms"
import { MessageOption, MessageIntf, MessageResolver } from "../../utils/Messages"

/**
 *
 */
const Context = createContext<MessageIntf>({
    fatal: (key: string, values?: Record<string, PrimitiveType>) => {},
    dialog: (
        severity: Severity,
        key: string,
        values?: Record<string, PrimitiveType>,
        options?: MessageOption[],
    ): Promise<MessageOption> => {
        return Promise.reject()
    },
    toast: (severity: Severity, key: string, values?: Record<string, PrimitiveType>) => {},
    block: (show: boolean) => {},
})

/**
 *
 * @param severity
 * @returns
 */
function severity2MessageBoxType(
    severity: Severity,
): "Confirm" | "Error" | "Information" | "Success" | "Warning" | undefined {
    switch (severity) {
        case Severity.Error:
            return "Error"
        case Severity.Warning:
            return "Warning"
        case Severity.Info:
            return "Information"
        case Severity.Success:
            return "Success"
        default:
            return undefined
    }
}

/**
 *
 * @param severity
 * @returns
 */
function severity2MessageBoxTitle(severity: Severity): string {
    switch (severity) {
        case Severity.Error:
            return "common_error_title"
        case Severity.Warning:
            return "common_warn_title"
        case Severity.Info:
            return "common_info_title"
        case Severity.Success:
            return "common_success_title"
        default:
            return "common_unkown_title"
    }
}

/**
 *
 * @param severity
 * @returns
 */
function severity2MessageStripDesign(
    severity: Severity,
): "Critical" | "Information" | "Negative" | "Positive" {
    switch (severity) {
        case Severity.Error:
            return "Negative"
        case Severity.Warning:
            return "Critical"
        case Severity.Info:
            return "Information"
        case Severity.Success:
            return "Positive"
        default:
            return "Information"
    }
}

/**
 *
 */
function MessagesProvider(props: { children: ReactNode }) {
    const [type, setType] = useState<"fatal" | "dialog" | "toast" | "block" | undefined>()
    const [sev, setSev] = useState<Severity>(Severity.Error)
    const [textKey, setTextKey] = useState<string>("common_error_unkown")
    // const [visible, setVisible] = useState<boolean>(false)
    const [values, setValues] = useState<Record<string, PrimitiveType> | undefined>()
    const [opts, setOpts] = useState<MessageOption[]>([])
    const intl = useIntl()
    const resolverRef = useRef<MessageResolver | undefined>(undefined)

    /**
     *
     * @param key
     * @param values
     */
    const fatal = (key: string, values?: Record<string, PrimitiveType>) => {
        setType("fatal")
        setTextKey(key)
        setValues(values)
    }

    /**
     *
     * @param severity
     * @param key
     * @param values
     */
    const dialog = (
        severity: Severity,
        key: string,
        values?: Record<string, PrimitiveType>,
        options?: MessageOption[],
    ): Promise<MessageOption> => {
        console.log("open dialog!")
        const p = new Promise<MessageOption>((resolve) => (resolverRef.current = resolve))
        setType("dialog")
        setSev(severity)
        setTextKey(key)
        setValues(values)
        setOpts(options ?? [])

        return p
    }

    /**
     *
     * @param severity
     * @param key
     * @param values
     */
    const toast = (severity: Severity, key: string, values?: Record<string, PrimitiveType>) => {
        setType("toast")
        setSev(severity)
        setTextKey(key)
        setValues(values)
    }

    /**
     *
     */
    const block = (show: boolean) => {
        setType(show ? "block" : undefined)
    }

    /**
     *
     * @param event
     */
    const handleClose = (action?: string, escPressed?: true) => {
        console.log(`Closing messagebox with ${action}`)
        // TODO(ML) call handler
        // if (resolverRef.current) {
        //     resolverRef.current(event.detail.action)
        // }
        setType(undefined)
    }

    const actions: ReactNode[] = []
    if (opts) {
        opts.forEach((opt) => {
            switch (opt) {
                case MessageOption.Ok:
                    actions.push(MessageBoxAction.OK)
                    break
                case MessageOption.Retry:
                    actions.push(MessageBoxAction.Retry)
                    break
                default:
                    actions.push(opt)
            }
        })
    }
    if (actions.length === 0) {
        actions.push(MessageBoxAction.OK)
    }

    console.log(`Messages type: ${type}`)

    return (
        <Context.Provider value={{ fatal, dialog, toast, block }}>
            <>
                {ReactDOM.createPortal(
                    <MessageBox
                        titleText={intl.formatMessage(
                            { id: severity2MessageBoxTitle(sev) },
                            values,
                        )}
                        type={severity2MessageBoxType(sev)}
                        actions={actions}
                        open={type == "dialog"}
                        onClose={handleClose}
                    >
                        <p>{intl.formatMessage({ id: textKey }, values)}</p>
                    </MessageBox>,
                    document.body,
                )}
                {ReactDOM.createPortal(
                    <Dialog
                        headerText={intl.formatMessage({ id: "common_fatal_title" }, values)}
                        open={type == "fatal"}
                        onBeforeClose={(e) => e.preventDefault()}
                        onClose={() => {}}
                        state="Negative"
                    >
                        <IllustratedMessage
                            name="SimpleError"
                            titleText={intl.formatMessage({ id: textKey }, values)}
                            subtitleText={intl.formatMessage({ id: "common_fatal_subtitle" })}
                        />
                    </Dialog>,
                    document.body,
                )}
                {type === "block" && (
                    <div
                        style={{
                            width: "100%",
                            height: "100%",
                            background: "black",
                            opacity: 0.6,
                            position: "fixed",
                            top: 0,
                            left: 0,
                            zIndex: 200,
                            display: "flex",
                            justifyContent: "center",
                            alignItems: "center",
                        }}
                    >
                        <BusyIndicator delay={0} active />
                    </div>
                )}
                {ReactDOM.createPortal(
                    <Toast
                        duration={3000}
                        placement="BottomCenter"
                        open={type === "toast"}
                        onClose={() => setType(undefined)}
                        style={{
                            width: "30vw",
                            maxWidth: "800px",
                        }}
                    >
                        <MessageStrip
                            design={severity2MessageStripDesign(sev)}
                            hideCloseButton={true}
                            style={{
                                position: "absolute",
                                top: "0",
                                left: "0",
                                bottom: "0",
                            }}
                        >
                            {intl.formatMessage({ id: textKey }, values)}
                        </MessageStrip>
                    </Toast>,
                    document.body,
                )}
            </>
            {props.children}
        </Context.Provider>
    )
}

export { MessagesProvider, Context as MessagesContext }
export const useMessages = () => useContext(Context)
