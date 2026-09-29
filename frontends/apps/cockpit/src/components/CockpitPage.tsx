import { useEffect, JSX } from "react"

import { useIntl } from "react-intl"

import {
    DynamicPage,
    DynamicPageHeader,
    DynamicPageTitle,
    Title,
    Toolbar,
    ToolbarButton,
} from "@ui5/webcomponents-react"

import { getLanguage, Margin, useMessages } from "commons"

import { useProcessStore } from "../state/processes"
import { Settings, useVisualStore } from "../state/visual"
import ProcessListView from "./ProcessListView"
import ProcessListFilter from "./ProcessListFilter"
import ProcessDetailsView from "./ProcessDetailsView"
import ProcessFormView from "./ProcessFormView"

export default function () {
    const intl = useIntl()
    const messages = useMessages()
    const visualState = useVisualStore((state) => state)
    const loadSettings = useVisualStore((state) => state.loadSettings)
    const findProcesses = useProcessStore((state) => state.findProcesses)
    const setFilter = useProcessStore((state) => state.setFilter)
    const filter = useProcessStore((state) => state.filter)

    useEffect(() => {
        loadSettings(messages, getLanguage()).then((data) => {
            useProcessStore.getState().initFilter((data as any).data as Settings)
            findProcesses(messages, useProcessStore.getState().filter)
        })
    }, [])

    return (
        <DynamicPage
            style={{ width: "100vw", height: "100vh" }}
            titleArea={
                <DynamicPageTitle
                    actionsBar={
                        visualState.view === "list" ? (
                            <Toolbar>
                                <ToolbarButton
                                    text={intl.formatMessage({ id: "button_clear" })}
                                    onClick={() => setFilter({ profiles: filter.profiles })}
                                />
                                <ToolbarButton
                                    design="Emphasized"
                                    text={intl.formatMessage({ id: "button_go" })}
                                    onClick={() => findProcesses(messages, useProcessStore.getState().filter)}
                                />
                            </Toolbar>
                        ) : undefined
                    }
                    heading={<Title level="H1">{intl.formatMessage({ id: "app_title" })}</Title>}
                    subheading={
                        <Title level="H2">
                            {intl.formatMessage(
                                { id: "app_subtitle_" + visualState.view },
                                { ...visualState.selectedProcess },
                            )}
                        </Title>
                    }
                    snappedHeading={
                        <Title level="H1">{intl.formatMessage({ id: "app_title" })}</Title>
                    }
                    snappedSubheading={
                        <Title level="H2">
                            {intl.formatMessage(
                                { id: "app_subtitle_" + visualState.view },
                                { ...visualState.selectedProcess },
                            )}
                        </Title>
                    }
                    navigationBar={
                        <>
                            {visualState.view !== "list" && (
                                <Toolbar design="Transparent">
                                    <ToolbarButton
                                        design="Transparent"
                                        icon="decline"
                                        onClick={() => visualState.setView("list")}
                                    />
                                </Toolbar>
                            )}
                        </>
                    }
                />
            }
            headerArea={
                <DynamicPageHeader>
                    {visualState.view === "list" && <ProcessListFilter />}
                </DynamicPageHeader>
            }
        >
            <div style={{ marginTop: Margin.MEDIUM }}>
                {visualState.view === "list" && <ProcessListView />}
                {visualState.view === "details" && <ProcessDetailsView />}
                {visualState.view === "form" && <ProcessFormView />}
            </div>
        </DynamicPage>
    )
}
