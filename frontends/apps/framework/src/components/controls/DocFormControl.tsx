import { ReactNode, useState, useRef, useEffect, useLayoutEffect } from "react"
import {
    FlexBox,
    FlexBoxAlignItems,
    FlexBoxDirection,
    IllustratedMessage,
    SplitterElement,
    SplitterLayout,
    Tab,
    TabContainer,
    TabContainerDomRef,
    Ui5CustomEvent,
    Button,
    Text,
} from "@ui5/webcomponents-react"
import "@ui5/webcomponents-fiori/dist/illustrations/ErrorScreen"
import "@ui5/webcomponents-fiori/dist/illustrations/NoData"
import { TabContainerTabSelectEventDetail } from "@ui5/webcomponents/dist/TabContainer"
import { Document, Page, Thumbnail, Outline, pdfjs } from "react-pdf"

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

import "react-pdf/dist/Page/TextLayer.css"
import "react-pdf/dist/Page/AnnotationLayer.css"
import { DocumentCallback } from "react-pdf/dist/shared/types"

pdfjs.GlobalWorkerOptions.workerSrc = new URL(
    "pdfjs-dist/build/pdf.worker.min.mjs",
    import.meta.url,
).toString()

// Outside of React component
const options = {
    cMapUrl: "/cmaps/",
}

