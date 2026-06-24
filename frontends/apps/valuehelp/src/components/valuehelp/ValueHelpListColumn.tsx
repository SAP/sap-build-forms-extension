import {
    Button,
    CheckBox,
    FilterBar,
    FilterGroupItem,
    FlexBox,
    Input,
    InputDomRef,
    List,
    ListDomRef,
    ListItemStandard,
    MultiComboBox,
    MultiComboBoxItem,
    Switch,
    Text,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import { ListItemClickEventDetail } from "@ui5/webcomponents/dist/List"
import ListSelectionMode from "@ui5/webcomponents/dist/types/ListSelectionMode"
import { useIntl } from "react-intl"

import { Margin } from "commons"

import { ValueHelpDef } from "../../features/model"

interface ValueHelpListColumnProps {
    slot?: string
    defs: ValueHelpDef[]
    adapters: string[]
    searchId: string
    searchAdapter: string[]
    listMode: ListSelectionMode
    selectedDefs: string[]
    currentDefId: string | undefined
    onSearchIdChange(id: string): void
    onSearchAdapterChange(adapters: string[]): void
    onFilter(): void
    onClearFilter(): void
    onSelectItem(def: ValueHelpDef): void
    onToggleListMode(): void
    onToggleSelectedDef(id: string): void
    onSelectAll(): void
    onAdd(): void
    onDeleteSelected(): void
    onDownload(): void
    onUpload(): void
}

export default function ({
    slot,
    defs,
    adapters,
    searchId,
    searchAdapter,
    listMode,
    selectedDefs,
    currentDefId,
    onSearchIdChange,
    onSearchAdapterChange,
    onFilter,
    onClearFilter,
    onSelectItem,
    onToggleListMode,
    onToggleSelectedDef,
    onSelectAll,
    onAdd,
    onDeleteSelected,
    onDownload,
    onUpload,
}: ValueHelpListColumnProps) {
    const intl = useIntl()
    return (
        <div slot={slot} style={{ height: "100%", overflowY: "auto" }}>
            <FilterBar
                onClear={() => {
                    onSearchIdChange("")
                    onSearchAdapterChange([])
                    onClearFilter()
                }}
                onGo={onFilter}
                search={
                    <Input
                        value={searchId}
                        onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            onSearchIdChange(e.target.attributes.getNamedItem("value")!.nodeValue!)
                        }}
                        onKeyDown={(e) => {
                            if (e.key === "Enter") {
                                const val =
                                    // @ts-expect-error
                                    e.target.attributes.getNamedItem("value")!.nodeValue!
                                onSearchIdChange(val)
                                onFilter()
                            }
                        }}
                    />
                }
                showResetButton
                hideFilterConfiguration
                showClearOnFB
                showGoOnFB
            >
                <FilterGroupItem label={intl.formatMessage({ id: "lbl_adapter" })} filterKey="1">
                    <MultiComboBox
                        onSelectionChange={(e) => {
                            onSearchAdapterChange(e.detail.items.map((a) => a.id))
                        }}
                    >
                        {adapters.map((item: string) => (
                            <MultiComboBoxItem
                                text={item}
                                key={item}
                                id={item}
                                selected={searchAdapter.includes(item)}
                            />
                        ))}
                    </MultiComboBox>
                </FilterGroupItem>
            </FilterBar>

            <FlexBox direction="Row" justifyContent="SpaceBetween">
                <FlexBox alignItems="Center">
                    {listMode === ListSelectionMode.Multiple && (
                        <CheckBox
                            text={intl.formatMessage({ id: "lbl_select_all" })}
                            onChange={onSelectAll}
                            checked={defs.length > 0 && defs.every((d) => selectedDefs.includes(d.id))}
                        />
                    )}
                    <Switch
                        onChange={onToggleListMode}
                        checked={listMode === ListSelectionMode.Multiple}
                        style={{ marginLeft: Margin.SMALL }}
                    />
                    <Text style={{ marginLeft: Margin.SMALL }}>{intl.formatMessage({ id: "lbl_multiselect" })}</Text>
                </FlexBox>

                <FlexBox alignItems="Center" wrap="Wrap">
                    <Button design="Transparent" icon="upload" onClick={onUpload}>
                        {intl.formatMessage({ id: "lbl_upload_file" })}
                    </Button>
                    <Button
                        design="Transparent"
                        icon="download"
                        style={{ marginLeft: Margin.SMALL }}
                        disabled={listMode === ListSelectionMode.Multiple && selectedDefs.length < 1}
                        onClick={onDownload}
                    >
                        {listMode === ListSelectionMode.Single
                            ? intl.formatMessage({ id: "lbl_download" })
                            : intl.formatMessage({ id: "lbl_download_selected" })}
                    </Button>
                    <Button design="Transparent" icon="add" onClick={onAdd}>
                        {intl.formatMessage({ id: "lbl_new_value_help" })}
                    </Button>
                    {listMode === ListSelectionMode.Multiple && (
                        <Button
                            design="Transparent"
                            icon="delete"
                            style={{ marginLeft: Margin.SMALL }}
                            disabled={selectedDefs.length < 1}
                            onClick={onDeleteSelected}
                        >
                            {intl.formatMessage({ id: "lbl_delete_selected" })}
                        </Button>
                    )}
                </FlexBox>
            </FlexBox>

            <List
                headerText={intl.formatMessage({ id: "lbl_value_helps" })}
                selectionMode={listMode}
                id="valueHelpList"
                onSelectionChange={(e) => {
                    if (listMode === ListSelectionMode.Multiple) {
                        onToggleSelectedDef(e.detail.targetItem.id)
                    }
                }}
                onItemClick={(e: Ui5CustomEvent<ListDomRef, ListItemClickEventDetail>) => {
                    onSelectItem(defs.find((vh) => vh.id === e.detail.item.id)!)
                }}
            >
                {defs.map((item: ValueHelpDef) => (
                    <ListItemStandard
                        description={
                            item.description
                                ? item.adapter
                                    ? item.description + " | " + item.adapter
                                    : item.description
                                : item.adapter
                        }
                        key={item.id}
                        id={item.id}
                        icon="navigation-right-arrow"
                        iconEnd={true}
                        navigated={currentDefId === item.id}
                        selected={selectedDefs.includes(item.id)}
                    >
                        {item.id}
                    </ListItemStandard>
                ))}
            </List>
        </div>
    )
}
