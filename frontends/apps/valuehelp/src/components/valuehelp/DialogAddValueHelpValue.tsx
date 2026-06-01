import { useState } from "react"

import { createUseStyles } from "react-jss"
import { Bar, Button, Dialog, Form, FormItem, Input, Label } from "@ui5/webcomponents-react"

import { ValueHelpValue } from "../../features/model"

/**
 * Dialog to Add a Value Help Value
 */
interface DialogAddValueHelpValueProps {
    // dialogAddValueOpen: boolean
    // valueHelpValue: ValueHelpValue

    setDialogAddValueOpen(o: boolean): void

    changeValueHelpValue(changedValueHelpValue: ValueHelpValue): void
}

export default function (props: DialogAddValueHelpValueProps) {
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

    const classes = useStyles()
    const [newValueKey, setNewValueKey] = useState("")
    const [newValueValue, setNewValueValue] = useState("")

    const [isKeyEmpty, setIsKeyEmpty] = useState(false)
    const [isKeyExistent, setIsKeyExistent] = useState(false)

    return (
        <Dialog
            className={classes.dialog}
            footer={
                <Bar
                    design="Footer"
                    className={classes.bar}
                    endContent={
                        <Button
                            className={classes.button}
                            onClick={function _a() {
                                props.setDialogAddValueOpen(false)
                                setNewValueKey("")
                                setNewValueValue("")
                            }}
                        >
                            Close
                        </Button>
                    }
                >
                    <Button
                        design="Emphasized"
                        style={{ marginInline: 2 }}
                        onClick={function _a() {
                            if (Object.hasOwn(props.valueHelpValue.values, newValueKey)) {
                                setIsKeyExistent(true)
                            } else if (newValueKey.trim().length > 0) {
                                var v = props.valueHelpValue.values
                                v[newValueKey] = newValueValue
                                props.changeValueHelpValue({ ...props.valueHelpValue, values: v })
                                setNewValueKey("")
                                setNewValueValue("")
                                props.setDialogAddValueOpen(false)
                            } else {
                                setIsKeyEmpty(true)
                            }
                        }}
                    >
                        Add
                    </Button>
                </Bar>
            }
            headerText="Add Value Help Value"
            open={true}
        >
            <Form className={classes.form} layout="S1 M1 L1 XL1" labelSpan="S1 M1 L1 XL1">
                <FormItem labelContent={<Label required>Key</Label>}>
                    <Input
                        value={newValueKey}
                        required
                        valueState={isKeyEmpty || isKeyExistent ? "Negative" : "None"}
                        valueStateMessage={
                            isKeyEmpty ? (
                                <span>Key must not be empty!</span>
                            ) : (
                                <span>Key is already existent</span>
                            )
                        }
                        onInput={() => {
                            setIsKeyEmpty(false)
                            setIsKeyExistent(false)
                        }}
                        onChange={(e) => {
                            setNewValueKey(e.target.attributes.getNamedItem("value")!.nodeValue!)
                            if (
                                e.target.attributes.getNamedItem("value")!.nodeValue!.trim()
                                    .length == 0
                            ) {
                                setIsKeyEmpty(true)
                            } else {
                                setIsKeyEmpty(false)
                            }
                        }}
                    />
                </FormItem>
                <FormItem labelContent={<Label>Value</Label>}>
                    <Input
                        value={newValueValue}
                        onChange={(e) => {
                            setNewValueValue(e.target.attributes.getNamedItem("value")!.nodeValue!)
                        }}
                    />
                </FormItem>
            </Form>
        </Dialog>
    )
}
