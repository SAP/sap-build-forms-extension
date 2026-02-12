import { format, Locale, parse } from "date-fns"
import { enGB, de } from "date-fns/locale"

export const DEFAULT_LOCALE = "en"
const INTERNAL_DATE_FORMAT = "yyyy-MM-dd"
const INTERNAL_DATE_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss"

// mapping between locale strings and date-fns locales
const LOCALES: Record<string, Locale> = {
    de: de,
    en: enGB,
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
export function toInternalDate(localeDate: string | Date | Date[], locale: string): string {
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

export function toInternalDateTime(localDateTime: string | Date, locale: string): string {
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

export function formatCurrentDate(date: Date, format: String) {
    return format
        .replace("dddd", date.toLocaleString("default", { weekday: "long" })) //e.g. Monday
        .replace("ddd", date.toLocaleString("default", { weekday: "long" }).substring(0, 3)) //e.g. Mon
        .replace("dd", String(date.getDate()).padStart(2, "0")) //e.g. 03
        .replace("d", String(date.getDate())) //e.g. 3
        .replace("MMMM", date.toLocaleString("default", { month: "long" })) //e.g. January
        .replace("MMM", date.toLocaleString("default", { month: "long" }).substring(0, 3)) //e.g. Jan
        .replace("MM", String(date.getMonth() + 1).padStart(2, "0")) //e.g. 01
        .replace("M", String(date.getMonth() + 1)) //e.g. 1
        .replace("yyyy", String(date.getFullYear())) //e.g. 2024
        .replace("yy", String(date.getFullYear().toString().slice(-2))) //e.g. 24
}

export function formatCurrentTime(time: Date, format: String) {
    var returnValue = format
        .replace("HH", String(time.getHours()).padStart(2, "0"))
        .replace("hh", String(time.getHours()).padStart(2, "0"))
        .replace("mm", String(time.getMinutes()).padStart(2, "0"))
        .replace("MM", String(time.getMinutes()).padStart(2, "0"))
        .replace("ss", String(time.getSeconds()).padStart(2, "0"))
        .replace("SS", String(time.getSeconds()).padStart(2, "0"))
        .replace(
            "KK",
            time.getHours() >= 12
                ? String(time.getHours() - 12).padStart(2, "0")
                : String(time.getHours()).padStart(2, "0"),
        )
        .replace("a", time.getHours() >= 12 ? "PM" : "AM")
    return returnValue
}
