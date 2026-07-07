import { useEffect, useState } from "react"

import ReactDOM from "react-dom"
import { useIntl } from "react-intl"

import {
    Bar,
    Button,
    Dialog,
    FlexBox,
    Input,
    Table,
    TableCell,
    TableHeaderCell,
    TableHeaderRow,
    TableRow,
    Text,
} from "@ui5/webcomponents-react"

import { useMessages } from "commons"

import { ControlProps, handleChange, handleEnterFocus, handleLeaveFocus, getPlaceholder } from "./Control"
import ControlContainer from "./ControlFlexContainer"
import { CurrencyAmount, FormService } from "../../features/sessions/forms"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { ValuehelpsService } from "../../features/valuehelps/logic"
import { elementInfo2ValueState, elementInfo2ValueStateText } from "./utils"

/**
 * Currency entry interface.
 */
export interface CurrencyEntry {
    isoCode: string
    digits: number
    name: string
}

/**
 * Testing currencies used when no value help is configured
 */
export const FALLBACK_CURRENCIES: CurrencyEntry[] = [
    { isoCode: "EUR", digits: 2, name: "Euro" },
    { isoCode: "USD", digits: 2, name: "US Dollar" },
    { isoCode: "GBP", digits: 2, name: "British Pound Sterling" },
    { isoCode: "JPY", digits: 0, name: "Japanese Yen" },
    { isoCode: "HUF", digits: 0, name: "Hungarian Forint" },
    { isoCode: "TND", digits: 3, name: "Tunisian Dinar" },
]

/**
 * Map a value help record to CurrencyEntry[].
 *
 * The value help value must encode both digits and display name using the format:
 *   "<digits>|<name>"   e.g.  "2|Euro"  or  "0|Japanese Yen"
 */
function vhToCurrencyEntries(
    values: Record<string, string>,
    displayFormat?: string,
): CurrencyEntry[] {
    return Object.entries(values).map(([isoCode, encoded]) => {
        const pipeIndex = encoded.indexOf("|")
        const digits = pipeIndex >= 0 ? parseInt(encoded.substring(0, pipeIndex), 10) : 2
        const rawName = pipeIndex >= 0 ? encoded.substring(pipeIndex + 1) : encoded
        const name = ValuehelpsService.formatVHOption(
            isoCode,
            rawName,
            displayFormat
                ? { displayFormat, emptySelection: false, name: "", validate: false }
                : undefined,
        )
        return { isoCode, digits: isNaN(digits) ? 2 : digits, name }
    })
}

/**
 * Parses an amount string, supporting both comma and period as decimal separators.
 */
function parseAmountString(value: string): number {
    const normalized = value.trim()
    const lastDot = normalized.lastIndexOf(".")
    const lastComma = normalized.lastIndexOf(",")

    let result: string
    if (lastComma === -1) {
        result = normalized.replace(/,/g, "")
    } else if (lastDot === -1) {
        result =
            normalized.substring(0, lastComma) + "." + normalized.substring(lastComma + 1)
        result = result.replace(/,/g, "")
    } else if (lastComma > lastDot) {
        result = normalized.replace(/\./g, "")
        const ci = result.lastIndexOf(",")
        result = result.substring(0, ci) + "." + result.substring(ci + 1)
    } else {
        result = normalized.replace(/,/g, "")
    }
    return parseFloat(result)
}

/**
 * Returns the decimal separator character for the current locale (e.g. "," for German).
 */
function getDecimalSeparator(): string {
    return (1.1).toLocaleString(navigator.language).charAt(1)
}

/**
 * CurrencyDialog interface
 */
interface CurrencyDialogProps {
    open: boolean
    selectedCode: string
    currencies: CurrencyEntry[]
    allowEmpty: boolean
    onSelect: (currency: CurrencyEntry | null) => void
    onClose: () => void
}

/**
 * CurrencyDialog component definition
 * @param Open whether the dialog is open
 * @param selectedCode currently selected currency code
 * @param currencies list of available currencies to choose from
 * @param allowEmpty whether to show an empty selection option
 * @param onSelect callback when a currency is selected (or empty selection)
 * @param onClose callback when the dialog is closed without selection
 * @returns CurrencyDialog component
 */
