import React, { useEffect, useMemo, useRef, useState } from "react"
import {
    AttachmentDesignType,
    DataTypeValue,
    DesignValue,
    Elem,
    ElementPart,
    ElemForTable,
    InputValue,
    leafNodes,
    Message,
    Parent,
    Scenario,
    SelectValue,
    StyleValue,
    TextPostfix,
    UploadType,
} from "../../utils/scenarioDefinitions"
import {
    AnalyticalTable,
    AnalyticalTableVisibleRowCountMode,
    Bar,
    Breadcrumbs,
    Button,
    CheckBox,
    Dialog,
    FlexBox,
    Input,
    MessageBoxType,
    Option,
    Select,
} from "@ui5/webcomponents-react"
import { createUseStyles } from "react-jss"
import useMessagesStore from "../../state/messages"
import ElementTypeSelect from "./ElementTypeSelect"
import useElementsStore from "../../state/elements"
import {
    calculateCol,
    changeElAtTypeChangeTable,
    elementInfo2ValueState,
    getHighestSeverity,
} from "../../utils/formUtils"
import BreadcrumbItemParents from "./BreadcrumbItemParents"
import { RowType } from "@ui5/webcomponents-react/dist/components/AnalyticalTable/types"
import ValidationTable from "./ValidationTable"
import StructureTabTextsInput from "./StructureTabTextsInput"
import CategoriesTable from "./CategoriesTable"

interface Props {
    version: number
    defaultLanguage: string | undefined
    treeItemsShown: Scenario | null | undefined
    el: Elem | undefined
    element: string
    parents: Parent[]
    scenarioMixinName: string
    update: number
    renderTable: number
    setEl: (e: any) => void
    setElement: (e: any) => void
    setParents: (e: any) => void
    setIndexesDelete: (e: any) => void
    setAddDialogOpen: (e: any) => void
    openMessageBox: (e1: any, e2: any, e3: any) => void
    setUpdate: (e: any) => void
    setRenderTable: (e: any) => void
}

const useStyles = createUseStyles({
    largeInput: {
        width: "100%",
    },
    buttonsTree: {
        display: "flex",
        flexDirection: "column",
        marginRight: 20,
    },
    buttonTree: {
        width: 40,
        marginRight: 2,
        marginBlock: 5,
    },
    dialog: {
        paddingTop: 10,
        paddingInline: 3,
        minWidth: 900,
    },
    bar: {
        paddingBlock: 3,
    },
    dialogButton: {
        marginInline: 2,
    },
    errorLabel: {
        color: "red",
    },
})

