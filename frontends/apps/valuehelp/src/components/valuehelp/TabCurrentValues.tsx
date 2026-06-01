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

import { ValueHelpDef, ValueHelpValue } from "../../features/model"
import { RefObject } from "react"

/**
 *  The properties for the DialogAddValueHelpValue component.
 */
interface DialogAddValueHelpValueProps {
    edit: boolean
    refValueHelpDef: RefObject<ValueHelpDef | undefined>
    valueHelpValue: ValueHelpValue | undefined
    language: string | undefined

    changeValueHelpValue(v: ValueHelpValue): void
    changeLanguage(language: string, def: ValueHelpDef): void
    setDialogAddValueOpen(v: boolean): void
}

export default function ({
    edit,
    refValueHelpDef,
    valueHelpValue,
    language,
    changeValueHelpValue,
    changeLanguage,
    setDialogAddValueOpen,
}: DialogAddValueHelpValueProps) {
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
                            changeLanguage(
                                e.detail.selectedOption.innerText,
                                refValueHelpDef.current!,
                            )
                        }
                        valueState="None"
                    >
                        {refValueHelpDef.current?.languages.map((item: string) => {
                            return (
                                <Option key={item} selected={language == item}>
                                    {item}
                                </Option>
                            )
                        })}
                    </Select>
                </FormItem>
                {/* <FormItem labelContent={<Label>Valid until</Label>}>
                    {valueHelpValue != undefined && (
                        <Label style={{ marginBlock: 10 }}>{valueHelpValue.validUntil}</Label>
                    )}
                </FormItem> */}
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
                        {valueHelpValue != undefined && (
                            <>
                                {Object.keys(valueHelpValue.values)
                                    .sort()
                                    .map((key) => {
                                        return (
                                            <TableRow key={key}>
                                                <TableCell>
                                                    <Label>{key}</Label>
                                                </TableCell>
                                                <TableCell>
                                                    {edit &&
                                                        refValueHelpDef.current?.adapter ==
                                                            "local" && (
                                                            <Input
                                                                type={InputType.Text}
                                                                value={valueHelpValue?.values[key]}
                                                                placeholder={
                                                                    valueHelpValue?.values[key]
                                                                }
                                                                onChange={(
                                                                    e: Ui5CustomEvent<
                                                                        InputDomRef,
                                                                        never
                                                                    >,
                                                                ) => {
                                                                    var v = valueHelpValue?.values
                                                                    v[key] =
                                                                        e.target.attributes.getNamedItem(
                                                                            "value",
                                                                        )!.nodeValue!
                                                                    changeValueHelpValue({
                                                                        ...valueHelpValue!,
                                                                        values: v,
                                                                    })
                                                                }}
                                                                style={{ width: "100%" }}
                                                            />
                                                        )}
                                                    {(!edit ||
                                                        refValueHelpDef.current?.adapter !=
                                                            "local") && (
                                                        <Text>{valueHelpValue?.values[key]}</Text>
                                                    )}
                                                </TableCell>
                                                <TableCell>
                                                    {edit &&
                                                        refValueHelpDef.current?.adapter ==
                                                            "local" && (
                                                            <Button
                                                                icon="decline"
                                                                design={ButtonDesign.Transparent}
                                                                onClick={() => {
                                                                    var v = valueHelpValue?.values
                                                                    delete v[key]
                                                                    changeValueHelpValue({
                                                                        ...valueHelpValue!,
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
                        {edit && language && refValueHelpDef.current?.adapter == "local" && (
                            <TableRow>
                                <TableCell>
                                    <Button
                                        icon="add"
                                        onClick={() => {
                                            setDialogAddValueOpen(true)
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
