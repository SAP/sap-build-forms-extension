/**
 * Defines a class for value-help version. It defines a single
 * value help for a locale and version with all it's values.
 */
export class ValueHelpsVersion {
    name: string
    locale: string
    version: number
    values: Record<string, string>

    constructor() {
        this.name = ""
        this.locale = ""
        this.version = 0
        this.values = {}
    }
}
