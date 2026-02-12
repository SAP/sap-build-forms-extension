import { useState } from "react"

import {
    Bar,
    Button,
    Dialog,
    FlexBox,
    FlexBoxDirection,
    Form,
    FormItem,
    Input,
    Label,
    MultiComboBox,
    MultiComboBoxItem,
    RadioButton,
    Select,
    Option,
} from "@ui5/webcomponents-react"
import InputType from "@ui5/webcomponents/dist/types/InputType"

import { Margin } from "commons"

import { ValueHelpDef } from "../../features/definitions"
import { useValueHelpState } from "../../features/store"

/**
 * Props for the DialogAddValueHelpDefinition component
 */
interface DialogAddValueHelpDefinitionProps {
    dialogAddDefOpen: boolean
    isIdExistent: boolean
    availableLanguages: string[]
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
    const state = useValueHelpState()

    const [newDefId, setNewDefId] = useState("")
    const [newDefDescription, setNewDefDescription] = useState("")
    const [newDefTtlCheckbox, setNewDefTtlCheckbox] = useState(-1)
    const [newDefTtl, setNewDefTtl] = useState("")
    const [newDefAdapter, setNewDefAdapter] = useState("local")
    const [newDefConfig, setNewDefConfig] = useState("")
    const [newDefLanguages, setNewDefLanguages] = useState<string[]>([])

    const [isIdEmpty, setIsIdEmpty] = useState(false)

