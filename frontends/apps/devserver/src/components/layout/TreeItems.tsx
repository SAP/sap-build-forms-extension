import React from "react"
import { useIntl } from "react-intl"
import { FlexBox, Icon, Tag, TreeItem, TreeItemCustom } from "@ui5/webcomponents-react"
import { Elem, Message } from "../../utils/scenarioDefinitions"
import { compare, containsChildSearchString } from "../../utils/sortUtils"
import useMessagesStore from "../../state/messages"
import { getChildrenMessageSeverities, getHighestSeverity, toPascalCase } from "../../utils/formUtils"
import SeverityIcon from "./SeverityIcon"
import TagDesign from "@ui5/webcomponents/dist/types/TagDesign"
import DraggableTreeItem from "./DraggableTreeItem"
import { useVariantFilter } from "./VariantFilterContext"
import { elementMatchesSelectedVariants } from "../../utils/variantUtils"

interface Props {
    items: Elem[]
    id: string
    sortBefore: string
    scenarioVersion: number
    searchString: string
    element: string | undefined
    version: number
    scenarioMixinName: string
    setUpdate?: (value: any) => void
    level?: number
}

export default function TreeItems(props: Props) {
    var subkey = -1
    const map1 = new Map()
    const currentLevel = props.level || 1
    const intl = useIntl()
    
    const shouldExpand = React.useMemo(() => {
        if (props.searchString.length > 0) {
            return props.items.some(item => containsChildSearchString(item, props.searchString))
        }
        return currentLevel < 2
    }, [props.searchString, props.items, currentLevel])

    const allMessages = useMessagesStore((state) => state.messages)
    const { selectedVariants } = useVariantFilter()
    const messages: any = React.useMemo(
        () => allMessages.filter((m: Message) => m.defVersion == props.scenarioVersion),
        [allMessages, props.scenarioVersion]
    )

    const getDimmedStyle = (visible?: string) => {
        const matches = elementMatchesSelectedVariants(visible, selectedVariants)
        return selectedVariants.length > 0 && !matches ? { opacity: 0.45 } : undefined
    }

    const isElementEffectivelyDimmed = (item?: Elem): boolean => {
        if (!item) return false
        if (selectedVariants.length === 0) return false
        
        const matches = elementMatchesSelectedVariants(item.visible, selectedVariants)
        
        // If element doesn't match variant, dim
        if (!matches) return true
        
        // Element matches variant, check if all its children are dimmed
        return areAllChildrenDimmed(item.elements)
    }

    const areAllChildrenDimmed = (items?: Elem[]): boolean => {
        if (!items || items.length === 0) return false
        
        return items.every((item) => isElementEffectivelyDimmed(item))
    }

    const getDimmedStyleWithChildren = (visible?: string, children?: Elem[]) => {
        if (selectedVariants.length === 0) return undefined
        
        const matches = elementMatchesSelectedVariants(visible, selectedVariants)
        if (!matches) return { opacity: 0.45 }
        
        // Element matches, check if all children are dimmed
        if (areAllChildrenDimmed(children)) {
            return { opacity: 0.45 }
        }
        
        return undefined
    }

    while (subkey < props.items.length - 1) {
        subkey++
        map1.set(props.items[subkey].name + props.items[subkey].sort, subkey)
    }

    let itemsSorted
    if (props.searchString.length > 0) {
        itemsSorted = [...props.items]
            .filter((item) => {
                return containsChildSearchString(item, props.searchString)
            })
            .sort(compare)
    } else {
        itemsSorted = [...props.items].sort(compare)
    }

    return (
        <>
            {itemsSorted.map((item) => {
                var i = props.id + map1.get(item.name + item.sort).toString() + "x"
                var searchString =
                    props.searchString.length == 0
                        ? ""
                        : item.name.toLocaleLowerCase().includes(props.searchString.toLowerCase())
                        ? ""
                        : props.searchString

                const itemMatchesSearch = props.searchString.length > 0 && 
                    (item.name.toLocaleLowerCase().includes(props.searchString.toLowerCase()) ||
                     containsChildSearchString(item, props.searchString))

                return (
                    <DraggableTreeItem
                        key={i}
                        id={i}
                        title={item.name}
                        selected={i == props.element}
                        navigated={i == props.element}
                        version={props.version}
                        scenarioMixinName={props.scenarioMixinName}
                        setUpdate={props.setUpdate}
                        expanded={shouldExpand || itemMatchesSearch}
                        dimmed={selectedVariants.length > 0 && (!elementMatchesSelectedVariants(item.visible, selectedVariants) || areAllChildrenDimmed(item.elements))}
                        content={
                            <div
                                style={{
                                    display: "flex",
                                    flexDirection: "row",
                                    justifyContent: "flex-start",
                                }}
                            >
                                <SeverityIcon
                                    message="There are errors in children of this element"
                                    isMainElement={false}
                                    severity={getHighestSeverity(
                                        getChildrenMessageSeverities(messages, item, true),
                                    )}
                                />
                                <SeverityIcon
                                    message=""
                                    isMainElement={true}
                                    severity={getHighestSeverity(
                                        messages
                                            .filter(
                                                (a: any) =>
                                                    a.elementId ==
                                                    toPascalCase(item.name),
                                            )
                                            .map((e: Message) => e.severity),
                                    )}
                                />
                                <div>
                                    <FlexBox direction="Row">
                                        <Tag design={TagDesign.Set2} colorScheme="10">
                                            {props.sortBefore != ""
                                                ? `${props.sortBefore}.${item.sort}`
                                                : `${item.sort}`}
                                        </Tag>
                                        &nbsp; &nbsp;
                                        <FlexBox
                                            direction="Column"
                                            justifyContent="Center"
                                        >{`${item.name} (${item.type})`}</FlexBox>
                                    </FlexBox>
                                </div>
                            </div>
                        }
                    >
                        {item.type == "form" && (
                            <>
                                {item.headerSegment &&
                                    ((searchString.length > 0 &&
                                        containsChildSearchString(
                                            item.headerSegment,
                                            searchString,
                                        )) ||
                                        searchString.length == 0) && (
                                        <TreeItemCustom
                                            key={`${i}hx`}
                                            id={`${i}hx`}
                                            title={item.headerSegment.name}
                                            selected={`${i}hx` == props.element}
                                            navigated={`${i}hx` == props.element}
                                            style={getDimmedStyleWithChildren(item.headerSegment.visible, item.headerSegment.elements)}
                                            content={
                                                <div
                                                    style={{
                                                        display: "flex",
                                                        flexDirection: "row",
                                                        justifyContent: "space-between",
                                                    }}
                                                >
                                                    {messages.filter(
                                                        (a: any) =>
                                                            a.elementId ==
                                                            item
                                                                .headerSegment!.name.charAt(0)
                                                                .toUpperCase() +
                                                                item.headerSegment!.name.slice(1),
                                                    ).length > 0 && (
                                                        <Icon
                                                            name="warning"
                                                            showTooltip={true}
                                                            accessibleName={messages
                                                                .filter(
                                                                    (a: any) =>
                                                                        a.elementId ==
                                                                        item
                                                                            .headerSegment!.name.charAt(
                                                                                0,
                                                                            )
                                                                            .toUpperCase() +
                                                                            item.headerSegment!.name.slice(
                                                                                1,
                                                                            ),
                                                                )
                                                                .map((e: Message) => e.message)
                                                                .join(", ")}
                                                            style={{
                                                                marginInlineEnd: 10,
                                                                color: "grey",
                                                            }}
                                                        />
                                                    )}
                                                    <div>
                                                        <FlexBox direction="Row">
                                                            <Tag
                                                                design={TagDesign.Set2}
                                                                colorScheme="10"
                                                            >
                                                                {props.sortBefore != ""
                                                                    ? `${props.sortBefore}.${item.sort}.h`
                                                                    : `${item.sort}.h`}
                                                            </Tag>
                                                            &nbsp;&nbsp;
                                                            <FlexBox
                                                                direction="Column"
                                                                justifyContent="Center"
                                                            >{`${item.headerSegment?.name} (headerSegment)`}</FlexBox>
                                                        </FlexBox>
                                                    </div>
                                                </div>
                                            }
                                        >
                                            <TreeItems
                                                items={item.headerSegment.elements}
                                                id={`${i}hx`}
                                                scenarioVersion={props.scenarioVersion}
                                                element={props.element}
                                                sortBefore={
                                                    props.sortBefore == ""
                                                        ? `${item.sort}.h`
                                                        : `${props.sortBefore}.${item.sort}.h`
                                                }
                                                searchString={
                                                    searchString.length == 0
                                                        ? ""
                                                        : item.headerSegment.name
                                                              .toLocaleLowerCase()
                                                              .includes(searchString.toLowerCase())
                                                        ? ""
                                                        : searchString
                                                }
                                                version={props.version}
                                                scenarioMixinName={props.scenarioMixinName}
                                                setUpdate={props.setUpdate}
                                                level={currentLevel + 1}
                                            />
                                        </TreeItemCustom>
                                    )}
                            </>
                        )}

                        <TreeItems
                            items={item.elements}
                            id={i}
                            element={props.element}
                            sortBefore={
                                props.sortBefore == ""
                                    ? String(item.sort)
                                    : props.sortBefore + "." + item.sort
                            }
                            searchString={searchString}
                            scenarioVersion={props.scenarioVersion}
                            version={props.version}
                            scenarioMixinName={props.scenarioMixinName}
                            setUpdate={props.setUpdate}
                            level={currentLevel + 1}
                        />

                        {(item.type == "form" || item.type == "wizard" || item.type == "docform") && (
                            <>
                                {item.footer &&
                                    ((searchString.length > 0 &&
                                        containsChildSearchString(item.footer, searchString)) ||
                                        searchString.length == 0) && (
                                        <TreeItemCustom
                                            key={`${i}fx`}
                                            id={`${i}fx`}
                                            title={item.footer.name}
                                            selected={`${i}fx` == props.element}
                                            navigated={`${i}fx` == props.element}
                                            style={getDimmedStyleWithChildren(item.footer.visible, item.footer.elements)}
                                            content={
                                                <div
                                                    style={{
                                                        display: "flex",
                                                        flexDirection: "row",
                                                        justifyContent: "space-between",
                                                    }}
                                                >
                                                    {messages.filter(
                                                        (a: any) =>
                                                            a.elementId ==
                                                            item
                                                                .footer!.name.charAt(0)
                                                                .toUpperCase() +
                                                                item.footer!.name.slice(1),
                                                    ).length > 0 && (
                                                        <Icon
                                                            name="warning"
                                                            showTooltip={true}
                                                            accessibleName={messages
                                                                .filter(
                                                                    (a: any) =>
                                                                        a.elementId ==
                                                                        item
                                                                            .footer!.name.charAt(0)
                                                                            .toUpperCase() +
                                                                            item.footer!.name.slice(
                                                                                1,
                                                                            ),
                                                                )
                                                                .map((e: Message) => e.message)
                                                                .join(", ")}
                                                            style={{
                                                                marginInlineEnd: 10,
                                                                color: "grey",
                                                            }}
                                                        />
                                                    )}
                                                    <div>
                                                        <FlexBox direction="Row">
                                                            <Tag
                                                                design={TagDesign.Set2}
                                                                colorScheme="10"
                                                            >
                                                                {props.sortBefore != ""
                                                                    ? `${props.sortBefore}.${item.sort}.f`
                                                                    : `${item.sort}.f`}
                                                            </Tag>
                                                            &nbsp;&nbsp;
                                                            <FlexBox
                                                                direction="Column"
                                                                justifyContent="Center"
                                                            >{`${item.footer.name} (${item.footer.type})`}</FlexBox>
                                                        </FlexBox>
                                                    </div>
                                                </div>
                                            }
                                        >
                                            <TreeItems
                                                items={item.footer.elements}
                                                id={`${i}fx`}
                                                scenarioVersion={props.scenarioVersion}
                                                element={props.element}
                                                sortBefore={
                                                    props.sortBefore == ""
                                                        ? `${item.sort}.f`
                                                        : `${props.sortBefore}.${item.sort}.f`
                                                }
                                                searchString={
                                                    searchString.length == 0
                                                        ? ""
                                                        : item.footer.name
                                                              .toLocaleLowerCase()
                                                              .includes(searchString.toLowerCase())
                                                        ? ""
                                                        : searchString
                                                }
                                                version={props.version}
                                                scenarioMixinName={props.scenarioMixinName}
                                                setUpdate={props.setUpdate}
                                                level={currentLevel + 1} 
                                            />
                                            {item.footer.leftElements && (
                                                <TreeItemCustom
                                                    key={`${i}fxlx`}
                                                    id={`${i}fxlx`}
                                                    content={ <div> {intl.formatMessage({ id: "structure_left_elements" })} </div> }
                                                >
                                                    <TreeItems
                                                        items={item.footer.leftElements}
                                                        id={`${i}fxlx`}
                                                        scenarioVersion={props.scenarioVersion}
                                                        element={props.element}
                                                        sortBefore={
                                                            props.sortBefore == ""
                                                                ? `${item.sort}.f.l`
                                                                : `${props.sortBefore}.${item.sort}.f.l`
                                                        }
                                                        searchString={
                                                            searchString.length == 0
                                                                ? ""
                                                                : item.footer.name
                                                                      .toLocaleLowerCase()
                                                                      .includes(
                                                                          searchString.toLowerCase(),
                                                                      )
                                                                ? ""
                                                                : searchString
                                                        }
                                                        version={props.version}
                                                        scenarioMixinName={props.scenarioMixinName}
                                                        setUpdate={props.setUpdate}
                                                        level={currentLevel + 1} 
                                                    />
                                                </TreeItemCustom>
                                            )}
                                            {item.footer.rightElements && (
                                                <TreeItemCustom
                                                    key={`${i}fxrx`}
                                                    id={`${i}fxrx`}
                                                    content={ <div style={{ fontSize: "0.875rem" }}> {intl.formatMessage({ id: "structure_right_elements" })} </div> }
                                                >
                                                    <TreeItems
                                                        items={item.footer.rightElements}
                                                        id={`${i}fxrx`}
                                                        scenarioVersion={props.scenarioVersion}
                                                        element={props.element}
                                                        sortBefore={
                                                            props.sortBefore == ""
                                                                ? `${item.sort}.f.r`
                                                                : `${props.sortBefore}.${item.sort}.f.r`
                                                        }
                                                        searchString={
                                                            searchString.length == 0
                                                                ? ""
                                                                : item.footer.name
                                                                      .toLocaleLowerCase()
                                                                      .includes(
                                                                          searchString.toLowerCase(),
                                                                      )
                                                                ? ""
                                                                : searchString
                                                        }
                                                        version={props.version}
                                                        scenarioMixinName={props.scenarioMixinName}
                                                        setUpdate={props.setUpdate}
                                                        level={currentLevel + 1} 
                                                    />
                                                </TreeItemCustom>
                                            )}
                                        </TreeItemCustom>
                                    )}
                            </>
                        )}

                        {(item.type == "dialog" || item.type == "searchhelp") && (
                            <>
                                {item.footer &&
                                    ((searchString.length > 0 &&
                                        containsChildSearchString(item.footer, searchString)) ||
                                        searchString.length == 0) && (
                                        <TreeItemCustom
                                            key={`${i}fx`}
                                            id={`${i}fx`}
                                            title={item.footer.name}
                                            selected={`${i}fx` == props.element}
                                            navigated={`${i}fx` == props.element}
                                            style={getDimmedStyleWithChildren(item.footer.visible, item.footer.elements)}
                                            content={
                                                <div
                                                    style={{
                                                        display: "flex",
                                                        flexDirection: "row",
                                                        justifyContent: "space-between",
                                                    }}
                                                >
                                                    {messages.filter(
                                                        (a: any) =>
                                                            a.elementId ==
                                                            item
                                                                .footer!.name.charAt(0)
                                                                .toUpperCase() +
                                                                item.footer!.name.slice(1),
                                                    ).length > 0 && (
                                                        <Icon
                                                            name="warning"
                                                            showTooltip={true}
                                                            accessibleName={messages
                                                                .filter(
                                                                    (a: any) =>
                                                                        a.elementId ==
                                                                        item
                                                                            .footer!.name.charAt(0)
                                                                            .toUpperCase() +
                                                                            item.footer!.name.slice(
                                                                                1,
                                                                            ),
                                                                )
                                                                .map((e: Message) => e.message)
                                                                .join(", ")}
                                                            style={{
                                                                marginInlineEnd: 10,
                                                                color: "grey",
                                                            }}
                                                        />
                                                    )}
                                                    <div>
                                                        <FlexBox direction="Row">
                                                            <Tag
                                                                design={TagDesign.Set2}
                                                                colorScheme="10"
                                                            >
                                                                {props.sortBefore != ""
                                                                    ? `${props.sortBefore}.${item.sort}.f`
                                                                    : `${item.sort}.f`}
                                                            </Tag>
                                                            &nbsp;&nbsp;
                                                            <FlexBox
                                                                direction="Column"
                                                                justifyContent="Center"
                                                            >{`${item.footer.name} (${item.footer.type})`}</FlexBox>
                                                        </FlexBox>
                                                    </div>
                                                </div>
                                            }
                                        >
                                            <TreeItems
                                                items={item.footer.elements}
                                                id={`${i}fx`}
                                                scenarioVersion={props.scenarioVersion}
                                                element={props.element}
                                                sortBefore={
                                                    props.sortBefore == ""
                                                        ? `${item.sort}.f`
                                                        : `${props.sortBefore}.${item.sort}.f`
                                                }
                                                searchString={
                                                    searchString.length == 0
                                                        ? ""
                                                        : item.footer.name
                                                              .toLocaleLowerCase()
                                                              .includes(searchString.toLowerCase())
                                                        ? ""
                                                        : searchString
                                                }
                                                version={props.version}
                                                scenarioMixinName={props.scenarioMixinName}
                                                setUpdate={props.setUpdate}
                                                level={currentLevel + 1} 
                                            />
                                            {item.footer.leftElements && (
                                                <TreeItem
                                                    text={intl.formatMessage({ id: "structure_left_elements" })}
                                                    key={`${i}fxlx`}
                                                    id={`${i}fxlx`}
                                                    style={{ fontSize: "0.875rem" }}
                                                >
                                                    <TreeItems
                                                        items={item.footer.leftElements}
                                                        id={`${i}fxlx`}
                                                        scenarioVersion={props.scenarioVersion}
                                                        element={props.element}
                                                        sortBefore={
                                                            props.sortBefore == ""
                                                                ? `${item.sort}.f.l`
                                                                : `${props.sortBefore}.${item.sort}.f.l`
                                                        }
                                                        searchString={
                                                            searchString.length == 0
                                                                ? ""
                                                                : item.footer.name
                                                                      .toLocaleLowerCase()
                                                                      .includes(
                                                                          searchString.toLowerCase(),
                                                                      )
                                                                ? ""
                                                                : searchString
                                                        }
                                                        version={props.version}
                                                        scenarioMixinName={props.scenarioMixinName}
                                                        setUpdate={props.setUpdate}
                                                        level={currentLevel + 1}  
                                                    />
                                                </TreeItem>
                                            )}
                                            {item.footer.rightElements && (
                                                <TreeItem
                                                    text={intl.formatMessage({ id: "structure_right_elements" })}
                                                    key={`${i}fxrx`}
                                                    id={`${i}fxrx`}
                                                    style={{ fontSize: "0.875rem" }}
                                                >
                                                    <TreeItems
                                                        items={item.footer.rightElements}
                                                        id={`${i}fxrx`}
                                                        scenarioVersion={props.scenarioVersion}
                                                        element={props.element}
                                                        sortBefore={
                                                            props.sortBefore == ""
                                                                ? `${item.sort}.f.r`
                                                                : `${props.sortBefore}.${item.sort}.f.r`
                                                        }
                                                        searchString={
                                                            searchString.length == 0
                                                                ? ""
                                                                : item.footer.name
                                                                      .toLocaleLowerCase()
                                                                      .includes(
                                                                          searchString.toLowerCase(),
                                                                      )
                                                                ? ""
                                                                : searchString
                                                        }
                                                        version={props.version}
                                                        scenarioMixinName={props.scenarioMixinName}
                                                        setUpdate={props.setUpdate}
                                                        level={currentLevel + 1}  
                                                    />
                                                </TreeItem>
                                            )}
                                        </TreeItemCustom>
                                    )}
                            </>
                        )}

                        {item.type == "table" && (
                            <>
                                {item.toolbar &&
                                    ((searchString.length > 0 &&
                                        containsChildSearchString(item.toolbar, searchString)) ||
                                        searchString.length == 0) && (
                                        <TreeItemCustom
                                            key={`${i}tx`}
                                            id={`${i}tx`}
                                            title={item.toolbar.name}
                                            selected={`${i}tx` == props.element}
                                            navigated={`${i}tx` == props.element}
                                            style={getDimmedStyleWithChildren(item.toolbar.visible, item.toolbar.elements)}
                                            content={
                                                <div
                                                    style={{
                                                        display: "flex",
                                                        flexDirection: "row",
                                                        justifyContent: "space-between",
                                                    }}
                                                >
                                                    {messages.filter(
                                                        (a: any) =>
                                                            a.elementId ==
                                                            item
                                                                .toolbar!.name.charAt(0)
                                                                .toUpperCase() +
                                                                item.toolbar!.name.slice(1),
                                                    ).length > 0 && (
                                                        <Icon
                                                            name="warning"
                                                            showTooltip={true}
                                                            accessibleName={messages
                                                                .filter(
                                                                    (a: any) =>
                                                                        a.elementId ==
                                                                        item
                                                                            .toolbar!.name.charAt(0)
                                                                            .toUpperCase() +
                                                                            item.toolbar!.name.slice(
                                                                                1,
                                                                            ),
                                                                )
                                                                .map((e: Message) => e.message)
                                                                .join(", ")}
                                                            style={{
                                                                marginInlineEnd: 10,
                                                                color: "grey",
                                                            }}
                                                        />
                                                    )}
                                                    <div>
                                                        <FlexBox direction="Row">
                                                            <Tag
                                                                design={TagDesign.Set2}
                                                                colorScheme="10"
                                                            >
                                                                {props.sortBefore != ""
                                                                    ? `${props.sortBefore}.${item.sort}.t`
                                                                    : `${item.sort}.t`}
                                                            </Tag>
                                                            &nbsp;&nbsp;
                                                            <FlexBox
                                                                direction="Column"
                                                                justifyContent="Center"
                                                            >{`${item.toolbar.name} (${item.toolbar.type})`}</FlexBox>
                                                        </FlexBox>
                                                    </div>
                                                </div>
                                            }
                                        >
                                            <TreeItems
                                                items={item.toolbar.elements}
                                                id={`${i}tx`}
                                                scenarioVersion={props.scenarioVersion}
                                                element={props.element}
                                                sortBefore={
                                                    props.sortBefore == ""
                                                        ? `${item.sort}.t`
                                                        : `${props.sortBefore}.${item.sort}.t`
                                                }
                                                searchString={
                                                    searchString.length == 0
                                                        ? ""
                                                        : item.toolbar.name
                                                              .toLocaleLowerCase()
                                                              .includes(searchString.toLowerCase())
                                                        ? ""
                                                        : searchString
                                                }
                                                version={props.version}
                                                scenarioMixinName={props.scenarioMixinName}
                                                setUpdate={props.setUpdate}
                                                level={currentLevel + 1} 
                                            />
                                            {item.toolbar.leftElements && (
                                                <TreeItem
                                                    text={intl.formatMessage({ id: "structure_left_elements" })}
                                                    key={`${i}txlx`}
                                                    id={`${i}txlx`}
                                                >
                                                    <TreeItems
                                                        items={item.toolbar.leftElements}
                                                        id={`${i}txlx`}
                                                        scenarioVersion={props.scenarioVersion}
                                                        element={props.element}
                                                        sortBefore={
                                                            props.sortBefore == ""
                                                                ? `${item.sort}.t.l`
                                                                : `${props.sortBefore}.${item.sort}.t.l`
                                                        }
                                                        searchString={
                                                            searchString.length == 0
                                                                ? ""
                                                                : item.toolbar.name
                                                                      .toLocaleLowerCase()
                                                                      .includes(
                                                                          searchString.toLowerCase(),
                                                                      )
                                                                ? ""
                                                                : searchString
                                                        }
                                                        version={props.version}
                                                        scenarioMixinName={props.scenarioMixinName}
                                                        setUpdate={props.setUpdate}
                                                        level={currentLevel + 1} 
                                                    />
                                                </TreeItem>
                                            )}
                                            {item.toolbar.rightElements && (
                                                <TreeItem
                                                    text={intl.formatMessage({ id: "structure_right_elements" })}
                                                    key={`${i}txrx`}
                                                    id={`${i}txrx`}
                                                >
                                                    <TreeItems
                                                        items={item.toolbar.rightElements}
                                                        id={`${i}txrx`}
                                                        scenarioVersion={props.scenarioVersion}
                                                        element={props.element}
                                                        sortBefore={
                                                            props.sortBefore == ""
                                                                ? `${item.sort}.t.r`
                                                                : `${props.sortBefore}.${item.sort}.t.r`
                                                        }
                                                        searchString={
                                                            searchString.length == 0
                                                                ? ""
                                                                : item.toolbar.name
                                                                      .toLocaleLowerCase()
                                                                      .includes(
                                                                          searchString.toLowerCase(),
                                                                      )
                                                                ? ""
                                                                : searchString
                                                        }
                                                        version={props.version}
                                                        scenarioMixinName={props.scenarioMixinName}
                                                        setUpdate={props.setUpdate}
                                                        level={currentLevel + 1} 
                                                    />
                                                </TreeItem>
                                            )}
                                        </TreeItemCustom>
                                    )}
                            </>
                        )}

                        {item.type == "toolbar" && (
                            <>
                                {item.leftElements && (
                                    <TreeItem text={intl.formatMessage({ id: "structure_left_elements" })} key={`${i}lx`} id={`${i}lx`} style={{ fontSize: "0.875rem" }}>
                                        <TreeItems
                                            items={item.leftElements}
                                            id={`${i}lx`}
                                            scenarioVersion={props.scenarioVersion}
                                            element={props.element}
                                            sortBefore={
                                                props.sortBefore == ""
                                                    ? `${item.sort}.l`
                                                    : `${props.sortBefore}.${item.sort}.l`
                                            }
                                            searchString={searchString}
                                            version={props.version}
                                            scenarioMixinName={props.scenarioMixinName}
                                            setUpdate={props.setUpdate}
                                            level={currentLevel + 1} 
                                        />
                                    </TreeItem>
                                )}
                                {item.rightElements && (
                                    <TreeItem text={intl.formatMessage({ id: "structure_right_elements" })} key={`${i}rx`} id={`${i}rx`} style={{ fontSize: "0.875rem" }}>
                                        <TreeItems
                                            items={item.rightElements}
                                            id={`${i}rx`}
                                            scenarioVersion={props.scenarioVersion}
                                            element={props.element}
                                            sortBefore={
                                                props.sortBefore == ""
                                                    ? `${item.sort}.r`
                                                    : `${props.sortBefore}.${item.sort}.r`
                                            }
                                            searchString={searchString}
                                            version={props.version}
                                            scenarioMixinName={props.scenarioMixinName}
                                            setUpdate={props.setUpdate}
                                            level={currentLevel + 1}
                                        />
                                    </TreeItem>
                                )}
                            </>
                        )}
                    </DraggableTreeItem>
                )
            })}
        </>
    )
}