import { FlexBox, Input, Label, Switch, Icon } from "@ui5/webcomponents-react"
import { useState } from "react"
import { useIntl } from "react-intl"
import { Elem, Parent, Scenario } from "../../utils/scenarioDefinitions"
import StructureTabTree from "./StructureTabTree"
import StructureTabTable from "./StructureTabTable"
import { VariantFilterProvider } from "./VariantFilterContext"

interface Props {
    version: number
    defaultLanguage: string | undefined
    treeItemsShown: Scenario | null | undefined
    update: number
    el: Elem | undefined
    element: string
    parents: Parent[]
    copiedEl: Elem | undefined
    scenarioMixinName: string
    renderTable: number
    setEl: (e: any) => void
    setElement: (e: any) => void
    setParents: (e: any) => void
    setNewEl: (e: any) => void
    setIndexesDelete: (e: any) => void
    setAddDialogOpen: (e: any) => void
    setCopyDialogOpen: (e: any) => void
    openMessageBox: (e1: any, e2: any, e3: any) => void
    setUpdate: (e: any) => void
    setSelectedTreeItem: (e: any) => void
    setRenderTable: (e: any) => void
    setCopiedEl: (e: any) => void
    selectedVariants: string[]
    registerFlushPendingNameCommit?: (fn: (() => void) | undefined) => void
}

export default function StructureTab(props: Props) {
    const [tableView, setTableView] = useState(false)
    const [search, setSearch] = useState<string>("")
    const intl = useIntl()

    return (
        <VariantFilterProvider selectedVariants={props.selectedVariants}>
            <FlexBox direction="Column">
            <FlexBox
                justifyContent="SpaceBetween"
                alignItems="Center"
                style={{ marginBottom: "1rem", gap: "1rem" }}
            >
                <FlexBox style={{ flex: 1 }} />

                <FlexBox justifyContent="Center" alignItems="Center" style={{ flex: 1 }}>
                    <Input
                        icon={<Icon name="search" />}
                        style={{ width: "100%", minWidth: "300px" }}
                        onInput={(e) => {
                            const searchValue = e.target.value ? e.target.value.trim() : "";
                            setSearch(searchValue);
                        }}
                        type="Text"
                        valueState="None"
                        value={search}
                        showClearIcon={true}
                        placeholder={intl.formatMessage({ id: "structure_search_placeholder" })}
                    />
                </FlexBox>

                <FlexBox
                    justifyContent="End"
                    alignItems="Center"
                    direction="Row"
                    style={{ flex: 1, gap: "0.5rem" }}
                >
                    <Label>{intl.formatMessage({ id: "structure_table_view_label" })}</Label>
                    <Switch
                        onChange={(e) => {
                            const nextTableView = e.target.checked!
                            setTableView(nextTableView)

                            // When switching from table to tree, refresh data from store first.
                            if (!nextTableView) {
                                props.setUpdate((prev: number) => prev + 1)
                            }
                        }}
                    />
                </FlexBox>
            </FlexBox>

            {!tableView && (
                <StructureTabTree
                    version={props.version}
                    defaultLanguage={props.defaultLanguage}
                    treeItemsShown={props.treeItemsShown}
                    scenarioMixinName={props.scenarioMixinName}
                    update={props.update}
                    setNewEl={props.setNewEl}
                    setIndexesDelete={props.setIndexesDelete}
                    setAddDialogOpen={props.setAddDialogOpen}
                    setCopyDialogOpen={props.setCopyDialogOpen}
                    setUpdate={props.setUpdate}
                    openMessageBox={props.openMessageBox}
                    setSelectedTreeItem={props.setSelectedTreeItem}
                    el={props.el}
                    setEl={props.setEl}
                    element={props.element}
                    setElement={props.setElement}
                    parents={props.parents}
                    setParents={props.setParents}
                    copiedEl={props.copiedEl}
                    setCopiedEl={props.setCopiedEl}
                    search={search} 
                    registerFlushPendingNameCommit={props.registerFlushPendingNameCommit}
                />
            )}
            {tableView && (
                <StructureTabTable
                    version={props.version}
                    defaultLanguage={props.defaultLanguage}
                    treeItemsShown={props.treeItemsShown}
                    scenarioMixinName={props.scenarioMixinName}
                    setIndexesDelete={props.setIndexesDelete}
                    setAddDialogOpen={props.setAddDialogOpen}
                    openMessageBox={props.openMessageBox}
                    el={props.el}
                    setEl={props.setEl}
                    element={props.element}
                    setElement={props.setElement}
                    parents={props.parents}
                    setParents={props.setParents}
                    update={props.update}
                    setUpdate={props.setUpdate}
                    renderTable={props.renderTable}
                    setRenderTable={props.setRenderTable}
                    search={search} 
                />
            )}
            </FlexBox>
        </VariantFilterProvider>
    )
}