    return (
        <Dialog
            style={{ paddingTop: Margin.SMALL, paddingInline: Margin.TINY }}
            footer={
                <Bar
                    design="Footer"
                    style={{ paddingBlock: Margin.TINY }}
                    endContent={
                        <Button
                            onClick={function _a() {
                                props.setDialogAddDefOpen(false)
                                setNewDefId("")
                                setNewDefDescription("")
                                setNewDefTtl("")
                                setNewDefTtlCheckbox(-1)
                                setNewDefAdapter("local")
                                setNewDefConfig("")
                                setNewDefLanguages([])
                                setIsIdEmpty(false)
                            }}
                        >
                            Close
                        </Button>
                    }
                >
                    <Button
                        design="Emphasized"
                        style={{ marginInline: Margin.TINY }}
                        onClick={async function _a() {
                            if (
                                newDefId.trim().length > 0 &&
                                newDefAdapter.trim().length > 0 &&
                                newDefTtlCheckbox >= -1 &&
                                Number(newDefTtl) >= -2 &&
                                Number.isInteger(Number(newDefTtl)) &&
                                (newDefTtlCheckbox < 1 || Number(newDefTtl) > 0)
                            ) {
                                props.addValueHelpDef({
                                    id: newDefId.trim(),
                                    description: newDefDescription.trim(),
                                    ttl: Number(newDefTtl),
                                    adapter:
                                        newDefAdapter.trim().toLowerCase() == "local"
                                            ? "local"
                                            : newDefAdapter.trim(),
                                    config: newDefConfig.trim(),
                                    languages: newDefLanguages,
                                })
                                props.setDialogAddDefOpen(false)
                                setIsIdEmpty(false)
                                setNewDefId("")
                                setNewDefDescription("")
                                setNewDefTtlCheckbox(-1)
                                setNewDefTtl("")
                                setNewDefAdapter("local")
                                setNewDefConfig("")
                                setNewDefLanguages([])
                            } else {
                                if (newDefId.trim().length == 0) {
                                    setIsIdEmpty(true)
                                }
                            }
                        }}
                    >
                        Add
                    </Button>
                </Bar>
            }
            headerText="Add Value Help Definition"
            open={props.dialogAddDefOpen}
        >
            <Form
                style={{ padding: Margin.TINY, width: "100%" }}
                layout="S1 M1 L1 XL1"
                labelSpan="S1 M1 L1 XL1"
            >
                <FormItem labelContent={<Label required>ID</Label>}>
                    <Input
                        value={newDefId}
                        required
                        valueState={isIdEmpty || props.isIdExistent ? "Negative" : "None"}
                        valueStateMessage={<span>ID must not be empty</span>}
                        onChange={(e) => {
                            setNewDefId(e.target.attributes.getNamedItem("value")!.nodeValue!)
                            if (
                                e.target.attributes.getNamedItem("value")!.nodeValue!.trim()
                                    .length == 0
                            ) {
                                setIsIdEmpty(true)
                            }
                        }}
                        onInput={() => {
                            setIsIdEmpty(false)
                            props.setIsIdExistent(false)
                        }}
                    />
                </FormItem>
                <FormItem labelContent={<Label>Description</Label>}>
                    <Input
                        value={newDefDescription}
                        onChange={(e) => {
                            setNewDefDescription(
                                e.target.attributes.getNamedItem("value")!.nodeValue!,
                            )
                        }}
                    />
                </FormItem>
                <FormItem labelContent={<Label required>Time to live</Label>}>
                    <FlexBox direction={FlexBoxDirection.Column}>
                        <RadioButton
                            text="static"
                            checked={newDefTtlCheckbox == -1}
                            onChange={() => {
                                setNewDefTtlCheckbox(-1)
                                setNewDefTtl("-1")
                            }}
                        />
                        <RadioButton
                            text="refresh"
                            checked={newDefTtlCheckbox == 0}
                            onChange={() => {
                                setNewDefTtlCheckbox(0)
                                setNewDefTtl("0")
                            }}
                        />
                        <FlexBox>
                            <RadioButton
                                text="time buffer (in min)"
                                checked={newDefTtlCheckbox == 1}
                                onChange={() => {
                                    setNewDefTtlCheckbox(1)
                                    setNewDefTtl("1")
                                }}
                            />
                            {newDefTtlCheckbox == 1 && (
                                <Input
                                    value={newDefTtl}
                                    style={{ width: 87 }}
                                    type={InputType.Number}
                                    valueState={
                                        Number(newDefTtl) > 0 && Number.isInteger(Number(newDefTtl))
                                            ? "None"
                                            : "Negative"
                                    }
                                    valueStateMessage={
                                        <span>Time buffer must be greater than 0</span>
                                    }
                                    onChange={(e) => {
                                        setNewDefTtl(
                                            e.target.attributes.getNamedItem("value")!.nodeValue!,
                                        )
                                    }}
                                />
                            )}
                        </FlexBox>
                    </FlexBox>
                </FormItem>
                <FormItem labelContent={<Label required>Adapter</Label>}>
                    <div>
                        <Select
                            onChange={(e) => {
                                setNewDefAdapter(e.detail.selectedOption.innerText)
                            }}
                            valueState="None"
                        >
                            <Option key="local" selected={newDefAdapter == "local"}>
                                local
                            </Option>
                            {state.adapters.map((a) => {
                                if (a.toLowerCase() != "local") {
                                    return (
                                        <Option key={a} selected={newDefAdapter == a}>
                                            {a}
                                        </Option>
                                    )
                                }
                            })}
                        </Select>
                    </div>
                </FormItem>
                <FormItem labelContent={<Label>Config</Label>}>
                    <Input
                        value={newDefConfig}
                        onChange={(e) => {
                            setNewDefConfig(e.target.attributes.getNamedItem("value")!.nodeValue!)
                        }}
                    />
                </FormItem>
                <FormItem labelContent={<Label>Languages</Label>}>
                    <MultiComboBox
                        onSelectionChange={function Xs(e) {
                            setNewDefLanguages(e.detail.items.map((i) => i.id))
                        }}
                        style={{ maxWidth: "150px" }}
                    >
                        {props.availableLanguages.map((l) => {
                            return (
                                <MultiComboBoxItem
                                    text={l}
                                    id={l}
                                    key={l}
                                    selected={newDefLanguages.includes(l)}
                                />
                            )
                        })}
                    </MultiComboBox>
                </FormItem>
            </Form>
        </Dialog>
    )
}
