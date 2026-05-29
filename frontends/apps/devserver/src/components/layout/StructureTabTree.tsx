import React from "react"
import {
    Breadcrumbs,
    BreadcrumbsItem,
    Button,
    CheckBox,
    FlexBox,
    Form,
    FormItem,
    Icon,
    Input,
    Label,
    Link,
    MessageBoxType,
    Option,
    Page,
    Select,
    SplitterElement,
    SplitterLayout,
    Text,
    Tree,
} from "@ui5/webcomponents-react"
import { createUseStyles } from "react-jss"
import ElementTypeSelect from "./ElementTypeSelect"
import {
    AttachmentDesignType,
    DataTypeValue,
    DesignValue,
    Elem,
    ElementPart,
    InputValue,
    Message,
    Parent,
    Scenario,
    SelectValue,
    StyleValue,
    TextPostfix,
    UploadType,
} from "../../utils/scenarioDefinitions"
import useElementsStore from "../../state/elements"
import TreeItems from "./TreeItems"
import {
    calculateCol,
    changeElAtTypeChange,
    elementInfo2ValueState,
    getHighestSeverity,
} from "../../utils/formUtils"
import useMessagesStore from "../../state/messages"
import ListSelectionMode from "@ui5/webcomponents/dist/types/ListSelectionMode"
import StructureTabActions from "./StructureTabActions"
import ValidationTable from "./ValidationTable"
import StructureTabTextsInput from "./StructureTabTextsInput"
import CategoriesTable from "./CategoriesTable"

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
    setCopiedEl: (e: any) => void
    search: string
    registerFlushPendingNameCommit?: (fn: (() => void) | undefined) => void
}

const useStyles = createUseStyles({
    splitterLeft: {
        maxWidth: "80%",
        display: "flex",
        flexDirection: "row",
    },
    attributes: {
        display: "flex",
        flexDirection: "column",
        justifyContent: "flex-start",
        width: "100%",
        "& [ui5-form-item]": {
            "& [slot='labelContent']": {
                textAlign: "end !important",
            },
        },
    },
    title: {
        padding: 36,
    },
    largeInput: {
        width: "90%",
    },
})

export default function StructureTabTree(props: Props) {
    const classes = useStyles()
    const editBaseData = useElementsStore((state) => state.editBaseData)
    const editDetailData = useElementsStore((state) => state.editDetailData)
    const editTexts = useElementsStore((state) => state.editTexts)

    const [mode, setMode] = React.useState<ListSelectionMode>(ListSelectionMode.Single)
    const [nameDraft, setNameDraft] = React.useState<string>(props.el?.name ?? "")
const treeRef = React.useRef<HTMLElement>(null)
const currentNameRef = React.useRef<string | undefined>(props.el?.name)
const textsRef = React.useRef<any>(props.treeItemsShown?.texts ?? {})
const nameCommitTimeoutRef = React.useRef<ReturnType<typeof setTimeout> | null>(null)
const isTypingNameRef = React.useRef<boolean>(false)
const nameDraftRef = React.useRef<string>(props.el?.name ?? "")

React.useEffect(() => {
    currentNameRef.current = props.el?.name
}, [props.el?.name])

React.useEffect(() => {
    textsRef.current = props.treeItemsShown?.texts ?? {}
}, [props.treeItemsShown?.texts])

React.useEffect(() => {
    if (nameCommitTimeoutRef.current) {
        clearTimeout(nameCommitTimeoutRef.current)
        nameCommitTimeoutRef.current = null
    }
    isTypingNameRef.current = false
    const value = props.el?.name ?? ""
    setNameDraft(value)
    nameDraftRef.current = value
}, [props.element])

React.useEffect(() => {
    if (!isTypingNameRef.current) {
        const value = props.el?.name ?? ""
        setNameDraft(value)
        nameDraftRef.current = value
    }
}, [props.el?.name])

React.useEffect(() => {
    return () => {
        if (nameCommitTimeoutRef.current) {
            clearTimeout(nameCommitTimeoutRef.current)
        }
    }
}, [])

    const flushPendingNameCommit = () => {
        if (nameCommitTimeoutRef.current) {
            clearTimeout(nameCommitTimeoutRef.current)
            nameCommitTimeoutRef.current = null
        }

        if (isTypingNameRef.current) {
            commitNameChange(nameDraftRef.current)
            isTypingNameRef.current = false
        }
    }

React.useEffect(() => {
    props.registerFlushPendingNameCommit?.(flushPendingNameCommit)
    return () => {
        props.registerFlushPendingNameCommit?.(undefined)
    }
}, [props.registerFlushPendingNameCommit, flushPendingNameCommit])

    const commitNameChange = (newName: string) => {
        if (!props.el) {
            return
        }

        const oldName = currentNameRef.current
        if (oldName === newName) {
            return
        }

        if (props.treeItemsShown?.root != undefined && props.treeItemsShown?.root === oldName) {
            editBaseData({
                scenarioMixinName: props.scenarioMixinName,
                version: props.version,
                root: newName,
            })
        }

        const newEl = { ...props.el, name: newName }

        if (oldName != null && oldName !== newName) {
            const texts = JSON.parse(JSON.stringify(textsRef.current ?? {}))
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
                        texts[language][newKey] = texts[language][oldKey] ?? ""
                    }
                    delete texts[language][oldKey]
                })
            })

            textsRef.current = texts
            editTexts({
                version: props.version,
                texts: texts,
                scenarioMixinName: props.scenarioMixinName,
            })
        }

        currentNameRef.current = newName
        props.setEl(newEl)
        editDetailData({
            version: props.version,
            indexes: props.element,
            newEl: newEl,
            scenarioMixinName: props.scenarioMixinName,
        })
        props.setUpdate((prev: number) => prev + 1)
    }

