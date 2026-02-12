import { MessageBox, MessageBoxType } from "@ui5/webcomponents-react"
import { JSX } from "react"

interface Props {
    handleClose: (e: any) => void
    messageBoxType: MessageBoxType
    open: boolean
    text: JSX.Element
}

export default function (props: Props) {
    return (
        <MessageBox
            onBeforeClose={function Ta() {}}
            onBeforeOpen={function Ta() {}}
            onClose={function Ta(e) {
                props.handleClose(e)
            }}
            type={props.messageBoxType}
            open={props.open}
        >
            {props.text}
        </MessageBox>
    )
}
