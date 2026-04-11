import {
    Button,
    Form,
    FormItem,
    Input,
    InputDomRef,
    Label,
    Option,
    Select,
    Tab,
    Table,
    TableCell,
    TableHeaderCell,
    TableHeaderRow,
    TableRow,
    Text,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import InputType from "@ui5/webcomponents/dist/types/InputType"
import ButtonDesign from "@ui5/webcomponents/dist/types/ButtonDesign"

import {ValueHelpDef, ValueHelpValue} from "../../features/model"

/**
 *  The properties for the DialogAddValueHelpValue component.
 */
interface DialogAddValueHelpValueProps {
    edit: boolean
    currentValueHelpDef: ValueHelpDef | undefined
    valueHelpValue: ValueHelpValue | undefined
    language: string | undefined

    setCurrentValueHelpDef(v: ValueHelpDef): void

    changeValueHelpValue(v: ValueHelpValue): void

    changeLanguage(language: string, def: ValueHelpDef): void

    setDialogAddValueOpen(v: boolean): void
}

export default function (props: DialogAddValueHelpValueProps) {
    return (
        <Tab icon="folder" text="Current Values">
            <Form
                layout="S1 M1 L1 XL1"
                labelSpan="S4 M3 L2 XL2"
                style={{
                    alignItems: "center",
                }}
            >
                <FormItem labelContent={<Label>Language</Label>}>
                    <Select
                        onChange={(e) =>
                            props.changeLanguage(
                                e.detail.selectedOption.innerText,
                                props.currentValueHelpDef!,
                            )
                        }
                        valueState="None"
                    >
                        {props.currentValueHelpDef?.languages.map((item: string) => {
                            return (
                                <Option key={item} selected={props.language == item}>
                                    {item}
                                </Option>
                            )
                        })}
                    </Select>
                </FormItem>
                <FormItem labelContent={<Label>Valid until</Label>}>
                    {props.valueHelpValue != undefined && (
                        <Label style={{marginBlock: 10}}>{props.valueHelpValue.validUntil}</Label>
                    )}
                </FormItem>
                <FormItem labelContent={<Label>Values</Label>}>
                    <Table
                        headerRow={
                            <TableHeaderRow sticky>
                                <TableHeaderCell width="12rem">Key</TableHeaderCell>
                                <TableHeaderCell>Value</TableHeaderCell>
                                <TableHeaderCell width="100px"></TableHeaderCell>
                            </TableHeaderRow>
                        }
                    >
                        {props.valueHelpValue != undefined && (
                            <>
                                {Object.keys(props.valueHelpValue.values)
                                    .sort()
                                    .map(function (key) {
                                        return (
                                            <TableRow key={key}>
                                                <TableCell>
                                                    <Label>{key}</Label>
                                                </TableCell>
                                                <TableCell>
                                                    {props.edit &&
                                                        props.currentValueHelpDef?.adapter ==
                                                        "local" && (
                                                            <Input
                                                                type={InputType.Text}
                                                                value={
                                                                    props.valueHelpValue?.values[
                                                                        key
                                                                        ]
                                                                }
                                                                placeholder={
                                                                    props.valueHelpValue?.values[
                                                                        key
                                                                        ]
                                                                }
                                                                onChange={(
                                                                    e: Ui5CustomEvent<
                                                                        InputDomRef,
                                                                        never
                                                                    >,
                                                                ) => {
                                                                    var v =
                                                                        props.valueHelpValue?.values
                                                                    v[key] =
                                                                        e.target.attributes.getNamedItem(
                                                                            "value",
                                                                        )!.nodeValue!
                                                                    props.changeValueHelpValue({
                                                                        ...props.valueHelpValue!,
                                                                        values: v,
                                                                    })
                                                                }}
                                                                style={{width: "100%"}}
                                                            />
                                                        )}
                                                    {(!props.edit ||
                                                        props.currentValueHelpDef?.adapter !=
                                                        "local") && (
                                                        <Text>
                                                            {props.valueHelpValue?.values[key]}
                                                        </Text>
                                                    )}
                                                </TableCell>
                                                <TableCell>
                                                    {props.edit &&
                                                        props.currentValueHelpDef?.adapter ==
                                                        "local" && (
                                                            <Button
                                                                icon="decline"
                                                                design={ButtonDesign.Transparent}
                                                                onClick={() => {
                                                                    var v =
                                                                        props.valueHelpValue?.values
                                                                    delete v[key]
                                                                    props.changeValueHelpValue({
                                                                        ...props.valueHelpValue!,
                                                                        values: v,
                                                                    })
                                                                }}
                                                            />
                                                        )}
                                                </TableCell>
                                            </TableRow>
                                        )
                                    })}
                            </>
                        )}
                        {props.edit &&
                            props.language &&
                            props.currentValueHelpDef?.adapter == "local" && (
                                <TableRow>
                                    <TableCell>
                                        <Button
                                            icon="add"
                                            onClick={() => {
                                                props.setDialogAddValueOpen(true)
                                            }}
                                        >
                                            Add value
                                        </Button>
                                    </TableCell>
                                    <TableCell></TableCell>
                                    <TableCell></TableCell>
                                </TableRow>
                            )}
                    </Table>
                </FormItem>
            </Form>
        </Tab>
    )
}
