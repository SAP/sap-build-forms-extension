import React from "react"
import { FlexBox, Icon, Tag, TreeItem, TreeItemCustom } from "@ui5/webcomponents-react"
import { Elem, Message } from "../../utils/scenarioDefinitions"
import { compare, containsChildSearchString } from "../../utils/sortUtils"
import useMessagesStore from "../../state/messages"
import { getChildrenMessageSeverities, getHighestSeverity } from "../../utils/formUtils"
import SeverityIcon from "./SeverityIcon"
import TagDesign from "@ui5/webcomponents/dist/types/TagDesign"
import DraggableTreeItem from "./DraggableTreeItem"

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
}

export default function TreeItems(props: Props) {
    var subkey = -1
    const map1 = new Map()
    
    const allMessages = useMessagesStore((state) => state.messages)
    const messages: any = React.useMemo(
        () => allMessages.filter((m: Message) => m.defVersion == props.scenarioVersion),
        [allMessages, props.scenarioVersion]
    )

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
                                        getChildrenMessageSeverities(item, messages, true),
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
                                                    item.name.charAt(0).toUpperCase() +
                                                        item.name.slice(1),
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
                        />

                        {(item.type == "form" || item.type == "wizard") && (
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
                                            />
                                            {item.footer.leftElements && (
                                                <TreeItemCustom
                                                    key={`${i}fxlx`}
                                                    id={`${i}fxlx`}
                                                    content={ <div> Left elements </div> }
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
                                                    />
                                                </TreeItemCustom>
                                            )}
                                            {item.footer.rightElements && (
                                                <TreeItemCustom
                                                    key={`${i}fxrx`}
                                                    id={`${i}fxrx`}
                                                    content={ <div> Right elements </div> }
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
                                            />
                                            {item.footer.leftElements && (
                                                <TreeItem
                                                    text={`Left elements`}
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
                                                    />
                                                </TreeItem>
                                            )}
                                            {item.footer.rightElements && (
                                                <TreeItem
                                                    text={`Right elements`}
                                                    key={`${i}fxrx`}
                                                    id={`${i}fxrx`}
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
                                            />
                                            {item.toolbar.leftElements && (
                                                <TreeItem
                                                    text={`Left elements`}
                                                    key={`${i}txlx`}
                                                    id={`${i}txlx`}
                                                    style={{ fontSize: "0.875rem" }}
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
                                                    />
                                                </TreeItem>
                                            )}
                                            {item.toolbar.rightElements && (
                                                <TreeItem
                                                    text={`Right elements`}
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
                                    <TreeItem text={"Left elements"} key={`${i}lx`} id={`${i}lx`} style={{ fontSize: "0.875rem" }}>
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
                                        />
                                    </TreeItem>
                                )}
                                {item.rightElements && (
                                    <TreeItem text={"Right elements"} key={`${i}rx`} id={`${i}rx`} style={{ fontSize: "0.875rem" }}>
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
