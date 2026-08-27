import {
    Button,
    CheckBox,
    Input,
    Option,
    Select,
    Table,
    TableCell,
    TableHeaderCell,
    TableHeaderRow,
    TableRow,
} from "@ui5/webcomponents-react"
import {
    BeanValidation,
    Elem,
    FixedValidation,
    MinMaxValidation,
    RegexValidation,
    severities,
    SpELValidation,
} from "../../utils/scenarioDefinitions"

interface Props {
    el: Elem
    setNewEl: (e: any) => void
}

export default function ValidationTable(props: Props) {
    return (
            <Table
                headerRow={
                    <TableHeaderRow>
                        <TableHeaderCell min-width="120px" > 
                            <span>Type</span>
                        </TableHeaderCell>
                        <TableHeaderCell min-width="120px" >
                            <span>Value</span>
                        </TableHeaderCell>
                        <TableHeaderCell min-width="140px" >
                            <span>Severity</span>
                        </TableHeaderCell>
                        <TableHeaderCell min-width="120px" >
                            <span>MessageKey</span>
                        </TableHeaderCell>
                        <TableHeaderCell width="55px" >
                            <Button
                                onClick={() => {
                                    if (props.el?.validationRules) {
                                        props.setNewEl({
                                            ...props.el,
                                            validationRules: [
                                                ...props.el?.validationRules!,
                                                {
                                                    type: "regex",
                                                    pattern: "",
                                                    severity: "information",
                                                    messageKey: "",
                                                },
                                            ],
                                        })
                                    } else {
                                        props.setNewEl({
                                            ...props.el,
                                            validationRules: [
                                                {
                                                    type: "regex",
                                                    pattern: "",
                                                    severity: "information",
                                                    messageKey: "",
                                                },
                                            ],
                                        })
                                    }
                                }}
                            >
                                +
                            </Button>
                        </TableHeaderCell>
                    </TableHeaderRow>
                }
        >
            {props.el.validationRules?.map((item) => {
                return (
                    <TableRow key={Math.random()}>
                        <TableCell>
                            <Select
                                onChange={function Ta(e) {
                                    const type = e.detail.selectedOption.id
                                    if (["min", "max"].includes(type)) {
                                        var item3: MinMaxValidation = {
                                            type: type,
                                            severity: item.severity,
                                            messageKey: item.messageKey,
                                            limit: "",
                                            inclusive: false,
                                        }
                                        props.setNewEl({
                                            ...props.el,
                                            validationRules: props.el?.validationRules?.map((v) =>
                                                v == item
                                                    ? {
                                                          ...item3,
                                                      }
                                                    : v,
                                            ),
                                        })
                                    } else if (type == "fixed") {
                                        var item4: FixedValidation = {
                                            type: type,
                                            severity: item.severity,
                                            messageKey: item.messageKey,
                                            length: "",
                                            fractions: "",
                                        }
                                        props.setNewEl({
                                            ...props.el,
                                            validationRules: props.el?.validationRules?.map((v) =>
                                                v == item
                                                    ? {
                                                          ...item4,
                                                      }
                                                    : v,
                                            ),
                                        })
                                    } else if (type == "spel") {
                                        var item5: SpELValidation = {
                                            type: type,
                                            severity: item.severity,
                                            messageKey: item.messageKey,
                                            expression: "",
                                        }
                                        props.setNewEl({
                                            ...props.el,
                                            validationRules: props.el?.validationRules?.map((v) =>
                                                v == item
                                                    ? {
                                                          ...item5,
                                                      }
                                                    : v,
                                            ),
                                        })
                                    } else if (type == "regex") {
                                        var item6: RegexValidation = {
                                            type: type,
                                            severity: item.severity,
                                            messageKey: item.messageKey,
                                            pattern: "",
                                        }
                                        props.setNewEl({
                                            ...props.el,
                                            validationRules: props.el?.validationRules?.map((v) =>
                                                v == item
                                                    ? {
                                                          ...item6,
                                                      }
                                                    : v,
                                            ),
                                        })
                                    } else if (type == "bean") {
                                        var item7: BeanValidation = {
                                            type: type,
                                            severity: item.severity,
                                            messageKey: item.messageKey,
                                            beanName: "",
                                        }
                                        props.setNewEl({
                                            ...props.el,
                                            validationRules: props.el?.validationRules?.map((v) =>
                                                v == item
                                                    ? {
                                                          ...item7,
                                                      }
                                                    : v,
                                            ),
                                        })
                                    }
                                }}
                            >
                                {(["string", "int", "decimal", "date", "time", "datetime"].includes(
                                    props.el?.dataType!,
                                ) ||
                                    ["table"].includes(props.el?.type!)) && (
                                    <Option selected={item.type == "min"} key="min" id="min">
                                        min
                                    </Option>
                                )}
                                {(["string", "int", "decimal", "date", "time", "datetime"].includes(
                                    props.el?.dataType!,
                                ) ||
                                    ["table", "attachment"].includes(props.el?.type!)) && (
                                    <Option selected={item.type == "max"} key="max" id="max">
                                        max
                                    </Option>
                                )}
                                {(["string", "decimal"].includes(props.el?.type!) ||
                                    ["string", "decimal"].includes(props.el?.dataType!)) && (
                                    <Option selected={item.type == "fixed"} key="fixed" id="fixed">
                                        fixed
                                    </Option>
                                )}
                                {["input", "edit"].includes(props.el?.type!) && (
                                    <Option selected={item.type == "regex"} key="regex" id="regex">
                                        regex
                                    </Option>
                                )}
                                <Option selected={item.type == "spel"} key="spel" id="spel">
                                    SpEL
                                </Option>
                                <Option selected={item.type == "bean"} key="bean" id="bean">
                                    bean
                                </Option>
                            </Select>
                        </TableCell>

                        <TableCell>
                            <div
                                style={{
                                    display: "flex",
                                    flexDirection: "column",
                                    width: "100%",
                                }}
                            >
                                <Input
                                    style={{ width: "100%" }}
                                    value={(() => {
                                        switch (item.type?.toLowerCase()) {
                                            case "min":
                                                return (item as MinMaxValidation).limit
                                            case "max":
                                                return (item as MinMaxValidation).limit
                                            case "fixed":
                                                return (item as FixedValidation).length
                                            case "regex":
                                                return (item as RegexValidation).pattern
                                            case "spel":
                                                return (item as SpELValidation).expression
                                            case "bean":
                                                return (item as BeanValidation).beanName
                                            default:
                                                return ""
                                        }
                                    })()}
                                    placeholder={(() => {
                                        switch (item.type?.toLowerCase()) {
                                            case "min":
                                                return "limit"
                                            case "max":
                                                return "limit"
                                            case "fixed":
                                                return "length"
                                            case "regex":
                                                return "pattern"
                                            case "spel":
                                                return "expression"
                                            case "bean":
                                                return "beanName"
                                            default:
                                                return "limit"
                                        }
                                    })()}
                                    onChange={(e) => {
                                        if (
                                            item.type?.toLowerCase() == "min" ||
                                            item.type?.toLowerCase() == "max"
                                        ) {
                                            props.setNewEl({
                                                ...props.el,
                                                validationRules: props.el?.validationRules?.map(
                                                    (v) =>
                                                        v === item
                                                            ? {
                                                                  ...item,
                                                                  limit: e.target.attributes.getNamedItem(
                                                                      "value",
                                                                  )!.nodeValue!,
                                                                  inclusive: false,
                                                              }
                                                            : v,
                                                ),
                                            })
                                        } else if (item.type?.toLowerCase() == "fixed") {
                                            props.setNewEl({
                                                ...props.el,
                                                validationRules: props.el?.validationRules?.map(
                                                    (v) =>
                                                        v === item
                                                            ? {
                                                                  ...item,
                                                                  length: e.target.attributes.getNamedItem(
                                                                      "value",
                                                                  )!.nodeValue!,
                                                              }
                                                            : v,
                                                ),
                                            })
                                        } else if (item.type?.toLowerCase() == "regex") {
                                            props.setNewEl({
                                                ...props.el,
                                                validationRules: props.el?.validationRules?.map(
                                                    (v) =>
                                                        v === item
                                                            ? {
                                                                  ...item,
                                                                  pattern:
                                                                      e.target.attributes.getNamedItem(
                                                                          "value",
                                                                      )!.nodeValue!,
                                                              }
                                                            : v,
                                                ),
                                            })
                                        } else if (item.type?.toLowerCase() == "spel") {
                                            props.setNewEl({
                                                ...props.el,
                                                validationRules: props.el?.validationRules?.map(
                                                    (v) =>
                                                        v === item
                                                            ? {
                                                                  ...item,
                                                                  expression:
                                                                      e.target.attributes.getNamedItem(
                                                                          "value",
                                                                      )!.nodeValue!,
                                                              }
                                                            : v,
                                                ),
                                            })
                                        } else if (item.type?.toLowerCase() == "bean") {
                                            props.setNewEl({
                                                ...props.el,
                                                validationRules: props.el?.validationRules?.map(
                                                    (v) =>
                                                        v === item
                                                            ? {
                                                                  ...item,
                                                                  beanName:
                                                                      e.target.attributes.getNamedItem(
                                                                          "value",
                                                                      )!.nodeValue!,
                                                              }
                                                            : v,
                                                ),
                                            })
                                        }
                                    }}
                                />
                                {(item.type == "min" || item.type == "max") && (
                                    <CheckBox
                                        text={`Include value`}
                                        checked={(item as MinMaxValidation).inclusive}
                                        style={{ maxWidth: "100%" }}
                                        onChange={(e) => {
                                            props.setNewEl({
                                                ...props.el,
                                                validationRules: props.el?.validationRules?.map(
                                                    (v) =>
                                                        v === item
                                                            ? {
                                                                  ...item,
                                                                  inclusive: e.target.checked!,
                                                              }
                                                            : v,
                                                ),
                                            })
                                        }}
                                    />
                                )}
                                {item.type == "fixed" && (
                                    <Input
                                        style={{ width: "100%" }}
                                        value={(item as FixedValidation).fractions}
                                        placeholder="fractions"
                                        onChange={(e) => {
                                            props.setNewEl({
                                                ...props.el,
                                                validationRules: props.el?.validationRules?.map(
                                                    (v) =>
                                                        v === item
                                                            ? {
                                                                  ...item,
                                                                  fractions:
                                                                      e.target.attributes.getNamedItem(
                                                                          "value",
                                                                      )!.nodeValue!,
                                                              }
                                                            : v,
                                                ),
                                            })
                                        }}
                                    />
                                )}
                            </div>
                        </TableCell>

                        <TableCell>
                            <Select
                                onChange={function Ta(e) {
                                    props.setNewEl({
                                        ...props.el,
                                        validationRules: props.el?.validationRules?.map((v) =>
                                            v === item
                                                ? {
                                                      ...item,
                                                      severity: e.detail.selectedOption.id,
                                                  }
                                                : v,
                                        ),
                                    })
                                }}
                            >
                                {severities.map((severity) => {
                                    return (
                                        <Option
                                            key={severity.text}
                                            id={severity.id}
                                            selected={item.severity == severity.id}
                                            icon={severity.icon}
                                        >
                                            {severity.text}
                                        </Option>
                                    )
                                })}
                            </Select>
                        </TableCell>

                        <TableCell>
                            <Input
                                value={item.messageKey}
                                onChange={(e) => {
                                    props.setNewEl({
                                        ...props.el,
                                        validationRules: props.el?.validationRules?.map((v) =>
                                            v == item
                                                ? {
                                                      ...item,
                                                      messageKey:
                                                          e.target.attributes.getNamedItem("value")!
                                                              .nodeValue!,
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
                                        validationRules: props.el?.validationRules?.filter(
                                            (a) => a !== item,
                                        ),
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
