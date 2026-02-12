import { Form } from "./forms"
import { FrontendJournal } from "./journal"

/**
 * 
 */
export class Session {
    id: string
    locale: string
    form: Form
    journal: FrontendJournal
    state: string

    constructor(
        id: string,
        locale: string,
        state: string,
        form: Form,
    ) {
        this.id = id
        this.locale = locale
        this.state = state
        this.form = form
        this.journal = new FrontendJournal()
    }
}
