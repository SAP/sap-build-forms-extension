import { useState, useRef, useEffect, useCallback } from "react"
import { createPortal } from "react-dom"
import { Button } from "@ui5/webcomponents-react"
import { FormService } from "../../features/sessions/forms"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { update } from "../../features/sessions/sessionSlice"
import { ElementProp } from "../../features/sessions/journal"
import { ControlProps, getLabel } from "./Control"
import PdfViewerContent from "./PdfViewerContent"

const PANEL_MIN_W = 400
const PANEL_MIN_H = 300
const PANEL_DEFAULT_W = 700
const PANEL_DEFAULT_H = 600

function FloatingPdfPanel(props: ControlProps & { docUrl: string | undefined }) {
    const { def, rowId, texts, docUrl } = props
    const dispatch = useAppDispatch()
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    const initialW = def.size?.width ? parseInt(def.size.width) || PANEL_DEFAULT_W : PANEL_DEFAULT_W
    const initialH = def.size?.height ? parseInt(def.size.height) || PANEL_DEFAULT_H : PANEL_DEFAULT_H

    const [pos, setPos] = useState({ x: 80, y: 80 })
    const [size, setSize] = useState({ w: initialW, h: initialH })
    const dragRef = useRef<{ startX: number; startY: number; origX: number; origY: number } | null>(null)
    const resizeRef = useRef<{ startX: number; startY: number; origW: number; origH: number } | null>(null)
    const panelRef = useRef<HTMLDivElement>(null)

    const title = getLabel(texts, def)

    const onHeaderMouseDown = useCallback(
        (e: React.MouseEvent) => {
            e.preventDefault()
            dragRef.current = { startX: e.clientX, startY: e.clientY, origX: pos.x, origY: pos.y }
        },
        [pos],
    )

    const onResizeMouseDown = useCallback(
        (e: React.MouseEvent) => {
            e.preventDefault()
            e.stopPropagation()
            resizeRef.current = { startX: e.clientX, startY: e.clientY, origW: size.w, origH: size.h }
        },
        [size],
    )

    useEffect(() => {
        const onMouseMove = (e: MouseEvent) => {
            if (dragRef.current) {
                const dx = e.clientX - dragRef.current.startX
                const dy = e.clientY - dragRef.current.startY
                setPos({ x: dragRef.current.origX + dx, y: dragRef.current.origY + dy })
            }
            if (resizeRef.current) {
                const dx = e.clientX - resizeRef.current.startX
                const dy = e.clientY - resizeRef.current.startY
                setSize({
                    w: Math.max(PANEL_MIN_W, resizeRef.current.origW + dx),
                    h: Math.max(PANEL_MIN_H, resizeRef.current.origH + dy),
                })
            }
        }
        const onMouseUp = () => {
            dragRef.current = null
            resizeRef.current = null
        }
        window.addEventListener("mousemove", onMouseMove)
        window.addEventListener("mouseup", onMouseUp)
        return () => {
            window.removeEventListener("mousemove", onMouseMove)
            window.removeEventListener("mouseup", onMouseUp)
        }
    }, [])

    const close = () =>
        dispatch(update({ def, rowId, prop: ElementProp.Visible, value: false }))

    if (!element?.vi) return null

    return createPortal(
        <div
            ref={panelRef}
            style={{
                position: "fixed",
                left: pos.x,
                top: pos.y,
                width: size.w || PANEL_DEFAULT_W,
                height: size.h || PANEL_DEFAULT_H,
                zIndex: 1000,
                display: "flex",
                flexDirection: "column",
                background: "var(--sapBackgroundColor, #fff)",
                border: "1px solid var(--sapGroup_ContentBorderColor, #d9d9d9)",
                boxShadow: "0 4px 24px rgba(0,0,0,0.25)",
                borderRadius: "4px",
                overflow: "hidden",
                userSelect: dragRef.current || resizeRef.current ? "none" : "auto",
                contain: "layout size",
            }}
        >
            {/* Title bar — drag handle */}
            <div
                onMouseDown={onHeaderMouseDown}
                style={{
                    display: "flex",
                    alignItems: "center",
                    padding: "0 .5rem",
                    height: "2.5rem",
                    background: "var(--sapShellColor, #0a6ed1)",
                    color: "var(--sapShell_TextColor, #fff)",
                    cursor: "move",
                    flexShrink: 0,
                    userSelect: "none",
                }}
            >
                <span
                    style={{
                        flex: 1,
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                        whiteSpace: "nowrap",
                    }}
                >
                </span>
                <Button
                    icon="decline"
                    design="Transparent"
                    title="Close"
                    onClick={close}
                    style={{ color: "var(--sapShell_TextColor, #fff)" }}
                />
            </div>

            {/* PDF content */}
            <div
                style={{
                    flex: 1,
                    overflow: "hidden",
                    minHeight: 0,
                    minWidth: 0,
                    display: "flex",
                    position: "relative",
                }}
            >
                <PdfViewerContent docUrl={docUrl} />
            </div>

            {/* Resize handle */}
            <div
                onMouseDown={onResizeMouseDown}
                style={{
                    position: "absolute",
                    right: 0,
                    bottom: 0,
                    width: "16px",
                    height: "16px",
                    cursor: "se-resize",
                    background: "transparent",
                    zIndex: 1,
                }}
            />
        </div>,
        document.body,
    )
}

export default function PdfViewerControl(props: ControlProps) {
    const { def, rowId } = props
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    const url = (element?.va as string | undefined) || undefined

    if (def.floating) {
        return <FloatingPdfPanel {...props} docUrl={url} />
    }

    const w = def.size?.width || "97%"
    const h = def.size?.height || "600px"

    return (
        <div
            style={{
                width: w,
                height: h,
                overflow: "hidden",
                minWidth: 0,
                minHeight: 0,
                boxSizing: "border-box",
                display: "flex",
                position: "relative",
                contain: "layout size",
            }}
        >
            <PdfViewerContent docUrl={url} />
        </div>
    )
}