export default function StructureTabTable(props: Props) {
    const classes = useStyles()
    const editBaseData = useElementsStore((state) => state.editBaseData)
    const editDetailData = useElementsStore((state) => state.editDetailData)
    const editTexts = useElementsStore((state) => state.editTexts)

    const allMessages = useMessagesStore((state) => state.messages)
    const messages = React.useMemo(
        () => allMessages.filter((m: Message) => m.defVersion == props.version),
        [allMessages, props.version]
    )

    const [elementsTable, setElementsTable] = useState<ElemForTable[]>([])
    const [elementsTableShown, setElementsTableShown] = useState<ElemForTable[]>(elementsTable)
    const [selectedElemForTable, setSelectedElemForTable] = useState<ElemForTable | undefined>()
    const [selectedRowId, setSelectedRowId] = useState<Record<string, boolean>>()
    const [rowsById, setRowsById] = useState<Record<string, RowType>>()

    const elementsTableRef = useRef(elementsTable)
    const treeItemsRef = useRef(props.treeItemsShown)

    const [validationDialogOpen, setValidationDialogOpen] = useState<boolean>(false)
    const [categoriesDialogOpen, setCategoriesDialogOpen] = useState<boolean>(false)

    const memorizedData = useMemo(() => elementsTableShown, [elementsTableShown])
    const memorizedSelectedRow = useMemo(() => selectedRowId, [selectedRowId])

    console.log(memorizedData)

    useEffect(() => {
        treeItemsRef.current = props.treeItemsShown
        if (props.treeItemsShown?.elements) {
            var preparedItems = prepareItems(props.treeItemsShown.elements, undefined, "")
            var flattenedItems = constructTree(preparedItems)
            elementsTableRef.current = flattenedItems
            setElementsTable(flattenedItems)
            if (preparedItems.length != elementsTableShown.length) {
                setElementsTableShown(preparedItems)
            }
        }
    }, [props.treeItemsShown, props.update])

    useEffect(() => {
        treeItemsRef.current = props.treeItemsShown
        if (props.treeItemsShown?.elements) {
            var preparedItems = prepareItems(props.treeItemsShown.elements, undefined, "")
            var flattenedItems = constructTree(preparedItems)
            elementsTableRef.current = flattenedItems
            setElementsTable(flattenedItems)
            setElementsTableShown(preparedItems)
        }
    }, [props.renderTable])

    useEffect(() => {
        treeItemsRef.current = props.treeItemsShown
        if (props.treeItemsShown?.elements) {
            var preparedItems = prepareItems(props.treeItemsShown.elements, undefined, "")
            var flattenedItems = constructTree(preparedItems)
            elementsTableRef.current = flattenedItems
            setElementsTable(flattenedItems)
            setElementsTableShown(preparedItems)
        }
    }, [])

    const updateEl = (newEl: ElemForTable) => {
        elementsTableRef.current = elementsTableRef.current.map((item) =>
            item.index == newEl.index ? newEl : item,
        )
        const newEl2: ElemForTable = { ...newEl }
        const { parent, index, ...rest } = newEl2
        Object.assign(newEl2, rest)
        editDetailData({
            version: props.version,
            scenarioMixinName: props.scenarioMixinName,
            indexes: newEl.index,
            newEl: newEl2,
        })
        props.setElement(newEl.index)
        props.setEl(newEl2)
        props.setUpdate((prev: number) => prev + 1)
    }

    const getElemByIndex = (index: string): ElemForTable | undefined => {
        return elementsTableRef.current.find((item) => item.index === index)
    }

    // Helper function to flatten tree structure
    function constructTree(items: ElemForTable[]): ElemForTable[] {
        const result: ElemForTable[] = []
        items.forEach(item => {
            result.push(item)
            if (item.subRows && item.subRows.length > 0) {
                result.push(...constructTree(item.subRows))
            }
        })
        return result
    }

    function prepareItems(
        elements: Elem[],
        parent: ElemForTable | undefined,
        index: string,
    ): ElemForTable[] {
        return elements.map((elem, i) => {
            var newIndex = index + i + "x"
            var newElem: ElemForTable = { ...elem, index: newIndex, parent: parent, subRows: [] }

            if (elem.headerSegment) {
                var newIndex2 = newIndex + "hx"
                var newHeaderSegment: ElemForTable = {
                    ...elem.headerSegment,
                    index: newIndex2,
                    parent: newElem,
                    subRows: prepareItems(elem.headerSegment.elements, newElem, newIndex2)
                }
                newElem.subRows!.push(newHeaderSegment)
            }

            if (elem.toolbar) {
                var newIndex2 = newIndex + "tx"
                var newToolbar: ElemForTable = {
                    ...elem.toolbar,
                    index: newIndex2,
                    parent: newElem,
                    subRows: prepareItems(elem.toolbar.elements, newElem, newIndex2)
                }
                newElem.subRows!.push(newToolbar)
            }

            if (elem.elements && elem.elements.length > 0) {
                newElem.subRows!.push(...prepareItems(elem.elements, newElem, newIndex))
            }

            if (elem.footer) {
                var newIndex2 = newIndex + "fx"
                var footerSubRows: ElemForTable[] = []

                if (elem.footer.elements) {
                    footerSubRows.push(...prepareItems(elem.footer.elements, newElem, newIndex2))
                }

                if (elem.footer.leftElements) {
                    footerSubRows.push(...prepareItems(elem.footer.leftElements, newElem, newIndex2 + "lx"))
                }

                if (elem.footer.rightElements) {
                    footerSubRows.push(...prepareItems(elem.footer.rightElements, newElem, newIndex2 + "rx"))
                }

                var newFooter: ElemForTable = {
                    ...elem.footer,
                    index: newIndex2,
                    parent: newElem,
                    subRows: footerSubRows
                }
                newElem.subRows!.push(newFooter)
            }

            if (elem.leftElements) {
                newElem.subRows!.push(...prepareItems(elem.leftElements, newElem, newIndex + "lx"))
            }

            if (elem.rightElements) {
                newElem.subRows!.push(...prepareItems(elem.rightElements, newElem, newIndex + "rx"))
            }

            return newElem
        })
    }

    // rows are expanded when opening the TreeTable
    const expandedState = useMemo(() => {
        const expanded: Record<string, boolean> = {};

        const expandAll = (rows: any[], parentId = '') => {
            rows.forEach((row, idx) => {
                const id = parentId ? `${parentId}.${idx}` : `${idx}`;
                expanded[id] = true;
                if (row.subRows?.length) {
                    expandAll(row.subRows, id);
                }
            });
        };

        expandAll(memorizedData);
        return expanded;
    }, [memorizedData]);

    const columns = useMemo(
        () => [
            {
                Header: "Name",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.name
                },
                width: 250,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <Input
                                value={getElemByIndex(instance.row.original.index)?.name}
                                className={classes.largeInput}
                                onChange={(e) => {
                                    const oldName = getElemByIndex(
                                        instance.row.original.index,
                                    )?.name
                                    var newName: any =
                                        e.target.attributes.getNamedItem("value")!.nodeValue!

                                    if (
                                        treeItemsRef.current?.root != undefined &&
                                        treeItemsRef.current?.root === oldName
                                    ) {
                                        editBaseData({
                                            scenarioMixinName: props.scenarioMixinName,
                                            version: props.version,
                                            root: newName,
                                        })
                                        props.setUpdate((prev: number) => prev + 1)
                                    }

                                    var texts: any = JSON.parse(
                                        JSON.stringify(treeItemsRef.current?.texts!),
                                    )

                                    Object.keys(treeItemsRef.current?.texts!).map((l) => {
                                        texts![l][`${newName}.short`] =
                                            texts![l][`${oldName}.short` as any]
                                        delete texts![l][`${oldName}.short`]

                                        texts[l]![`${newName}.long`] =
                                            texts![l][`${oldName}.long` as any]
                                        delete texts![l][`${oldName}.long`]

                                        texts![l][`${newName}.title`] =
                                            texts![l][`${oldName}.title` as any]
                                        delete texts![l][`${oldName}.title`]

                                        texts[l][`${newName}.doc`] =
                                            texts[l][`${oldName}.doc` as any]
                                        delete texts[l][`${oldName}.doc`]
                                    })

                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            name: newName,
                                        })
                                    }

                                    editTexts({
                                        version: props.version,
                                        texts: texts,
                                        scenarioMixinName: props.scenarioMixinName,
                                    })
                                    props.setUpdate((prev: number) => prev + 1)
                                }}
                                valueState={
                                    messages.filter(
                                        (m) =>
                                            m.elementId ==
                                            getElemByIndex(instance.row.original.index)?.name &&
                                            m.elementPart == ElementPart.Name,
                                    ).length > 0
                                        ? elementInfo2ValueState(
                                            getHighestSeverity(
                                                messages
                                                    .filter(
                                                        (a: any) =>
                                                            a.elementId ==
                                                            getElemByIndex(
                                                                instance.row.original.index,
                                                            )?.name &&
                                                            a.elementPart == ElementPart.Name,
                                                    )
                                                    .map((e: Message) => e.severity),
                                            ),
                                        )
                                        : "None"
                                }
                                valueStateMessage={
                                    messages.filter(
                                        (m) =>
                                            m.elementId ==
                                            getElemByIndex(instance.row.original.index)?.name &&
                                            m.elementPart == ElementPart.Name,
                                    ).length > 0 ? (
                                        messages.filter(
                                            (a: any) =>
                                                a.elementId ==
                                                getElemByIndex(instance.row.original.index)
                                                    ?.name && a.elementPart == ElementPart.Name,
                                        ).length > 1 ? (
                                            <span>
                                                Errors:
                                                <ul>
                                                    {messages
                                                        .filter(
                                                            (a: any) =>
                                                                a.elementId ==
                                                                getElemByIndex(
                                                                    instance.row.original.index,
                                                                )?.name &&
                                                                a.elementPart == ElementPart.Name,
                                                        )
                                                        .map((m: Message) => (
                                                            <li key={m.message}>{m.message}</li>
                                                        ))}
                                                </ul>
                                            </span>
                                        ) : (
                                            <span>
                                                {
                                                    messages.filter(
                                                        (a: any) =>
                                                            a.elementPart == ElementPart.Name,
                                                    )[0].message
                                                }
                                            </span>
                                        )
                                    ) : (
                                        <span></span>
                                    )
                                }
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Sort",
                accessor: "sort",
                width: 20,
            },
            {
                Header: "Type",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.type
                },
                disableSortBy: true,
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <ElementTypeSelect
                            parentType={getElemByIndex(instance.row.original.index)?.parent?.type}
                            type={getElemByIndex(instance.row.original.index)?.type?.toString()}
                            treeItemsShown={treeItemsRef.current}
                            changeItem={true}
                            messages={messages.filter(
                                (m) =>
                                    m.elementId ==
                                    getElemByIndex(instance.row.original.index)?.name,
                            )}
                            className={classes.largeInput}
                            isScenario={props.scenarioMixinName == "Scenario" ? true : false}
                            onChange={(e) => {
                                var v = getElemByIndex(instance.row.original.index)
                                if (v != undefined) {
                                    updateEl(
                                        changeElAtTypeChangeTable(
                                            v,
                                            e.detail.selectedOption.textContent!.toString(),
                                        ),
                                    )
                                }
                            }}
                        />
                    )
                },
            },
            {
                Header: "Data Type",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.dataType
                },
                disableSortBy: true,
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {["input", "edit", "autocomplete"].includes(
                                getElemByIndex(instance.row.original.index)?.type!,
                            ) && (
                                    <Select
                                        valueState={
                                            messages.filter(
                                                (a: any) =>
                                                    a.elementId ==
                                                    getElemByIndex(instance.row.original.index)
                                                        ?.name &&
                                                    a.elementPart == ElementPart.DataType,
                                            ).length > 0
                                                ? elementInfo2ValueState(
                                                    getHighestSeverity(
                                                        messages
                                                            .filter(
                                                                (a: any) =>
                                                                    a.elementPart ==
                                                                    ElementPart.DataType &&
                                                                    a.elementId ==
                                                                    getElemByIndex(
                                                                        instance.row.original
                                                                            .index,
                                                                    )?.name,
                                                            )
                                                            .map((e: Message) => e.severity),
                                                    ),
                                                )
                                                : "None"
                                        }
                                        valueStateMessage={
                                            messages.length > 0 &&
                                                messages.filter(
                                                    (a: any) =>
                                                        a.elementId ==
                                                        getElemByIndex(instance.row.original.index)
                                                            ?.name &&
                                                        a.elementPart == ElementPart.DataType,
                                                ).length > 0 ? (
                                                <span>
                                                    {messages
                                                        .filter(
                                                            (a: any) =>
                                                                a.elementId ==
                                                                getElemByIndex(
                                                                    instance.row.original.index,
                                                                )?.name &&
                                                                a.elementPart == ElementPart.DataType,
                                                        )
                                                        .map((e: Message) => e.message)
                                                        .join(", ")}
                                                </span>
                                            ) : (
                                                <span></span>
                                            )
                                        }
                                        onChange={(e) => {
                                            var v = getElemByIndex(instance.row.original.index)
                                            if (v != undefined) {
                                                updateEl({
                                                    ...v,
                                                    dataType:
                                                        DataTypeValue[
                                                        e.detail.selectedOption.innerText!.toString() as keyof typeof DataTypeValue
                                                        ],
                                                })
                                            }
                                        }}
                                    >
                                        {(Object.keys(DataTypeValue) as Array<string>).map((key) => {
                                            return (
                                                <Option
                                                    selected={
                                                        getElemByIndex(
                                                            instance.row.original.index,
                                                        )?.dataType?.toString() ==
                                                        DataTypeValue[key as keyof typeof DataTypeValue]
                                                    }
                                                    key={key}
                                                >
                                                    {key}
                                                </Option>
                                            )
                                        })}
                                    </Select>
                                )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Col sm",
                accessor: (originalRow: Record<string, any>) => {
                    return (
                        /sm:\d+/
                            .exec(getElemByIndex(originalRow.index)?.col!)
                            ?.toString()
                            .split(":")[1] || ""
                    )
                },
                width: 25,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <Input
                                value={
                                    /sm:\d+/
                                        .exec(getElemByIndex(instance.row.original.index)?.col!)
                                        ?.toString()
                                        .split(":")[1] || ""
                                }
                                className={classes.largeInput}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            col: calculateCol(
                                                v.col!,
                                                e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                                "sm",
                                            ),
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Col md",
                accessor: (originalRow: Record<string, any>) => {
                    return (
                        /md:\d+/
                            .exec(getElemByIndex(originalRow.index)?.col!)
                            ?.toString()
                            .split(":")[1] || ""
                    )
                },
                width: 25,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <Input
                                value={
                                    /md:\d+/
                                        .exec(getElemByIndex(instance.row.original.index)?.col!)
                                        ?.toString()
                                        .split(":")[1] || ""
                                }
                                className={classes.largeInput}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            col: calculateCol(
                                                v.col!,
                                                e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                                "md",
                                            ),
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Col lg",
                accessor: (originalRow: Record<string, any>) => {
                    return (
                        /lg:\d+/
                            .exec(getElemByIndex(originalRow.index)?.col!)
                            ?.toString()
                            .split(":")[1] || ""
                    )
                },
                width: 20,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <Input
                                value={
                                    /lg:\d+/
                                        .exec(getElemByIndex(instance.row.original.index)?.col!)
                                        ?.toString()
                                        .split(":")[1] || ""
                                }
                                className={classes.largeInput}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            col: calculateCol(
                                                v.col!,
                                                e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                                "lg",
                                            ),
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Col xl",
                accessor: (originalRow: Record<string, any>) => {
                    return (
                        /xl:\d+/
                            .exec(getElemByIndex(originalRow.index)?.col!)
                            ?.toString()
                            .split(":")[1] || ""
                    )
                },
                width: 20,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <Input
                                value={
                                    /xl:\d+/
                                        .exec(getElemByIndex(instance.row.original.index)?.col!)
                                        ?.toString()
                                        .split(":")[1] || ""
                                }
                                className={classes.largeInput}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            col: calculateCol(
                                                v.col!,
                                                e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                                "xl",
                                            ),
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Visible",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.visible
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <Input
                                value={getElemByIndex(instance.row.original.index)?.visible}
                                className={classes.largeInput}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            visible:
                                                e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Editable",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.editable
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <Input
                                value={getElemByIndex(instance.row.original.index)?.editable}
                                className={classes.largeInput}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            editable:
                                                e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Required",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.required
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <Input
                                value={getElemByIndex(instance.row.original.index)?.required}
                                className={classes.largeInput}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            required:
                                                e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "CSS",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.css
                },
                disableFilters: true,
                disableSortBy: true,
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <Input
                                value={getElemByIndex(instance.row.original.index)?.css}
                                className={classes.largeInput}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            css: e.target.attributes.getNamedItem("value")!
                                                .nodeValue!,
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Show label",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.showLabel
                },
                disableFilters: true,
                disableSortBy: true,
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox
                            alignItems="Center"
                            justifyContent="Center"
                            direction="Row"
                            className={classes.largeInput}
                        >
                            <CheckBox
                                checked={getElemByIndex(instance.row.original.index)?.showLabel}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            showLabel: e.target.checked,
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Show help",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.showHelp
                },
                disableFilters: true,
                disableSortBy: true,
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox
                            alignItems="Center"
                            justifyContent="Center"
                            direction="Row"
                            className={classes.largeInput}
                        >
                            <CheckBox
                                checked={getElemByIndex(instance.row.original.index)?.showHelp}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            showHelp: e.target.checked,
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Show as Column",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.showAsColumn
                },
                disableFilters: true,
                disableSortBy: true,
                width: 130,
                Cell: (instance: any) => {
                    return (
                        <FlexBox
                            alignItems="Center"
                            justifyContent="Center"
                            direction="Row"
                            className={classes.largeInput}
                        >
                            <CheckBox
                                checked={getElemByIndex(instance.row.original.index)?.showAsColumn}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            showAsColumn: e.target.checked,
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Texts short",
                accessor: (originalRow: Record<string, any>) => {
                    return (
                        treeItemsRef.current?.texts![
                        props.defaultLanguage
                            ? props.defaultLanguage!
                            : (Object.keys(treeItemsRef.current?.texts!).sort()[0] as any)
                        ]?.[
                        `${getElemByIndex(originalRow.index)?.name}${TextPostfix.short}` as any
                        ] ?? ""
                    )
                },
                width: 180,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <StructureTabTextsInput
                                postfix={TextPostfix.short}
                                texts={treeItemsRef.current?.texts}
                                defaultLanguage={props.defaultLanguage}
                                currentName={getElemByIndex(instance.row.original.index)?.name}
                                scenarioMixinName={props.scenarioMixinName}
                                version={props.version}
                                setUpdate={props.setUpdate}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Texts long",
                accessor: (originalRow: Record<string, any>) => {
                    return (
                        treeItemsRef.current?.texts![
                        props.defaultLanguage
                            ? props.defaultLanguage!
                            : (Object.keys(treeItemsRef.current?.texts!).sort()[0] as any)
                        ]?.[
                        `${getElemByIndex(originalRow.index)?.name}${TextPostfix.long}` as any
                        ] ?? ""
                    )
                },
                width: 180,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <StructureTabTextsInput
                                postfix={TextPostfix.long}
                                texts={treeItemsRef.current?.texts}
                                defaultLanguage={props.defaultLanguage}
                                currentName={getElemByIndex(instance.row.original.index)?.name}
                                scenarioMixinName={props.scenarioMixinName}
                                version={props.version}
                                setUpdate={props.setUpdate}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Texts title",
                accessor: (originalRow: Record<string, any>) => {
                    return (
                        treeItemsRef.current?.texts![
                        props.defaultLanguage
                            ? props.defaultLanguage!
                            : (Object.keys(treeItemsRef.current?.texts!).sort()[0] as any)
                        ]?.[
                        `${getElemByIndex(originalRow.index)?.name}${TextPostfix.title}` as any
                        ] ?? ""
                    )
                },
                width: 180,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <StructureTabTextsInput
                                postfix={TextPostfix.title}
                                texts={treeItemsRef.current?.texts}
                                defaultLanguage={props.defaultLanguage}
                                currentName={getElemByIndex(instance.row.original.index)?.name}
                                scenarioMixinName={props.scenarioMixinName}
                                version={props.version}
                                setUpdate={props.setUpdate}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Texts doc",
                accessor: (originalRow: Record<string, any>) => {
                    return (
                        treeItemsRef.current?.texts![
                        props.defaultLanguage
                            ? props.defaultLanguage!
                            : (Object.keys(treeItemsRef.current?.texts!).sort()[0] as any)
                        ]?.[
                        `${getElemByIndex(originalRow.index)?.name}${TextPostfix.doc}` as any
                        ] ?? ""
                    )
                },
                width: 180,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <StructureTabTextsInput
                                postfix={TextPostfix.doc}
                                texts={treeItemsRef.current?.texts}
                                defaultLanguage={props.defaultLanguage}
                                currentName={getElemByIndex(instance.row.original.index)?.name}
                                scenarioMixinName={props.scenarioMixinName}
                                version={props.version}
                                setUpdate={props.setUpdate}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Default Value",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.defaultValue
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {[
                                "input",
                                "image",
                                "alert",
                                "icon",
                                "text",
                                "select",
                                "checkbox",
                                "radio",
                            ].includes(getElemByIndex(instance.row.original.index)?.type!) && (
                                    <Input
                                        value={
                                            getElemByIndex(instance.row.original.index)?.defaultValue
                                        }
                                        placeholder={instance.row.original.defaultValue}
                                        className={classes.largeInput}
                                        onChange={(e) => {
                                            var v = getElemByIndex(instance.row.original.index)
                                            if (v != undefined) {
                                                updateEl({
                                                    ...v,
                                                    defaultValue:
                                                        e.target.attributes.getNamedItem("value")!
                                                            .nodeValue!,
                                                })
                                            }
                                        }}
                                    />
                                )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Skip in summary",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.wizardFormatOptions?.skipInSummary
                },
                disableFilters: true,
                disableSortBy: true,
                width: 100,
                Cell: (instance: any) => {
                    if (props.parents.find((e) => e.elem.type == "wizard")) {
                        return (
                            <FlexBox
                                alignItems="Center"
                                justifyContent="Center"
                                direction="Row"
                                className={classes.largeInput}
                            >
                                <CheckBox
                                    checked={
                                        getElemByIndex(instance.row.original.index)
                                            ?.wizardFormatOptions?.skipInSummary
                                    }
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                wizardFormatOptions: {
                                                    skipInForm:
                                                        v.wizardFormatOptions?.skipInForm || false,
                                                    skipInSummary: e.target.checked,
                                                },
                                            })
                                        }
                                    }}
                                />
                            </FlexBox>
                        )
                    }
                },
            },
            {
                Header: "Skip in form",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.wizardFormatOptions?.skipInForm
                },
                disableFilters: true,
                disableSortBy: true,
                width: 100,
                Cell: (instance: any) => {
                    if (props.parents.find((e) => e.elem.type == "wizard")) {
                        return (
                            <FlexBox
                                alignItems="Center"
                                justifyContent="Center"
                                direction="Row"
                                className={classes.largeInput}
                            >
                                <CheckBox
                                    checked={
                                        getElemByIndex(instance.row.original.index)
                                            ?.wizardFormatOptions?.skipInForm
                                    }
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                wizardFormatOptions: {
                                                    skipInForm: e.target.checked,
                                                    skipInSummary:
                                                        v.wizardFormatOptions?.skipInSummary ||
                                                        false,
                                                },
                                            })
                                        }
                                    }}
                                />
                            </FlexBox>
                        )
                    }
                },
            },
            {
                Header: "Min column width",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.columnOptions?.minColumnWidth
                },
                disableFilters: true,
                disableSortBy: true,
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <Input
                                value={
                                    getElemByIndex(instance.row.original.index)?.columnOptions
                                        ?.minColumnWidth
                                }
                                className={classes.largeInput}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            columnOptions: {
                                                maxColumnWidth:
                                                    v.columnOptions?.maxColumnWidth || "",
                                                minColumnWidth:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            },
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Max column width",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.columnOptions?.maxColumnWidth
                },
                disableFilters: true,
                disableSortBy: true,
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            <Input
                                value={
                                    getElemByIndex(instance.row.original.index)?.columnOptions
                                        ?.maxColumnWidth
                                }
                                className={classes.largeInput}
                                onChange={(e) => {
                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            columnOptions: {
                                                minColumnWidth:
                                                    v.columnOptions?.minColumnWidth || "",
                                                maxColumnWidth:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            },
                                        })
                                    }
                                }}
                            />
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Name Value Help",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.valueHelp?.name
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {["currency", "multiselect", "radio", "select"].includes(
                                getElemByIndex(instance.row.original.index)?.type!,
                            ) && (
                                    <Input
                                        value={
                                            getElemByIndex(instance.row.original.index)?.valueHelp?.name
                                        }
                                        placeholder={instance.row.original.valueHelp?.name}
                                        className={classes.largeInput}
                                        onChange={(e) => {
                                            var v = getElemByIndex(instance.row.original.index)
                                            if (v != undefined) {
                                                updateEl({
                                                    ...v,
                                                    valueHelp: {
                                                        ...v.valueHelp,
                                                        name: e.target.attributes.getNamedItem("value")!
                                                            .nodeValue!,
                                                        validate: v.valueHelp?.validate || false,
                                                        emptySelection:
                                                            v.valueHelp?.emptySelection || false,
                                                        displayFormat: v.valueHelp?.displayFormat || "",
                                                    },
                                                })
                                            }
                                        }}
                                    />
                                )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Validate Value Help",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.valueHelp?.validate
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {["currency", "multiselect", "radio", "select"].includes(
                                getElemByIndex(instance.row.original.index)?.type!,
                            ) && (
                                    <CheckBox
                                        checked={
                                            getElemByIndex(instance.row.original.index)?.valueHelp
                                                ?.validate
                                        }
                                        onChange={(e) => {
                                            var v = getElemByIndex(instance.row.original.index)
                                            if (v != undefined) {
                                                updateEl({
                                                    ...v,
                                                    valueHelp: {
                                                        ...v.valueHelp,
                                                        name: v.valueHelp?.name || "",
                                                        validate: e.target.checked!,
                                                        emptySelection:
                                                            v.valueHelp?.emptySelection || false,
                                                        displayFormat: v.valueHelp?.displayFormat || "",
                                                    },
                                                })
                                            }
                                        }}
                                    />
                                )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "EmptySelection Value Help",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.valueHelp?.emptySelection
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {["currency", "multiselect", "radio", "select"].includes(
                                getElemByIndex(instance.row.original.index)?.type!,
                            ) && (
                                    <CheckBox
                                        checked={
                                            getElemByIndex(instance.row.original.index)?.valueHelp
                                                ?.emptySelection
                                        }
                                        onChange={(e) => {
                                            var v = getElemByIndex(instance.row.original.index)
                                            if (v != undefined) {
                                                updateEl({
                                                    ...v,
                                                    valueHelp: {
                                                        ...v.valueHelp,
                                                        name: v.valueHelp?.name || "",
                                                        validate: v.valueHelp?.validate || false,
                                                        emptySelection: e.target.checked!,
                                                        displayFormat: v.valueHelp?.displayFormat || "",
                                                    },
                                                })
                                            }
                                        }}
                                    />
                                )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "DisplayFormat Value Help",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.valueHelp?.displayFormat
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {["currency", "multiselect", "radio", "select"].includes(
                                getElemByIndex(instance.row.original.index)?.type!,
                            ) && (
                                    <Input
                                        value={
                                            getElemByIndex(instance.row.original.index)?.valueHelp
                                                ?.displayFormat
                                        }
                                        placeholder={instance.row.original.valueHelp?.displayFormat}
                                        className={classes.largeInput}
                                        onChange={(e) => {
                                            var v = getElemByIndex(instance.row.original.index)
                                            if (v != undefined) {
                                                updateEl({
                                                    ...v,
                                                    valueHelp: {
                                                        ...v.valueHelp,
                                                        name: v.valueHelp?.name || "",
                                                        validate: v.valueHelp?.validate || false,
                                                        emptySelection:
                                                            v.valueHelp?.emptySelection || false,
                                                        displayFormat:
                                                            e.target.attributes.getNamedItem("value")!
                                                                .nodeValue!,
                                                    },
                                                })
                                            }
                                        }}
                                    />
                                )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Input type",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.inputType
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "input" && (
                                <Select
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                inputType:
                                                    InputValue[
                                                    e.detail.selectedOption.innerText!.toString() as keyof typeof InputValue
                                                    ],
                                            })
                                        }
                                    }}
                                >
                                    {(Object.keys(InputValue) as Array<string>).map((key) => {
                                        return (
                                            <Option
                                                selected={
                                                    getElemByIndex(instance.row.original.index)
                                                        ?.inputType ==
                                                    InputValue[key as keyof typeof InputValue]
                                                }
                                                key={key}
                                            >
                                                {key}
                                            </Option>
                                        )
                                    })}
                                </Select>
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Design",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.design
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "button" && (
                                <Select
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                design: DesignValue[
                                                    e.detail.selectedOption.innerText!.toString() as keyof typeof DesignValue
                                                ],
                                            })
                                        }
                                    }}
                                >
                                    <Option
                                        selected={
                                            getElemByIndex(instance.row.original.index)?.design ==
                                            DesignValue[
                                            "Default" as keyof typeof DesignValue
                                            ] ||
                                            getElemByIndex(
                                                instance.row.original.index,
                                            )?.design?.toString() == ""
                                        }
                                        key={"Default"}
                                    >
                                        {"Default"}
                                    </Option>
                                    <Option
                                        selected={
                                            getElemByIndex(instance.row.original.index)?.design ==
                                            DesignValue["Emphasized" as keyof typeof DesignValue]
                                        }
                                        key={"Emphasized"}
                                    >
                                        {"Emphasized"}
                                    </Option>
                                    <Option
                                        selected={
                                            getElemByIndex(instance.row.original.index)?.design ==
                                            DesignValue["Positive" as keyof typeof DesignValue]
                                        }
                                        key={"Positive"}
                                    >
                                        {"Positive"}
                                    </Option>
                                    <Option
                                        selected={
                                            getElemByIndex(instance.row.original.index)?.design ==
                                            DesignValue["Negative" as keyof typeof DesignValue]
                                        }
                                        key={"Negative"}
                                    >
                                        {"Negative"}
                                    </Option>
                                    <Option
                                        selected={
                                            getElemByIndex(instance.row.original.index)?.design ==
                                            DesignValue["Transparent" as keyof typeof DesignValue]
                                        }
                                        key={"Transparent"}
                                    >
                                        {"Transparent"}
                                    </Option>
                                    <Option
                                        selected={
                                            getElemByIndex(instance.row.original.index)?.design ==
                                            DesignValue["Attention" as keyof typeof DesignValue]
                                        }
                                        key={"Attention"}
                                    >
                                        {"Attention"}
                                    </Option>
                                </Select>
                            )}
                            {getElemByIndex(instance.row.original.index)?.type! == "alert" && (
                                <Select
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                design: DesignValue[
                                                    e.detail.selectedOption.innerText!.toString() as keyof typeof DesignValue
                                                ],
                                            })
                                        }
                                    }}
                                >
                                    <Option
                                        selected={
                                            getElemByIndex(instance.row.original.index)?.design ==
                                            DesignValue["Positive" as keyof typeof DesignValue]
                                        }
                                        key={"Positive"}
                                    >
                                        {"Positive"}
                                    </Option>
                                    <Option
                                        selected={
                                            getElemByIndex(instance.row.original.index)?.design ==
                                            DesignValue["Negative" as keyof typeof DesignValue]
                                        }
                                        key={"Negative"}
                                    >
                                        {"Negative"}
                                    </Option>
                                    <Option
                                        selected={
                                            getElemByIndex(instance.row.original.index)?.design ==
                                            DesignValue["Warn" as keyof typeof DesignValue]
                                        }
                                        key={"Warn"}
                                    >
                                        {"Warn"}
                                    </Option>
                                    <Option
                                        selected={
                                            getElemByIndex(instance.row.original.index)?.design ==
                                            DesignValue["Info" as keyof typeof DesignValue]
                                        }
                                        key={"Info"}
                                    >
                                        {"Info"}
                                    </Option>
                                </Select>
                            )}
                            {getElemByIndex(instance.row.original.index)?.type! == "attachment" && (
                                <Select
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                design: AttachmentDesignType[
                                                    e.detail.selectedOption.innerText!.toString() as keyof typeof AttachmentDesignType
                                                ],
                                            })
                                        }
                                    }}
                                >
                                    {(Object.keys(AttachmentDesignType) as Array<string>).map(
                                        (key) => {
                                            return (
                                                <Option
                                                    selected={
                                                        getElemByIndex(instance.row.original.index)
                                                            ?.design ==
                                                        AttachmentDesignType[
                                                        key as keyof typeof AttachmentDesignType
                                                        ]
                                                    }
                                                    key={key}
                                                >
                                                    {key}
                                                </Option>
                                            )
                                        },
                                    )}
                                </Select>
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Icon",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.icon
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {["button", "alert"].includes(
                                getElemByIndex(instance.row.original.index)?.type!,
                            ) && (
                                    <Input
                                        value={getElemByIndex(instance.row.original.index)?.icon}
                                        placeholder={instance.row.original.icon}
                                        className={classes.largeInput}
                                        onChange={(e) => {
                                            var v = getElemByIndex(instance.row.original.index)
                                            if (v != undefined) {
                                                updateEl({
                                                    ...v,
                                                    icon: e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                                })
                                            }
                                        }}
                                    />
                                )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Tooltip",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.tooltip
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "button" && (
                                <Input
                                    value={getElemByIndex(instance.row.original.index)?.tooltip}
                                    placeholder={instance.row.original.tooltip}
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                tooltip:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                        }
                                    }}
                                />
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Path",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.path
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "mixin" && (
                                <Input
                                    value={getElemByIndex(instance.row.original.index)?.path}
                                    placeholder={instance.row.original.path}
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                path: e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                            })
                                        }
                                    }}
                                />
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "MixinName",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.mixinName
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "mixin" && (
                                <Input
                                    value={getElemByIndex(instance.row.original.index)?.mixinName}
                                    placeholder={instance.row.original.mixinName}
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                mixinName:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                        }
                                    }}
                                />
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "MixinVersion",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.version
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "mixin" && (
                                <Input
                                    value={getElemByIndex(
                                        instance.row.original.index,
                                    )?.version?.toString()}
                                    placeholder={instance.row.original.version}
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                version: Number(
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                                ).valueOf(),
                                            })
                                        }
                                    }}
                                />
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Upload Type",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.cardinality
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "attachment" && (
                                <Select
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                cardinality:
                                                    UploadType[
                                                    e.detail.selectedOption.innerText!.toString() as keyof typeof UploadType
                                                    ],
                                            })
                                        }
                                    }}
                                >
                                    {(Object.keys(UploadType) as Array<string>).map((key) => {
                                        return (
                                            <Option
                                                selected={
                                                    getElemByIndex(instance.row.original.index)
                                                        ?.cardinality ==
                                                    UploadType[key as keyof typeof UploadType]
                                                }
                                                key={key}
                                            >
                                                {key}
                                            </Option>
                                        )
                                    })}
                                </Select>
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Adapter",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.adapter
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "attachment" && (
                                <Input
                                    value={getElemByIndex(instance.row.original.index)?.adapter}
                                    placeholder={instance.row.original.adapter}
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                adapter:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                        }
                                    }}
                                />
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "File types",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.fileTypes
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "attachment" && (
                                <Input
                                    value={getElemByIndex(instance.row.original.index)?.fileTypes}
                                    placeholder={instance.row.original.fileTypes}
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                fileTypes:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                        }
                                    }}
                                />
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "HasDescription",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.hasDescription
                },
                disableFilters: true,
                disableSortBy: true,
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "attachment" && (
                                <CheckBox
                                    checked={
                                        getElemByIndex(instance.row.original.index)?.hasDescription
                                    }
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                hasDescription: e.target.checked!,
                                            })
                                        }
                                    }}
                                />
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Categories",
                accessor: "categories",
                disableFilters: true,
                disableSortBy: true,
                width: 70,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "attachment" && (
                                <Button
                                    icon="inspect"
                                    onClick={() => {
                                        var index = instance.row.original.index
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v) {
                                            const newEl2: ElemForTable = { ...v }
                                            delete newEl2.parent
                                            delete newEl2.index
                                            props.setEl(newEl2)
                                            props.setElement(index)
                                        }

                                        setCategoriesDialogOpen(true)
                                    }}
                                />
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Select",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.select
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "table" && (
                                <Select
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                select: SelectValue[
                                                    e.detail.selectedOption.innerText!.toString() as keyof typeof SelectValue
                                                ],
                                            })
                                        }
                                    }}
                                >
                                    {(Object.keys(SelectValue) as Array<string>).map((key) => {
                                        return (
                                            <Option
                                                selected={
                                                    getElemByIndex(instance.row.original.index)
                                                        ?.select ==
                                                    SelectValue[key as keyof typeof SelectValue]
                                                }
                                                key={key}
                                            >
                                                {key}
                                            </Option>
                                        )
                                    })}
                                </Select>
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Style",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.style
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "table" && (
                                <Select
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                style: StyleValue[
                                                    e.detail.selectedOption.innerText!.toString() as keyof typeof StyleValue
                                                ],
                                            })
                                        }
                                    }}
                                >
                                    {(Object.keys(StyleValue) as Array<string>).map((key) => {
                                        return (
                                            <Option
                                                selected={
                                                    getElemByIndex(instance.row.original.index)
                                                        ?.style ==
                                                    StyleValue[key as keyof typeof StyleValue]
                                                }
                                                key={key}
                                            >
                                                {key}
                                            </Option>
                                        )
                                    })}
                                </Select>
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Dialog Key",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.dialogKey
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "searchhelp" && (
                                <Input
                                    value={getElemByIndex(instance.row.original.index)?.dialogKey}
                                    placeholder={instance.row.original.dialogKey}
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                dialogKey:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                        }
                                    }}
                                />
                            )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Height",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.size?.height
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {["searchhelp", "dialog", "image"].includes(
                                getElemByIndex(instance.row.original.index)?.type!,
                            ) && (
                                    <Input
                                        value={
                                            getElemByIndex(instance.row.original.index)?.size?.height
                                        }
                                        placeholder={instance.row.original.size?.height}
                                        className={classes.largeInput}
                                        onChange={(e) => {
                                            var v = getElemByIndex(instance.row.original.index)
                                            if (v != undefined) {
                                                updateEl({
                                                    ...v,
                                                    size: {
                                                        width: v.size?.width || "",
                                                        height: e.target.attributes.getNamedItem(
                                                            "value",
                                                        )!.nodeValue!,
                                                    },
                                                })
                                            }
                                        }}
                                    />
                                )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Width",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.size?.width
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {["searchhelp", "dialog", "image"].includes(
                                getElemByIndex(instance.row.original.index)?.type!,
                            ) && (
                                    <Input
                                        value={getElemByIndex(instance.row.original.index)?.size?.width}
                                        placeholder={instance.row.original.size?.width}
                                        className={classes.largeInput}
                                        onChange={(e) => {
                                            var v = getElemByIndex(instance.row.original.index)
                                            if (v != undefined) {
                                                updateEl({
                                                    ...v,
                                                    size: {
                                                        height: v.size?.width || "",
                                                        width: e.target.attributes.getNamedItem(
                                                            "value",
                                                        )!.nodeValue!,
                                                    },
                                                })
                                            }
                                        }}
                                    />
                                )}
                        </FlexBox>
                    )
                },
            },
            {
                Header: "Validation",
                accessor: "validationRules",
                disableFilters: true,
                disableSortBy: true,
                width: 70,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {["input", "edit", "attachment", "table"].includes(
                                getElemByIndex(instance.row.original.index)?.type!,
                            ) && (
                                    <Button
                                        icon="inspect"
                                        onClick={() => {
                                            var index = instance.row.original.index
                                            var v = getElemByIndex(instance.row.original.index)
                                            if (v) {
                                                const newEl2: ElemForTable = { ...v }
                                                delete newEl2.parent
                                                delete newEl2.index
                                                props.setEl(newEl2)
                                                props.setElement(index)
                                            }

                                            setValidationDialogOpen(true)
                                        }}
                                    />
                                )}
                        </FlexBox>
                    )
                },
            },
        ],
        [props.renderTable],
    )

    return (
        <FlexBox style={{ paddingBottom: 15 }} >
            <div className={classes.buttonsTree}>
                <Button
                    icon="add"
                    onClick={function Ta() {
                        props.setAddDialogOpen(true)
                    }}
                    disabled={
                        props.el != undefined &&
                        props.el.type != undefined &&
                        leafNodes.includes(props.el?.type)
                    }
                    className={classes.buttonTree}
                />
                <Button
                    icon="delete"
                    disabled={!props.element}
                    onClick={function Ta() {
                        var i = props.element
                        if (i![i!.length - 2] == "l" || i![i!.length - 2] == "r") {
                            props.openMessageBox(
                                MessageBoxType.Error,
                                undefined,
                                <>This node cannot be deleted.</>,
                            )
                        } else {
                            props.openMessageBox(
                                MessageBoxType.Confirm,
                                "Delete",
                                <>
                                    Are you sure you want to delete the node{" "}
                                    <b>
                                        <i>{props.el?.name}</i>{" "}
                                    </b>
                                    and all its child nodes?
                                    <br />
                                    This operation cannot be undone.
                                </>,
                            )
                            props.setIndexesDelete({
                                indexes: i,
                                name: props.el?.name,
                            })
                            props.setEl(undefined)
                        }
                    }}
                    className={classes.buttonTree}
                />
            </div>
            <FlexBox direction="Column" style={{ width: "100%" }}
            >
                <Breadcrumbs
                    design="Standard"
                    onItemClick={function Ki(e) {
                        if (rowsById) {
                            const row = Object.values(rowsById).find(
                                (item) => item.original.index === e.detail.item.id,
                            )
                            if (row) {
                                setSelectedRowId({ [row.id]: true })
                                setSelectedElemForTable(row.original as ElemForTable)
                                props.setElement(row.original.index)
                                props.setEl(row.original)
                            }
                        }
                    }}
                    separators="Slash"
                >
                    {selectedElemForTable && <BreadcrumbItemParents item={selectedElemForTable} />}
                </Breadcrumbs>
                <AnalyticalTable
                    isTreeTable
                    style={{ maxWidth: `calc(100% - 60px)`, overflow: "auto" }}
                    data={memorizedData}
                    retainColumnWidth={true}
                    filterable={true}
                    sortable={true}
                    loading={elementsTable.length > 0 ? false : true}
                    loadingDelay={0}
                    minRows={5}
                    visibleRowCountMode={AnalyticalTableVisibleRowCountMode.Fixed}
                    visibleRows={10}
                    infiniteScroll={true}
                    infiniteScrollThreshold={10}
                    columns={columns}
                    selectionMode="Single"
                    selectionBehavior="RowOnly"
                    selectedRowIds={memorizedSelectedRow}
                    onLoadMore={() => { }}
                    onRowSelect={(e) => {
                        props.setElement(e?.detail.row?.original.index)
                        props.setEl(e?.detail.row?.original)
                        setSelectedElemForTable(e?.detail.row?.original as ElemForTable)
                        setSelectedRowId(e?.detail.selectedRowIds)
                        setRowsById(e?.detail.rowsById)
                    }}
                    reactTableOptions={{
                        initialState: {
                            expanded: expandedState
                        }
                    }}
                />
            </FlexBox>
            <Dialog
                className={classes.dialog}
                footer={
                    <Bar
                        design="Footer"
                        className={classes.bar}
                        endContent={
                            <Button
                                design="Emphasized"
                                onClick={function _a() {
                                    setCategoriesDialogOpen(false)
                                }}
                            >
                                Close
                            </Button>
                        }
                    ></Bar>
                }
                headerText="Categories"
                open={categoriesDialogOpen}
            >
                {props.el && (
                    <CategoriesTable
                        el={props.el}
                        setNewEl={(e: Elem) => {
                            var index = props.element
                            var v = elementsTableRef.current.find((item) => item.index == index)
                            if (v != undefined) {
                                const newV: ElemForTable = {
                                    ...v,
                                    categories: e.categories,
                                }
                                elementsTableRef.current = elementsTableRef.current.map((item) =>
                                    item.index == newV.index ? newV : item,
                                )
                                const newEl2: ElemForTable = { ...newV }
                                delete newEl2.parent
                                delete newEl2.index
                                editDetailData({
                                    version: props.version,
                                    scenarioMixinName: props.scenarioMixinName,
                                    indexes: index,
                                    newEl: newEl2,
                                })
                                props.setElement(index)
                                props.setEl(newEl2)
                                props.setUpdate((prev: number) => prev + 1)
                            }
                        }}
                    />
                )}
            </Dialog>
            <Dialog
                className={classes.dialog}
                footer={
                    <Bar
                        design="Footer"
                        className={classes.bar}
                        endContent={
                            <Button
                                design="Emphasized"
                                onClick={function _a() {
                                    setValidationDialogOpen(false)
                                }}
                            >
                                Close
                            </Button>
                        }
                    ></Bar>
                }
                headerText="Validation"
                open={validationDialogOpen}
            >
                {props.el && (
                    <ValidationTable
                        el={props.el}
                        setNewEl={(e: Elem) => {
                            var index = props.element
                            var v = elementsTableRef.current.find((item) => item.index == index)
                            if (v != undefined) {
                                const newV: ElemForTable = {
                                    ...v,
                                    validationRules: e.validationRules,
                                }
                                elementsTableRef.current = elementsTableRef.current.map((item) =>
                                    item.index == newV.index ? newV : item,
                                )
                                const newEl2: ElemForTable = { ...newV }
                                delete newEl2.parent
                                delete newEl2.index
                                editDetailData({
                                    version: props.version,
                                    scenarioMixinName: props.scenarioMixinName,
                                    indexes: index,
                                    newEl: newEl2,
                                })
                                props.setElement(index)
                                props.setEl(newEl2)
                                props.setUpdate((prev: number) => prev + 1)
                            }
                        }}
                    />
                )}
            </Dialog>
        </FlexBox>
    )
}
