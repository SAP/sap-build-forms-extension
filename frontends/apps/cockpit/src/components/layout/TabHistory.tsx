import { Tab, Text } from "@ui5/webcomponents-react"
import { useIntl } from "react-intl"

export default function () {
    const intl = useIntl()

    return (
        <Tab icon="history" text={intl.formatMessage({ id: "tab_history" })}>
            <Text>{intl.formatMessage({ id: "common_not_implemented" })}</Text>
        </Tab>
    )
}
