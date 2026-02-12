import { IntlShape } from "react-intl"
import { format, Locale, parse } from "date-fns"
import { enGB, de } from "date-fns/locale"

import { DataType, Definition } from "../../src/features/sessions/definitions"
import { ElementDataType } from "../../src/features/sessions/forms"

const DEFAULT_LOCALE = "en"
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
function formatDataAsString(
    def: Definition,
    va: ElementDataType | undefined,
    intl: IntlShape,
): string {
    switch (def.dataType) {
        case DataType.Date:
            return intl.formatDate(va as Date)
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
function formatDate(d: Date, locale: string): string {
    locale = checkLocale(locale)
    return format(d, "P", { locale: LOCALES[locale] })
}

/**
 *
 * @param localeDate
 * @param locale
 * @returns
 */
function toInternalDate(localeDate: string | Date | Date[], locale: string): string {
    let d: Date | undefined = undefined

    if (typeof localeDate === "string") {
        locale = checkLocale(locale)
        // parse the given locale date
        d = parse(localeDate, "P", new Date(), { locale: LOCALES[locale] })
    } else if (localeDate instanceof Date) {
        d = localeDate
    } else {
        // not supported
        return ""
    }

    return format(d, INTERNAL_DATE_FORMAT)
}

function toInternalDateTime(localDateTime: string | Date, locale: string): string {
    let d: Date | undefined = undefined

    if (typeof localDateTime === "string") {
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
function fromInternalDate(internalDate: string, locale: string): Date {
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
function fromInternalDateTime(internalDateTime: string, locale: string): string {
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