function CurrencyDialog({
    open,
    selectedCode,
    currencies,
    allowEmpty,
    onSelect,
    onClose,
}: CurrencyDialogProps) {
    const intl = useIntl()

    if (!open) return null

    return ReactDOM.createPortal(
        <Dialog
            open={true}
            headerText={intl.formatMessage({ id: "currency_dialog_title" })}
            footer={
                <Bar
                    design="Footer"
                    endContent={
                        <Button onClick={onClose}>
                            {intl.formatMessage({ id: "common_close" })}
                        </Button>
                    }
                />
            }
            onClose={onClose}
            style={{ minWidth: "28rem" }}
        >
            <Table
                headerRow={
                    <TableHeaderRow>
                        <TableHeaderCell minWidth="5rem">
                            <Text>{intl.formatMessage({ id: "currency_col_iso_code" })}</Text>
                        </TableHeaderCell>
                        <TableHeaderCell minWidth="4rem">
                            <Text>{intl.formatMessage({ id: "currency_col_digits" })}</Text>
                        </TableHeaderCell>
                        <TableHeaderCell>
                            <Text>{intl.formatMessage({ id: "currency_col_name" })}</Text>
                        </TableHeaderCell>
                    </TableHeaderRow>
                }
                noDataText={intl.formatMessage({ id: "common_no_data" })}
            >
                {allowEmpty && (
                    <TableRow
                        key="__empty__"
                        interactive
                        onClick={() => onSelect(null)}
                        style={{ cursor: "pointer" }}
                    >
                        <TableCell>
                            <Text>–</Text>
                        </TableCell>
                        <TableCell>
                            <Text></Text>
                        </TableCell>
                        <TableCell>
                            <Text>{intl.formatMessage({ id: "currency_empty_selection" })}</Text>
                        </TableCell>
                    </TableRow>
                )}
                {currencies.map((currency) => (
                    <TableRow
                        key={currency.isoCode}
                        interactive
                        onClick={() => onSelect(currency)}
                        style={{
                            cursor: "pointer",
                            background:
                                currency.isoCode === selectedCode
                                    ? "var(--sapList_SelectionBackgroundColor)"
                                    : undefined,
                        }}
                    >
                        <TableCell>
                            <Text style={{ fontWeight: "bold" }}>{currency.isoCode}</Text>
                        </TableCell>
                        <TableCell>
                            <Text>{currency.digits}</Text>
                        </TableCell>
                        <TableCell>
                            <Text>{currency.name}</Text>
                        </TableCell>
                    </TableRow>
                ))}
            </Table>
        </Dialog>,
        document.body,
    )
}

/**
 * CurrencyControl displays a currency ISO code selector alongside an amount input.
 * The value is stored as a CurrencyAmount object { currency, amount }.
 * When no value help is configured, FALLBACK_CURRENCIES are used.
 * @param props ControlProps
 * @returns CurrencyControl component
 */