React.useEffect(() => {
    if (props.search && props.treeItemsShown?.elements) {
        const timeoutId = setTimeout(() => {
            const matchedIndex = findIndexByName(props.search)
            if (matchedIndex) {
                changeElement(matchedIndex)
                
                setTimeout(() => {
                    const treeElement = treeRef.current
                    if (treeElement) {
                        const selectedItem = treeElement.querySelector(`[id="${matchedIndex}"]`)
                        if (selectedItem) {
                            selectedItem.scrollIntoView({ 
                                behavior: 'smooth', 
                                block: 'center' 
                            })
                        }
                    }
                }, 300)
            }
        }, 500)

        return () => clearTimeout(timeoutId)
    }
}, [props.search])

    function changeElement(i: string) {
        flushPendingNameCommit()
        props.setElement(i)

        var parentsInternally = []

        var indexes: string[] = i.split("x").filter((item) => item)
        var iter = 2
        var newEl: Elem = props.treeItemsShown!.elements![Number(indexes[0])]

        if (indexes.length == 1 && props.treeItemsShown?.elements) {
            parentsInternally.push({
                elem: newEl,
                index: indexes[0] + "x",
            })
        } else {
            var parentEl = props.treeItemsShown!.elements![Number(indexes[0])]
            parentsInternally.push({
                elem: parentEl,
                index: indexes[0] + "x",
            })
            if (indexes[1] == "f") {
                newEl = newEl.footer!
                parentsInternally.push({
                    elem: newEl,
                    index: indexes[0] + "xfx",
                })
            } else if (indexes[1] == "h") {
                newEl = newEl.headerSegment!
                parentsInternally.push({
                    elem: newEl,
                    index: indexes[0] + "xhx",
                })
            } else {
                newEl = newEl.elements[Number(indexes[1])]
                parentsInternally.push({
                    elem: newEl,
                    index: indexes.slice(0, 2).join("x") + "x",
                })
            }
            var itemsSorted = props.treeItemsShown!.elements![Number(indexes[0])].elements

            while (iter < indexes.length && props.treeItemsShown?.elements) {
                parentEl = newEl

                if (indexes[iter] == "l") {
                    iter = iter + 1
                    itemsSorted = newEl.leftElements!
                    newEl = newEl.leftElements![Number(indexes[iter])]
                } else if (indexes[iter] == "r") {
                    iter = iter + 1
                    itemsSorted = newEl.rightElements!
                    newEl = newEl.rightElements![Number(indexes[iter])]
                } else {
                    itemsSorted = newEl.elements
                    if (indexes[iter] == "f") {
                        newEl = newEl.footer!
                    } else if (indexes[iter] == "t") {
                        newEl = newEl.toolbar!
                    } else if (indexes[iter] == "h") {
                        iter = iter + 1
                        newEl = newEl.headerSegment!
                    } else {
                        newEl = newEl.elements[Number(indexes[iter])]
                    }
                }
                if (newEl) {
                    parentsInternally.push({
                        elem: newEl,
                        index: indexes.slice(0, iter + 1).join("x") + "x",
                    })
                }

                iter = iter + 1
            }
        }
        props.setParents(parentsInternally)

        if (i.slice(-2) == "lx" || i.slice(-2) == "rx") {
            props.setEl(undefined)
        } else if (newEl) {
            props.setEl(newEl)
        }
    }

    function findIndexByName(name: string): string | null {
        const findInElements = (elements: any[], parentIndex = ""): string | null => {
            for (let i = 0; i < elements.length; i++) {
                const element = elements[i];
                const currentIndex = parentIndex ? `${parentIndex}${i}x` : `${i}x`;

                // Case-insensitive partial match
                if (element.name?.toLowerCase().includes(name.toLowerCase())) return currentIndex;
                if (element.footer?.name?.toLowerCase().includes(name.toLowerCase())) return `${currentIndex}fx`;
                if (element.headerSegment?.name?.toLowerCase().includes(name.toLowerCase())) return `${currentIndex}hx`;

                if (element.elements?.length > 0) {
                    const found = findInElements(element.elements, currentIndex);
                    if (found) return found;
                }
            }
            return null;
        };

        return findInElements(props.treeItemsShown?.elements || []);
    }

    const allMessages = useMessagesStore((state) => state.messages)
    const messages = React.useMemo(() => {
        if (props.el) {
            const elementId = props.el.name.charAt(0).toUpperCase() + props.el.name.slice(1)
            return allMessages.filter((m) => m.defVersion == props.version && m.elementId == elementId)
        }
        return allMessages.filter((m) => m.defVersion == props.version)
    }, [allMessages, props.version, props.el?.name])

    return (
        <SplitterLayout
            style={{
                height: "calc(100vh - 200px)",
                width: "100%",
                overflow: "hidden" 

            }}
        >
            <SplitterElement className={classes.splitterLeft} >
                <StructureTabActions
                    setAddDialogOpen={props.setAddDialogOpen}
                    copiedEl={props.copiedEl}
                    setCopiedEl={props.setCopiedEl}
                    setCopyDialogOpen={props.setCopyDialogOpen}
                    el={props.el}
                    setEl={props.setEl}
                    element={props.element}
                    setElement={props.setElement}
                    mode={mode}
                    setMode={setMode}
                    parents={props.parents}
                    setParents={props.setParents}
                    scenarioMixinName={props.scenarioMixinName}
                    search={props.search} 
                    treeItemsShown={props.treeItemsShown}
                    update={props.update}
                    setUpdate={props.setUpdate}
                    version={props.version}
                    showDelete={true}
                    showSort={true}
                />
                <div style={{ width: "100%", height: "100%", display: "flex", flexDirection: "column" }}>
                    <div style={{ overflow: "auto", flex: 1 }}>
                    <Breadcrumbs
                        design="Standard"
                        onItemClick={function Ki(e) {
                            const itemId = e.detail.item.id
                            changeElement(itemId)
                            
                            setTimeout(() => {
                                const treeElement = treeRef.current
                                if (treeElement) {
                                    const selectedItem = treeElement.querySelector(`[id="${itemId}"]`)
                                    if (selectedItem) {
                                        selectedItem.scrollIntoView({ 
                                            behavior: 'smooth', 
                                            block: 'center' 
                                        })
                                    }
                                }
                            }, 100)
                        }}
                        separators="Slash"
                        style={{ paddingBottom: 10 }}
                    >
                        {props.parents.map((item) => {
                            return (
                                <BreadcrumbsItem key={item.index} id={item.index}>
                                    {item.elem.name}
                                </BreadcrumbsItem>
                            )
                        })}
                    </Breadcrumbs>
                    <Tree 
                        ref={treeRef}
                        style={{ width: '99%' }}
                        onItemClick={function Ta() {
                            if (mode == ListSelectionMode.Delete) {
                                setMode(ListSelectionMode.Single)
                            }
                        }}
                        onItemDelete={function Ta(e) {
                            var i = e.detail.item?.attributes
                                .getNamedItem("id")
                                ?.nodeValue?.toString()
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
                                            <i>{e.detail.item.title}</i>{" "}
                                        </b>
                                        and all its child nodes?
                                        

                                        This operation cannot be undone.
                                    </>,
                                )
                                props.setIndexesDelete({
                                    indexes: i,
                                    name: e.detail.item.title,
                                })
                                if (props.el && e.detail.item.title.includes(props.el.name)) {
                                    props.setEl(undefined)
                                    props.setParents([])
                                }
                            }
                        }}
                        onSelectionChange={function Ta(e) {
                            if (mode == ListSelectionMode.Single) {
                                props.setSelectedTreeItem(e.detail.selectedItems[0])
                                changeElement(
                                    e.detail.selectedItems[0].attributes
                                        .getNamedItem("id")!
                                        .nodeValue!.toString(),
                                )
                            }
                        }}
                        selectionMode={mode}
                    >
                        {props.treeItemsShown && (
                            <TreeItems
                                items={props.treeItemsShown.elements!}
                                id={""}
                                searchString={props.search}
                                scenarioVersion={props.version}
                                sortBefore=""
                                element={props.element}
                                version={props.version}
                                scenarioMixinName={props.scenarioMixinName}
                                setUpdate={props.setUpdate}
                            />
                        )}
                    </Tree>
                    </div>
                </div>
            </SplitterElement>
            <SplitterElement>
                {props.el && (
                    <Page className={classes.attributes}>
                        <Form
                            layout="S1 M1 L1 XL1"
                            labelSpan="S1 M2 L3 XL3"
                            headerText={props.el?.name}
                        >
                            {(props.el?.name || props.el?.name == "") && (
                                <FormItem labelContent={<Label>Name</Label>}>
                                    <Input
                                        value={nameDraft}
                                        placeholder={nameDraft}
                                        className={classes.largeInput}
                                        valueState={
                                            messages.length > 0 &&
                                                messages.filter(
                                                    (a: any) => a.elementPart == ElementPart.Name,
                                                ).length > 0
                                                ? elementInfo2ValueState(
                                                    getHighestSeverity(
                                                        messages
                                                            .filter(
                                                                (a: any) =>
                                                                    a.elementPart ==
                                                                    ElementPart.Name,
                                                            )
                                                            .map((e: Message) => e.severity),
                                                    ),
                                                )
                                                : "None"
                                        }
                                        valueStateMessage={
                                            messages.length > 0 &&
                                                messages.filter(
                                                    (a: any) => a.elementPart == ElementPart.Name,
                                                ).length > 0 ? (
                                                messages.filter(
                                                    (a: any) => a.elementPart == ElementPart.Name,
                                                ).length > 1 ? (
                                                    <span>
                                                        Errors:
                                                        <ul>
                                                            {messages
                                                                .filter(
                                                                    (a: any) =>
                                                                        a.elementPart ==
                                                                        ElementPart.Name,
                                                                )
                                                                .map((m: Message) => (
                                                                    <li key={m.message}>
                                                                        {m.message}
                                                                    </li>
                                                                ))}
                                                        </ul>
                                                    </span>
                                                ) : (
                                                    <span>
                                                        {
                                                            messages.filter(
                                                                (a: any) =>
                                                                    a.elementPart ==
                                                                    ElementPart.Name,
                                                            )[0].message
                                                        }
                                                    </span>
                                                )
                                            ) : (
                                                <span></span>
                                            )
                                        }
                                        onInput={(e) => {
                                            const newName =
                                                e.target.attributes.getNamedItem("value")!
                                                    .nodeValue! as string

                                            isTypingNameRef.current = true
                                            setNameDraft(newName)
                                            nameDraftRef.current = newName

                                            if (nameCommitTimeoutRef.current) {
                                                clearTimeout(nameCommitTimeoutRef.current)
                                            }

                                            nameCommitTimeoutRef.current = setTimeout(() => {
                                                commitNameChange(newName)
                                                isTypingNameRef.current = false
                                                nameCommitTimeoutRef.current = null
                                            }, 350)
                                        }}
                                        onBlur={() => {
                                            if (nameCommitTimeoutRef.current) {
                                                clearTimeout(nameCommitTimeoutRef.current)
                                                nameCommitTimeoutRef.current = null
                                            }
                                            commitNameChange(nameDraftRef.current)
                                            isTypingNameRef.current = false
                                        }}
                                    />
                                </FormItem>
                            )}

                            {props.el?.sort != undefined && (
                                <FormItem labelContent={<Label>Sort</Label>}>
                                    <FlexBox
                                        style={{
                                            height: 30,
                                            alignItems: "center",
                                        }}
                                    >
                                        <Label>{props.el?.sort}</Label>
                                    </FlexBox>
                                </FormItem>
                            )}

                            {(props.el?.name || props.el?.name == "") && (
                                <FormItem labelContent={<Label>Texts</Label>}>
                                    <Form
                                        layout="S1 M1 L2 XL1"
                                        labelSpan="S2 M2 L1 XL1"
                                        style={{ width: "90%" }}
                                    >
                                        <FormItem labelContent={<Label>short</Label>}>
                                            <StructureTabTextsInput
                                                postfix={TextPostfix.short}
                                                texts={props.treeItemsShown?.texts!}
                                                defaultLanguage={props.defaultLanguage}
                                                currentName={props.el.name}
                                                scenarioMixinName={props.scenarioMixinName}
                                                version={props.version}
                                                setUpdate={props.setUpdate}
                                            />
                                        </FormItem>
                                        <FormItem labelContent={<Label>long</Label>}>
                                            <StructureTabTextsInput
                                                postfix={TextPostfix.long}
                                                texts={props.treeItemsShown?.texts!}
                                                defaultLanguage={props.defaultLanguage}
                                                currentName={props.el.name}
                                                scenarioMixinName={props.scenarioMixinName}
                                                version={props.version}
                                                setUpdate={props.setUpdate}
                                            />
                                        </FormItem>
                                        <FormItem labelContent={<Label>title</Label>}>
                                            <StructureTabTextsInput
                                                postfix={TextPostfix.title}
                                                texts={props.treeItemsShown?.texts!}
                                                defaultLanguage={props.defaultLanguage}
                                                currentName={props.el.name}
                                                scenarioMixinName={props.scenarioMixinName}
                                                version={props.version}
                                                setUpdate={props.setUpdate}
                                            />
                                        </FormItem>
                                        <FormItem labelContent={<Label>doc</Label>}>
                                            <StructureTabTextsInput
                                                postfix={TextPostfix.doc}
                                                texts={props.treeItemsShown?.texts!}
                                                defaultLanguage={props.defaultLanguage}
                                                currentName={props.el.name}
                                                scenarioMixinName={props.scenarioMixinName}
                                                version={props.version}
                                                setUpdate={props.setUpdate}
                                            />
                                        </FormItem>
                                    </Form>
                                </FormItem>
                            )}

                            {(props.el?.type || props.el?.type == "") && (
                                <FormItem labelContent={<Label>Type</Label>}>
                                    <ElementTypeSelect
                                        parentType={
                                            props.parents.length > 1
                                                ? props.parents[props.parents.length - 2].elem.type
                                                : undefined
                                        }
                                        type={props.el.type}
                                        treeItemsShown={props.treeItemsShown}
                                        changeItem={true}
                                        messages={messages}
                                        className={classes.largeInput}
                                        isScenario={
                                            props.scenarioMixinName == "Scenario" ? true : false
                                        }
                                        onChange={function Ta(e) {
                                            const newEl = changeElAtTypeChange(
                                                props.el!,
                                                e.detail.selectedOption.textContent!.toString(),
                                            )
                                            props.setNewEl(newEl)
                                        }}
                                    />
                                </FormItem>
                            )}

                            {props.el?.type == "button" && (
                                <FormItem labelContent={<Label>Design</Label>}>
                                    <Select
                                        className={classes.largeInput}
                                        onChange={function Ta(e) {
                                            props.setNewEl({
                                                ...props.el,
                                                design: DesignValue[
                                                    e.detail.selectedOption.innerText!.toString() as keyof typeof DesignValue
                                                ],
                                            })
                                        }}
                                    >
                                        <Option
                                            selected={
                                                props.el?.design?.toString() ==
                                                DesignValue[
                                                "Default" as keyof typeof DesignValue
                                                ] || props.el?.design?.toString() == ""
                                            }
                                            key={"Default"}
                                        >
                                            {"Default"}
                                        </Option>
                                        <Option
                                            selected={
                                                props.el?.design?.toString() ==
                                                DesignValue[
                                                "Emphasized" as keyof typeof DesignValue
                                                ]
                                            }
                                            key={"Emphasized"}
                                        >
                                            {"Emphasized"}
                                        </Option>
                                        <Option
                                            selected={
                                                props.el?.design?.toString() ==
                                                DesignValue["Positive" as keyof typeof DesignValue]
                                            }
                                            key={"Positive"}
                                        >
                                            {"Positive"}
                                        </Option>
                                        <Option
                                            selected={
                                                props.el?.design?.toString() ==
                                                DesignValue["Negative" as keyof typeof DesignValue]
                                            }
                                            key={"Negative"}
                                        >
                                            {"Negative"}
                                        </Option>
                                        <Option
                                            selected={
                                                props.el?.design?.toString() ==
                                                DesignValue[
                                                "Transparent" as keyof typeof DesignValue
                                                ]
                                            }
                                            key={"Transparent"}
                                        >
                                            {"Transparent"}
                                        </Option>
                                        <Option
                                            selected={
                                                props.el?.design?.toString() ==
                                                DesignValue["Attention" as keyof typeof DesignValue]
                                            }
                                            key={"Attention"}
                                        >
                                            {"Attention"}
                                        </Option>
                                    </Select>
                                </FormItem>
                            )}

                            {props.el?.type == "alert" && (
                                <FormItem labelContent={<Label>Design</Label>}>
                                    <Select
                                        className={classes.largeInput}
                                        onChange={function Ta(e) {
                                            props.setNewEl({
                                                ...props.el,
                                                design: DesignValue[
                                                    e.detail.selectedOption.innerText!.toString() as keyof typeof DesignValue
                                                ],
                                            })
                                        }}
                                    >
                                        <Option
                                            selected={
                                                props.el?.design?.toString() ==
                                                DesignValue["Positive" as keyof typeof DesignValue]
                                            }
                                            key={"Positive"}
                                        >
                                            {"Positive"}
                                        </Option>
                                        <Option
                                            selected={
                                                props.el?.design?.toString() ==
                                                DesignValue["Negative" as keyof typeof DesignValue]
                                            }
                                            key={"Negative"}
                                        >
                                            {"Negative"}
                                        </Option>
                                        <Option
                                            selected={
                                                props.el?.design?.toString() ==
                                                DesignValue["Warn" as keyof typeof DesignValue]
                                            }
                                            key={"Warn"}
                                        >
                                            {"Warn"}
                                        </Option>
                                        <Option
                                            selected={
                                                props.el?.design?.toString() ==
                                                DesignValue["Info" as keyof typeof DesignValue]
                                            }
                                            key={"Info"}
                                        >
                                            {"Info"}
                                        </Option>
                                    </Select>
                                </FormItem>
                            )}

                            {(props.el?.type == "button" || props.el?.type == "alert" || props.el?.type == "icon") && (
                                <FormItem labelContent={<Label>Icon</Label>}>
                                    <div
                                        style={{
                                            display: "flex",
                                            flexDirection: "row",
                                            width: "90%",
                                        }}
                                    >
                                        <Input
                                            value={props.el?.icon}
                                            placeholder={props.el?.icon}
                                            style={{ flex: 1 }}
                                            onInput={(e) => {
                                                props.setNewEl({
                                                    ...props.el,
                                                    icon: e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                                })
                                            }}
                                        />
                                        <div
                                            style={{
                                                display: "flex",
                                                justifyContent: "center",
                                                alignItems: "center",
                                                marginInline: 20,
                                            }}
                                        >
                                            <Text
                                                style={{
                                                    width: "60px",
                                                }}
                                            >
                                                Preview:{" "}
                                            </Text>
                                            <Icon name={props.el.icon} />
                                        </div>
                                        <Link
                                            design="Default"
                                            wrappingType="None"
                                            href="https://sapui5.hana.ondemand.com/sdk/test-resources/sap/m/demokit/iconExplorer/webapp/index.html#"
                                            target="_blank"
                                            rel="noopener noreferrer"
                                        >
                                            <Button style={{ marginTop: 3, width: "30px", display: "flex", alignItems: "center", justifyContent: "center" }}>
                                                <Icon name="search" />
                                            </Button>
                                        </Link>
                                    </div>
                                </FormItem>
                            )}

                            {(props.el?.type == "button" || props.el?.type == "icon") && (
                                <FormItem labelContent={<Label>Tooltip</Label>}>
                                    <Input
                                        value={props.el?.tooltip}
                                        placeholder={props.el?.tooltip}
                                        className={classes.largeInput}
                                        onInput={(e) => {
                                            props.setNewEl({
                                                ...props.el,
                                                tooltip:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                        }}
                                    />
                                </FormItem>
                            )}

                            {props.el?.type == "mixin" && (
                                <FormItem labelContent={<Label>Path</Label>}>
                                    <Input
                                        value={props.el?.path}
                                        placeholder={props.el?.path}
                                        className={classes.largeInput}
                                        onInput={(e) => {
                                            props.setNewEl({
                                                ...props.el,
                                                path: e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                            })
                                        }}
                                    />
                                </FormItem>
                            )}

                            {props.el?.type == "mixin" && (
                                <FormItem labelContent={<Label>Mixin Name</Label>}>
                                    <Input
                                        value={props.el?.mixinName}
                                        placeholder={props.el?.mixinName}
                                        className={classes.largeInput}
                                        onInput={(e) => {
                                            props.setNewEl({
                                                ...props.el,
                                                mixinName:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                        }}
                                    />
                                </FormItem>
                            )}

                            {props.el?.type == "mixin" && (
                                <FormItem labelContent={<Label>Version</Label>}>
                                    <Input
                                        value={props.el?.version?.toString()}
                                        placeholder={props.el?.version?.toString()}
                                        className={classes.largeInput}
                                        onInput={(e) => {
                                            props.setNewEl({
                                                ...props.el,
                                                version: Number(
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                                ).valueOf(),
                                            })
                                        }}
                                    />
                                </FormItem>
                            )}

                            {props.el?.type == "attachment" && (
                                <>
                                    <FormItem labelContent={<Label>File types</Label>}>
                                        <Input
                                            value={props.el?.fileTypes || ""}
                                            placeholder={props.el?.fileTypes || ""}
                                            className={classes.largeInput}
                                            onInput={(e) => {
                                                props.setNewEl({
                                                    ...props.el,
                                                    fileTypes:
                                                        e.target.attributes.getNamedItem("value")!
                                                            .nodeValue!,
                                                })
                                            }}
                                        />
                                    </FormItem>
                                    <FormItem labelContent={<Label>Cardinality</Label>}>
                                        <Select
                                            className={classes.largeInput}
                                            onChange={function Ta(e) {
                                                props.setNewEl({
                                                    ...props.el,
                                                    cardinality:
                                                        UploadType[
                                                        e.detail.selectedOption.innerText!.toString() as keyof typeof UploadType
                                                        ],
                                                })
                                            }}
                                        >
                                            {(Object.keys(UploadType) as Array<string>).map(
                                                (key) => {
                                                    return (
                                                        <Option
                                                            selected={
                                                                props.el?.cardinality?.toString() ==
                                                                UploadType[
                                                                key as keyof typeof UploadType
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
                                    </FormItem>
                                    <FormItem labelContent={<Label>Design</Label>}>
                                        <Select
                                            className={classes.largeInput}
                                            onChange={function Ta(e) {
                                                props.setNewEl({
                                                    ...props.el,
                                                    design: AttachmentDesignType[
                                                        e.detail.selectedOption.innerText!.toString() as keyof typeof AttachmentDesignType
                                                    ],
                                                })
                                            }}
                                        >
                                            {(
                                                Object.keys(AttachmentDesignType) as Array<string>
                                            ).map((key) => {
                                                return (
                                                    <Option
                                                        selected={
                                                            props.el?.design?.toString() ==
                                                            AttachmentDesignType[
                                                            key as keyof typeof AttachmentDesignType
                                                            ]
                                                        }
                                                        key={key}
                                                    >
                                                        {key}
                                                    </Option>
                                                )
                                            })}
                                        </Select>
                                    </FormItem>
                                    <FormItem labelContent={<Label>Adapter</Label>}>
                                        <Input
                                            value={props.el?.adapter || "database"}
                                            placeholder={props.el?.adapter || "database"}
                                            className={classes.largeInput}
                                            onInput={(e) => {
                                                props.setNewEl({
                                                    ...props.el,
                                                    adapter:
                                                        e.target.attributes.getNamedItem("value")!
                                                            .nodeValue!,
                                                })
                                            }}
                                        />
                                    </FormItem>
                                    <FormItem labelContent={<Label>Has Description</Label>}>
                                        <CheckBox
                                            checked={props.el.hasDescription}
                                            onChange={(e) => {
                                                props.setNewEl({
                                                    ...props.el,
                                                    hasDescription: e.target.checked!,
                                                })
                                            }}
                                        />
                                    </FormItem>
                                    <FormItem labelContent={<Label>Categories</Label>}>
                                        <div style={{ width: "90%" }}>
                                            <CategoriesTable el={props.el} setNewEl={props.setNewEl} />
                                        </div>
                                    </FormItem>
                                </>
                            )}

                            {(props.el?.type == "input" ||
                                props.el?.type == "edit" ||
                                props.el?.type == "autocomplete") && (
                                    <FormItem labelContent={<Label>Data Type</Label>}>
                                        <Select
                                            className={classes.largeInput}
                                            valueState={
                                                messages.length > 0 &&
                                                    messages.filter(
                                                        (a: any) => a.elementPart == ElementPart.DataType,
                                                    ).length > 0
                                                    ? elementInfo2ValueState(
                                                        getHighestSeverity(
                                                            messages
                                                                .filter(
                                                                    (a: any) =>
                                                                        a.elementPart ==
                                                                        ElementPart.DataType,
                                                                )
                                                                .map((e: Message) => e.severity),
                                                        ),
                                                    )
                                                    : "None"
                                            }
                                            valueStateMessage={
                                                messages.length > 0 &&
                                                    messages.filter(
                                                        (a: any) => a.elementPart == ElementPart.DataType,
                                                    ).length > 0 ? (
                                                    <span>
                                                        {messages
                                                            .filter(
                                                                (a: any) =>
                                                                    a.elementPart ==
                                                                    ElementPart.DataType,
                                                            )
                                                            .map((e: Message) => e.message)
                                                            .join(", ")}
                                                    </span>
                                                ) : (
                                                    <span></span>
                                                )
                                            }
                                            onChange={function Ta(e) {
                                                props.setNewEl({
                                                    ...props.el,
                                                    dataType:
                                                        DataTypeValue[
                                                        e.detail.selectedOption.innerText!.toString() as keyof typeof DataTypeValue
                                                        ],
                                                })
                                            }}
                                        >
                                            {(Object.keys(DataTypeValue) as Array<string>).map(
                                                (key) => {
                                                    return (
                                                        <Option
                                                            selected={
                                                                props.el?.dataType?.toString() ==
                                                                DataTypeValue[
                                                                key as keyof typeof DataTypeValue
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
                                    </FormItem>
                                )}

                            {(props.el?.type == "table" || props.el?.type == "attachment") && (
                                <>
                                    <FormItem labelContent={<Label>Select</Label>}>
                                        <Select
                                            className={classes.largeInput}
                                            onChange={function Ta(e) {
                                                props.setNewEl({
                                                    ...props.el,
                                                    select: SelectValue[
                                                        e.detail.selectedOption.innerText!.toString() as keyof typeof SelectValue
                                                    ],
                                                })
                                            }}
                                        >
                                            {(Object.keys(SelectValue) as Array<string>).map(
                                                (key) => {
                                                    return (
                                                        <Option
                                                            selected={
                                                                props.el?.select?.toString() ==
                                                                SelectValue[
                                                                key as keyof typeof SelectValue
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
                                    </FormItem>
                                </>
                            )}

                            {props.el?.type == "table" && (
                                <>
                                    <FormItem labelContent={<Label>Style</Label>}>
                                        <Select
                                            className={classes.largeInput}
                                            onChange={function Ta(e) {
                                                props.setNewEl({
                                                    ...props.el,
                                                    style: StyleValue[
                                                        e.detail.selectedOption.innerText!.toString() as keyof typeof StyleValue
                                                    ],
                                                })
                                            }}
                                        >
                                            {(Object.keys(StyleValue) as Array<string>).map(
                                                (key) => {
                                                    return (
                                                        <Option
                                                            selected={
                                                                props.el?.style?.toString() ==
                                                                StyleValue[
                                                                key as keyof typeof StyleValue
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
                                    </FormItem>
                                    <FormItem labelContent={<Label>Pagesize</Label>}>
                                        <Input
                                            className={classes.largeInput}
                                            value={(props.el?.pageSize ?? 10).toString()}
                                            onInput={(e) => {
                                                props.setNewEl({
                                                    ...props.el,
                                                    pageSize: parseInt(e.target.value) || 10,
                                                })
                                            }}
                                        />
                                    </FormItem>
                                </>
                            )}

                            {(props.el?.type == "image" ||
                                props.el?.type == "searchhelp" ||
                                props.el?.type == "dialog") && (
                                    <FormItem labelContent={<Label>Size</Label>}>
                                        <Form
                                            layout="S1 M1 L2 XL1"
                                            labelSpan="S2 M2 L1 XL1"
                                            style={{ width: "90%" }}
                                        >
                                            <FormItem labelContent={<Label>Height</Label>}>
                                                <Input
                                                    value={props.el?.size?.height || ""}
                                                    placeholder={props.el?.size?.height || ""}
                                                    onInput={(e) => {
                                                        props.setNewEl({
                                                            ...props.el,
                                                            size: {
                                                                ...props.el?.size,
                                                                height: e.target.attributes.getNamedItem(
                                                                    "value",
                                                                )!.nodeValue!,
                                                            },
                                                        })
                                                    }}
                                                />
                                            </FormItem>
                                            <FormItem labelContent={<Label>Width</Label>}>
                                                <Input
                                                    value={props.el?.size?.width || ""}
                                                    placeholder={props.el?.size?.width || ""}
                                                    onInput={(e) => {
                                                        props.setNewEl({
                                                            ...props.el,
                                                            size: {
                                                                ...props.el?.size,
                                                                width: e.target.attributes.getNamedItem(
                                                                    "value",
                                                                )!.nodeValue!,
                                                            },
                                                        })
                                                    }}
                                                />
                                            </FormItem>
                                        </Form>
                                    </FormItem>
                                )}

                            {(props.el?.type == "input" ||
                                props.el?.type == "alert" ||
                                props.el?.type == "icon" ||
                                props.el?.type == "text" ||
                                props.el?.type == "select" ||
                                props.el?.type == "checkbox" ||
                                props.el?.type == "radio") && (
                                    <FormItem labelContent={<Label>Default Value</Label>}>
                                        <Input
                                            value={props.el?.defaultValue}
                                            placeholder={props.el?.defaultValue}
                                            className={classes.largeInput}
                                            onInput={(e) => {
                                                props.setNewEl({
                                                    ...props.el,
                                                    defaultValue:
                                                        e.target.attributes.getNamedItem("value")!
                                                            .nodeValue!,
                                                })
                                            }}
                                        />
                                    </FormItem>
                                )}

                            {(props.el?.type == "button" || props.el?.type == "link") && (
                                <>
                                    <FormItem labelContent={<Label>Link URL</Label>}>
                                        <Input
                                            value={props.el?.linkHRef}
                                            placeholder="https://example.com"
                                            className={classes.largeInput}
                                            onInput={(e) => {
                                                props.setNewEl({
                                                    ...props.el,
                                                    linkHRef:
                                                        e.target.attributes.getNamedItem("value")!
                                                            .nodeValue!,
                                                })
                                            }}
                                        />
                                    </FormItem>
                                    {props.el?.type == "link" && (
                                        <FormItem labelContent={<Label>Link Text</Label>}>
                                            <Input
                                                value={props.el?.linkText}
                                                className={classes.largeInput}
                                                onInput={(e) => {
                                                    props.setNewEl({
                                                        ...props.el,
                                                        linkText:
                                                            e.target.attributes.getNamedItem("value")!
                                                                .nodeValue!,
                                                    })
                                                }}
                                            />
                                        </FormItem>
                                    )}
                                </>
                            )}

                            {props.el?.type == "image" && (
                                <FormItem labelContent={<Label>Image URL / URI</Label>}>
                                    <div
                                        style={{
                                            display: "flex",
                                            flexDirection: "column",
                                            gap: "10px",
                                            width: "90%",
                                        }}
                                    >
                                        <div
                                            style={{
                                                display: "flex",
                                                flexDirection: "row",
                                                gap: "10px",
                                                alignItems: "center",
                                            }}
                                        >
                                            <Input
                                                value={props.el?.defaultValue}
                                                placeholder="https://example.com/image.jpg or data:image/png;base64,..."
                                                className={classes.largeInput}
                                                style={{ flex: 1 }}
                                                onInput={(e) => {
                                                    props.setNewEl({
                                                        ...props.el,
                                                        defaultValue:
                                                            e.target.attributes.getNamedItem("value")!
                                                                .nodeValue!,
                                                    })
                                                }}
                                            />
                                            <Button
                                                icon="upload"
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
                                                                props.setNewEl({
                                                                    ...props.el,
                                                                    defaultValue: dataUri,
                                                                })
                                                            }
                                                            reader.readAsDataURL(file)
                                                        }
                                                    }
                                                    input.click()
                                                }}
                                            >
                                                Choose File
                                            </Button>
                                        </div>
                                        {props.el?.defaultValue && (
                                            <img
                                                src={props.el.defaultValue}
                                                alt="Preview"
                                                style={{
                                                    maxWidth: "200px",
                                                    maxHeight: "200px",
                                                    objectFit: "contain",
                                                    border: "1px solid #ccc",
                                                    borderRadius: "4px",
                                                }}
                                            />
                                        )}
                                    </div>
                                </FormItem>
                            )}

                            {(props.el?.type == "input" ||
                                props.el?.type == "edit" ||
                                props.el?.type == "autocomplete") && (
                                <FormItem labelContent={<Label>Input Type</Label>}>
                                    <Select
                                        className={classes.largeInput}
                                        onChange={function Ta(e) {
                                            props.setNewEl({
                                                ...props.el,
                                                inputType:
                                                    InputValue[
                                                    e.detail.selectedOption.innerText!.toString() as keyof typeof InputValue
                                                    ],
                                            })
                                        }}
                                    >
                                        {(Object.keys(InputValue) as Array<string>).map((key) => {
                                            return (
                                                <Option
                                                    selected={
                                                        props.el?.inputType?.toString() ==
                                                        InputValue[key as keyof typeof InputValue]
                                                    }
                                                    key={key}
                                                >
                                                    {key}
                                                </Option>
                                            )
                                        })}
                                    </Select>
                                </FormItem>
                            )}

                            {(props.el?.col || props.el?.col == "") && (
                                <FormItem labelContent={<Label>Col</Label>}>
                                    <FormItem labelContent={<Label>sm</Label>}>
                                        <Input
                                            value={
                                                /sm:\d+/
                                                    .exec(props.el?.col!)
                                                    ?.toString()
                                                    .split(":")[1] || ""
                                            }
                                            style={{ width: 10 }}
                                            onInput={(e) => {
                                                props.setNewEl({
                                                    ...props.el,
                                                    col: calculateCol(
                                                        props.el?.col!,
                                                        e.target.attributes.getNamedItem("value")!
                                                            .nodeValue!,
                                                        "sm",
                                                    ),
                                                })
                                            }}
                                        />
                                    </FormItem>
                                    <FormItem labelContent={<Label>md</Label>}>
                                        <Input
                                            value={
                                                /md:\d+/
                                                    .exec(props.el?.col!)
                                                    ?.toString()
                                                    .split(":")[1] || ""
                                            }
                                            style={{ width: 10 }}
                                            onInput={(e) => {
                                                props.setNewEl({
                                                    ...props.el,
                                                    col: calculateCol(
                                                        props.el?.col!,
                                                        e.target.attributes.getNamedItem("value")!
                                                            .nodeValue!,
                                                        "md",
                                                    ),
                                                })
                                            }}
                                        />
                                    </FormItem>
                                    <FormItem labelContent={<Label>lg</Label>}>
                                        <Input
                                            value={
                                                /lg:\d+/
                                                    .exec(props.el?.col!)
                                                    ?.toString()
                                                    .split(":")[1] || ""
                                            }
                                            style={{ width: 10 }}
                                            onInput={(e) => {
                                                props.setNewEl({
                                                    ...props.el,
                                                    col: calculateCol(
                                                        props.el?.col!,
                                                        e.target.attributes.getNamedItem("value")!
                                                            .nodeValue!,
                                                        "lg",
                                                    ),
                                                })
                                            }}
                                        />
                                    </FormItem>
                                    <FormItem labelContent={<Label>xl</Label>}>
                                        <Input
                                            value={
                                                /xl:\d+/
                                                    .exec(props.el?.col!)
                                                    ?.toString()
                                                    .split(":")[1] || ""
                                            }
                                            style={{ width: 10 }}
                                            onInput={(e) => {
                                                props.setNewEl({
                                                    ...props.el,
                                                    col: calculateCol(
                                                        props.el?.col!,
                                                        e.target.attributes.getNamedItem("value")!
                                                            .nodeValue!,
                                                        "xl",
                                                    ),
                                                })
                                            }}
                                        />
                                    </FormItem>
                                </FormItem>
                            )}

                            {(props.el?.type == "currency" ||
                                props.el?.type == "multiselect" ||
                                props.el?.type == "radio" ||
                                props.el?.type == "select") && (
                                    <FormItem labelContent={<Label>Value Help</Label>}>
                                        <Form
                                            layout="S1 M1 L2 XL1"
                                            labelSpan="S2 M2 L1 XL1"
                                            style={{ width: "90%" }}
                                        >
                                            <FormItem labelContent={<Label>Name</Label>}>
                                                <Input
                                                    value={props.el?.valueHelp?.name}
                                                    placeholder={props.el?.valueHelp?.name}
                                                    className={classes.largeInput}
                                                    onInput={(e) => {
                                                        props.setNewEl({
                                                            ...props.el,
                                                            valueHelp: {
                                                                ...props.el?.valueHelp,
                                                                name: e.target.attributes.getNamedItem(
                                                                    "value",
                                                                )!.nodeValue!,
                                                            },
                                                        })
                                                    }}
                                                />
                                            </FormItem>
                                            <FormItem labelContent={<Label>Validate</Label>}>
                                                <CheckBox
                                                    checked={props.el.valueHelp?.validate}
                                                    onChange={(e) => {
                                                        props.setNewEl({
                                                            ...props.el,
                                                            valueHelp: {
                                                                ...props.el?.valueHelp,
                                                                validate: e.target.checked!,
                                                            },
                                                        })
                                                    }}
                                                />
                                            </FormItem>
                                            <FormItem labelContent={<Label>Empty Selection</Label>}>
                                                <CheckBox
                                                    checked={props.el.valueHelp?.emptySelection}
                                                    onChange={(e) => {
                                                        props.setNewEl({
                                                            ...props.el,
                                                            valueHelp: {
                                                                ...props.el?.valueHelp,
                                                                emptySelection: e.target.checked!,
                                                            },
                                                        })
                                                    }}
                                                />
                                            </FormItem>
                                            <FormItem labelContent={<Label>Display Format</Label>}>
                                                <Input
                                                    value={props.el?.valueHelp?.displayFormat}
                                                    placeholder={props.el?.valueHelp?.displayFormat}
                                                    className={classes.largeInput}
                                                    onInput={(e) => {
                                                        props.setNewEl({
                                                            ...props.el,
                                                            valueHelp: {
                                                                ...props.el?.valueHelp,
                                                                displayFormat:
                                                                    e.target.attributes.getNamedItem(
                                                                        "value",
                                                                    )!.nodeValue!,
                                                            },
                                                        })
                                                    }}
                                                />
                                            </FormItem>
                                        </Form>
                                    </FormItem>
                                )}

                            {props.el?.type == "searchhelp" && (
                                <FormItem labelContent={<Label>Dialog Key</Label>}>
                                    <Input
                                        value={props.el?.dialogKey}
                                        placeholder={props.el?.dialogKey}
                                        className={classes.largeInput}
                                        onInput={(e) => {
                                            props.setNewEl({
                                                ...props.el,
                                                dialogKey:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                        }}
                                    />
                                </FormItem>
                            )}

                            {(props.el?.visible || props.el?.visible == "") && (
                                <FormItem labelContent={<Label>Visible</Label>}>
                                    <Input
                                        value={props.el?.visible}
                                        placeholder={props.el?.visible}
                                        className={classes.largeInput}
                                        onInput={(e) => {
                                            props.setNewEl({
                                                ...props.el,
                                                visible:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                        }}
                                    />
                                </FormItem>
                            )}
                            {(props.el?.editable || props.el?.editable == "") && (
                                <FormItem labelContent={<Label>Editable</Label>}>
                                    <Input
                                        value={props.el?.editable}
                                        placeholder={props.el?.editable}
                                        className={classes.largeInput}
                                        onInput={(e) => {
                                            props.setNewEl({
                                                ...props.el,
                                                editable:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                        }}
                                    />
                                </FormItem>
                            )}
                            {(props.el?.required || props.el?.required == "") && (
                                <FormItem labelContent={<Label>Required</Label>}>
                                    <Input
                                        value={props.el?.required}
                                        placeholder={props.el?.required}
                                        className={classes.largeInput}
                                        onInput={(e) => {
                                            props.setNewEl({
                                                ...props.el,
                                                required:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                        }}
                                    />
                                </FormItem>
                            )}
                            <FormItem labelContent={<Label>CSS</Label>}>
                                <Input
                                    value={props.el?.css || ""}
                                    placeholder={props.el?.css || ""}
                                    className={classes.largeInput}
                                    onInput={(e) => {
                                        props.setNewEl({
                                            ...props.el,
                                            css: e.target.attributes.getNamedItem("value")!
                                                .nodeValue!,
                                        })
                                    }}
                                />
                            </FormItem>
                            <FormItem labelContent={<Label>Show label</Label>}>
                                <CheckBox
                                    checked={props.el.showLabel}
                                    onChange={(e) => {
                                        props.setNewEl({
                                            ...props.el,
                                            showLabel: e.target.checked!,
                                        })
                                    }}
                                />
                            </FormItem>
                            <FormItem labelContent={<Label>Show help</Label>}>
                                <CheckBox
                                    checked={props.el.showHelp}
                                    onChange={(e) => {
                                        props.setNewEl({
                                            ...props.el,
                                            showHelp: e.target.checked!,
                                        })
                                    }}
                                />
                            </FormItem>
                            {(
                                props.el?.type === "alert" ||
                                props.el?.type === "attachment" ||
                                props.el?.type === "button" ||
                                props.el?.type === "checkbox" ||
                                props.el?.type === "currency" ||
                                props.el?.type === "daterange" ||
                                props.el?.type === "edit" ||
                                props.el?.type === "icon" ||
                                props.el?.type === "image" ||
                                props.el?.type === "input" ||
                                props.el?.type === "link" ||
                                props.el?.type === "multiselect" ||
                                props.el?.type === "radio" ||
                                props.el?.type === "select" ||
                                props.el?.type === "table" ||
                                props.el?.type === "text"
                            ) && (
                                <FormItem labelContent={<Label>Line break</Label>}>
                                    <CheckBox
                                        checked={props.el.lineBreak}
                                        onChange={(e) => {
                                            props.setNewEl({
                                                ...props.el,
                                                lineBreak: e.target.checked!,
                                            })
                                        }}
                                    />
                                </FormItem>
                            )}
                            {props.parents.find((e) => e.elem.type == "wizard") && (
                                <FormItem labelContent={<Label>Wizard format options</Label>}>
                                    <Form
                                        layout="S1 M1 L2 XL1"
                                        labelSpan="S2 M1 L1 XL1"
                                        style={{ width: "90%" }}
                                    >
                                        <FormItem labelContent={<Label>Skip in summary</Label>}>
                                            <CheckBox
                                                checked={
                                                    props.el.wizardFormatOptions?.skipInSummary
                                                }
                                                onChange={(e) => {
                                                    props.setNewEl({
                                                        ...props.el,
                                                        wizardFormatOptions: {
                                                            ...props.el?.wizardFormatOptions,
                                                            skipInSummary: e.target.checked!,
                                                        },
                                                    })
                                                }}
                                            />
                                        </FormItem>
                                        <FormItem labelContent={<Label>Skip in form</Label>}>
                                            <CheckBox
                                                checked={props.el.wizardFormatOptions?.skipInForm}
                                                onChange={(e) => {
                                                    props.setNewEl({
                                                        ...props.el,
                                                        wizardFormatOptions: {
                                                            ...props.el?.wizardFormatOptions,
                                                            skipInForm: e.target.checked!,
                                                        },
                                                    })
                                                }}
                                            />
                                        </FormItem>
                                    </Form>
                                </FormItem>
                            )}

                            {props.parents.find((e) => e.elem.type == "table") && (
                                <>
                                    <FormItem labelContent={<Label>Column options</Label>}>
                                        <Form
                                            layout="S1 M1 L2 XL1"
                                            labelSpan="S2 M2 L1 XL1"
                                            style={{ width: "90%" }}
                                        >
                                            <FormItem labelContent={<Label>Min column width</Label>}>
                                                <Input style={{ width: "100%" }}
                                                    value={props.el?.columnOptions?.minColumnWidth || ""}
                                                    placeholder={
                                                        props.el?.columnOptions?.minColumnWidth || ""
                                                    }
                                                    className={classes.largeInput}
                                                    onInput={(e) => {
                                                        props.setNewEl({
                                                            ...props.el,
                                                            columnOptions: {
                                                                ...props.el?.columnOptions,
                                                                minColumnWidth:
                                                                    e.target.attributes.getNamedItem(
                                                                        "value",
                                                                    )!.nodeValue!,
                                                            },
                                                        })
                                                    }}
                                                />
                                            </FormItem>
                                            <FormItem labelContent={<Label>Max column width</Label>}>
                                                <Input style={{ width: "100%" }}
                                                    value={props.el?.columnOptions?.maxColumnWidth || ""}
                                                    placeholder={
                                                        props.el?.columnOptions?.maxColumnWidth || ""
                                                    }
                                                    className={classes.largeInput}
                                                    onInput={(e) => {
                                                        props.setNewEl({
                                                            ...props.el,
                                                            columnOptions: {
                                                                ...props.el?.columnOptions,
                                                                maxColumnWidth:
                                                                    e.target.attributes.getNamedItem(
                                                                        "value",
                                                                    )!.nodeValue!,
                                                            },
                                                        })
                                                    }}
                                                />
                                            </FormItem>
                                        </Form>{" "}
                                    </FormItem>

                                    {props.el?.showAsColumn != undefined && (
                                        <FormItem labelContent={<Label>Show as Column</Label>}>
                                            <CheckBox
                                                checked={props.el.showAsColumn}
                                                onChange={(e) => {
                                                    props.setNewEl({
                                                        ...props.el,
                                                        showAsColumn: e.target.checked!,
                                                    })
                                                }}
                                            />
                                        </FormItem>
                                    )}
                                </>
                            )}

                            {props.el &&
                                (props.el.type == "input" ||
                                    props.el.type == "edit" ||
                                    props.el.type == "attachment" ||
                                    props.el.type == "table") && (
                                    <FormItem labelContent={<Label>Validation</Label>}>
                                        <div style={{ width: "90%" }}>
                                            <ValidationTable el={props.el} setNewEl={props.setNewEl}></ValidationTable>
                                        </div>
                                    </FormItem>
                                )}
                        </Form>
                    </Page>
                )}
            </SplitterElement>
        </SplitterLayout>
    )
}
