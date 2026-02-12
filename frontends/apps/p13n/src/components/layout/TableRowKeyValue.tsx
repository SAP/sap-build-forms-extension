import { Button, Input, TableCell, TableRow } from "@ui5/webcomponents-react"
import { Value } from "../../features/personalizationDefinitions"
import { useIntl } from "react-intl"
import { useEffect, useState } from "react"
import ButtonDesign from "@ui5/webcomponents/dist/types/ButtonDesign"

interface DialogAddUserProps {
    item: string
    edit: boolean
    locale: string
    values: Value[]
    setValues(v: Value[]): void
}

export default function (props: DialogAddUserProps) {
    useEffect(() => {
        if (val.length == 0 || val.includes(")") || val.includes("(")) {
            setError(true)
        } else {
            setError(false)
        }
    }, [props.item])

    const intl = useIntl()
    var key = props.item.substring(props.item.indexOf("(") + 1, props.item.lastIndexOf(")"))
    var val: string
    if (key) {
        val = props.item.substring(0, props.item.lastIndexOf(key) - 1).trim()
    } else {
        val = props.item
    }

    const [error, setError] = useState(false)

    return (
        <TableRow key={props.item}>
            <TableCell>
                <span>{key}</span>
            </TableCell>
            <TableCell>
                {!props.edit && <span>{val}</span>}
                {props.edit && (
                    <Input
                        value={val}
                        valueState={
                            val.length == 0 ||
                            val.includes(")") ||
                            val.includes("(") ||
                            error == true
                                ? "Negative"
                                : "None"
                        }
                        onChange={(e) => {
                            if (
                                e.target.value.trim().length > 0 &&
                                !e.target.value.includes("(") &&
                                !e.target.value.includes(")")
                            ) {
                                setError(false)
                                props.setValues(
                                    props.values.map((v: Value) =>
                                        v.locale == props.locale
                                            ? {
                                                  ...v,
                                                  values: v.values.map((t: string) =>
                                                      t == props.item
                                                          ? e.target.value.trim() +
                                                            `${key ? ` (${key})` : ""}`
                                                          : t,
                                                  ),
                                              }
                                            : v,
                                    ),
                                )
                            } else {
                                setError(true)
                            }
                        }}
                    />
                )}
            </TableCell>
            <TableCell>
                {props.edit && props.locale == "_" && (
                    <Button
                        design={ButtonDesign.Transparent}
                        icon="decline"
                        onClick={() => {
                            props.setValues(
                                props.values.map((v: Value) => ({
                                    ...v,
                                    values: key
                                        ? v.values.filter((va) => {
                                              return !va.includes("(" + key + ")")
                                          })
                                        : v.values.filter((va) => {
                                              return va != props.item
                                          }),
                                })),
                            )
                        }}
                        tooltip={intl.formatMessage({ id: "p13n_form_delete_key" })}
                    />
                )}
            </TableCell>
        </TableRow>
    )
}
