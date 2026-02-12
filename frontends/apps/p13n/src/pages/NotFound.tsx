import { createUseStyles } from "react-jss"

import { Bar, FlexBox, IllustratedMessage, Title } from "@ui5/webcomponents-react"
import "@ui5/webcomponents-fiori/dist/illustrations/PageNotFound.js"
import { ThemingParameters } from "@ui5/webcomponents-react-base"

import { PageProvider } from "../components/layout/Page"

const useStyles = createUseStyles({
    root: {
        width: "100%",
        height: "100%",
    },
})

export default function () {
    const classes = useStyles()

    return (
        <PageProvider
            header={
                <Bar>
                    <Title level="H1" style={{ fontSize: ThemingParameters.sapFontHeader3Size }}>
                        Personalization Frontend
                    </Title>
                </Bar>
            }
            content={
                <FlexBox direction="Column" justifyContent="Center" className={classes.root}>
                    <IllustratedMessage name="PageNotFound"></IllustratedMessage>
                </FlexBox>
            }
        />
    )
}
