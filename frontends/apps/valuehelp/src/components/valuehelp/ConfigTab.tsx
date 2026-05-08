import { JSX, RefObject, useRef } from "react"

import { MessageBoxType, Tab } from "@ui5/webcomponents-react"

import { ValueHelpDef } from "../../features/model"
import ValueHelpDefinitionForm from "./ValueHelpDefinitionForm"

/**
 *  Properties of the ConfigTab component.
 */
interface ConfigTabProps {
    edit: boolean
    availableLanguages: string[]
    refValueHelpDef: RefObject<ValueHelpDef | undefined>

    setValueHelpDef(v: ValueHelpDef): void
    changeLanguages(v: ValueHelpDef): void
    openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string): void
}

/**
 *  Configuration tab of the ValueHelp editor.
 *
 * @param props
 * @returns
 */
export default function ({
    availableLanguages,
    changeLanguages,
    refValueHelpDef,
    edit,
    setValueHelpDef,
}: ConfigTabProps) {
    return (
        <Tab icon="settings" selected text="Config">
            <ValueHelpDefinitionForm
                edit={edit}
                availableLanguages={availableLanguages}
                refValueHelpDef={refValueHelpDef}
                setCurrentValueHelpDef={setValueHelpDef}
                changeLanguages={changeLanguages}
            />
        </Tab>
    )
}
