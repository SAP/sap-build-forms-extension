import { IntlShape } from "react-intl"
import { format, Locale, parse } from "date-fns"
import {
    enGB, de, nl, fr, es, it, pt, bg, cs, da, fi, el, hr, hu, sk, ro, uk, sr, et, lt, lv
} from "date-fns/locale"

import { DataType, Definition } from "../../src/features/sessions/definitions"
import { ElementDataType } from "../../src/features/sessions/forms"

export const DEFAULT_LOCALE = "en"
const INTERNAL_DATE_FORMAT = "yyyy-MM-dd"
const INTERNAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"
const INTERNAL_TIME_FORMAT = "HH:mm:ss"

// mapping between locale strings and date-fns locales
const LOCALES: Record<string, Locale> = {
    en: enGB,   // english
    de: de,     // german
    nl: nl,     // dutch
    fr: fr,     // french
    es: es,     // spanish
    it: it,     // italian
    pt: pt,     // portuguese
    bg: bg,     // bulgarian
    cs: cs,     // czech
    da: da,     // danish
    fi: fi,     // finnish
    el: el,     // greek
    hr: hr,     // croatian
    hu: hu,     // hungarian
    sk: sk,     // slovak
    ro: ro,     // romanian
    et: et,     // estonian
    lt: lt,     // lithuanian
    lv: lv,     // latvian
}

/**
 *
 * @param va
 */
export function formatDataAsString(
    def: Definition,
    va: ElementDataType | undefined,
    intl: IntlShape,
): string {
    switch (def.dataType) {
        case DataType.Date:
            return intl.formatDate(va as Date)
        case DataType.Decimal:
        case DataType.Int:
            return intl.formatNumber(va as number)
        default:
            return va as string
    }
}

/**
 * Checks if a Date object is valid
 * @param d Date to validate
 * @returns true if date is valid, false otherwise
 */
function isValidDate(d: Date): boolean {
    if (!(d instanceof Date)) {
        return false
    }
    const time = d.getTime()
    return typeof time === "number" && !isNaN(time) && isFinite(time)
}

/**
 * Checks the given locale if it is supported
 *
 * @param locale
 * @returns normalized locale string or "en" as fallback
 */
function checkLocale(locale: string): string {
    // Extract language part before hyphen (e.g., "de-DE" -> "de")
    const language = locale.split("-")[0]
    if (language in LOCALES) {
        return language
    }
    return DEFAULT_LOCALE
}

/**
 * Format a Date object into a locale-specific date string
 * @param d
 * @param locale
 * @returns
 */
export function formatDate(d: string, locale: string): string {
    locale = checkLocale(locale)
    const dateObj = new Date(d)
    return format(dateObj, "P", { locale: LOCALES[locale] })
}

/**
 * Convert Date object to internal format (yyyy-MM-dd)
 * 
 * @param localeDate Date object from UI5 DatePicker
 * @param locale the locale identifier
 * @returns internal date format (yyyy-MM-dd) or undefined if invalid
 */
export function toInternalDate(
    localeDate: string | Date | Date[],
    locale: string,
): string | undefined {
    if (localeDate instanceof Date) {
        return isValidDate(localeDate) 
            ? format(localeDate, INTERNAL_DATE_FORMAT) 
            : undefined
    }
    return undefined
}


/**
 * Convert Time to internal format (HH:mm:ss)
 * 
 * @param localeTime Date object from UI5 TimePicker
 * @param locale the locale identifier
 * @returns internal time format (HH:mm:ss) or undefined if invalid
 */
export function toInternalTime(localeTime: string | Date | undefined, locale: string): string | undefined {
    if (localeTime instanceof Date) {
        return isValidDate(localeTime) 
            ? format(localeTime, INTERNAL_TIME_FORMAT) 
            : undefined
    }
    return undefined
}

/**
 * Convert internal time format (HH:mm:ss) to locale-specific time string
 * 
 * @param internalTime 
 * @param locale 
 * @returns 
 */
