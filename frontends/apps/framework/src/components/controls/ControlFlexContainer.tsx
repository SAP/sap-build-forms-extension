import { PropsWithChildren, useState } from "react"
import { useIntl, IntlShape } from "react-intl"

import { FlexBox, FlexBoxJustifyContent, Icon, Label, Popover, Text } from "@ui5/webcomponents-react"

import "@ui5/webcomponents-icons/dist/sys-help.js"
import { DataType, Definition, UIElement } from "../../features/sessions/definitions"
import { Element, FormService } from "../../features/sessions/forms"
import { useAppSelector } from "../../features/store"
import { getDoc, getLabel } from "./Control"

/**
 *
 */
interface Props {
    def: Definition
    texts: Record<string, string>
    value?: Element
    asTableCell: boolean
    rowId: string
    justifyContent?:
        | "Start"
        | "Center"
        | "End"
        | FlexBoxJustifyContent
        | "SpaceAround"
        | "SpaceBetween"
        | undefined
}

export function computeValidationHints(def: Definition, intl: IntlShape): string[] {
    const hints: string[] = []
    const limits = def.limits
    if (!limits) return hints

    const isCount = def.uiElement === UIElement.Table || def.uiElement === UIElement.Attachment
    const isString = !isCount && (def.dataType === DataType.String || def.dataType === undefined)
    const isNumeric = def.dataType === DataType.Int || def.dataType === DataType.Decimal
    const { min, max, fixedLength, fixedFractions, showMinHint, showMaxHint, showFixedHint } = limits

    if (showFixedHint && fixedLength !== undefined) {
        const key = def.dataType === DataType.Decimal
            ? "default.msg.validation.hint.fixed.decimal"
            : "default.msg.validation.hint.fixed.string"
        const values: Record<string, string> = { length: fixedLength }
        if (def.dataType === DataType.Decimal && fixedFractions !== undefined) {
            values.fractions = fixedFractions
        }
        hints.push(intl.formatMessage({ id: key }, values))
    }

    const wantMin = showMinHint && min !== undefined
    const wantMax = showMaxHint && max !== undefined
    if (wantMin || wantMax) {
        let kind: "string" | "number" | "count" | "date" | "datetime" | "time" | null = null
        let formatValue: (v: string) => string = (v) => v

        if (isCount) kind = "count"
        else if (isString) kind = "string"
        else if (isNumeric) kind = "number"
        else if (def.dataType === DataType.Date) {
            kind = "date"
            formatValue = (v) => intl.formatDate(v, { dateStyle: "medium" })
        } else if (def.dataType === DataType.DateTime) {
            kind = "datetime"
            formatValue = (v) => intl.formatDate(v, { dateStyle: "medium", timeStyle: "medium" })
        } else if (def.dataType === DataType.Time) {
            kind = "time"
            formatValue = (v) => {
                const [h, m] = v.split(":")
                const d = new Date()
                d.setHours(parseInt(h) || 0, parseInt(m) || 0, 0, 0)
                return intl.formatTime(d, { timeStyle: "short" })
            }
        }

        if (kind) {
            if (wantMin && wantMax) {
                hints.push(intl.formatMessage(
                    { id: `default.msg.validation.hint.${kind}.range` },
                    { min: formatValue(min!), max: formatValue(max!) }
                ))
            } else if (wantMin) {
                hints.push(intl.formatMessage(
                    { id: `default.msg.validation.hint.${kind}.min` },
                    { min: formatValue(min!) }
                ))
            } else {
                hints.push(intl.formatMessage(
                    { id: `default.msg.validation.hint.${kind}.max` },
                    { max: formatValue(max!) }
                ))
            }
        }
    }

    return hints
}

/**
 *
 * @param props
 * @returns
 */
export default function (props: Props & PropsWithChildren) {
    const { asTableCell, children, def, justifyContent, rowId, texts } = props
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const labelText = getLabel(texts, def)
    const helpIconId = "help-" + def.key
    const [helpOpen, setHelpOpen] = useState(false)
    const intl = useIntl()

    const validationHints = computeValidationHints(def, intl)
    const showPopover = def.showHelp || validationHints.length > 0

    return (
        <FlexBox
            direction="Column"
            fitContainer
            alignItems="Stretch"
            justifyContent={justifyContent ?? "Center"}
            style={{ height: "100%" }}
        >
            {!asTableCell && (
                <FlexBox alignItems="Center" style={{ gap: "0.25rem" }}>
                    <Label
                        id={"l" + def.key}
                        for={def.key}
                        required={element?.rq}
                        style={def.showLabel === false ? { visibility: "hidden" } : undefined}
                    >
                        {def.showLabel !== false ? labelText : ""}
                    </Label>
                    {showPopover && (
                        <>
                            <Icon
                                id={helpIconId}
                                name="sys-help"
                                style={{ cursor: "pointer", fontSize: "0.5rem" }}
                                onClick={() => setHelpOpen(true)}
                            />
                            <Popover
                                opener={helpIconId}
                                open={helpOpen}
                                placement="End"
                                onClose={() => setHelpOpen(false)}
                            >
                                <div style={{ maxWidth: "20rem" }}>
                                    <Text>{def.showHelp && getDoc(texts, def)}</Text>
                                    {def.showHelp && validationHints.length > 0 && (
                                        <hr style={{ margin: "0.5rem 0", border: "none", borderTop: "1px solid var(--sapSeparatorColor)" }} />
                                    )}
                                    {validationHints.map((hint, i) => (
                                        <div key={i}><Text>{hint}</Text></div>
                                    ))}
                                </div>
                            </Popover>
                        </>
                    )}
                </FlexBox>
            )}
            {children}
        </FlexBox>
    )
}
