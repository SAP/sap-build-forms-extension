import { useIntl } from "react-intl"

import { Bar, FlexBox, IllustratedMessage, Title } from "@ui5/webcomponents-react"
import "@ui5/webcomponents-fiori/dist/illustrations/PageNotFound.js"
import { ThemingParameters } from "@ui5/webcomponents-react-base"

import { PageProvider } from "../components/layout/Page"

export function NotFound() {
    const intl = useIntl()

    return (
        <PageProvider
            header={
                <Bar>
                    <Title level="H1" style={{ fontSize: ThemingParameters.sapFontHeader3Size }}>
                        {intl.formatMessage({ id: "app_title", defaultMessage: "FORMS" })}
                    </Title>
                </Bar>
            }
            content={
                <FlexBox
                    direction="Column"
                    justifyContent="Center"
                    style={{ width: "100%", height: "100%" }}
                >
                    <IllustratedMessage name="PageNotFound"></IllustratedMessage>
                </FlexBox>
            }
        />
    )
}
