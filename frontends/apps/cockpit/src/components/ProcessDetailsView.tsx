import {
    TabContainer,
    TabContainerDomRef,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"

import { useVisualStore } from "../state/visual"
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
        const index = evt.detail.tabIndex
        if (index === 0) setDetailTab("details")
        else if (index === 1) setDetailTab("tasks")
        // else if (index === 2) setDetailTab("history")
        else if (index === 3) setDetailTab("feeds")
        else console.error(`Unknown tab selected (${index})`)
    }

    return (
        <TabContainer headerBackgroundDesign="Transparent" onTabSelect={handleTabSelect}>
            <TabDetails selectedProcess={selectedProcess} selected={detailTab === "details"} />
            <TabTasks selected={detailTab === "tasks"} />
            {/* <TabHistory selected={detailTab === "history"} /> */}
            <TabFeeds selected={detailTab === "feeds"} />
        </TabContainer>
    )
}
