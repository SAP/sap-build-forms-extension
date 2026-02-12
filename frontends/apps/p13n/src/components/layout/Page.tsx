import { createContext, ReactNode, useContext, useEffect, useState } from "react"

import { createUseStyles } from "react-jss"

import { Page, UI5WCSlotsNode } from "@ui5/webcomponents-react"

/**
 *
 */
interface PageInterface {
    setHeader(content: UI5WCSlotsNode): void
    setFooter(content: UI5WCSlotsNode): void
}

/**
 *
 */
const Context = createContext<PageInterface>({
    setHeader: (content: UI5WCSlotsNode): void => {},
    setFooter: (content: UI5WCSlotsNode): void => {},
})

/**
 *
 */
export interface PageProps {
    header?: UI5WCSlotsNode
    content?: ReactNode
    footer?: UI5WCSlotsNode
}

const useStyles = createUseStyles({
    root: {
        width: "100vw",
        height: "100vh",
    },
})

/**
 *
 * @param props
 * @returns
 */
function PageProvider({ header, content, footer }: PageProps) {
    const [h, setH] = useState<UI5WCSlotsNode>(header)
    const [f, setF] = useState<UI5WCSlotsNode>(footer)
    const classes = useStyles()

    const intf = {
        setHeader: (content: UI5WCSlotsNode): void => {
            setH(content)
        },
        setFooter: (content: UI5WCSlotsNode): void => {
            setF(content)
        },
    }

    useEffect(() => {
        setH(header)
    }, [header])

    useEffect(() => {
        setF(footer)
    }, [footer])

    return (
        <Context.Provider value={intf}>
            <Page header={h} footer={f} className={classes.root}>
                {content ?? <></>}
            </Page>
        </Context.Provider>
    )
}

export { PageProvider, Context as PageContext }

export const usePage = () => useContext(Context)
