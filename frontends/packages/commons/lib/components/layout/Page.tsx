import { createContext, ReactNode, useContext, useEffect, useState } from "react"

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
    // @ts-ignore
    setHeader: (content: UI5WCSlotsNode): void => {},
    // @ts-ignore
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

/**
 *
 * @param props
 * @returns
 */
function PageProvider({ header, content, footer }: PageProps) {
    const [h, setH] = useState<UI5WCSlotsNode>(header)
    const [f, setF] = useState<UI5WCSlotsNode>(footer)

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
            <Page header={h} footer={f} style={{ width: "100vw", height: "100vh" }}>
                {content ?? <></>}
            </Page>
        </Context.Provider>
    )
}

export { PageProvider, Context as PageContext }

export const usePage = () => useContext(Context)
