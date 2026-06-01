import { RefObject } from "react"

import { Bar, Button, Dialog } from "@ui5/webcomponents-react"

import { Margin } from "commons"

import { ValueHelpDef } from "../../features/model"
import ValueHelpDefinitionForm from "./ValueHelpDefinitionForm"

/**
 * Props for the DialogAddValueHelpDefinition component
 */
interface DialogAddValueHelpDefinitionProps {
    availableLanguages: string[]
    setDialogAddDefOpen(o: boolean): void
    addValueHelpDef(d: ValueHelpDef): void
    refValueHelpDef: RefObject<ValueHelpDef | undefined>
    changeLanguages(v: ValueHelpDef): void
}

/**
 *  Dialog for adding a new Value Help Definition
 *
 * @param props
 * @returns
 */
export default function ({
    addValueHelpDef,
    availableLanguages,
    refValueHelpDef,
    changeLanguages,
    setDialogAddDefOpen,
}: DialogAddValueHelpDefinitionProps) {
    const handleAdd = async () => {
        if (
            refValueHelpDef.current &&
            refValueHelpDef.current.id.trim().length > 0 &&
            refValueHelpDef.current.adapter.trim().length > 0 &&
            refValueHelpDef.current.ttl >= -2 &&
            Number.isInteger(refValueHelpDef.current.ttl) &&
            (refValueHelpDef.current.ttl < 1 || Number(refValueHelpDef.current.ttl) > 0)
        ) {
            addValueHelpDef(refValueHelpDef.current)
            setDialogAddDefOpen(false)
        } else {
            alert("Please enter a valid ID, Adapter and TTL (>= -2) for the Value Help Definition.")
            if (refValueHelpDef.current && refValueHelpDef.current.id.trim().length == 0) {
            }
        }
    }

    return (
        <Dialog
            style={{
                paddingTop: Margin.SMALL,
                paddingInline: Margin.TINY,
                minWidth: "700px",
                maxWidth: "90vw",
            }}
            footer={
                <Bar
                    design="Footer"
                    style={{ paddingBlock: Margin.TINY }}
                    endContent={
                        <Button
                            onClick={function _a() {
                                setDialogAddDefOpen(false)
                            }}
                        >
                            Close
                        </Button>
                    }
                >
                    <Button
                        design="Emphasized"
                        style={{ marginInline: Margin.TINY }}
                        onClick={handleAdd}
                    >
                        Add
                    </Button>
                </Bar>
            }
            headerText="Add Value Help Definition"
            open={true}
        >
            <ValueHelpDefinitionForm
                edit={true}
                isNew={true}
                availableLanguages={availableLanguages}
                refValueHelpDef={refValueHelpDef}
                changeLanguages={changeLanguages}
            />
        </Dialog>
    )
}
