import { Tab, Text } from "@ui5/webcomponents-react"
import { useIntl } from "react-intl"

interface Props {
    selected?: boolean
}

export default function TabHistory(props: Props) {
    const intl = useIntl()

    return (
        <Tab icon="history" text={intl.formatMessage({ id: "tab_history" })} selected={props.selected} data-tab-id="history">
            <Text>{intl.formatMessage({ id: "common_not_implemented" })}</Text>
        </Tab>
    )
}
