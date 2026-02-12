import { useIntl } from "react-intl"

import {
    Grid,
    Label,
    Tab,
    TabContainer,
    TabContainerDomRef,
    Text,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"

import { useVisualStore } from "../state/visual"
import { TabContainerTabSelectEventDetail } from "@ui5/webcomponents/dist/TabContainer"
import { formatDate, getLanguage, Margin } from "commons"

function DetailTab() {
    const intl = useIntl()
    const process = useVisualStore((state) => state.selectedProcess)

    return (
        <div style={{ display: "grid", gridTemplateColumns: "12em auto", rowGap: Margin.MEDIUM }}>
            <Label for="fId">{intl.formatMessage({ id: "label_id" })}</Label>
            <Text id="fId">{process?.id}</Text>

            <Label for="fRefId">{intl.formatMessage({ id: "label_ref_id" })}</Label>
            <Text id="fRefId">{process?.refId}</Text>

            <Label for="fDescription" style={{ marginTop: Margin.LARGE }}>
                {intl.formatMessage({ id: "label_description" })}
            </Label>
            <Text id="fDescription" style={{ marginTop: Margin.LARGE }}>
                {process?.description}
            </Text>

            <Label for="fFunctionalId">{intl.formatMessage({ id: "label_functional_id" })}</Label>
            <Text id="fFunctionalId">{process?.functionalId}</Text>

            <Label for="fStatus" style={{ marginTop: Margin.LARGE }}>
                {intl.formatMessage({ id: "label_status" })}
            </Label>
            <Text id="fStatus" style={{ marginTop: Margin.LARGE }}>
                {process?.state}
            </Text>

            <Label for="fDetailState">{intl.formatMessage({ id: "label_detail_state" })}</Label>
            <Text id="fDetailState">{process?.detailState}</Text>

            <Label for="fStartedBy">{intl.formatMessage({ id: "label_started_by" })}</Label>
            <Text id="fStartedBy">{process?.startedBy}</Text>

            <Label for="fStartedAt">{intl.formatMessage({ id: "label_started_at" })}</Label>
            <Text id="fStartedAt">
                {process?.startedAt ? formatDate(process?.startedAt, getLanguage()) : ""}
            </Text>

            <Label for="fFinishedAt">{intl.formatMessage({ id: "label_finished_at" })}</Label>
            <Text id="fFinishedAt">
                {process?.finishedAt ? formatDate(process?.finishedAt, getLanguage()) : ""}
            </Text>

            <Label for="fScenario" style={{ marginTop: Margin.LARGE }}>
                {intl.formatMessage({ id: "label_scenario" })}
            </Label>
            <Text id="fScenario" style={{ marginTop: Margin.LARGE }}>
                {process?.scenarioName}
            </Text>

            <Label for="fScenarioVersion">
                {intl.formatMessage({ id: "label_scenario_version" })}
            </Label>
            <Text id="fScenarioVersion">{process?.scenarioVersion}</Text>
        </div>
    )
}

function TimelineTab() {
    return <></>
}

export default function () {
    const intl = useIntl()
    const detailTab = useVisualStore((state) => state.detailTab)
    const setDetailTab = useVisualStore((state) => state.setDetailTab)

    const handleTabSelect = (
        evt: Ui5CustomEvent<TabContainerDomRef, TabContainerTabSelectEventDetail>,
    ) => {
        if (evt.detail.tabIndex === 0) {
            setDetailTab("details")
        } else if (evt.detail.tabIndex === 1) {
            setDetailTab("timeline")
        } else {
            console.error(`Unkonwn tab selected (${evt.detail.tabIndex})`)
        }
    }

    return (
        <TabContainer headerBackgroundDesign="Transparent" onTabSelect={handleTabSelect}>
            <Tab
                selected={detailTab === "details"}
                icon="detail-more"
                text={intl.formatMessage({ id: "show_tab_details" })}
            >
                <DetailTab />
            </Tab>
            <Tab
                selected={detailTab === "timeline"}
                icon="list"
                text={intl.formatMessage({ id: "show_tabl_timeline" })}
            >
                <TimelineTab />
            </Tab>
        </TabContainer>
    )
}
