import { createContext, ReactNode, useContext, useRef, useState } from "react"
import ReactDOM from "react-dom"

import { useIntl } from "react-intl"
import { PrimitiveType } from "intl-messageformat"

import {
    Dialog,
    IllustratedMessage,
    MessageBox,
    MessageBoxAction,
    Toast,
} from "@ui5/webcomponents-react"
// @ts-ignore
import "@ui5/webcomponents-fiori/dist/illustrations/SimpleError"

import {
    highestSeverity,
    Message,
    MessageIntf,
    MessageOption,
    MessageResolver,
    Severity,
} from "../../utils/messages"

/**
 *
 */
const Context = createContext<MessageIntf>({
    // @ts-ignore
    fatal: (key: string, values?: Record<string, PrimitiveType>) => {},
    // @ts-ignore
    dialog: (msg: Message[], options?: MessageOption[]): Promise<MessageOption> => {
        return Promise.reject()
    },
    // @ts-ignore
    toast: (msg: Message[]) => {},
    // @ts-ignore
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
function severityFormatName(severity: Severity): "Warning" | "Information" | "Error" | "Success" {
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
            return "Information"
    }
}

/**
 *
 */
function MessagesProvider(props: { children: ReactNode }) {
    const intl = useIntl()
    const resolverRef = useRef<MessageResolver | undefined>(undefined)

    const [type, setType] = useState<"fatal" | "dialog" | "toast" | "block" | undefined>()
    const [messages, setMessages] = useState<Message[]>([])
    const [opts, setOpts] = useState<MessageOption[]>([])

    /**
     *
     * @param key
     * @param values
     */
    const fatal = (key: string, values?: Record<string, PrimitiveType>) => {
        setType("fatal")
        setMessages([{ style: "dialog", severity: Severity.Error, key, params: values }])
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
        params?: Record<string, PrimitiveType>,
        options?: MessageOption[],
    ): Promise<MessageOption> => {
        // console.log("open dialog!")
        const p = new Promise<MessageOption>((resolve) => {
            resolverRef.current = resolve
        })
        setType("dialog")
        setMessages([{ style: "dialog", severity, key, params }])
        setOpts(options ?? [])

        return p
    }

    /**
     *
     * @param severity
     * @param key
     * @param values
     */
    const toast = (severity: Severity, key: string, params?: Record<string, PrimitiveType>) => {
        setType("toast")
        setMessages([{ style: "toast", severity, key, params }])
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
    // @ts-ignore
    const handleClose = (action?: string, escPressed?: true) => {
        // console.log(`Closing messagebox with ${action}`)
        let result: MessageOption | undefined = undefined
        switch (action) {
            case MessageBoxAction.OK:
                result = MessageOption.Ok
                break
            case MessageBoxAction.Retry:
                result = MessageOption.Retry
                break
            case MessageBoxAction.Yes:
                result = MessageOption.Yes
                break
            case MessageBoxAction.No:
                result = MessageOption.No
                break
            default:
                if (escPressed) {
                    result = MessageOption.Cancel
                } else {
                    console.error(`Unknown action ${action}`)
                    setType(undefined)
                    return
                }
        }
        if (resolverRef.current && result) {
            resolverRef.current(result)
        }
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
                case MessageOption.Yes:
                    actions.push(MessageBoxAction.Yes)
                    break
                case MessageOption.No:
                    actions.push(MessageBoxAction.No)
                    break
                default:
                    actions.push(opt)
            }
        })
    }
    if (actions.length === 0) {
        actions.push(MessageBoxAction.OK)
    }

    // console.log(`Messages type: ${type}`)

    return (
        <Context.Provider value={{ fatal, dialog, toast, block }}>
            <>
                {ReactDOM.createPortal(
                    <MessageBox
                        titleText={intl.formatMessage({
                            id: severity2MessageBoxTitle(highestSeverity(messages)),
                        })}
                        type={severity2MessageBoxType(highestSeverity(messages))}
                        actions={actions}
                        open={type == "dialog"}
                        onClose={handleClose}
                    >
                        {messages.map((m, i) => (
                            <p key={"m" + i}>{intl.formatMessage({ id: m.key }, m.params)}</p>
                        ))}
                    </MessageBox>,
                    document.body,
                )}
                {messages &&
                    messages.length > 0 &&
                    ReactDOM.createPortal(
                        <Dialog
                            headerText={intl.formatMessage({ id: "common_fatal_title" })}
                            open={type == "fatal"}
                            onBeforeClose={(e) => e.preventDefault()}
                            onClose={() => {}}
                            state="Negative"
                        >
                            <IllustratedMessage
                                name="SimpleError"
                                titleText={intl.formatMessage(
                                    { id: messages[0].key },
                                    messages[0].params,
                                )}
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
                            zIndex: 999,
                            display: "flex",
                            justifyContent: "center",
                            alignItems: "center",
                        }}
                    >
                        <div
                            id="splash"
                            className="loader"
                            style={{
                                position: "absolute",
                                left: "calc((100vw - 80px)/2)",
                                top: "calc((100vh - 80px)/2)",
                            }}
                        ></div>
                    </div>
                )}
                {messages &&
                    messages.length > 0 &&
                    type === "toast" &&
                    ReactDOM.createPortal(
                        <Toast
                            duration={3000}
                            placement="BottomCenter"
                            open={type === "toast"}
                            onClose={() => setType(undefined)}
                            style={{
                                width: "200px",
                                maxWidth: "400px",
                                minHeight: "10px",
                                borderWidth: "1px",
                                borderStyle: "solid",
                                borderColor: `var(--sap${severityFormatName(messages[0].severity)}Color)`,
                                backgroundColor: `var(--sap${severityFormatName(messages[0].severity)}Background)`,
                                color: `var(--sap${severityFormatName(messages[0].severity)}Color)`,
                                fontSize: "var(--sapFontHeader6Size)",
                                display: "flex",
                                alignItems: "center",
                                justifyContent: "center",
                            }}
                        >
                            {intl.formatMessage({ id: messages[0].key }, messages[0].params)}
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
