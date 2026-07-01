import React, { useState, useEffect, useMemo, useRef } from "react"
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
import { useVariantFilter } from "./VariantFilterContext"
import { elementMatchesSelectedVariants } from "../../utils/variantUtils"

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
    search: string
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
    const { selectedVariants } = useVariantFilter()
    const messages = React.useMemo(
        () => allMessages.filter((m: Message) => m.defVersion == props.version),
        [allMessages, props.version]
    )

    const tableHooks = useMemo(
        () => [
            (hooks: any) => {
                hooks.getRowProps.push((rowProps: any, { row }: any) => {
                    const matches = elementMatchesSelectedVariants(
                        row?.original?.visible,
                        selectedVariants,
                    )
                    const shouldDim = selectedVariants.length > 0 && !matches
                    return [
                        rowProps,
                        {
                            style: {
                                ...(rowProps?.style || {}),
                                opacity: shouldDim ? 0.45 : 1,
                            },
                        },
                    ]
                })
            },
        ],
        [selectedVariants],
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
    const refreshTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null)

    const memorizedSelectedRow = useMemo(() => selectedRowId, [selectedRowId])

    const scheduleRefresh = () => {
        if (refreshTimeoutRef.current) {
            clearTimeout(refreshTimeoutRef.current)
        }

        // Coalesce frequent keystroke updates into one table refresh to avoid flicker.
        refreshTimeoutRef.current = setTimeout(() => {
            props.setUpdate((prev: number) => prev + 1)
            refreshTimeoutRef.current = null
        }, 120)
    }

    const memorizedData = useMemo(() => {
        if (!props.search) return elementsTableShown;

        const searchLower = props.search.toLowerCase();

        const itemOrChildrenMatch = (item: ElemForTable): boolean => {
            if (!item) return false;

            if (item.name?.toLowerCase().includes(searchLower)) {
                return true;
            }

            if (item.subRows && item.subRows.length > 0) {
                return item.subRows.some(subItem => itemOrChildrenMatch(subItem));
            }

            return false;
        };

        const filterItems = (items: ElemForTable[]): ElemForTable[] => {
            if (!items) return [];

            return items
                .filter(item => itemOrChildrenMatch(item))
                .map(item => ({
                    ...item,
                    subRows: item.subRows ? filterItems(item.subRows) : []
                }));
        };

        return filterItems(elementsTableShown);
    }, [elementsTableShown, props.search]);

    useEffect(() => {
        if (props.search && elementsTable.length > 0) {
            const timeoutId = setTimeout(() => {
                const searchLower = props.search.toLowerCase();

                const matchedItem = elementsTable.find(item =>
                    item.name?.toLowerCase().includes(searchLower)
                );

                if (matchedItem) {
                    setTimeout(() => {
                        const allRows = document.querySelectorAll('[role="row"]');
                        let targetRow: Element | null = null;

                        allRows.forEach(row => {
                            const cells = row.querySelectorAll('[role="gridcell"]');
                            const firstCell = cells[0];

                            if (firstCell && firstCell.textContent?.includes(matchedItem.name)) {
                                targetRow = row;
                            }
                        });

                        if (targetRow) {
                            const rowElement = targetRow as HTMLElement;
                            rowElement.style.transition = 'background-color 0.3s ease';
                            rowElement.style.backgroundColor = '#e3f2fd';

                            setTimeout(() => {
                                rowElement.style.backgroundColor = '';
                            }, 2000);
                        }
                    }, 800);
                }
            }, 500);

            return () => clearTimeout(timeoutId);
        }
    }, [props.search, elementsTable]);

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

    useEffect(() => {
        return () => {
            if (refreshTimeoutRef.current) {
                clearTimeout(refreshTimeoutRef.current)
                props.setUpdate((prev: number) => prev + 1)
                refreshTimeoutRef.current = null
            }
        }
    }, [])

    const updateEl = (newEl: ElemForTable) => {
        elementsTableRef.current = elementsTableRef.current.map((item) =>
            item.index == newEl.index ? newEl : item,
        )
        const { parent, index, ...newEl2 } = newEl
        editDetailData({
            version: props.version,
            scenarioMixinName: props.scenarioMixinName,
            indexes: newEl.index,
            newEl: newEl2,
        })
        props.setElement(newEl.index)
        props.setEl(newEl2 as Elem)
        scheduleRefresh()
    }

    const getElemByIndex = (index: string): ElemForTable | undefined => {
        return elementsTableRef.current.find((item) => item.index === index)
    }

    const getInputValue = (e: any): string => {
        const target = e?.target as { value?: string; attributes?: NamedNodeMap } | undefined
        const valueFromTarget = target?.value
        if (typeof valueFromTarget === "string") {
            return valueFromTarget
        }

        const valueFromAttribute = target?.attributes
            ?.getNamedItem("value")
            ?.nodeValue
        return valueFromAttribute ?? ""
    }

    const lineBreakAllowedTypes = ["alert", "attachment", "button", "checkbox", "currency", "daterange", "edit", "icon", "image", "input", "link", "multiselect", "radio", "select", "table", "text"]

    const hasLineBreakSupport = (elem: ElemForTable | undefined) =>
        elem?.type != null && lineBreakAllowedTypes.includes(elem.type)

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

    const expandedState = useMemo(() => {
        const expanded: Record<string, boolean> = {};

        const expandAll = (rows: any[], parentId = '', level = 0) => {
            rows.forEach((row, idx) => {
                const id = parentId ? `${parentId}.${idx}` : `${idx}`;

                if (props.search && row?.original) {
                    const item = row.original as ElemForTable;
                    const searchLower = props.search.toLowerCase();

                    const hasMatchInTree = (item: ElemForTable | undefined): boolean => {
                        if (!item) return false;
                        if (item.name?.toLowerCase().includes(searchLower)) return true;
                        if (item.subRows && item.subRows.length > 0) {
                            return item.subRows.some(sub => hasMatchInTree(sub));
                        }
                        return false;
                    };

                    if (hasMatchInTree(item)) {
                        expanded[id] = true;
                    }
                } else {
                    if (level < 2) {
                        expanded[id] = true;
                    }
                }

                if (row.subRows && row.subRows.length > 0) {
                    expandAll(row.subRows, id, level + 1);
                }
            });
        };

        if (memorizedData.length > 0) {
            expandAll(memorizedData);
        }

        return expanded;
    }, [memorizedData, props.search]);

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
                                        getInputValue(e)

                                    if (oldName === newName) {
                                        return
                                    }

                                    if (
                                        treeItemsRef.current?.root != undefined &&
                                        treeItemsRef.current?.root === oldName
                                    ) {
                                        editBaseData({
                                            scenarioMixinName: props.scenarioMixinName,
                                            version: props.version,
                                            root: newName,
                                        })
                                    }

                                    if (oldName != null && treeItemsRef.current?.texts) {
                                        const texts = JSON.parse(
                                            JSON.stringify(treeItemsRef.current.texts),
                                        )
                                        const postfixes = [
                                            TextPostfix.short,
                                            TextPostfix.long,
                                            TextPostfix.title,
                                            TextPostfix.doc,
                                        ]

                                        Object.keys(texts).forEach((language) => {
                                            if (!texts[language]) {
                                                texts[language] = {}
                                            }
                                            postfixes.forEach((postfix) => {
                                                const oldKey = `${oldName}${postfix}`
                                                const newKey = `${newName}${postfix}`
                                                if (texts[language][newKey] === undefined) {
                                                    texts[language][newKey] =
                                                        texts[language][oldKey] ?? ""
                                                }
                                                delete texts[language][oldKey]
                                            })
                                        })

                                        treeItemsRef.current = {
                                            ...treeItemsRef.current,
                                            texts,
                                        }
                                        editTexts({
                                            version: props.version,
                                            scenarioMixinName: props.scenarioMixinName,
                                            texts,
                                        })
                                    }

                                    var v = getElemByIndex(instance.row.original.index)
                                    if (v != undefined) {
                                        updateEl({
                                            ...v,
                                            name: newName,
                                        })
                                    }
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
                                        {(Object.keys(DataTypeValue) as Array<string>).filter(
                                            (key) => !(getElemByIndex(instance.row.original.index)?.type === "input" && key === "Auto")
                                        ).map((key) => {
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
                                                getInputValue(e),
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
                                                getInputValue(e),
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
                                                getInputValue(e),
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
                                                getInputValue(e),
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
                                                getInputValue(e),
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
                                                getInputValue(e),
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
                                                getInputValue(e),
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
                                            css: getInputValue(e),
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
                Header: "Line break",
                accessor: (originalRow: Record<string, any>) => {
                    const elem = getElemByIndex(originalRow.index)
                    return hasLineBreakSupport(elem) ? elem?.lineBreak : undefined
                },
                disableFilters: true,
                disableSortBy: true,
                width: 100,
                Cell: (instance: any) => {
                    const elem = getElemByIndex(instance.row.original.index)

                    if (!hasLineBreakSupport(elem)) return null

                    return (
                        <FlexBox
                            alignItems="Center"
                            justifyContent="Center"
                            direction="Row"
                            className={classes.largeInput}
                        >
                            <CheckBox
                                checked={elem?.lineBreak}
                                onChange={(e) => {
                                    if (elem) {
                                        updateEl({
                                            ...elem,
                                            lineBreak: e.target.checked,
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
                                setUpdate={scheduleRefresh}
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
                                setUpdate={scheduleRefresh}
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
                                setUpdate={scheduleRefresh}
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
                                setUpdate={scheduleRefresh}
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
                    const elemType = getElemByIndex(instance.row.original.index)?.type
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {elemType === "image" ? (
                                <div
                                    style={{
                                        display: "flex",
                                        flexDirection: "row",
                                        gap: "5px",
                                        alignItems: "center",
                                        width: "100%",
                                    }}
                                >
                                    <Input
                                        value={
                                            getElemByIndex(instance.row.original.index)?.defaultValue
                                        }
                                        placeholder="URL or data URI"
                                        style={{ flex: 1 }}
                                        onChange={(e) => {
                                            var v = getElemByIndex(instance.row.original.index)
                                            if (v != undefined) {
                                                updateEl({
                                                    ...v,
                                                    defaultValue:
                                                        getInputValue(e),
                                                })
                                            }
                                        }}
                                    />
                                    <Button
                                        icon="upload"
                                        design="Default"
                                        onClick={() => {
                                            const input = document.createElement("input")
                                            input.type = "file"
                                            input.accept = "image/*"
                                            input.onchange = (e: Event) => {
                                                const target = e.target as HTMLInputElement
                                                const file = target.files?.[0]
                                                if (file) {
                                                    const reader = new FileReader()
                                                    reader.onload = (event) => {
                                                        const dataUri = event.target?.result as string
                                                        var v = getElemByIndex(instance.row.original.index)
                                                        if (v != undefined) {
                                                            updateEl({
                                                                ...v,
                                                                defaultValue: dataUri,
                                                            })
                                                        }
                                                    }
                                                    reader.readAsDataURL(file)
                                                }
                                            }
                                            input.click()
                                        }}
                                    />
                                </div>
                            ) : [
                                "input",
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
                                                    getInputValue(e),
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
                Header: "Link URL",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.linkHRef
                },
                width: 100,
                Cell: (instance: any) => {
                    const type = getElemByIndex(instance.row.original.index)?.type
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {(type === "link" || type === "button") && (
                                <Input
                                    value={getElemByIndex(instance.row.original.index)?.linkHRef}
                                    placeholder="https://example.com"
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                linkHRef: getInputValue(e),
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
                Header: "Link Text",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.linkText
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "link" && (
                                <Input
                                    value={getElemByIndex(instance.row.original.index)?.linkText}
                                    placeholder="Link display text"
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                linkText: getInputValue(e),
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
                                                    getInputValue(e),
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
                                                    getInputValue(e),
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
                                                        name: getInputValue(e),
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
                                                            getInputValue(e),
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
                            {["button", "alert", "icon"].includes(
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
                                                    icon: getInputValue(e),
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
                            {["button", "icon"].includes(getElemByIndex(instance.row.original.index)?.type!) && (
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
                                                    getInputValue(e),
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
                                                path: getInputValue(e),
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
                                                    getInputValue(e),
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
                                                    getInputValue(e),
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
                                    value={getElemByIndex(instance.row.original.index)?.adapter || "database"}
                                    placeholder={instance.row.original.adapter || "database"}
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                adapter:
                                                    getInputValue(e),
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
                                                    getInputValue(e),
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
                            {(getElemByIndex(instance.row.original.index)?.type === "table" || getElemByIndex(instance.row.original.index)?.type === "attachment") && (
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
                Header: "Pagesize",
                accessor: (originalRow: Record<string, any>) => {
                    return getElemByIndex(originalRow.index)?.pageSize
                },
                width: 100,
                Cell: (instance: any) => {
                    return (
                        <FlexBox style={{ width: "100%" }}>
                            {getElemByIndex(instance.row.original.index)?.type! == "table" && (
                                <Input
                                    value={(getElemByIndex(instance.row.original.index)?.pageSize ?? 10).toString()}
                                    className={classes.largeInput}
                                    onChange={(e) => {
                                        var v = getElemByIndex(instance.row.original.index)
                                        if (v != undefined) {
                                            updateEl({
                                                ...v,
                                                pageSize: parseInt(getInputValue(e)) || 10,
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
                                                    getInputValue(e),
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
                                                        height: getInputValue(e),
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
                                                        height: v.size?.height || "",
                                                        width: getInputValue(e),
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
                    tableHooks={tableHooks}
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
