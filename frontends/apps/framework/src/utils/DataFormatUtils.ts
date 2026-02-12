import { IntlShape } from "react-intl"
import { format, Locale, parse } from "date-fns"
import { enGB, de } from "date-fns/locale"

import { DataType, Definition } from "../../src/features/sessions/definitions"
import { ElementDataType } from "../../src/features/sessions/forms"

export const DEFAULT_LOCALE = "en"
const INTERNAL_DATE_FORMAT = "yyyy-MM-dd"
const INTERNAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

// mapping between locale strings and date-fns locales
const LOCALES: Record<string, Locale> = {
    de: de,
    en: enGB,
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
 * Checks the given locale and if not one of the support locales it returns "en",
 * as the default locale
 *
 * @param locale
 * @returns
 */
function checkLocale(locale: string): string {
    switch (locale) {
        case "de":
            return locale
        default:
            return DEFAULT_LOCALE
    }
}

/**
 *
 * @param d
 * @param locale
 * @returns
 */
export function formatDate(d: Date, locale: string): string {
    locale = checkLocale(locale)
    return format(d, "P", { locale: LOCALES[locale] })
}

/**
 *
 * @param localeDate
 * @param locale
 * @returns
 */
export function toInternalDate(
    localeDate: string | Date | Date[],
    locale: string,
): string | undefined {
    let d: Date | undefined = undefined

    if (typeof localeDate === "string") {
        if (localeDate.length == 0) {
            return undefined
        }
        locale = checkLocale(locale)
        // parse the given locale date
        d = parse(localeDate, "P", new Date(), { locale: LOCALES[locale] })
    } else if (localeDate instanceof Date) {
        if (typeof localeDate === "undefined" || isNaN(localeDate.getTime())) {
            return undefined
        }
        d = localeDate
    } else {
        // not supported
        return ""
    }

    return format(d, INTERNAL_DATE_FORMAT)
}

/**
 * 
 * @param localDateTime 
 * @param locale 
 * @returns 
 */
export function toInternalDateTime(
    localDateTime: string | Date,
    locale: string,
): string | undefined {
    let d: Date | undefined = undefined

    if (typeof localDateTime === "string") {
        if (localDateTime.length == 0) {
            return undefined
        }
        locale = checkLocale(locale)
        const parts = localDateTime.split(",")
        // parse the given locale date
        d = parse(parts[0].trim(), "P", new Date(), { locale: LOCALES[locale] })
        // parse the given locale time
        const t = parse(parts[1].trim(), "pp", new Date(), { locale: LOCALES[locale] })
        // set Hours/Minutes/Secods to returned date
        d.setHours(t.getHours())
        d.setMinutes(t.getMinutes())
        d.setSeconds(t.getSeconds())
    } else if (localDateTime instanceof Date) {
        if (typeof localDateTime === "undefined" || isNaN(localDateTime.getTime())) {
            return undefined
        }
        d = localDateTime
    } else {
        // not supported
        return ""
    }

    return format(d, INTERNAL_DATE_TIME_FORMAT)
}

/**
 *
 * @param internalDate
 * @param locale
 * @returns
 */
export function fromInternalDate(internalDate: string, locale: string): Date {
    locale = checkLocale(locale)
    // parse internal date
    const d = parse(internalDate.substring(0, 10), INTERNAL_DATE_FORMAT, new Date())
    // now format it as a local date
    // return format(d, "P", { locale: LOCALES[locale] })
    return d
}

/**
 *
 * @param internalDate
 * @param locale
 * @returns
 */
export function fromInternalDateTime(internalDateTime: string, locale: string): string {
    if (!internalDateTime) {
        return ""
    }

    locale = checkLocale(locale)
    // parse internal date
    const d = parse(internalDateTime.substring(0, 19), INTERNAL_DATE_TIME_FORMAT, new Date())
    const result =
        format(d, "P", { locale: LOCALES[locale] }) +
        ", " +
        format(d, "pp", { locale: LOCALES[locale] })
    // now format it as a local date
    // return format(d, "P", { locale: LOCALES[locale] })
    return result
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