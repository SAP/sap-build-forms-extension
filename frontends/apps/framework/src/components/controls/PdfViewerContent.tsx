import { useState, useRef, useEffect, useLayoutEffect } from "react"
import {
    FlexBox,
    FlexBoxAlignItems,
    FlexBoxDirection,
    IllustratedMessage,
    Button,
    Text,
} from "@ui5/webcomponents-react"
import "@ui5/webcomponents-fiori/dist/illustrations/ErrorScreen"
import "@ui5/webcomponents-fiori/dist/illustrations/NoData"
import { Document, Page, Thumbnail, pdfjs } from "react-pdf"

import "react-pdf/dist/Page/TextLayer.css"
import "react-pdf/dist/Page/AnnotationLayer.css"
import { DocumentCallback } from "react-pdf/dist/shared/types"

pdfjs.GlobalWorkerOptions.workerSrc = new URL(
    "pdfjs-dist/build/pdf.worker.min.mjs",
    import.meta.url,
).toString()

const options = {
    cMapUrl: "/cmaps/",
}

const pdfCss = `
/* Force html/body to never show scrollbars — the app manages its own scrolling */
html, body {
    margin: 0 !important;
    padding: 0 !important;
    width: 100% !important;
    height: 100% !important;
    overflow: hidden !important;
}

/* Root React container must fill exactly its parent, no vw/vh */
#root {
    width: 100% !important;
    height: 100% !important;
    max-width: 100vw !important;
    max-height: 100vh !important;
    overflow: hidden !important;
    box-sizing: border-box !important;
}

/* UI5 Page component sometimes uses 100vh — clamp it */
ui5-page {
    max-height: 100% !important;
    height: 100% !important;
    overflow: hidden !important;
}

/* Root document container — never wider than parent */
.pdf-viewer-wrapper .react-pdf__Document {
  max-width: 100% !important;
  width: 100% !important;
  display: flex !important;
  flex-direction: column !important;
  align-items: center !important;
  box-sizing: border-box !important;
  min-width: 0 !important;
}

/* Page container — hard clamp so nothing can push outer layout */
.pdf-viewer-wrapper .react-pdf__Page {
  position: relative !important;
  display: block !important;
  isolation: isolate !important;
  box-sizing: border-box !important;
  max-width: 100% !important;
  min-width: 0 !important;
  height: auto !important;
}

/* Canvas — clamp CSS display width to container. Backing store stays sharp. */
.pdf-viewer-wrapper .react-pdf__Page__canvas {
  display: block !important;
  position: relative !important;
  z-index: 1 !important;
  user-select: none !important;
  max-width: 100% !important;
  height: auto !important;
}

/* Text layer overlay — must match canvas size, not overflow */
.pdf-viewer-wrapper .react-pdf__Page__textContent,
.pdf-viewer-wrapper .react-pdf__Page__textContent.textLayer {
  position: absolute !important;
  top: 0 !important;
  left: 0 !important;
  z-index: 2 !important;
  overflow: hidden !important;
  opacity: 1 !important;
  line-height: 1.0 !important;
  max-width: 100% !important;
}

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

.pdf-viewer-wrapper .react-pdf__Page__textContent span::selection,
.pdf-viewer-wrapper .react-pdf__Page__textContent.textLayer span::selection,
.pdf-viewer-wrapper .react-pdf__Page__textContent span[role="presentation"]::selection {
  background: rgba(0, 100, 255, 0.4) !important;
  color: transparent !important;
}

.pdf-viewer-wrapper .react-pdf__Page__textContent span::-moz-selection,
.pdf-viewer-wrapper .react-pdf__Page__textContent.textLayer span::-moz-selection {
  background: rgba(0, 100, 255, 0.4) !important;
  color: transparent !important;
}

.pdf-viewer-wrapper .react-pdf__Page__textContent .markedContent {
  pointer-events: none !important;
}
.pdf-viewer-wrapper .react-pdf__Page__textContent .markedContent > span[role="presentation"] {
  pointer-events: auto !important;
}

.pdf-viewer-wrapper .react-pdf__Page__textContent br[role="presentation"] {
  display: none !important;
  user-select: none !important;
}

/* Annotation layer — must not overflow */
.pdf-viewer-wrapper .react-pdf__Page__annotations,
.pdf-viewer-wrapper .react-pdf__Page__annotations.annotationLayer {
  position: absolute !important;
  top: 0 !important;
  left: 0 !important;
  z-index: 3 !important;
  pointer-events: none !important;
  max-width: 100% !important;
}

.pdf-viewer-wrapper .react-pdf__Page__structTree,
.pdf-viewer-wrapper .structTree {
  position: absolute !important;
  width: 0 !important;
  height: 0 !important;
  overflow: hidden !important;
  pointer-events: none !important;
  opacity: 0 !important;
}

.pdf-viewer-wrapper .react-pdf__Page__textContent .endOfContent {
  display: none !important;
}

/* Hide Outline entirely — common cause of horizontal overflow */
.pdf-viewer-wrapper .react-pdf__Outline {
  display: none !important;
}
`

