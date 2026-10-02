import { useEffect, JSX } from "react"

import { useIntl } from "react-intl"

import {
    Bar,
    Button,
    DynamicPage,
    DynamicPageHeader,
    DynamicPageTitle,
    Form,
    FormItem,
    Label,
    Text,
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
    const cancelProcess = useProcessStore((state) => state.cancelProcess)
    const useAsTemplate = useProcessStore((state) => state.useAsTemplate)

    const handleCancelProcess = async () => {
        if (!visualState.selectedProcess) return
        await cancelProcess(messages, visualState.selectedProcess.id)
    }

    const handleUseAsTemplate = async () => {
        if (!visualState.selectedProcess) return
        await useAsTemplate(messages, visualState.selectedProcess.id)
    }

    useEffect(() => {
        loadSettings(messages, getLanguage()).then((data) => {
            useProcessStore.getState().initFilter((data as any).data as Settings)
            findProcesses(messages, useProcessStore.getState().filter)
        })
    }, [])

    return (
        <DynamicPage
            style={{ width: "100vw", height: "100vh" }}
            showFooter={visualState.view === "details"}
            footerArea={
                <Bar design="Footer"
                    endContent={
                        <>
                            <Button
                                icon="decline"
                                disabled={!visualState.selectedProcess?.cancelable}
                                onClick={handleCancelProcess}
                            >
                                {intl.formatMessage({ id: "button_cancel_process" })}
                            </Button>
                            <Button
                                icon="duplicate"
                                disabled={!visualState.selectedProcess?.templatable}
                                onClick={handleUseAsTemplate}
                            >
                                {intl.formatMessage({ id: "button_use_as_template" })}
                            </Button>
                        </>
                    }
                />
            }
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
                    heading={
                        <Title level="H1">
                            {visualState.view !== "list" && visualState.selectedProcess
                                ? visualState.selectedProcess.scenarioName
                                : intl.formatMessage({ id: "app_title" })}
                        </Title>
                    }
                    subheading={
                        <Title level="H2">
                            {intl.formatMessage(
                                { id: "app_subtitle_" + visualState.view },
                                { ...visualState.selectedProcess },
                            )}
                        </Title>
                    }
                    snappedHeading={
                        <Title level="H1">
                            {visualState.view !== "list" && visualState.selectedProcess
                                ? visualState.selectedProcess.scenarioName
                                : intl.formatMessage({ id: "app_title" })}
                        </Title>
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
                    {visualState.view === "details" && visualState.selectedProcess && (
                        <Form layout="S1 M1 L1 XL1" labelSpan="S5 M5 L5 XL5" style={{ width: "fit-content", minWidth: "40rem", padding: `0 ${Margin.MEDIUM}` }}>
                            <FormItem
                                labelContent={
                                    <Label>{intl.formatMessage({ id: "label_functional_id" })}</Label>
                                }
                            >
                                <Text>{visualState.selectedProcess.functionalId}</Text>
                            </FormItem>
                            <FormItem
                                labelContent={
                                    <Label>{intl.formatMessage({ id: "label_description" })}</Label>
                                }
                            >
                                <Text>{visualState.selectedProcess.description}</Text>
                            </FormItem>
                            <FormItem
                                labelContent={
                                    <Label>
                                        {intl.formatMessage({ id: "label_additional_information" })}
                                    </Label>
                                }
                            >
                                <Text>{visualState.selectedProcess.additionalInformation}</Text>
                            </FormItem>
                        </Form>
                    )}
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
