/**
 * Value Help Definitions and Values
 *
 * This file contains the definitions and types used for value helps in the application.
 * It includes interfaces for value help definitions and values.
 */
export interface ValueHelpDef {
    id: string
    ttl: number
    adapter: string
    config: string
    description: string
    languages: string[]
}

/**
 * Value Help Value Interface
 *
 * This interface defines the structure of a value help value, which includes an ID, version,
 */
export interface ValueHelpValue {
    id: string
    version: number
    locale: string
    validUntil: string
    values: any
}
