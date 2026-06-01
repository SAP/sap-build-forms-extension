import { JSX, RefObject } from "react"

import { MessageBoxType, Tab } from "@ui5/webcomponents-react"

import { ValueHelpDef } from "../../features/model"
import ValueHelpDefinitionForm from "./ValueHelpDefinitionForm"
import { UseFormReturn } from "react-hook-form"

/**
 *  Properties of the ConfigTab component.
 */
interface ConfigTabProps {
    availableLanguages: string[]
    form: UseFormReturn<ValueHelpDef>

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
export default function ({ availableLanguages, changeLanguages, form }: ConfigTabProps) {
    return (
        <Tab icon="settings" selected text="Config">
            <ValueHelpDefinitionForm
                editMode={true}
                isNew={false}
                availableLanguages={availableLanguages}
                form={form}
                changeLanguages={changeLanguages}
            />
        </Tab>
    )
}