export function fromInternalTime(internalTime: string, locale: string): string {
    if (!internalTime) {
        return ""
    }

    try {
        const d = parse(internalTime, INTERNAL_TIME_FORMAT, new Date())
        
        if (!isValidDate(d)) {
            return ""
        }
        
        locale = checkLocale(locale)
        // For English, use 12-hour format with AM/PM; for other locales, use long time format with seconds
        const formatPattern = locale === "en" ? "h:mm:ss a" : "pp"
        return format(d, formatPattern, { locale: LOCALES[locale] })
    } catch (err) {
        return ""
    }
}

/**
 * 
 * @param localDateTime 
 * @param locale 
 * @returns 
 */
/**
 * Convert Date object to internal format (yyyy-MM-dd'T'HH:mm:ss)
 * 
 * @param localDateTime Date object from UI5 DateTimePicker
 * @param locale the locale identifier
 * @returns internal datetime format (yyyy-MM-dd'T'HH:mm:ss) or undefined if invalid
 */
export function toInternalDateTime(
    localDateTime: string | Date,
    locale: string,
): string | undefined {
    if (localDateTime instanceof Date) {
        return isValidDate(localDateTime) 
            ? format(localDateTime, INTERNAL_DATE_TIME_FORMAT) 
            : undefined
    }
    return undefined
}
export function fromInternalDateTime(internalDateTime: string, locale: string): string {
    if (!internalDateTime) {
        return ""
    }

    locale = checkLocale(locale)
    
    try {
        const d = parse(internalDateTime.substring(0, 19), INTERNAL_DATE_TIME_FORMAT, new Date())
        if (!isValidDate(d)) {
            return ""
        }
        // For English, use 12-hour format with AM/PM
        if (locale === "en") {
            return format(d, "MMM d, yyyy, h:mm:ss a", { locale: LOCALES[locale] })
        }
        // For all other locales, use locale-specific formatting which automatically adjusts
        return format(d, "P pp", { locale: LOCALES[locale] })
    } catch (err) {
        return ""
    }
}

/**
 * Convert locale-specific date range string to internal format
 * 
 * @param startDate start date string (from DateRangePicker)
 * @param endDate end date string (from DateRangePicker)
 * @param locale the locale identifier
 * @returns object with f and t properties in internal format, or undefined if parsing fails
 */
export interface InternalDateRange {
    f: string
    t: string
}

export function toInternalDateRange(
    startDate: string | Date | undefined,
    endDate: string | Date | undefined,
    locale: string,
): InternalDateRange | undefined {
    if (!startDate || !endDate) {
        return undefined
    }

    const startDateStr = startDate instanceof Date 
        ? format(startDate, INTERNAL_DATE_FORMAT) 
        : toInternalDate(startDate, locale)
    const endDateStr = endDate instanceof Date 
        ? format(endDate, INTERNAL_DATE_FORMAT) 
        : toInternalDate(endDate, locale)

    if (!startDateStr || !endDateStr) {
        return undefined
    }

    return { f: startDateStr, t: endDateStr }
}

/**
 * Format an internal date range for display
 * @param dateRange internal date range object
 * @param locale the locale identifier
 * @returns formatted date range string
 */
export function fromInternalDateRange(dateRange: InternalDateRange | undefined, locale: string): string {
    if (!dateRange) {
        return ""
    }
    
    const fd = formatDate(dateRange.f, locale)
    const td = formatDate(dateRange.t, locale)
    return fd + " - " + td
}

/**
 * 
 * @param value 
 * @param locale 
 * @returns 
 */
export function parseNumber(def: Definition, value: string, locale: string): number | undefined {
    if (!value || value.length === 0) {
        return undefined
    }

    const ds = def.dataType === DataType.Decimal ? getDecimalSeparator(locale) : ""
    let newValue = ""
    for (let i = 0; i < value.length; i++) {
        const ch = value.charAt(i)
        if ((ch >= '0' || ch <= '9') || (ch === '-' && i === 0) || (ch === ds)) {
            if (ch === ds) {
                newValue += "."
            } else {
                newValue += ch
            }
        }
    }

    return def.dataType === DataType.Int ? parseInt(newValue) : parseFloat(newValue)
}

/**
 * 
 * @param locale 
 * @returns 
 */
export function getDecimalSeparator(locale: Intl.LocalesArgument) {
    const numberWithDecimalSeparator = 1.1
    return Intl.NumberFormat(locale).formatToParts(numberWithDecimalSeparator).find(part => part.type === 'decimal')?.value
}