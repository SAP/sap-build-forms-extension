import {
    TabContainer,
    TabContainerDomRef,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"

import { DetailTabs, useVisualStore } from "../state/visual"
import { TabContainerTabSelectEventDetail } from "@ui5/webcomponents/dist/TabContainer"

import TabDetails from "./layout/TabDetails"
import TabTasks from "./layout/TabTasks"
import TabHistory from "./layout/TabHistory"
import TabFeeds from "./layout/TabFeeds"

export default function () {
    const detailTab = useVisualStore((state) => state.detailTab)
    const setDetailTab = useVisualStore((state) => state.setDetailTab)
    const selectedProcess = useVisualStore((state) => state.selectedProcess)

    const handleTabSelect = (
        evt: Ui5CustomEvent<TabContainerDomRef, TabContainerTabSelectEventDetail>,
    ) => {
        const tabId = (evt.detail.tab as HTMLElement).dataset.tabId as DetailTabs | undefined
        if (tabId) setDetailTab(tabId)
        else console.error("Unknown tab selected:", evt.detail.tab)
    }

    return (
        <TabContainer headerBackgroundDesign="Transparent" onTabSelect={handleTabSelect}>
            <TabDetails selectedProcess={selectedProcess} selected={detailTab === "details"} />
            <TabTasks selected={detailTab === "tasks"} />
            {/* <TabHistory selected={detailTab === "history"} /> */}
            <TabFeeds selectedProcess={selectedProcess} selected={detailTab === "feeds"} />
        </TabContainer>
    )
}