export default function (props: ControlProps) {
    const { def, globalEd, rowId, texts } = props
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const intl = useIntl()
    const form = useAppSelector((state) => state.session.form)
    const vhs = useAppSelector((state) => state.valuehelps.vhs)
    const locale = useAppSelector((state) => state.session.locale)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const [showDialog, setShowDialog] = useState<boolean>(false)
    const [currencies, setCurrencies] = useState<CurrencyEntry[]>(FALLBACK_CURRENCIES)
    const [amountFocused, setAmountFocused] = useState(false)
    const [editValue, setEditValue] = useState<string>("")

    // Load currency list from value help when configured
    useEffect(() => {
        if (def.vh && vhs[def.vh.name]) {
            ValuehelpsService.loadFormLocalstore(def.vh.name, locale).then((values) => {
                setCurrencies(vhToCurrencyEntries(values, def.vh?.displayFormat))
            })
        }
    }, [vhs])

    const va = element?.va as any | undefined
    // Handle both abbreviated names from backend (cur, a) and full names (currency, amount)
    const currency = va?.currency ?? va?.cur ?? ""
    const amount = va?.amount ?? va?.a ?? 0

    const selectedCurrency = currencies.find((c) => c.isoCode === currency)

    const isInvalid =
        def.vh?.validate === true && currency.length > 0 && selectedCurrency === undefined

    const isEditable = (element?.ed ?? false) && globalEd
    const allowEmpty = def.vh?.emptySelection === true

    const displayAmount = (() => {
        if (amountFocused) return editValue
        const digits = selectedCurrency?.digits ?? 2
        const formatter = new Intl.NumberFormat(navigator.language, {
            minimumFractionDigits: digits,
            maximumFractionDigits: digits,
        })
        if (isNaN(amount)) return formatter.format(0)
        return formatter.format(amount)
    })()

    const handleCurrencySelect = (currency: CurrencyEntry | null) => {
        handleChange(dispatch, def, rowId, messages, {
            cur: currency?.isoCode ?? "",
            a: amount,
        })
        setShowDialog(false)
    }

    const handleAmountChange = (value: string) => {
        // Strip anything that is not a digit, period, comma, or a leading minus sign
        const sanitized = value.replace(/[^0-9.,\-]/g, "").replace(/(?!^)-/g, "")
        const parsed = parseAmountString(sanitized)
        const digits = selectedCurrency?.digits ?? 2
        const decimalSep = getDecimalSeparator()

        const decimalIdx = sanitized.lastIndexOf(decimalSep)
        const typedDecimalDigits = decimalIdx >= 0 ? sanitized.length - decimalIdx - 1 : -1
        const isTypingDecimal =
            digits > 0 &&
            (sanitized.endsWith(decimalSep) ||
                (typedDecimalDigits >= 0 && typedDecimalDigits < digits))

        if (!isNaN(parsed) && !isTypingDecimal) {
            const editFormatter = new Intl.NumberFormat(navigator.language, {
                minimumFractionDigits: digits,
                maximumFractionDigits: digits,
                useGrouping: false,
            })
            setEditValue(editFormatter.format(parsed))
        } else {
            setEditValue(sanitized)
        }

        handleChange(dispatch, def, rowId, messages, { cur: currency, a: isNaN(parsed) ? 0 : parsed })
    }

    return (
        <ControlContainer {...props}>
            <FlexBox alignItems="Center" style={{ width: "100%", gap: "0" }}>
                <Button
                    design={isInvalid ? "Negative" : "Transparent"}
                    onClick={() => isEditable && setShowDialog(true)}
                    disabled={!isEditable}
                    style={{
                        minWidth: "3.5rem",
                        fontWeight: "bold",
                        borderRight: "none",
                        borderRadius:
                            "var(--sapField_BorderCornerRadius) 0 0 var(--sapField_BorderCornerRadius)",
                        height: "var(--sapElement_Height)",
                        border: "var(--sapField_BorderWidth) solid var(--sapField_BorderColor)",
                    }}
                >
                    {currency || "–"}
                </Button>
                <Input
                    value={displayAmount.toString()}
                    placeholder={getPlaceholder(texts, def)}
                    onChange={(e) => handleAmountChange(e.target.value ?? "")}
                    onFocus={() => {
                        const digits = selectedCurrency?.digits ?? 2
                        const editFormatter = new Intl.NumberFormat(navigator.language, {
                            minimumFractionDigits: digits,
                            maximumFractionDigits: digits,
                            useGrouping: false,
                        })
                        setEditValue(editFormatter.format(isNaN(amount) ? 0 : amount))
                        setAmountFocused(true)
                        handleEnterFocus(dispatch, def, rowId, messages)
                    }}
                    onBlur={() => {
                        setAmountFocused(false)
                        handleLeaveFocus(dispatch, def, rowId, messages)
                    }}
                    readonly={!isEditable}
                    required={element?.rq}
                    valueState={elementInfo2ValueState(element?.msg)}
                    valueStateMessage={elementInfo2ValueStateText(intl, element?.msg)}
                    style={{
                        flex: 1,
                        borderRadius:
                            "0 var(--sapField_BorderCornerRadius) var(--sapField_BorderCornerRadius) 0",
                    }}
                />
            </FlexBox>
            <CurrencyDialog
                open={showDialog}
                selectedCode={currency}
                currencies={currencies}
                allowEmpty={allowEmpty}
                onSelect={handleCurrencySelect}
                onClose={() => setShowDialog(false)}
            />
        </ControlContainer>
    )
}