export interface PdfViewerContentProps {
    docUrl: string | undefined
    onToggleLayout?: () => void
    isVerticalLayout?: boolean
}

export default function PdfViewerContent({
    docUrl,
    onToggleLayout,
    isVerticalLayout,
}: PdfViewerContentProps) {
    const [numPages, setNumPages] = useState(0)
    const [pageNumber, setPageNumber] = useState(1)
    const [scale, setScale] = useState(1.0)
    const [baseWidth, setBaseWidth] = useState(600)
    const [loadError, setLoadError] = useState<Error | null>(null)
    const [retryKey, setRetryKey] = useState(0)
    const [showThumbnails, setShowThumbnails] = useState(false)
    const pdfContentRef = useRef<HTMLDivElement>(null)

    const pageWidth = baseWidth * scale

    // Inject PDF CSS once
    useEffect(() => {
        const styleId = "react-pdf-text-layer-styles"
        if (!document.getElementById(styleId)) {
            const style = document.createElement("style")
            style.id = styleId
            style.textContent = pdfCss
            document.head.appendChild(style)
        }
    }, [])

    // Robust width tracking: measure now, next frame, then again after 100ms & 500ms
    // to handle tab/layout activation timing in production bundles.
    useLayoutEffect(() => {
        const el = pdfContentRef.current
        if (!el) return

        const updateWidth = () => {
            const w = el.clientWidth
            if (w > 0 && w < 5000) {
                setBaseWidth(Math.max(100, w - 40))
            }
        }

        updateWidth()
        requestAnimationFrame(updateWidth)
        const t1 = setTimeout(updateWidth, 100)
        const t2 = setTimeout(updateWidth, 500)

        const ro = new ResizeObserver(() => requestAnimationFrame(updateWidth))
        ro.observe(el)

        return () => {
            clearTimeout(t1)
            clearTimeout(t2)
            ro.disconnect()
        }
    }, [])

    // Scroll to page when pageNumber changes
    useEffect(() => {
        pdfContentRef.current
            ?.querySelector(`[data-page="${pageNumber}"]`)
            ?.scrollIntoView({ behavior: "smooth", block: "start" })
    }, [pageNumber])

    // Reset state when URL changes
    useEffect(() => {
        if (docUrl) {
            setNumPages(0)
            setPageNumber(1)
            setLoadError(null)
            setScale(1.0)
        }
    }, [docUrl])

    const onDocumentLoadSuccess = (pdf: DocumentCallback) => {
        setNumPages(pdf.numPages)
        setLoadError(null)
    }

    const handleLoadError = (error: Error) => {
        setLoadError(error)
    }

    const retryLoadPDF = () => {
        setLoadError(null)
        setRetryKey((k) => k + 1)
    }

    if (!docUrl) {
        return <IllustratedMessage name="NoData" />
    }

    if (loadError) {
        return (
            <FlexBox
                direction={FlexBoxDirection.Column}
                alignItems={FlexBoxAlignItems.Center}
                style={{ padding: "2rem" }}
            >
                <IllustratedMessage name="ErrorScreen" />
                <p style={{ marginTop: "1rem" }}>Failed to load: {loadError.message}</p>
                <Button onClick={retryLoadPDF} design="Emphasized">
                    Retry
                </Button>
            </FlexBox>
        )
    }

    return (
        <FlexBox
            direction="Row"
            style={{
                width: "100%",
                height: "100%",
                gap: ".5rem",
                overflow: "hidden",
                minWidth: 0,
                minHeight: 0,
                boxSizing: "border-box",
                contain: "layout size",
            }}
        >
            {showThumbnails && (
                <aside
                    style={{
                        width: "150px",
                        overflowY: "auto",
                        overflowX: "hidden",
                        borderRight: "1px solid #ccc",
                        padding: ".5rem",
                        flexShrink: 0,
                        boxSizing: "border-box",
                    }}
                >
                    <Document
                        key={`thumbnails-${retryKey}`}
                        file={docUrl}
                        onLoadSuccess={({ numPages: n }) => setNumPages(n)}
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

            <FlexBox
                direction="Column"
                style={{
                    flex: 1,
                    overflow: "hidden",
                    minWidth: 0,
                    minHeight: 0,
                }}
            >
                <nav
                    style={{
                        display: "flex",
                        gap: ".5rem",
                        alignItems: "center",
                        padding: ".5rem",
                        borderBottom: "1px solid #ccc",
                        flexShrink: 0,
                        minWidth: 0,
                        boxSizing: "border-box",
                    }}
                >
                    <Button
                        icon="menu2"
                        onClick={() => setShowThumbnails((v) => !v)}
                        design="Transparent"
                        title="Toggle thumbnails"
                    />
                    {onToggleLayout && (
                        <Button
                            icon="rotate"
                            onClick={onToggleLayout}
                            design="Transparent"
                            title={
                                isVerticalLayout
                                    ? "Switch to side-by-side layout"
                                    : "Switch to stacked layout (PDF on top)"
                            }
                        />
                    )}
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

                {/* The one and only scroll container inside the PDF viewer */}
                <div
                    ref={pdfContentRef}
                    className="pdf-viewer-wrapper"
                    style={{
                        flex: 1,
                        overflowY: "auto",
                        overflowX: "hidden",
                        padding: "1rem",
                        boxSizing: "border-box",
                        minHeight: 0,
                        minWidth: 0,
                        contain: "layout size",
                    }}
                >
                    {/* Inner wrapper: horizontally scrollable only when zoomed in */}
                    <div
                        style={{
                            width: "100%",
                            overflowX: scale > 1 ? "auto" : "hidden",
                            overflowY: "hidden",
                            boxSizing: "border-box",
                        }}
                    >
                        <div
                            style={{
                                display: "flex",
                                flexDirection: "column",
                                alignItems: "center",
                                width: scale > 1 ? "fit-content" : "100%",
                                minWidth: scale > 1 ? "100%" : 0,
                                margin: "0 auto",
                            }}
                        >
                            <Document
                                key={`${docUrl}-${retryKey}`}
                                file={docUrl}
                                onLoadSuccess={onDocumentLoadSuccess}
                                onLoadError={handleLoadError}
                                loading={
                                    <div style={{ padding: "2rem" }}>Loading PDF…</div>
                                }
                                options={options}
                            >
                                {numPages > 0 ? (
                                    Array.from({ length: numPages }, (_, i) => (
                                        <div
                                            key={i}
                                            data-page={i + 1}
                                            style={{
                                                marginBottom: "1.5rem",
                                                boxShadow: "0 2px 8px rgba(0,0,0,0.3)",
                                                background: "#fff",
                                                maxWidth: "100%",
                                            }}
                                        >
                                            <Page
                                                pageNumber={i + 1}
                                                renderTextLayer={true}
                                                renderAnnotationLayer={true}
                                                width={pageWidth}
                                            />
                                        </div>
                                    ))
                                ) : (
                                    <div style={{ padding: "2rem" }}>
                                        Preparing document…
                                    </div>
                                )}
                            </Document>
                        </div>
                    </div>
                </div>
            </FlexBox>
        </FlexBox>
    )
}