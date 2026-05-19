import { ReactNode, RefObject, useEffect, useRef, useState } from "react"

import ReactDOM from "react-dom"
import { IntlShape, useIntl } from "react-intl"

import { useMessages } from "commons"

import {
    Bar,
    Button,
    Dialog,
    Icon,
    Input,
    Label,
    SegmentedButton,
    SegmentedButtonDomRef,
    SegmentedButtonItem,
    Table,
    TableCell,
    TableHeaderCell,
    TableHeaderRow,
    TableRow,
    TableRowAction,
    TableSelectionMulti,
    TableSelectionMultiDomRef,
    TableSelectionSingle,
    TableSelectionSingleDomRef,
    Text,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import "@ui5/webcomponents-icons/dist/edit"
import "@ui5/webcomponents-icons/dist/delete"
import "@ui5/webcomponents-icons/dist/show"
import { SegmentedButtonSelectionChangeEventDetail } from "@ui5/webcomponents/dist/SegmentedButton"

import { DataType, Definition, UIElement, UserEventType } from "../../features/sessions/definitions"
import { ElementProp } from "../../features/sessions/journal"
import { Element, ElementMapRow, FormService, TableInfo } from "../../features/sessions/forms"
import { useAppDispatch, useAppSelector } from "../../features/store"
import { update } from "../../features/sessions/sessionSlice"
import { loadIntoCache } from "../../features/valuehelps/valuehelpsSlice"
import { deleteRow, triggerEvent } from "../../features/sessions/sessionActions"

import { ControlProps, getLabel, handleBrowseTable, handleChangeTablePageSize } from "./Control"
import Control from "./Control"
import ControlGridContainer from "./ControlGridContainer"
import { elementInfo2ValueState } from "./utils"

// Constants for different page sizes
const PAGE_SIZES = [5, 10, 15, 20, 25]

/**
 *
 */
interface DetailDialogProps extends ControlProps {
    rowIdRef: RefObject<string | undefined>
    edRef: RefObject<boolean>
    setVisible: (value: boolean) => void
}

/**
 *
 * @param param0
 * @returns
 */
function DetailDialog(props: DetailDialogProps) {
    const { rowIdRef, setVisible, edRef } = props
    const intl = useIntl()
    // const form = useAppSelector((state) => state.session.form)
    // const row = FormService.findRowById(rowIdRef.current!, form)

    // let va: ElementMap | undefined = undefined

    // if (rowIdRef.current && row) {
    //     va = (element.va as ElementMapRow[]).find((row) => row.id === rowIdRef.current)?.values
    // }

    return (
        <>
            {ReactDOM.createPortal(
                <Dialog
                    open={true}
                    headerText={intl.formatMessage({ id: "detail_dialog_title" })}
                    footer={
                        <Bar
                            endContent={
                                <Button onClick={() => setVisible(false)}>
                                    {intl.formatMessage({ id: "common_close" })}
                                </Button>
                            }
                        />
                    }
                    onClose={() => setVisible(false)}
                    style={{ width: "80vw", height: "80vh" }}
                >
                    <>
                        <ControlGridContainer
                            {...props}
                            globalEd={edRef.current ? props.globalEd : false}
                            rowId={rowIdRef.current!}
                        />
                    </>
                </Dialog>,
                document.body,
            )}
        </>
    )
}

/**
 * Renders a single cell inside the table
 *
 * @param props
 * @param row
 * @param def
 * @param inline
 * @param intl
 * @param vhsLoaded
 * @param cache
 * @returns
 */
function renderCell(
    props: ControlProps,
    row: ElementMapRow,
    def: Definition,
    inline: boolean,
    intl: IntlShape,
    vhsLoaded: boolean,
    cache: Record<string, Record<string, string>>,
): ReactNode {
    // first, let's check if the element is contained in values. If it's not visible then it's not
    // sent to the frontend and therefor not available. In this case we render an empty paragraph.
    if (!row.values[def.key]) {
        // console.log(`Error: Cannot find property '${def.id}' with key  '${def.key}'`)
        return <div></div>
    }

    // for inline we render each cell with it's according control
    if (inline) {
        return <Control {...props} rowId={row.id} def={def} asTableCell={true} />
    }

    // for non-inline tables we render text-only rows, at least for default
    const v = row.values[def.key].va

    switch (def.uiElement) {
        case UIElement.Button:
            return <Control {...props} rowId={row.id} def={def} asTableCell={true} />
        case UIElement.Checkbox:
            return (
                <Control {...props} rowId={row.id} def={def} asTableCell={true} globalEd={false} />
            )
        case UIElement.Icon:
            return <Control {...props} rowId={row.id} def={def} asTableCell={true} />
        case UIElement.Image:
            return <Control {...props} rowId={row.id} def={def} asTableCell={true} />
        case UIElement.Link:
            return <Control {...props} rowId={row.id} def={def} asTableCell={true} />
        case UIElement.DateRange:
            return <Text>{intl.formatDate(v as string)}</Text>
        case UIElement.MultiSelect:
        case UIElement.Select:
        case UIElement.RadioButtons:
            if (vhsLoaded && def.vh && def.vh.name.length > 0) {
                const vh = cache[def.vh.name]
                if (!vh) {
                    console.warn(`Cannot find value-help '${def.vh}'`)
                    return <Text>...</Text>
                }
                // console.log(`value-helps are loaded for '${def.id}' and value '${v}'`)

                let result = ""
                for (const part of (v as string).split(";")) {
                    const h = vh[part]
                    if (h && h.length > 0) {
                        if (result.length > 0) {
                            result += ", "
                        }
                        result += h
                    }
                }
                return <Text>{result}</Text>
            }
            return <Text>...</Text>
        default:
            // console.log(`Data for ${def.id} is ${v}`)
            switch (def.dataType) {
                case DataType.Date:
                case DataType.DateTime:
                    return <Text>{intl.formatDate(v as string)}</Text>
                case DataType.Time:
                    return <Text>{intl.formatTime(v as string)}</Text>
                case DataType.Int:
                case DataType.Decimal:
                    return <Text>{intl.formatNumber(v as number)}</Text>
                default:
                    return <Text>{v as string}</Text>
            }
    }
}

/**
 *
 * @param element
 * @returns
 */
function formatSelected(element?: Element) {
    let ids = ""
    const table = element?.va as TableInfo | undefined
    if (table?.d) {
        Object.entries(table.d).forEach(([key, row]) => {
            if (row.sel) {
                if (ids.length > 0) {
                    ids += " "
                }
                ids += key
            }
        })
        // console.log(`formatSelected for ${element?.key} => ${ids}`)
    }
    return ids
}

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { def, texts, rowId, asTableCell } = props
    const dispatch = useAppDispatch()
    const intl = useIntl()
    const messages = useMessages()
    const locale = useAppSelector((state) => state.session.locale)
    const vhsCache = useAppSelector((state) => state.valuehelps.cache)
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)
    const detailsRowIdRef = useRef<string>(undefined)
    const detailsEdRef = useRef<boolean>(false)
    const [showDialog, setShowDialog] = useState<boolean>(false)
    const [vhsLoaded, setVhsLoaded] = useState<boolean>(false)
    const [selectedRowIds, setSelectedRowIds] = useState<string>("")
    const [loading, setLoading] = useState<boolean>(false)

    const mode = def.select == "single" ? "Single" : def.select == "multiple" ? "Multiple" : "None"
    const inline = def.type === "inline"
    const table =
        (element?.va as TableInfo | undefined) ??
        ({
            p: 0,
            ps: PAGE_SIZES[0],
            s: 0,
            r: [],
            d: {},
            sd: "a",
        } as TableInfo)

    // console.log(`Table ${def.id}, number of rows: ${(value?.va as ElementMapRow[]).length}`)

    useEffect(() => {
        // create the string with all ids that are selected
        setSelectedRowIds(formatSelected(element))

        // load all value-helps
        let vhs: string[] = []
        for (const d of def.elements!) {
            if (d.vh && d.vh.name.length > 0) {
                vhs.push(d.vh.name)
            }
        }
        dispatch(loadIntoCache({ names: vhs, locale })).then(() => {
            setVhsLoaded(true)
        })
    }, [element])

    /**
     * Adjust page when total rows change or table size changes
     */
    useEffect(() => {
        const currentPage = Math.floor(table.p / table.ps) + 1
        const lastPage = Math.max(Math.ceil(table.s / table.ps), 1)

        if (currentPage > lastPage) {
            const newPosition = (lastPage - 1) * table.ps
            ;(async () => {
                setLoading(true)
                await handleBrowseTable(dispatch, def, rowId, messages, newPosition)
                setLoading(false)
            })()
        }
    }, [table.s, table.ps, table.p, dispatch, def, rowId, messages])

    /**
     *
     * @param rowId
     */
    const handleShowDetail = (rowId: string, ed: boolean) => {
        detailsRowIdRef.current = rowId
        detailsEdRef.current = ed
        setShowDialog(true)
    }

    /**
     *
     * @param e
     */
    const handleRowSelect = (
        e:
            | Ui5CustomEvent<TableSelectionSingleDomRef, never>
            | Ui5CustomEvent<TableSelectionMultiDomRef, never>,
    ) => {
        dispatch(
            update({
                def,
                rowId: rowId,
                selectedRowIds: e.target.selected.split(" "),
                prop: ElementProp.Selected,
                value: "",
            }),
        )

        setSelectedRowIds(formatSelected(element))
    }

    /**
     *
     * @param key
     */
    const handleSortClick = async (key: string) => {
        setLoading(true)
        const table = { ...(element?.va as TableInfo) }
        if (table.sf === key) {
            table.sd = table.sd === "a" ? "d" : "a"
        } else {
            table.sf = key
            table.sd = "a"
        }
        dispatch(
            update({
                def,
                rowId: rowId,
                prop: ElementProp.SortField,
                value: table.sf,
            }),
        )
        dispatch(
            update({
                def,
                rowId: rowId,
                prop: ElementProp.SortOrder,
                value: table.sd,
            }),
        )
        await dispatch(triggerEvent({ type: UserEventType.Sort, def, rowId, messages }))
        setLoading(false)
    }

    /**
     *
     */
    const handleNextPageButtonClick = async () => {
        setLoading(true)
        // Calculate next first pos but keep on eye on last page
        let p = table.p + table.ps
        if (p + table.ps > table.s) {
            p = Math.floor(table.s / table.ps) * table.ps
        }
        await handleBrowseTable(dispatch, def, rowId, messages, p)
        setLoading(false)
    }

    /**
     *
     */
    const handlePrevPageButtonClick = async () => {
        // Calculate next first pos but keep on eye on last page
        setLoading(true)
        await handleBrowseTable(dispatch, def, rowId, messages, Math.max(0, table.p - table.ps))
        setLoading(false)
    }

    /**
     *
     */
    const handleFirstPageButtonClick = async () => {
        setLoading(true)
        await handleBrowseTable(dispatch, def, rowId, messages, 0)
        setLoading(false)
    }

    /**
     *
     */
    const handleLastPageButtonClick = async () => {
        setLoading(true)

        // Calculate last page, but keep on eye on last page
        let page = Math.floor(table.s / table.ps)
        if (table.s % table.ps === 0) {
            page -= 1 // last page is not full
        }

        await handleBrowseTable(dispatch, def, rowId, messages, page * table.ps)
        setLoading(false)
    }

    /**
     *
     * @param evt
     */
    const handleVisibleRowsChange = async (
        evt: Ui5CustomEvent<SegmentedButtonDomRef, SegmentedButtonSelectionChangeEventDetail>,
    ) => {
        for (let i = 0; i < evt.detail.selectedItems.length; i++) {
            if (evt.detail.selectedItems[i].dataset["key"]) {
                const ps = parseInt(evt.detail.selectedItems[i].dataset.key!)
                setLoading(true)
                // Reset to first page when changing page size
                await handleBrowseTable(dispatch, def, rowId, messages, 0)
                await handleChangeTablePageSize(dispatch, def, rowId, messages, ps)
                setLoading(false)
                break
            }
        }
    }

    /**
     * Delete the given row
     *
     * @param row - the row to delete
     */
    const handleDeleteRow = (row: ElementMapRow) => {
        dispatch(deleteRow({ rowId, def, deleteRowId: row.id, messages }))
    }

    /**
     *
     */
    const calculateActionCount = (): number => {
        if (inline) {
            return element?.ed ? 1 : 0
        }
        return element?.ed ? 2 : 1
    }

    // create columns, taken from definition
    let columns: ReactNode[] = []
    if (Array.isArray(def.elements)) {
        for (let d of def.elements!) {
            if (!d.showAsColumn) {
                continue
            }
            columns.push(
                <TableHeaderCell
                    key={d.key}
                    minWidth="10rem"
                    style={{ display: "flex", alignContent: "center", justifyContent: "center" }}
                >
                    <Text style={{ marginRight: ".5rem" }}>{getLabel(texts, d)}</Text>
                    {d.key === table.sf && (
                        <Icon
                            name={table.sd === "d" ? "sort-descending" : "sort-ascending"}
                            style={{ color: "var(--sapButton_TextColor)" }}
                            mode="Interactive"
                            onClick={() => handleSortClick(d.key)}
                        />
                    )}
                    {d.key !== table.sf && (
                        <Icon
                            name="sort"
                            mode="Interactive"
                            onClick={() => handleSortClick(d.key)}
                        />
                    )}
                </TableHeaderCell>,
            )
        }
    }

    // create rows
    let rows: ReactNode[] = []
    if (table.r && table.r.length > 0) {
        for (let i = 0; i < table!.ps; i++) {
            const row = table.d![table.r[i]]
            let cells: ReactNode[] = []
            let actions: ReactNode[] = []

            if (row) {
                // render the data cells itself
                if (Array.isArray(def.elements)) {
                    for (let col of def.elements!) {
                        if (!col.showAsColumn) {
                            continue
                        }
                        cells.push(
                            <TableCell key={col.key}>
                                {renderCell(props, row, col, inline, intl, vhsLoaded, vhsCache)}
                            </TableCell>,
                        )
                    }
                }

                // add an additional empty cell for inline tables, or buttons for edit/show/delete for non-inline tables
                if (inline && element?.ed) {
                    actions.push(
                        <TableRowAction icon="delete" onClick={() => handleDeleteRow(row)} />,
                    )
                } else if (!inline) {
                    if (element?.ed) {
                        actions.push(
                            <TableRowAction
                                icon="edit"
                                onClick={() => handleShowDetail(row.id, true)}
                            />,
                        )
                        actions.push(
                            <TableRowAction icon="delete" onClick={() => handleDeleteRow(row)} />,
                        )
                    } else {
                        actions.push(
                            <TableRowAction
                                icon="show"
                                onClick={() => handleShowDetail(row.id, false)}
                            />,
                        )
                    }
                }

                rows.push(
                    <TableRow
                        key={row.id}
                        row-key={row.id}
                        interactive={false}
                        actions={<>{actions}</>}
                    >
                        {cells}
                    </TableRow>,
                )
            }
        }
    }
    // console.log(`TableControl: marked rows '${selectedRowIds}'`)

    const lastPage = Math.max(Math.ceil(table.s / table.ps), 1)
    const currPage = Math.floor(table.p / table.ps) + 1
    const tableRenderKey = `${rowId ?? "_"}-${def.key}-${table.p}-${table.ps}-${table.s}-${(table.r ?? []).join("|")}`
    const hasTableError = elementInfo2ValueState(element?.msg) === "Negative"

    const handleToolbarActionCompleted = async () => {
        setLoading(true)
        await handleBrowseTable(dispatch, def, rowId, messages, table.p)
        setLoading(false)
    }

    /**
     * Handle page input change, constraining to valid range
     */
    const handlePageInputChange = async (evt: any) => {
        const inputValue = evt.target.value.trim()
        const pageNum = inputValue === "" ? 1 : parseInt(inputValue)

        if (isNaN(pageNum)) return

        // Constrain to valid range [1, lastPage]
        const constrainedPage = Math.max(1, Math.min(pageNum, lastPage))

        // If the entered page is outside valid range, update input to show constrained value
        if (pageNum !== constrainedPage) {
            evt.target.value = "" + constrainedPage
        }

        setLoading(true)
        await handleBrowseTable(dispatch, def, rowId, messages, (constrainedPage - 1) * table.ps)
        setLoading(false)
    }
    // console.log(
    //     `table.p=${table.p}, table.ps=${table.ps}, table.s=${table.s}, lastPage=${lastPage}, currPage=${currPage}`,
    // )
    // console.log(`PageSize: ${table.ps}, Number of rows: ${table.s}`)

    return (
        <div>
            {!asTableCell && (
                <Label
                    id={"l" + def.key}
                    for={def.key}
                    required={element?.rq}
                    style={def.showLabel === false ? { visibility: "hidden" } : undefined}
                >
                    {def.showLabel !== false ? getLabel(texts, def) : ""}
                </Label>
            )}
            {def.toolbar && (
                <Control
                    {...props}
                    key={def.toolbar.key}
                    def={def.toolbar}
                    onAfterAction={handleToolbarActionCompleted}
                />
            )}
            <Table
                key={tableRenderKey}
                headerRow={<TableHeaderRow sticky>{columns}</TableHeaderRow>}
                style={{
                    width: "100%",
                    border: hasTableError ? "0.125rem solid var(--sapField_InvalidColor)" : undefined,
                    backgroundColor: hasTableError ? "var(--sapField_InvalidBackground)" : undefined,
                    borderRadius: hasTableError ? "0.25rem" : undefined,
                }}
                noDataText={intl.formatMessage({ id: "common_no_data" })}
                overflowMode="Scroll"
                rowActionCount={calculateActionCount()}
                loading={loading}
                loadingDelay={1}
            >
                {mode == "Single" && (
                    <TableSelectionSingle
                        slot="features"
                        selected={selectedRowIds}
                        onChange={handleRowSelect}
                    />
                )}
                {mode == "Multiple" && (
                    <TableSelectionMulti
                        slot="features"
                        selected={selectedRowIds}
                        onChange={handleRowSelect}
                    />
                )}
                {rows}
            </Table>
            <Bar
                startContent={
                    <SegmentedButton
                        selectionMode="Single"
                        onSelectionChange={handleVisibleRowsChange}
                    >
                        {PAGE_SIZES.map((size) => (
                            <SegmentedButtonItem
                                key={size}
                                data-key={size}
                                selected={size === table.ps}
                            >
                                {"" + size}
                            </SegmentedButtonItem>
                        ))}
                    </SegmentedButton>
                }
                endContent={
                    <>
                        <Button
                            id={def.key + "_firstPage"}
                            icon="media-rewind"
                            design="Transparent"
                            disabled={lastPage === 1}
                            onClick={handleFirstPageButtonClick}
                        />
                        <Button
                            id={def.key + "_prevPage"}
                            icon="media-reverse"
                            design="Transparent"
                            disabled={currPage === 1}
                            onClick={handlePrevPageButtonClick}
                        />
                        <Input 
                            value={"" + currPage} 
                            style={{ width: "3em" }}
                            onChange={handlePageInputChange}
                        ></Input>
                        <Text>&nbsp;/&nbsp;{lastPage}</Text>
                        <Button
                            id={def.key + "_nextPage"}
                            icon="media-play"
                            design="Transparent"
                            disabled={currPage === lastPage}
                            onClick={handleNextPageButtonClick}
                        />
                        <Button
                            id={def.key + "_lastPage"}
                            icon="media-forward"
                            design="Transparent"
                            disabled={lastPage === 1}
                            onClick={handleLastPageButtonClick}
                        />
                    </>
                }
            />
            {showDialog && (
                <DetailDialog
                    {...props}
                    rowIdRef={detailsRowIdRef}
                    edRef={detailsEdRef}
                    setVisible={setShowDialog}
                />
            )}
        </div>
    )
}
