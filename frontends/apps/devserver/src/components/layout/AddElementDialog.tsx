import {
    Bar,
    Button,
    Dialog,
    Form,
    FormItem,
    Input,
    Title,
    Text,
    Label,
} from "@ui5/webcomponents-react"
import {
    AttachmentDesignType,
    DataTypeValue,
    DesignValue,
    Elem,
    InputValue,
    Scenario,
    SelectValue,
    StyleValue,
    UploadType,
} from "../../utils/scenarioDefinitions"
import { createUseStyles } from "react-jss"
import { useState } from "react"
import useElementsStore from "../../state/elements"
import TreeItemBase from "@ui5/webcomponents/dist/TreeItemBase"
import ElementTypeSelect from "./ElementTypeSelect"
import { generateUniqueId } from "../../utils/formUtils"
import { useMessages, Severity } from "commons"

interface Props {
    dialogOpen: boolean
    update: number
    element: string | undefined
    el: Elem | undefined
    parentEl: Elem | undefined
    scenarioMixinName: string
    treeItemsShown: Scenario | null
    selectedTreeItem: TreeItemBase | undefined
    version: number
    setElement: (e: any) => void
    setEl: (e: any) => void
    setUpdate: (e: any) => void
    setDialogOpen: (e: any) => void
}

const useStyles = createUseStyles({
    button: {
        marginLeft: 20,
        marginRight: 20,
        fontSize: "medium",
    },
})

