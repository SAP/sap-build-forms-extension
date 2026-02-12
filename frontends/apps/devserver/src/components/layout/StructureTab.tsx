import { FlexBox, Label, Switch } from "@ui5/webcomponents-react"
import { useState } from "react"
import { Elem, Parent, Scenario } from "../../utils/scenarioDefinitions"
import StructureTabTree from "./StructureTabTree"
import StructureTabTable from "./StructureTabTable"

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
}

export default function StructureTab(props: Props) {
    const [tableView, setTableView] = useState(false)

    return (
        <FlexBox direction="Column">
            <FlexBox justifyContent="End" alignItems="End">
                <FlexBox justifyContent="Center" alignItems="Center" direction="Row">
                    <Label style={{ marginRight: "1rem" }}>Table view</Label>
                    <Switch
                        onChange={(e) => {
                            setTableView(e.target.checked!)
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
                />
            )}
        </FlexBox>
    )
}
