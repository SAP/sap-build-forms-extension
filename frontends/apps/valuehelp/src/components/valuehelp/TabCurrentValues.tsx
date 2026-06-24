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

/**
 *  The properties for the TabCurrentValues component.
 */
interface TabCurrentValuesProps {
    edit: boolean
    currentValueHelpDef: ValueHelpDef | undefined
    valueHelpValue: ValueHelpValue | undefined
    language: string | undefined
    changeValueHelpValue(v: ValueHelpValue): void
    changeLanguage(language: string, def: ValueHelpDef): void
    setDialogAddValueOpen(v: boolean): void
}

export default function (props: TabCurrentValuesProps) {
    const def = props.currentValueHelpDef
    const isLocal = def?.adapter === "local"

    // Columns: keyKey first, then all valueKeys
    const columns = def ? [def.keyKey, ...(def.valueKeys ?? [])] : []

    /**
     * Update a single cell value in the values array.
     */
    function updateCell(rowIndex: number, col: string, newVal: string) {
        const newValues = (props.valueHelpValue?.values ?? []).map((row, i) =>
            i === rowIndex ? { ...row, [col]: newVal } : row,
        )
        props.changeValueHelpValue({ ...props.valueHelpValue!, values: newValues })
    }

    /**
     * Remove a row from the values array.
     */
    function deleteRow(rowIndex: number) {
        const newValues = (props.valueHelpValue?.values ?? []).filter((_, i) => i !== rowIndex)
        props.changeValueHelpValue({ ...props.valueHelpValue!, values: newValues })
    }

    return (
        <Tab icon="folder" text="Current Values">
            <Form
                layout="S1 M1 L1 XL1"
                labelSpan="S4 M3 L2 XL2"
                style={{ alignItems: "center" }}
            >
                <FormItem labelContent={<Label>Language</Label>}>
                    <Select
                        key={props.language}
                        onChange={(e) =>
                            props.changeLanguage(e.detail.selectedOption.innerText, def!)
                        }
                        valueState="None"
                    >
                        {def?.languages.map((item: string) => (
                            <Option key={item} selected={props.language === item}>
                                {item}
                            </Option>
                        ))}
                    </Select>
                </FormItem>
                <FormItem labelContent={<Label>Values</Label>}>
                    <Table
                        headerRow={
                            <TableHeaderRow sticky>
                                {columns.map((col) => (
                                    <TableHeaderCell key={col}>{col}</TableHeaderCell>
                                ))}
                                {props.edit && isLocal && (
                                    <TableHeaderCell width="60px" />
                                )}
                            </TableHeaderRow>
                        }
                    >
                        {(props.valueHelpValue?.values ?? []).map((row, rowIndex) => (
                            <TableRow key={rowIndex}>
                                {columns.map((col, colIndex) => (
                                    <TableCell key={col}>
                                        {props.edit && isLocal ? (
                                            <Input
                                                type={InputType.Text}
                                                value={row[col] ?? ""}
                                                readonly={colIndex === 0}
                                                onChange={(e: Ui5CustomEvent<InputDomRef, never>) =>
                                                    updateCell(
                                                        rowIndex,
                                                        col,
                                                        e.target.attributes.getNamedItem("value")!.nodeValue!,
                                                    )
                                                }
                                                style={{ width: "100%" }}
                                            />
                                        ) : (
                                            <Text>{row[col]}</Text>
                                        )}
                                    </TableCell>
                                ))}
                                {props.edit && isLocal && (
                                    <TableCell>
                                        <Button
                                            icon="decline"
                                            design={ButtonDesign.Transparent}
                                            onClick={() => deleteRow(rowIndex)}
                                        />
                                    </TableCell>
                                )}
                            </TableRow>
                        ))}
                        {props.edit && props.language && isLocal && (
                            <TableRow>
                                <TableCell>
                                    <Button
                                        icon="add"
                                        onClick={() => props.setDialogAddValueOpen(true)}
                                    >
                                        Add value
                                    </Button>
                                </TableCell>
                                {columns.slice(1).map((col) => (
                                    <TableCell key={col} />
                                ))}
                                {props.edit && isLocal && <TableCell />}
                            </TableRow>
                        )}
                    </Table>
                </FormItem>
            </Form>
        </Tab>
    )
}
