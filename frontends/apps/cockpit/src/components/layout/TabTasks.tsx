import { Tab, Text } from "@ui5/webcomponents-react"
import { useIntl } from "react-intl"

interface Props {
    selected?: boolean
}

export default function (props: Props) {
    const intl = useIntl()

    return (
        <Tab icon="approvals" text={intl.formatMessage({ id: "tab_tasks" })} selected={props.selected}>
            <Text>{intl.formatMessage({ id: "common_not_implemented" })}</Text>
        </Tab>
    )
}
