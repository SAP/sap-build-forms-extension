import { useContext, useEffect, useState } from "react"

import { createUseStyles } from "react-jss"

import { Bar, Page, Title } from "@ui5/webcomponents-react"
import { ThemingParameters } from "@ui5/webcomponents-react-base"

import { ChangableIntlContext } from "commons"

import { Definition, findRoot } from "../../features/sessions/definitions"
import { getMessages } from "../../i18n/utils"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { checkValueHelps, loadValueHelp } from "../../features/valuehelps/valuehelpsSlice"
import { ROOT_ROW } from "../../features/sessions/forms"
import Control from "../controls/Control"
import { updateScreen } from "../../features/environment/environmentSlice"
import { SessionResponse } from "../../features/sessions/sessionActions"

const useStyles = createUseStyles({
    formPage: {
        "&::part(content)": {
            padding: 0,
        },
    },
})

/**
 *
 */
interface FormProps {
    response: SessionResponse
}

export default function (props: FormProps) {
    const classes = useStyles()
    const { response } = props
    const dispatch = useAppDispatch()
    const intlContext = useContext(ChangableIntlContext)
    const session = useAppSelector((state) => state.session)
    const [root, setRoot] = useState<Definition>()
    const [footer, setFooter] = useState<Definition>()
    const isShowMode = window.location.pathname.endsWith("/Show")

    useEffect(() => {
        // initial screen size
        dispatch(updateScreen())
        // register event-listener on resize event
        window.addEventListener("resize", () => {
            dispatch(updateScreen())
        })
    }, [])

    useEffect(() => {
        // console.log(`useEffect of FormPage started`)
        if (response) {
            // console.log(`useEffect is valid: ${response}`)
            const msgs = getMessages(response.locale)
            const text = response.def?.texts
            if (text) {
                intlContext.change(response.locale, { ...msgs, ...text })
            }
            const r = response.def ? findRoot(response.def) : undefined
            setRoot(r)
            setFooter(r?.footer)
            // console.log(`root set to ${r?.id}`)

            // handle value-helps. First check which are already available
            const p = dispatch(
                checkValueHelps({
                    locale: response.locale,
                    vhs: response.vhs,
                }),
            )
            // second steps: load the ones that are missing or to old
            p.then((action: any) => {
                const vhs: Record<string, boolean> = action.payload
                for (const name in vhs) {
                    if (!vhs[name]) {
                        dispatch(
                            loadValueHelp({
                                name,
                                sessionId: response.id,
                                locale: response.locale,
                            }),
                        )
                    }
                }
            })
        }
    }, [response])

    return (
        <Page
            header={
                <Bar design="Header">
                    <Title level="H1" style={{ fontSize: ThemingParameters.sapFontHeader3Size }}>
                        {session.headerTitle}
                    </Title>
                </Bar>
            }
            footer={
                <div slot="footer">
                    {footer && (
                        <Control
                            def={footer}
                            globalEd={!isShowMode}
                            texts={session.def!.texts}
                            vhs={session.vhs}
                            asTableCell={false}
                            withContainer={false}
                            rowId={ROOT_ROW}
                            design="Footer"
                        />
                    )}
                </div>
            }
            fixedFooter={true}
            className={classes.formPage}
            style={{
                backgroundColor: ThemingParameters.sapBackgroundColor,
                display: "block",
                height: "100vh",
            }}
        >
            <>
                {root && (
                    <Control
                        def={root}
                        globalEd={!isShowMode}
                        texts={session.def!.texts}
                        vhs={session.vhs}
                        asTableCell={false}
                        withContainer={true}
                        rowId={ROOT_ROW}
                    />
                )}
            </>
        </Page>
    )
}
