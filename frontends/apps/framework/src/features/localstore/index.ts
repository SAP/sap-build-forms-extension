import Dexie, { Table } from "dexie"

import { ValueHelpsVersion } from "../valuehelps"

/**
 *
 */
export class FormsDexie extends Dexie {
    valuehelps!: Table<ValueHelpsVersion>

    constructor() {
        super("forms")
        this.version(1).stores({
            valuehelps: "[name+locale+version],[name+locale]",
        })
    }
}

export const localstore = new FormsDexie()