export default function AddElementDialog(props: Props) {
    const classes = useStyles()
    const addElement = useElementsStore((state) => state.addElement)
    const editDetailData = useElementsStore((state) => state.editDetailData)
    const editTexts = useElementsStore((state) => state.editTexts)
    const [newNodeName, setNewNodeName] = useState<string>("")
    const [newNodeType, setNewNodeType] = useState<string>("")
    const { toast } = useMessages()

    return (
        <>
            <Dialog
                header={
                    <Bar>
                        <Title>Add element</Title>
                    </Bar>
                }
                footer={
                    <Bar
                        design="Footer"
                        endContent={
                            <Button
                                design="Positive"
                                className={classes.button}
                                tooltip="Add"
                                onClick={function Ta() {
                                    if (props.element) {
                                        var indexes = props
                                            .element!.split("x")
                                            .filter((item) => item)
                                    } else {
                                        indexes = []
                                    }

                                    var newEl: Elem = (newEl = {
                                        name: newNodeName,
                                        type: newNodeType,
                                        dataType: DataTypeValue.Auto,
                                        id: generateUniqueId(),
                                        sort: 0,
                                        defaultValue: "",
                                        col: "",
                                        css: "",
                                        showLabel: false,
                                        showHelp: false,
                                        columnOptions: {
                                            minColumnWidth: "",
                                            maxColumnWidth: "",
                                        },
                                        visible: "",
                                        editable: "",
                                        required: "",
                                        elements: [],
                                    })
                                    if (newNodeType == "button") {
                                        newEl = {
                                            ...newEl,
                                            design: DesignValue.Default,
                                            icon: "",
                                            tooltip: "",
                                        }
                                    } else if (
                                        ["currency", "multiselect", "radio", "select"].includes(
                                            newNodeType,
                                        )
                                    ) {
                                        newEl = {
                                            ...newEl,
                                            valueHelp: {
                                                name: "",
                                                validate: false,
                                                emptySelection: false,
                                                displayFormat: "",
                                            },
                                        }
                                    } else if (newNodeType == "dialog") {
                                        newEl = {
                                            ...newEl,
                                            size: {
                                                height: "",
                                                width: "",
                                            },
                                            footer: {
                                                name: newNodeName + "Footer",
                                                type: "toolbar",
                                                id: generateUniqueId(),
                                                sort: 0,
                                                css: "",
                                                showLabel: false,
                                                showHelp: false,
                                                columnOptions: {
                                                    minColumnWidth: "",
                                                    maxColumnWidth: "",
                                                },
                                                visible: "",
                                                editable: "",
                                                required: "",
                                                leftElements: [],
                                                rightElements: [],
                                                elements: [],
                                            },
                                        }
                                    } else if (newNodeType == "image") {
                                        newEl = {
                                            ...newEl,
                                            size: {
                                                height: "",
                                                width: "",
                                            },
                                        }
                                    } else if (newNodeType == "input") {
                                        newEl = {
                                            ...newEl,
                                            inputType: InputValue.Text,
                                        }
                                    } else if (newNodeType == "form") {
                                        newEl = {
                                            ...newEl,
                                            footer: {
                                                name: newNodeName + "Footer",
                                                type: "toolbar",
                                                id: generateUniqueId(),
                                                sort: 0,
                                                visible: "",
                                                editable: "",
                                                required: "",
                                                css: "",
                                                showLabel: false,
                                                showHelp: false,
                                                columnOptions: {
                                                    minColumnWidth: "",
                                                    maxColumnWidth: "",
                                                },
                                                leftElements: [],
                                                rightElements: [],
                                                elements: [],
                                            },
                                            headerSegment: {
                                                name: newNodeName + "Header",
                                                type: "segment",
                                                id: generateUniqueId(),
                                                sort: 0,
                                                defaultValue: "",
                                                col: "",
                                                css: "",
                                                showLabel: false,
                                                showHelp: false,
                                                columnOptions: {
                                                    minColumnWidth: "",
                                                    maxColumnWidth: "",
                                                },
                                                visible: "",
                                                editable: "",
                                                required: "",
                                                elements: [],
                                            },
                                        }
                                    } else if (newNodeType == "wizard") {
                                        newEl = {
                                            ...newEl,
                                            footer: {
                                                name: newNodeName + "Footer",
                                                type: "toolbar",
                                                id: generateUniqueId(),
                                                sort: 0,
                                                visible: "",
                                                editable: "",
                                                required: "",
                                                css: "",
                                                showLabel: false,
                                                showHelp: false,
                                                columnOptions: {
                                                    minColumnWidth: "",
                                                    maxColumnWidth: "",
                                                },
                                                leftElements: [],
                                                rightElements: [],
                                                elements: [],
                                            },
                                        }
                                    } else if (newNodeType == "searchhelp") {
                                        newEl = {
                                            ...newEl,
                                            footer: {
                                                name: newNodeName + "Footer",
                                                type: "toolbar",
                                                id: generateUniqueId(),
                                                sort: 0,
                                                visible: "",
                                                editable: "",
                                                required: "",
                                                css: "",
                                                showLabel: false,
                                                showHelp: false,
                                                columnOptions: {
                                                    minColumnWidth: "",
                                                    maxColumnWidth: "",
                                                },
                                                leftElements: [],
                                                rightElements: [],
                                                elements: [],
                                            },
                                            size: {
                                                height: "",
                                                width: "",
                                            },
                                            dialogKey: "",
                                        }
                                    } else if (newNodeType == "table") {
                                        newEl = {
                                            ...newEl,
                                            select: SelectValue.Single,
                                            style: StyleValue.Dialog,
                                            toolbar: {
                                                name: newNodeName + "Toolbar",
                                                type: "toolbar",
                                                id: generateUniqueId(),
                                                sort: 0,
                                                visible: "",
                                                editable: "",
                                                required: "",
                                                css: "",
                                                showLabel: false,
                                                showHelp: false,
                                                columnOptions: {
                                                    minColumnWidth: "",
                                                    maxColumnWidth: "",
                                                },
                                                leftElements: [],
                                                rightElements: [],
                                                elements: [],
                                            },
                                        }
                                    } else if (newNodeType == "alert") {
                                        newEl = { ...newEl, design: DesignValue.Positive, icon: "" }
                                    } else if (newNodeType == "mixin") {
                                        newEl = { ...newEl, path: "", mixinName: "", version: 0 }
                                    } else if (newNodeType == "attachment") {
                                        newEl = {
                                            ...newEl,
                                            fileTypes: "",
                                            cardinality: UploadType.Single,
                                            design: AttachmentDesignType.FileUploader,
                                            categories: [],
                                            adapter: "",
                                            hasDescription: false,
                                            select: SelectValue.None,
                                        }
                                    }

                                    //find new sort-property
                                    if (props.el) {
                                        if (props.el.elements.length > 0) {
                                            var maxSort = props.el?.elements?.reduce(
                                                (prev, current) =>
                                                    (prev.sort ? prev.sort : -1) >
                                                        (current.sort ? current.sort : -1)
                                                        ? prev
                                                        : current,
                                            ).sort
                                        } else {
                                            maxSort = 0
                                        }
                                    } else {
                                        if (props.parentEl) {
                                            var newParent
                                            if (props.parentEl.footer) {
                                                newParent = props.parentEl.footer
                                            } else if (props.parentEl.toolbar) {
                                                newParent = props.parentEl.toolbar
                                            } else {
                                                newParent = props.parentEl
                                            }
                                            if (props?.element![props.element!.length - 2] == "l") {
                                                if (newParent.leftElements!.length > 0) {
                                                    maxSort = newParent.leftElements?.reduce(
                                                        (prev, current) =>
                                                            (prev.sort ? prev.sort : -1) >
                                                                (current.sort ? current.sort : -1)
                                                                ? prev
                                                                : current,
                                                    ).sort
                                                } else {
                                                    maxSort = 0
                                                }
                                            } else {
                                                if (newParent.rightElements!.length > 0) {
                                                    maxSort = newParent.rightElements?.reduce(
                                                        (prev, current) =>
                                                            (prev.sort ? prev.sort : -1) >
                                                                (current.sort ? current.sort : -1)
                                                                ? prev
                                                                : current,
                                                    ).sort
                                                } else {
                                                    maxSort = 0
                                                }
                                            }
                                        } else {
                                            maxSort = props.treeItemsShown?.elements?.reduce(
                                                (prev, current) =>
                                                    (prev.sort ? prev.sort : -1) >
                                                        (current.sort ? current.sort : -1)
                                                        ? prev
                                                        : current,
                                            ).sort
                                        }
                                    }
                                    if (maxSort) {
                                        newEl.sort = maxSort + 10
                                    } else {
                                        newEl.sort = 10
                                    }

                                    var version = props.treeItemsShown?.version

                                    props.setElement(
                                        props.element! + props.el?.elements.length! + "x",
                                    )
                                    props.setEl(newEl)
                                    addElement({
                                        indexes: indexes.join("x"),
                                        version: version,
                                        newEl: newEl,
                                        scenarioMixinName: props.scenarioMixinName,
                                    })

                                    // Show toast notification
                                    toast(Severity.None, "element_added")

                                    var texts: any = JSON.parse(
                                        JSON.stringify(props.treeItemsShown?.texts!),
                                    )
                                    Object.keys(texts).forEach((key) => {
                                        texts![key][`${newEl.name}.short`] = ""
                                        texts[key]![`${newEl.name}.long`] = ""
                                        texts![key][`${newEl.name}.title`] = ""
                                        texts[key][`${newEl.name}.doc`] = ""
                                    })
                                    editTexts({
                                        version: props.version,
                                        texts: texts,
                                        scenarioMixinName: props.scenarioMixinName,
                                    })

                                    props.setUpdate(props.update + 1)
                                    if (props.selectedTreeItem) {
                                        props.selectedTreeItem!.expanded = true
                                    }

                                    props.setDialogOpen(false)
                                }}
                            >
                                Add
                            </Button>
                        }
                        startContent={
                            <Button
                                design="Negative"
                                className={classes.button}
                                tooltip="Close"
                                onClick={function Ta() {
                                    props.setDialogOpen(false)
                                }}
                            >
                                Close
                            </Button>
                        }
                    >
                        {["form", "wizard", "dialog", "searchhelp"].includes(props.el?.type!) &&
                            props.el?.footer == undefined && (
                                <Button
                                    className={classes.button}
                                    tooltip="Add footer"
                                    onClick={function Ta() {
                                        var newFooter: Elem = {
                                            name: props.el!.name + "Footer",
                                            type: "toolbar",
                                            visible: "",
                                            editable: "",
                                            required: "",
                                            sort: 0,
                                            dataType: DataTypeValue.Auto,
                                            defaultValue: "",
                                            col: "",
                                            css: "",
                                            showLabel: false,
                                            showHelp: false,
                                            columnOptions: {
                                                minColumnWidth: "",
                                                maxColumnWidth: "",
                                            },
                                            showAsColumn: false,
                                            leftElements: [],
                                            rightElements: [],
                                            elements: [],
                                        }
                                        var newEl = { ...props.el, footer: newFooter }
                                        props.setEl(newFooter)
                                        props.setElement(props.element! + "fx")
                                        editDetailData({
                                            version: props.version,
                                            indexes: props.element,
                                            newEl: newEl,
                                            scenarioMixinName: props.scenarioMixinName,
                                        })
                                        props.setUpdate(props.update + 1)
                                        if (props.selectedTreeItem) {
                                            props.selectedTreeItem!.expanded = true
                                        }
                                        props.setDialogOpen(false)
                                    }}
                                >
                                    Add footer
                                </Button>
                            )}
                        {props.el?.type == "table" && props.el?.toolbar == undefined && (
                            <Button
                                className={classes.button}
                                tooltip="Add toolbar"
                                onClick={function Ta() {
                                    var newToolbar: Elem = {
                                        name: props.el!.name + "Toolbar",
                                        type: "toolbar",
                                        visible: "",
                                        editable: "",
                                        required: "",
                                        sort: 0,
                                        dataType: DataTypeValue.Auto,
                                        defaultValue: "",
                                        col: "",
                                        css: "",
                                        showLabel: false,
                                        showHelp: false,
                                        columnOptions: {
                                            minColumnWidth: "",
                                            maxColumnWidth: "",
                                        },
                                        showAsColumn: false,
                                        leftElements: [],
                                        rightElements: [],
                                        elements: [],
                                    }
                                    var newEl = { ...props.el, toolbar: newToolbar }
                                    props.setElement(props.element! + "tx")
                                    props.setEl(newToolbar)
                                    editDetailData({
                                        version: props.version,
                                        indexes: props.element,
                                        newEl: newEl,
                                        scenarioMixinName: props.scenarioMixinName,
                                    })
                                    props.setUpdate(props.update + 1)
                                    if (props.selectedTreeItem) {
                                        props.selectedTreeItem!.expanded = true
                                    }
                                    props.setDialogOpen(false)
                                }}
                            >
                                Add toolbar
                            </Button>
                        )}
                        {props.el?.type == "form" && props.el?.headerSegment == undefined && (
                            <Button
                                className={classes.button}
                                tooltip="Add header segment"
                                onClick={function Ta() {
                                    var newHeaderSegment: Elem = {
                                        name: props.el!.name + "HeaderSegment",
                                        type: "segment",
                                        visible: "",
                                        editable: "",
                                        required: "",
                                        sort: 0,
                                        dataType: DataTypeValue.Auto,
                                        defaultValue: "",
                                        col: "",
                                        css: "",
                                        showLabel: false,
                                        showHelp: false,
                                        columnOptions: {
                                            minColumnWidth: "",
                                            maxColumnWidth: "",
                                        },
                                        showAsColumn: false,
                                        elements: [],
                                    }
                                    var newEl = { ...props.el, headerSegment: newHeaderSegment }
                                    props.setElement(props.element! + "hx")
                                    props.setEl(newHeaderSegment)
                                    editDetailData({
                                        version: props.version,
                                        indexes: props.element,
                                        newEl: newEl,
                                        scenarioMixinName: props.scenarioMixinName,
                                    })
                                    props.setUpdate(props.update + 1)
                                    if (props.selectedTreeItem) {
                                        props.selectedTreeItem!.expanded = true
                                    }
                                    props.setDialogOpen(false)
                                }}
                            >
                                Add header segment
                            </Button>
                        )}
                    </Bar>
                }
                headerText="Dialog Header"
                onBeforeOpen={function Ta() {
                    setNewNodeName(`newItem${(Math.random() + 1).toString(36).substring(7)}`)
                    if (props.el) {
                        switch (props.el.type) {
                            case undefined:
                                setNewNodeType("dialog")
                                break
                            case "form":
                                setNewNodeType("segment")
                                break
                            case "wizard":
                                setNewNodeType("segment")
                                break
                            case "segment":
                                setNewNodeType("group")
                                break
                            case "toolbar":
                                setNewNodeType("button")
                                break
                            default:
                                setNewNodeType("input")
                        }
                    } else {
                        if (props.parentEl) {
                            setNewNodeType("button")
                        } else {
                            setNewNodeType("dialog")
                        }
                    }
                }}
                open={props.dialogOpen}
                onClose={() => {
                    props.setDialogOpen(false)
                }}
                style={{ padding: 0, margin: 0, minWidth: "400px", width: "40%" }}
            >
                {props.el && (
                    <Text>
                        {
                            <>
                                Insert a new node under the node{" "}
                                <i>
                                    <b>{props.el?.name}.</b>
                                </i>
                            </>
                        }
                    </Text>
                )}
                {!props.el && props.element && (
                    <Text>
                        {
                            <>
                                Insert a new node to{" "}
                                <i>
                                    <b>
                                        {props.element[props.element.length - 2] == "l"
                                            ? "left"
                                            : "right"}{" "}
                                        elements
                                    </b>
                                </i>{" "}
                                of node{" "}
                                <i>
                                    <b>{props.parentEl?.name}.</b>
                                </i>
                            </>
                        }
                    </Text>
                )}
                {!props.el && !props.element && (
                    <Text>
                        {
                            <>
                                Insert a new node{" "}
                                <i>
                                    <b>on top of the tree.</b>
                                </i>
                            </>
                        }
                    </Text>
                )}

                <Form layout="S1 M1 L1 XL1" labelSpan="S2 M2 L3 XL3">
                    <FormItem labelContent={<Label>Name</Label>}>
                        {" "}
                        <Input
                            value={newNodeName}
                            onChange={(e) => {
                                setNewNodeName(
                                    e.target.attributes.getNamedItem("value")!.nodeValue!,
                                )
                            }}
                        />
                    </FormItem>
                    <FormItem labelContent={<Label>Element type</Label>}>
                        <ElementTypeSelect
                            type={newNodeType}
                            className=""
                            parentType={props.el?.type ? props.el?.type : props.parentEl?.type}
                            isScenario={props.scenarioMixinName == "Scenario" ? true : false}
                            treeItemsShown={props.treeItemsShown}
                            changeItem={false}
                            messages={[]}
                            onChange={(e) => {
                                setNewNodeType(e.detail.selectedOption.textContent!.toString())
                            }}
                        />
                    </FormItem>
                </Form>
            </Dialog>
        </>
    )
}
