import { useState } from "react"

import { createUseStyles } from "react-jss"
import { Bar, Button, Dialog, Form, FormItem, Input, InputDomRef, Label, Ui5CustomEvent } from "@ui5/webcomponents-react"

import { ValueHelpDef, ValueHelpValue } from "../../features/model"

/**
 * Dialog to Add a Value Help Value
 */
interface DialogAddValueHelpValueProps {
    dialogAddValueOpen: boolean
    currentValueHelpDef: ValueHelpDef | undefined
    valueHelpValue: ValueHelpValue | undefined
    setDialogAddValueOpen(o: boolean): void
    changeValueHelpValue(changedValueHelpValue: ValueHelpValue): void
}

const useStyles = createUseStyles({
    dialog: {
        paddingTop: 10,
        paddingInline: 3,
    },
    bar: {
        paddingBlock: 3,
    },
    button: {
        marginInline: 2,
    },
    form: {
        padding: 3,
    },
})

export default function (props: DialogAddValueHelpValueProps) {
    const classes = useStyles()

    const def = props.currentValueHelpDef
    const keyKey = def?.keyKey ?? "key"
    const valueKeys = def?.valueKeys ?? []
    const columns = [keyKey, ...valueKeys]

    // One input state entry per column
    const [inputs, setInputs] = useState<Record<string, string>>({})
    const [isKeyEmpty, setIsKeyEmpty] = useState(false)
    const [isKeyExistent, setIsKeyExistent] = useState(false)

    function handleClose() {
        props.setDialogAddValueOpen(false)
        setInputs({})
        setIsKeyEmpty(false)
        setIsKeyExistent(false)
    }

    function handleAdd() {
        const keyVal = (inputs[keyKey] ?? "").trim()

        if (keyVal.length === 0) {
            setIsKeyEmpty(true)
            return
        }

        const alreadyExists = (props.valueHelpValue?.values ?? []).some(
            (row) => row[keyKey] === keyVal,
        )
        if (alreadyExists) {
            setIsKeyExistent(true)
            return
        }

        // Build the new row from all column inputs
        const newRow: Record<string, string> = {}
        columns.forEach((col) => {
            newRow[col] = (inputs[col] ?? "").trim()
        })

        props.changeValueHelpValue({
            ...props.valueHelpValue!,
            values: [...(props.valueHelpValue?.values ?? []), newRow],
        })
        handleClose()
    }

    return (
        <Dialog
            className={classes.dialog}
            footer={
                <Bar
                    design="Footer"
                    className={classes.bar}
                    endContent={
                        <div>
                            <Button design="Emphasized" onClick={handleAdd}>
                                Add
                            </Button>
                            <Button className={classes.button} onClick={handleClose}>
                                Close
                            </Button>
                        </div>
                    } >
                </Bar>
            }
            headerText="Add Value Help Value"
            open={props.dialogAddValueOpen}
            style={{ minWidth: "50%" }}
        >
            <Form className={classes.form} layout="S1 M1 L1 XL1" labelSpan="S1 M1 L1 XL1">
                {columns.map((col) => {
                    const isKey = col === keyKey
                    return (
                        <FormItem key={col} labelContent={<Label required={isKey}>{col}</Label>}>
                            <Input
                                value={inputs[col] ?? ""}
                                valueState={
                                    isKey && (isKeyEmpty || isKeyExistent) ? "Negative" : "None"
                                }
                                valueStateMessage={
                                    isKey ? (
                                        isKeyEmpty ? (
                                            <span>{col} must not be empty</span>
                                        ) : (
                                            <span>{col} already exists</span>
                                        )
                                    ) : undefined
                                }
                                onInput={() => {
                                    if (isKey) {
                                        setIsKeyEmpty(false)
                                        setIsKeyExistent(false)
                                    }
                                }}
                                onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                    const val = e.target.attributes.getNamedItem("value")!.nodeValue!
                                    setInputs((prev) => ({ ...prev, [col]: val }))
                                    if (isKey) setIsKeyEmpty(val.trim().length === 0)
                                }}
                            />
                        </FormItem>
                    )
                })}
            </Form>
        </Dialog>
    )
}
