import {
    Button,
    CheckBox,
    Input,
    Table,
    TableCell,
    TableHeaderCell,
    TableHeaderRow,
    TableRow,
} from "@ui5/webcomponents-react"
import { useIntl } from "react-intl"
import { Elem } from "../../utils/scenarioDefinitions"

interface Props {
    el: Elem
    setNewEl: (e: any) => void
}

export default function CategoriesTable(props: Props) {
    const intl = useIntl()
    return (
            <Table
                headerRow={
                    <TableHeaderRow>
                        <TableHeaderCell min-width="120px" >
                            <span>{intl.formatMessage({ id: "categories_col_label" })}</span>
                        </TableHeaderCell>
                        <TableHeaderCell min-width="120px" >
                            <span>{intl.formatMessage({ id: "categories_col_helpvalue_name" })}</span>
                        </TableHeaderCell>
                        <TableHeaderCell min-width="80px" >
                            <span>{intl.formatMessage({ id: "categories_col_helpvalue_validate" })}</span>
                        </TableHeaderCell>
                        <TableHeaderCell min-width="80px" >
                            <span>{intl.formatMessage({ id: "categories_col_helpvalue_empty_selection" })}</span>
                        </TableHeaderCell>
                        <TableHeaderCell min-width="120px" >
                            <span>{intl.formatMessage({ id: "categories_col_helpvalue_display_format" })}</span>
                        </TableHeaderCell>
                        <TableHeaderCell width="55px" >
                            <Button
                                onClick={() => {
                                    props.setNewEl({
                                        ...props.el,
                                        categories: [
                                            ...props.el?.categories!,
                                            {
                                                label: "",
                                                hvOpt: {
                                                    name: "",
                                                    valiidate: false,
                                                    emptySelectoin: false,
                                                    displayFormat: "",
                                                },
                                            },
                                        ],
                                    })
                                }}
                            >
                                +
                            </Button>
                        </TableHeaderCell>
                    </TableHeaderRow>
                }
            >
                {props.el.categories?.map((item) => {
                    return (
                        <TableRow key={Math.random()}>
                            <TableCell>
                                <Input
                                    value={item.label}
                                    onChange={(e) => {
                                        props.setNewEl({
                                            ...props.el,
                                            categories: props.el?.categories?.map((v) =>
                                                v == item
                                                    ? {
                                                        ...item,
                                                        label: e.target.attributes.getNamedItem(
                                                            "value",
                                                        )!.nodeValue!,
                                                    }
                                                    : v,
                                            ),
                                        })
                                    }}
                                />
                            </TableCell>
                            <TableCell>
                                <Input
                                    value={item.hvOpt.name}
                                    onChange={(e) => {
                                        props.setNewEl({
                                            ...props.el,
                                            categories: props.el?.categories?.map((v) =>
                                                v == item
                                                    ? {
                                                        ...item,
                                                        hvOpt: {
                                                            ...item.hvOpt,
                                                            name: e.target.attributes.getNamedItem(
                                                                "value",
                                                            )!.nodeValue!,
                                                        },
                                                    }
                                                    : v,
                                            ),
                                        })
                                    }}
                                />
                            </TableCell>
                            <TableCell>
                                <CheckBox
                                    checked={item.hvOpt.validate}
                                    onChange={(e) => {
                                        props.setNewEl({
                                            ...props.el,
                                            categories: props.el?.categories?.map((v) =>
                                                v == item
                                                    ? {
                                                        ...item,
                                                        hvOpt: {
                                                            ...item.hvOpt,
                                                            validate: e.target.checked,
                                                        },
                                                    }
                                                    : v,
                                            ),
                                        })
                                    }}
                                />
                            </TableCell>
                            <TableCell>
                                <CheckBox
                                    checked={item.hvOpt.emptySelection}
                                    onChange={(e) => {
                                        props.setNewEl({
                                            ...props.el,
                                            categories: props.el?.categories?.map((v) =>
                                                v == item
                                                    ? {
                                                        ...item,
                                                        hvOpt: {
                                                            ...item.hvOpt,
                                                            emptySelection: e.target.checked,
                                                        },
                                                    }
                                                    : v,
                                            ),
                                        })
                                    }}
                                />
                            </TableCell>

                            <TableCell>
                                <Input
                                    value={item.hvOpt.displayFormat}
                                    onChange={(e) => {
                                        props.setNewEl({
                                            ...props.el,
                                            categories: props.el?.categories?.map((v) =>
                                                v == item
                                                    ? {
                                                        ...item,
                                                        hvOpt: {
                                                            ...item.hvOpt,
                                                            displayFormat:
                                                                e.target.attributes.getNamedItem(
                                                                    "value",
                                                                )!.nodeValue!,
                                                        },
                                                    }
                                                    : v,
                                            ),
                                        })
                                    }}
                                />
                            </TableCell>

                            <TableCell>
                                <Button
                                    onClick={function Ta() {
                                        props.setNewEl({
                                            ...props.el,
                                            categories: props.el?.categories?.filter((a) => a !== item),
                                        })
                                    }}
                                >
                                    -
                                </Button>
                            </TableCell>
                        </TableRow>
                    )
                })}
            </Table>
    )
}
