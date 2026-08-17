import { useEffect } from "react"

import { useForm } from "react-hook-form"
import { useIntl } from "react-intl"
import { Bar, Button, Dialog } from "@ui5/webcomponents-react"

import { Margin } from "commons"

import { ValueHelpDef } from "../../features/model"
import ValueHelpDefinitionForm from "./ValueHelpDefinitionForm"

/**
 * Props for the DialogAddValueHelpDefinition component
 */
interface DialogAddValueHelpDefinitionProps {
    dialogAddDefOpen: boolean
    isIdExistent: boolean
    availableLanguages: string[]
    availableAdapters: string[]
    existingIds: string[]
    setDialogAddDefOpen(o: boolean): void
    addValueHelpDef(d: ValueHelpDef): void
    setIsIdExistent(b: boolean): void
}

/**
 *  Dialog for adding a new Value Help Definition
 *
 * @param props
 * @returns
 */
export default function (props: DialogAddValueHelpDefinitionProps) {
    const intl = useIntl()
    const form = useForm<ValueHelpDef>({
        defaultValues: {
            id: "",
            description: "",
            ttl: -1,
            adapter: "local",
            config: "",
            languages: [],
            type: "freestyle",
            keyKey: "",
            valueKeys: [],
            formatTemplate: "",
        },
    })

    const { handleSubmit, reset, setError } = form

    // Reset the form each time the dialog opens
    useEffect(() => {
        if (props.dialogAddDefOpen) {
            reset()
        }
    }, [props.dialogAddDefOpen])

    // Reflect a 409 conflict back into the id field error
    useEffect(() => {
        if (props.isIdExistent) {
            setError("id", { type: "manual", message: intl.formatMessage({ id: "err_id_already_exists" }) })
        }
    }, [props.isIdExistent])

    function handleClose() {
        props.setDialogAddDefOpen(false)
        props.setIsIdExistent(false)
        reset()
    }

    function onSubmit(def: ValueHelpDef) {
        if (props.existingIds.includes(def.id)) {
            setError("id", { type: "manual", message: intl.formatMessage({ id: "err_id_already_exists" }) })
            return
        }
        props.addValueHelpDef(def)
    }

    return (
        <Dialog
            style={{ minWidth: "50%", paddingTop: Margin.SMALL, paddingInline: Margin.TINY }}
            footer={
                <Bar
                    design="Footer"
                    style={{ paddingBlock: Margin.TINY }}
                    endContent={
                    <div>
                        <Button onClick={handleClose}>
                            {intl.formatMessage({ id: "btn_close" })}
                        </Button>
                        <Button
                        design="Emphasized"
                        style={{ marginInline: Margin.TINY }}
                        onClick={() => handleSubmit(onSubmit)()}
                        >
                            {intl.formatMessage({ id: "btn_add" })}
                        </Button>
                    </div>
                    } >
                </Bar>
            }
            headerText={intl.formatMessage({ id: "dlg_add_def_title" })}
            open={props.dialogAddDefOpen}
        >
            <ValueHelpDefinitionForm
                isNew={true}
                editMode={true}
                availableLanguages={props.availableLanguages}
                availableAdapters={props.availableAdapters}
                changeLanguages={() => {}}
                form={form}
            />
        </Dialog>
    )
}
