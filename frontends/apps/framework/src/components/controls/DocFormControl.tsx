import { ReactNode, useState } from "react"
import {
    FlexBox,
    FlexBoxAlignItems,
    FlexBoxDirection,
    SplitterElement,
    SplitterLayout,
    Tab,
    TabContainer,
    TabContainerDomRef,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import { TabContainerTabSelectEventDetail } from "@ui5/webcomponents/dist/TabContainer"

import { Card2, Severity, useMessages } from "commons"

import { DocFormData, ElementInfo, FormService, ROOT_ROW } from "../../features/sessions/forms"
import { ControlProps, getLabel, handleChange } from "./Control"
import { useAppDispatch, useAppSelector } from "../../features/store"
import SegmentControl from "./SegmentControl"
import { UIElement, UserEventType } from "../../features/sessions/definitions"
import { update } from "../../features/sessions/sessionSlice"
import { ElementProp } from "../../features/sessions/journal"
import { isEventValid } from "../../features/sessions/sessionActions"
import DialogControl from "./DialogControl"
import PdfViewerContent from "./PdfViewerContent"

function calcDesign(ei: ElementInfo | boolean | undefined): "Default" | "Positive" | "Negative" {
    if (typeof ei === "object") {
        if (ei.severity === Severity.Error) return "Negative"
        if (ei.severity === Severity.Success) return "Positive"
    }
    return "Default"
}

/**
 * PDF Document Viewer Component
 */
export default function (props: ControlProps) {
    const { def, texts, rowId } = props
    const dispatch = useAppDispatch()
    const form = useAppSelector((state) => state.session.form)
    const messages = useMessages()
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const data = ((element?.va as DocFormData | undefined) ?? {}) as DocFormData

    // Layout orientation state: false = side-by-side, true = PDF on top
    const [isVerticalLayout, setIsVerticalLayout] = useState(false)

    // Key to force SplitterLayout remount and reset to 50/50
    const [splitterKey, setSplitterKey] = useState(0)

    /**
     * Toggles layout orientation and resets splitter to 50/50
     */
    const toggleLayout = () => {
        setIsVerticalLayout((v) => !v)
        setSplitterKey((k) => k + 1)
    }

    /**
     * Handles tab selection by updating the selectedTab in form data and dispatching a change event.
     */
    const handleTabSelect = async (
        evt: Ui5CustomEvent<TabContainerDomRef, TabContainerTabSelectEventDetail>,
    ) => {
        const key = evt.detail.tab.getAttribute("data-key")
        if (key) {
            const isValidEvent = isEventValid(UserEventType.Action, def)
            await handleChange(dispatch, def, rowId, messages, { ...data, selectedTab: key })
            if (isValidEvent) evt.preventDefault()
        }
    }

    // Create tabs for each (visible) segment
    const tabs: ReactNode[] = []
    const dialogs: ReactNode[] = []
    let hasSelection = Boolean(data.selectedTab)

    def.elements?.forEach((it) => {
        const childElement = FormService.findElementByRowAndKey(rowId, it.key, form)
        if (!childElement?.vi) return

        // If there is no current segment defined we implicitly set the first segment as selected
        if (!hasSelection) {
            hasSelection = true
            setTimeout(
                () =>
                    dispatch(
                        update({
                            def,
                            rowId,
                            prop: ElementProp.Value,
                            value: { ...data, selectedTab: childElement.key },
                        }),
                    ),
                0,
            )
        }

        // Rendering of segments as tabs
        if (it.uiElement === UIElement.Segment) {
            const isSelected = it.key === data.selectedTab
            tabs.push(
                <Tab
                    key={it.id}
                    data-key={it.key}
                    text={getLabel(texts, it)}
                    design={calcDesign(childElement.msg)}
                    selected={isSelected}
                >
                    {isSelected && (
                        <FlexBox
                            alignItems={FlexBoxAlignItems.Stretch}
                            direction={FlexBoxDirection.Column}
                            fitContainer
                            style={{ rowGap: ".5rem", overflowX: "scroll", overflowY: "auto" }}
                        >
                            <SegmentControl {...props} def={it} rowId={ROOT_ROW} />
                        </FlexBox>
                    )}
                </Tab>,
            )
        }
        // Rendering of dialogs
        if (it.uiElement === UIElement.Dialog) {
            dialogs.push(<DialogControl {...props} def={it} rowId={ROOT_ROW} key={it.key} />)
        }
    })

    /**
     * PDF Viewer Panel Component
     */
    const PdfViewerPanel = (
        <SplitterElement size="50%" style={{ overflow: "hidden" }}>
            <PdfViewerContent docUrl={data.docUrl} onToggleLayout={toggleLayout} isVerticalLayout={isVerticalLayout} />
        </SplitterElement>
    )

    /**
     * Form/Tabs Panel Component
     */
    const FormPanel = (
        <SplitterElement size="50%" style={{ overflow: "hidden" }}>
            <div style={{ width: "100%", height: "100%", overflow: "auto" }}>
                <TabContainer
                    collapsed={false}
                    contentBackgroundDesign="Solid"
                    onTabSelect={handleTabSelect}
                >
                    {tabs}
                </TabContainer>
            </div>
        </SplitterElement>
    )

    return (
        <>
            <FlexBox
                direction="Column"
                style={{ height: "100%", width: "100%", overflow: "hidden" }}
            >
                {def.header && (
                    <Card2
                        style={{
                            padding: ".5rem",
                            marginLeft: "2rem",
                            marginRight: "2rem",
                            marginTop: ".5rem",
                        }}
                    >
                        <SegmentControl {...props} def={def.header} rowId={ROOT_ROW} />
                    </Card2>
                )}

                <SplitterLayout
                    key={splitterKey}
                    vertical={isVerticalLayout}
                    style={{ height: "100%", width: "100%", flex: 1, overflow: "hidden" }}
                >
                    {PdfViewerPanel}
                    {FormPanel}
                </SplitterLayout>
            </FlexBox>
            {dialogs}
        </>
    )
}