// CSS for text layer selection and overflow containment
const pdfCss = `
/* Prevent page-level scrollbars */
html, body, #root {
  overflow: hidden !important;
  max-width: 100vw !important;
  max-height: 100vh !important;
}

/* Constrain document width */
.pdf-viewer-wrapper .react-pdf__Document {
  max-width: 100% !important;
}

/* Page container positioning context for layers */
.pdf-viewer-wrapper .react-pdf__Page {
  position: relative !important;
  display: inline-block !important;
  isolation: isolate !important;
  box-sizing: border-box !important;
}

/* Canvas layer renders visible PDF, sits below text layer */
.pdf-viewer-wrapper .react-pdf__Page__canvas {
  display: block !important;
  position: relative !important;
  z-index: 1 !important;
  user-select: none !important;
}

/* Text layer invisible overlay for text selection */
.pdf-viewer-wrapper .react-pdf__Page__textContent,
.pdf-viewer-wrapper .react-pdf__Page__textContent.textLayer {
  position: absolute !important;
  top: 0 !important;
  left: 0 !important;
  z-index: 2 !important;
  overflow: hidden !important;
  opacity: 1 !important;
  line-height: 1.0 !important;
}

/* Text spans invisible but selectable text */
.pdf-viewer-wrapper .react-pdf__Page__textContent > span,
.pdf-viewer-wrapper .react-pdf__Page__textContent.textLayer > span,
.pdf-viewer-wrapper .react-pdf__Page__textContent span[role="presentation"],
.pdf-viewer-wrapper .react-pdf__Page__textContent.textLayer span[role="presentation"] {
  color: transparent !important;
  position: absolute !important;
  white-space: pre !important;
  transform-origin: 0% 0% !important;
  cursor: text !important;
  pointer-events: auto !important;
  -webkit-user-select: text !important;
  -moz-user-select: text !important;
  user-select: text !important;
}

/* Selection highlight blue background when selecting */
.pdf-viewer-wrapper .react-pdf__Page__textContent span::selection,
.pdf-viewer-wrapper .react-pdf__Page__textContent.textLayer span::selection,
.pdf-viewer-wrapper .react-pdf__Page__textContent span[role="presentation"]::selection {
  background: rgba(0, 100, 255, 0.4) !important;
  color: transparent !important;
}

/* Firefox selection highlight */
.pdf-viewer-wrapper .react-pdf__Page__textContent span::-moz-selection,
.pdf-viewer-wrapper .react-pdf__Page__textContent.textLayer span::-moz-selection {
  background: rgba(0, 100, 255, 0.4) !important;
  color: transparent !important;
}

/* Marked content container don't block text selection */
.pdf-viewer-wrapper .react-pdf__Page__textContent .markedContent {
  pointer-events: none !important;
}

/* Marked content spans allow selection */
.pdf-viewer-wrapper .react-pdf__Page__textContent .markedContent > span[role="presentation"] {
  pointer-events: auto !important;
}

/* Hide BR elements to prevent layout issues */
.pdf-viewer-wrapper .react-pdf__Page__textContent br[role="presentation"] {
  display: none !important;
  user-select: none !important;
}

/* Annotation layer links and interactive elements */
.pdf-viewer-wrapper .react-pdf__Page__annotations,
.pdf-viewer-wrapper .react-pdf__Page__annotations.annotationLayer {
  position: absolute !important;
  top: 0 !important;
  left: 0 !important;
  z-index: 3 !important;
  pointer-events: none !important;
}

/* Hide structure tree accessibility feature not needed for display */
.pdf-viewer-wrapper .react-pdf__Page__structTree,
.pdf-viewer-wrapper .structTree {
  position: absolute !important;
  width: 0 !important;
  height: 0 !important;
  overflow: hidden !important;
  pointer-events: none !important;
  opacity: 0 !important;
}

/* Hide end of content marker */
.pdf-viewer-wrapper .react-pdf__Page__textContent .endOfContent {
  display: none !important;
}
`

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

    // PDF Viewer state
    const [numPages, setNumPages] = useState(0)
    const [pageNumber, setPageNumber] = useState(1)
    const [scale, setScale] = useState(1.0)
    const [baseWidth, setBaseWidth] = useState(600)
    const [loadError, setLoadError] = useState<Error | null>(null)
    const [retryKey, setRetryKey] = useState(0)
    const [showThumbnails, setShowThumbnails] = useState(false)
    const pdfContentRef = useRef<HTMLDivElement>(null)

    // Layout orientation state: false = side-by-side, true = PDF on top
    const [isVerticalLayout, setIsVerticalLayout] = useState(false)
    
    // Key to force SplitterLayout remount and reset to 50/50
    const [splitterKey, setSplitterKey] = useState(0)

    // Calculated values
    const pageWidth = baseWidth * scale

    // Inject styles into document head
    useEffect(() => {
        const styleId = "react-pdf-text-layer-styles"
        if (!document.getElementById(styleId)) {
            const style = document.createElement("style")
            style.id = styleId
            style.textContent = pdfCss
            document.head.appendChild(style)
        }
    }, [])

    // Track available width to size PDF pages consistently with the container
    useLayoutEffect(() => {
        const el = pdfContentRef.current
        if (!el) return

        const updateWidth = () => {
            if (el.clientWidth > 0) {
                setBaseWidth(el.clientWidth - 32)
            }
        }

        updateWidth()
        requestAnimationFrame(updateWidth)

        const ro = new ResizeObserver(() => requestAnimationFrame(updateWidth))
        ro.observe(el)

        return () => ro.disconnect()
    }, [])

    // Scroll to page when pageNumber changes
    useEffect(() => {
        pdfContentRef.current
            ?.querySelector(`[data-page="${pageNumber}"]`)
            ?.scrollIntoView({ behavior: "smooth", block: "start" })
    }, [pageNumber])

    // Reset state when URL changes
    useEffect(() => {
        if (data.docUrl) {
            setNumPages(0)
            setPageNumber(1)
            setLoadError(null)
            setScale(1.0)
        }
    }, [data.docUrl])

    /**
     * Toggles layout orientation and resets splitter to 50/50
     */
    const toggleLayout = () => {
        setIsVerticalLayout((v) => !v)
        setSplitterKey((k) => k + 1) // Force remount to reset 50/50 split
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

    /**
     * Handles successful PDF load
     */
    const onDocumentLoadSuccess = (pdf: DocumentCallback) => {
        console.log(`PDF loaded successfully! Pages: ${pdf.numPages}`)
        setNumPages(pdf.numPages)
        setLoadError(null)
    }

    /**
     * Handles PDF loading errors
     */
    const handleLoadError = (error: Error) => {
        console.error("PDF loading error:", error)
        setLoadError(error)
    }

    /**
     * Retries loading the PDF
     */
    const retryLoadPDF = () => {
        setLoadError(null)
        setRetryKey((k) => k + 1)
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
        <SplitterElement
            size="50%"
            style={{ overflow: "hidden" }}
        >
            {!data?.docUrl ? (
                <IllustratedMessage name="NoData" />
            ) : loadError ? (
                <FlexBox
                    direction="Column"
                    alignItems={FlexBoxAlignItems.Center}
                    style={{ padding: "2rem" }}
                >
                    <IllustratedMessage name="ErrorScreen" />
                    <p style={{ marginTop: "1rem" }}>Failed to load: {loadError.message}</p>
                    <Button onClick={retryLoadPDF} design="Emphasized">
                        Retry
                    </Button>
                </FlexBox>
            ) : (
                <FlexBox
                    direction="Row"
                    style={{ width: "100%", height: "100%", gap: ".5rem", overflow: "hidden" }}
                >
                    {/* Thumbnail Sidebar */}
                    {showThumbnails && (
                        <aside
                            style={{
                                width: "150px",
                                overflowY: "auto",
                                overflowX: "hidden",
                                borderRight: "1px solid #ccc",
                                padding: ".5rem",
                                flexShrink: 0,
                            }}
                        >
                            <Document
                                key={`thumbnails-${retryKey}`}
                                file={data.docUrl}
                                onLoadSuccess={({ numPages }) => setNumPages(numPages)}
                                onLoadError={handleLoadError}
                                options={options}
                            >
                                {Array.from({ length: numPages }, (_, i) => (
                                    <div
                                        key={i}
                                        onClick={() => setPageNumber(i + 1)}
                                        style={{
                                            cursor: "pointer",
                                            border:
                                                pageNumber === i + 1
                                                    ? "2px solid #0078d4"
                                                    : "2px solid transparent",
                                            marginBottom: ".25rem",
                                        }}
                                    >
                                        <Thumbnail pageNumber={i + 1} width={130} />
                                    </div>
                                ))}
                            </Document>
                        </aside>
                    )}

                    {/* Main PDF Viewer */}
                    <FlexBox
                        direction="Column"
                        style={{ flex: 1, overflow: "hidden", minWidth: 0 }}
                    >
                        {/* Navigation and Zoom Controls */}
                        <nav
                            style={{
                                display: "flex",
                                gap: ".5rem",
                                alignItems: "center",
                                padding: ".5rem",
                                borderBottom: "1px solid #ccc",
                                flexShrink: 0,
                            }}
                        >
                            <Button
                                icon="menu2"
                                onClick={() => setShowThumbnails((v) => !v)}
                                design="Transparent"
                                title="Toggle thumbnails"
                            />
                            <Button
                                icon="rotate"
                                onClick={toggleLayout}
                                design="Transparent"
                                title={
                                    isVerticalLayout
                                        ? "Switch to side-by-side layout"
                                        : "Switch to stacked layout (PDF on top)"
                                }
                            />
                            <div
                                style={{
                                    marginLeft: "auto",
                                    display: "flex",
                                    gap: ".5rem",
                                    alignItems: "center",
                                }}
                            >
                                <Button
                                    icon="less"
                                    onClick={() => setScale((s) => Math.max(s - 0.25, 0.5))}
                                    disabled={scale <= 0.5}
                                    design="Transparent"
                                    title="Zoom out"
                                />
                                <Text style={{ minWidth: "50px", textAlign: "center" }}>
                                    {Math.round(scale * 100)}%
                                </Text>
                                <Button
                                    icon="add"
                                    onClick={() => setScale((s) => Math.min(s + 0.25, 3))}
                                    disabled={scale >= 3}
                                    design="Transparent"
                                    title="Zoom in"
                                />
                            </div>
                        </nav>

                        {/* PDF Content */}
                        <div
                            ref={pdfContentRef}
                            className="pdf-viewer-wrapper"
                            style={{
                                flex: 1,
                                overflowY: "auto",
                                overflowX: "auto",
                                display: "flex",
                                flexDirection: "column",
                                alignItems: "center",
                                padding: "1rem",
                                boxSizing: "border-box",
                                minHeight: 0,
                                minWidth: 0,
                            }}
                        >
                            <div
                                style={{
                                    display: "flex",
                                    flexDirection: "column",
                                    alignItems: "center",
                                    minWidth: "fit-content",
                                }}
                            >
                                <Document
                                    key={`${data.docUrl}-${retryKey}`}
                                    file={data.docUrl}
                                    onLoadSuccess={onDocumentLoadSuccess}
                                    onLoadError={handleLoadError}
                                    loading={<div style={{ padding: "2rem" }}>Loading PDF…</div>}
                                    options={options}
                                >
                                    {numPages > 0 ? (
                                        <>
                                            <Outline
                                                onItemClick={({ pageNumber: pg }) =>
                                                    setPageNumber(pg)
                                                }
                                            />
                                            {Array.from({ length: numPages }, (_, i) => (
                                                <div
                                                    key={i}
                                                    data-page={i + 1}
                                                    style={{ marginBottom: "1rem" }}
                                                >
                                                    <Page
                                                        pageNumber={i + 1}
                                                        renderTextLayer={true}
                                                        renderAnnotationLayer={true}
                                                        width={pageWidth}
                                                    />
                                                </div>
                                            ))}
                                        </>
                                    ) : (
                                        <div style={{ padding: "2rem" }}>Preparing document…</div>
                                    )}
                                </Document>
                            </div>
                        </div>
                    </FlexBox>
                </FlexBox>
            )}
        </SplitterElement>
    )

    /**
     * Form/Tabs Panel Component
     */
    const FormPanel = (
        <SplitterElement
            size="50%"
            style={{ overflow: "hidden" }}
        >
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