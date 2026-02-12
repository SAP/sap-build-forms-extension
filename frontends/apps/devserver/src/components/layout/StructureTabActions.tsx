import { useState } from "react"
import { Elem, leafNodes, Parent, Scenario } from "../../utils/scenarioDefinitions"
import { ActionSheet, Button } from "@ui5/webcomponents-react"
import ListSelectionMode from "@ui5/webcomponents/dist/types/ListSelectionMode"
import { createUseStyles } from "react-jss"
import useElementsStore from "../../state/elements"
import { isInsertAllowed } from "../../utils/formUtils"
import { useMessages, Severity } from "commons"

interface Props {
    setAddDialogOpen: (e: any) => void
    copiedEl: Elem | undefined
    setCopiedEl: (e: any) => void
    setCopyDialogOpen: (e: any) => void
    el: Elem | undefined
    setEl: (e: any) => void
    element: string 
    setElement: (e: any) => void
    mode: ListSelectionMode
    setMode: (e: any) => void
    parents: Parent[]
    setParents: (e: any) => void
    scenarioMixinName: string
    search: string
    treeItemsShown: Scenario | null | undefined
    update: number
    setUpdate: (e: any) => void
    version: number
    showDelete: boolean
    showSort: boolean
}

const useStyles = createUseStyles({
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
})

export default function StructureTabActions(props: Props) {
    const classes = useStyles()
    const addElement = useElementsStore((state) => state.addElement)
    const removeElement = useElementsStore((state) => state.removeElement)
    const editDetailData = useElementsStore((state) => state.editDetailData)
    const elements = useElementsStore((state) => state.elements)
    const { toast } = useMessages()
    
    // Hilfsfunktion um Element aus dem Store anhand des Index-Pfads zu holen
    const getElementFromStore = (indexPath: string): any => {
        const indexes = indexPath.split("x").filter((item) => item)
        
        const scenarioIndex = props.scenarioMixinName === "Scenario"
            ? elements.findIndex((el) => el.version === props.version && "defaultLanguage" in el)
            : elements.findIndex((el) => el.version === props.version && el.name === props.scenarioMixinName)
        
        if (scenarioIndex < 0) return null
        
        let current: any = elements[scenarioIndex]
        
        for (let i = 0; i < indexes.length; i++) {
            if (!current) return null
            const next = indexes[i + 1]
            if (next === "f") {
                current = current.elements?.[indexes[i]]?.footer
                i++
            } else if (next === "h") {
                current = current.elements?.[indexes[i]]?.headerSegment
                i++
            } else if (next === "t") {
                current = current.elements?.[indexes[i]]?.toolbar
                i++
            } else {
                current = current.elements?.[indexes[i]]
            }
        }
        
        return current
    }
    const [onSortButton, setOnSortButton] = useState<boolean>(false)
    return (
        <div className={classes.buttonsTree}>
            <Button
                icon="add"
                onClick={function Ta() {
                    props.setAddDialogOpen(true)
                }}
                disabled={
                    props.mode == ListSelectionMode.Delete ||
                    (props.el != undefined &&
                        props.el.type != undefined &&
                        leafNodes.includes(props.el.type))
                }
                className={classes.buttonTree}
            />
            {props.mode == ListSelectionMode.Single && props.showDelete && (
                <Button
                    icon="delete"
                    onClick={function Ta() {
                        props.setMode(ListSelectionMode.Delete)
                    }}
                    className={classes.buttonTree}
                />
            )}
            {props.mode == ListSelectionMode.Delete && props.showDelete && (
                <Button
                    icon="cursor-arrow"
                    onClick={function Ta() {
                        props.setMode(ListSelectionMode.Single)
                    }}
                    className={classes.buttonTree}
                />
            )}
            {props.showSort && (
                <Button
                    icon="sort"
                    disabled={
                        props.mode == ListSelectionMode.Delete ||
                        props.search.trim() != ""
                    }
                    id="actionSheetOpener"
                    onClick={function Ta() {
                        if (props.mode != ListSelectionMode.Delete) {
                            setOnSortButton(!onSortButton)
                        }
                    }}
                    className={classes.buttonTree}
                />
            )}
            <ActionSheet
                accessibleRole="AlertDialog"
                horizontalAlign="Center"
                onClose={function Ta() {
                    setOnSortButton(false)
                }}
                opener="actionSheetOpener"
                placement="End"
                verticalAlign="Center"
                open={onSortButton}
                style={{
                    width:
                        props.parents.length > 1 &&
                        (props.parents[props.parents.length - 2].elem.type == "toolbar" ||
                            (props.parents[props.parents.length - 2].elem.headerSegment !=
                                undefined &&
                                props.el &&
                                props.parents[props.parents.length - 2].elem.elements.filter(
                                    (sibling) => sibling.sort! < props.el!.sort!,
                                ).length == 0 &&
                                props.parents[props.parents.length - 2].elem.headerSegment !=
                                    props.el))
                            ? 160
                            : 10,
                }}
            >
                <Button
                    icon="slim-arrow-up"
                    disabled={
                        !props.el ||
                        props.parents[props.parents.length - 2] == undefined ||
                        props.parents[props.parents.length - 2].elem.elements.filter(
                            (sibling) => sibling.sort! < props.el?.sort!,
                        ).length == 0 ||
                        !props.element
                    }
                    onClick={() => {
                        const indexes = props.element!.split("x").filter((item) => item)
                        const parentIndexPath = indexes.slice(0, -1).join("x")
                        
                        // Hole Parent aus dem Store
                        const currentParent = getElementFromStore(parentIndexPath)
                        if (!currentParent || !currentParent.elements) return
                        
                        // Finde das Element direkt davor
                        const filteredBefore = currentParent.elements.filter(
                            (sibling: Elem) => sibling.sort! < props.el!.sort!
                        )
                        if (filteredBefore.length === 0) return
                        
                        const elBefore: Elem = filteredBefore.reduce((max: Elem, sibling: Elem) =>
                            sibling.sort! > max.sort! ? sibling : max
                        )

                        // Tausche die Sort-Werte
                        const updatedCurrentEl = { ...props.el!, sort: elBefore.sort }
                        const updatedBeforeEl = { ...elBefore, sort: props.el!.sort }

                        // Aktualisiere Parent mit beiden getauschten Elementen
                        const updatedParent = {
                            ...currentParent,
                            elements: currentParent.elements.map((el: Elem) => {
                                if (el.name === props.el!.name && el.sort === props.el!.sort && el.id === props.el!.id) {
                                    return updatedCurrentEl
                                }
                                if (el.name === elBefore.name && el.sort === elBefore.sort && el.id === elBefore.id) {
                                    return updatedBeforeEl
                                }
                                return el
                            })
                        }

                        editDetailData({
                            version: props.version,
                            indexes: parentIndexPath,
                            newEl: updatedParent,
                            scenarioMixinName: props.scenarioMixinName,
                        })

                        props.setEl(updatedCurrentEl)
                        props.setUpdate(props.update + 1)
                    }}
                >
                    Up
                </Button>
                <Button
                    icon="slim-arrow-down"
                    disabled={
                        props.parents[props.parents.length - 2] == undefined ||
                        !props.el ||
                        props.parents[props.parents.length - 2].elem.elements.filter(
                            (sibling) => sibling.sort! > props.el!.sort!,
                        ).length == 0 ||
                        !props.element
                    }
                    onClick={() => {
                        const indexes = props.element!.split("x").filter((item) => item)
                        const parentIndexPath = indexes.slice(0, -1).join("x")
                        
                        // Hole Parent aus dem Store
                        const currentParent = getElementFromStore(parentIndexPath)
                        if (!currentParent || !currentParent.elements) return
                        
                        // Finde das Element direkt danach
                        const filteredAfter = currentParent.elements.filter(
                            (sibling: Elem) => sibling.sort! > props.el!.sort!
                        )
                        if (filteredAfter.length === 0) return
                        
                        const elAfter: Elem = filteredAfter.reduce((min: Elem, sibling: Elem) =>
                            sibling.sort! < min.sort! ? sibling : min
                        )

                        // Tausche die Sort-Werte
                        const updatedCurrentEl = { ...props.el!, sort: elAfter.sort }
                        const updatedAfterEl = { ...elAfter, sort: props.el!.sort }

                        // Aktualisiere Parent mit beiden getauschten Elementen
                        const updatedParent = {
                            ...currentParent,
                            elements: currentParent.elements.map((el: Elem) => {
                                if (el.name === props.el!.name && el.sort === props.el!.sort && el.id === props.el!.id) {
                                    return updatedCurrentEl
                                }
                                if (el.name === elAfter.name && el.sort === elAfter.sort && el.id === elAfter.id) {
                                    return updatedAfterEl
                                }
                                return el
                            })
                        }

                        editDetailData({
                            version: props.version,
                            indexes: parentIndexPath,
                            newEl: updatedParent,
                            scenarioMixinName: props.scenarioMixinName,
                        })

                        props.setEl(updatedCurrentEl)
                        props.setUpdate(props.update + 1)
                    }}
                >
                    Down
                </Button>
                <Button
                    icon="slim-arrow-left"
                    disabled={props.parents.length < 2 || !props.element}
                    onClick={() => {
                        const indexes = props.element!.split("x").filter((item) => item)
                        const parentIndexPath = indexes.slice(0, -1).join("x")
                        const grandParentIndexPath = indexes.slice(0, -2).join("x")
                        
                        // Hole Elemente aus dem Store
                        const currentParent = getElementFromStore(parentIndexPath)
                        const grandParent = grandParentIndexPath 
                            ? getElementFromStore(grandParentIndexPath)
                            : elements.find(el => 
                                props.scenarioMixinName === "Scenario" 
                                    ? el.version === props.version && "defaultLanguage" in el
                                    : el.version === props.version && el.name === props.scenarioMixinName
                              )
                        
                        if (!currentParent || !grandParent) return

                        // Erstelle verschobenes Element mit neuer Sort-Position
                        const newEl = { ...props.el!, sort: (currentParent.sort || 0) + 5 }

                        // Aktualisiertes Parent ohne das zu verschiebende Element
                        const updatedParent = {
                            ...currentParent,
                            elements: currentParent.elements.filter((el: Elem) => 
                                !(el.name === props.el!.name && el.sort === props.el!.sort && el.id === props.el!.id)
                            )
                        }
                        
                        // Aktualisiertes Großeltern-Element: Parent ersetzen + neues Element hinzufügen
                        const updatedGrandParent = {
                            ...grandParent,
                            elements: [
                                ...(grandParent.elements || []).map((el: Elem) => 
                                    el.name === currentParent.name && 
                                    el.sort === currentParent.sort &&
                                    el.id === currentParent.id ? updatedParent : el
                                ),
                                newEl
                            ]
                        }
                        
                        editDetailData({
                            version: props.version,
                            indexes: grandParentIndexPath,
                            newEl: updatedGrandParent,
                            scenarioMixinName: props.scenarioMixinName,
                        })

                        // Wähle nächstes Geschwister-Element oder undefined
                        const nextSibling = updatedParent.elements
                            .filter((sibling: Elem) => sibling.sort! > props.el!.sort!)
                            .reduce((min: Elem | null, sibling: Elem) =>
                                !min || sibling.sort! < min.sort! ? sibling : min,
                                null
                            )

                        props.setEl(nextSibling || undefined)
                        props.setUpdate(props.update + 1)
                    }}
                >
                    Left
                </Button>
                <Button
                    icon="slim-arrow-right"
                    disabled={
                        props.parents[props.parents.length - 2] == undefined ||
                        props.parents[props.parents.length - 2].elem.elements.filter(
                            (sibling) => sibling != undefined && props.el != undefined && sibling.sort! < props.el.sort!
                        ).length == 0 ||
                        !props.element
                    }
                    onClick={() => {
                        const indexes = props.element!.split("x").filter((item) => item)
                        const parentIndexPath = indexes.slice(0, -1).join("x")
                        
                        // Hole Parent aus dem Store
                        const currentParent = getElementFromStore(parentIndexPath)
                        if (!currentParent || !currentParent.elements) return
                        
                        // Finde das Element direkt davor (neues Ziel-Parent)
                        const filteredBefore = currentParent.elements.filter(
                            (sibling: Elem) => sibling.sort! < props.el!.sort!
                        )
                        if (filteredBefore.length === 0) return
                        
                        const elBefore: Elem = filteredBefore.reduce((max: Elem, sibling: Elem) =>
                            sibling.sort! > max.sort! ? sibling : max
                        )

                        // Berechne Sort-Position im neuen Parent (am Anfang einfügen)
                        const minSort = elBefore.elements && elBefore.elements.length > 0
                            ? elBefore.elements.reduce((min: number, obj: Elem) => 
                                obj.sort! < min ? obj.sort! : min, elBefore.elements[0].sort || 0)
                            : 0
                        const newEl = { ...props.el!, sort: minSort > 0 ? minSort - 10 : 5 }

                        // Aktualisiertes Parent ohne das zu verschiebende Element
                        const updatedParent = {
                            ...currentParent,
                            elements: currentParent.elements.filter((el: Elem) => 
                                !(el.name === props.el!.name && el.sort === props.el!.sort && el.id === props.el!.id)
                            ).map((el: Elem) => {
                                // Füge das Element als erstes Kind zum Ziel-Element hinzu
                                if (el.name === elBefore.name && el.sort === elBefore.sort && el.id === elBefore.id) {
                                    return {
                                        ...el,
                                        elements: [newEl, ...(el.elements || [])]
                                    }
                                }
                                return el
                            })
                        }

                        editDetailData({
                            version: props.version,
                            indexes: parentIndexPath,
                            newEl: updatedParent,
                            scenarioMixinName: props.scenarioMixinName,
                        })

                        props.setEl(newEl)
                        props.setElement(undefined)
                        props.setUpdate(props.update + 1)
                    }}
                >
                    Right
                </Button>
                <Button
                    icon="slim-arrow-right"
                    style={{
                        display:
                            props.parents.length > 1 &&
                            props.parents[props.parents.length - 2].elem.type == "toolbar" &&
                            props.element &&
                            !["l", "r"].includes(
                                props
                                    .element!.split("x")
                                    .filter((item) => item)
                                    .at(-2) ?? "",
                            )
                                ? "flex"
                                : "none",
                    }}
                    onClick={() => {
                        var indexes = props.element!.split("x").filter((item) => item)
                        var indexes2 = props.element!.slice(
                            0,
                            props.element!.lastIndexOf("x", props.element!.lastIndexOf("x") - 1) +
                                1,
                        )

                        var maxSort =
                            props.parents[props.parents.length - 2].elem.leftElements?.reduce(
                                (max: number, obj: Elem) => {
                                    return obj.sort! > max ? obj.sort! : max
                                },
                                0,
                            ) ?? 0
                        var newEl = { ...props.el!, sort: maxSort + 10 }

                        var newParentEl = { ...props.parents[props.parents.length - 2].elem }
                        newParentEl!.leftElements = [...newParentEl!.leftElements!, newEl]

                        editDetailData({
                            version: props.version,
                            indexes: indexes2,
                            newEl: newParentEl,
                            scenarioMixinName: props.scenarioMixinName,
                        })
                        removeElement({
                            indexes: indexes.join("x"),
                            version: props.version,
                            scenarioMixinName: props.scenarioMixinName,
                        })

                        props.setEl(newEl)
                        var parentsBefore = [...props.parents]
                        if (parentsBefore.length > 1 && newParentEl != undefined) {
                            var lastItem = parentsBefore[parentsBefore.length - 2]
                            lastItem!.elem = newParentEl
                            parentsBefore[parentsBefore.length - 2].elem = newParentEl
                            props.setParents(parentsBefore)
                        }
                        props.setUpdate(props.update + 1)
                    }}
                >
                    Left Elements
                </Button>
                <Button
                    icon="slim-arrow-right"
                    style={{
                        display:
                            props.parents.length > 1 &&
                            props.parents[props.parents.length - 2].elem.type == "toolbar" &&
                            props.element &&
                            !["l", "r"].includes(
                                props
                                    .element!.split("x")
                                    .filter((item) => item)
                                    .at(-2) ?? "",
                            )
                                ? "flex"
                                : "none",
                    }}
                    onClick={() => {
                        var indexes = props.element!.split("x").filter((item) => item)
                        var indexes2 = props.element!.slice(
                            0,
                            props.element!.lastIndexOf("x", props.element!.lastIndexOf("x") - 1) +
                                1,
                        )

                        var maxSort =
                            props.parents[props.parents.length - 2].elem.rightElements?.reduce(
                                (max: number, obj: Elem) => {
                                    return obj.sort! > max ? obj.sort! : max
                                },
                                0,
                            ) ?? 0
                        var newEl = { ...props.el!, sort: maxSort + 10 }

                        var newParentEl = { ...props.parents[props.parents.length - 2].elem }
                        newParentEl!.rightElements = [...newParentEl!.rightElements!, newEl]

                        editDetailData({
                            version: props.version,
                            indexes: indexes2,
                            newEl: newParentEl,
                            scenarioMixinName: props.scenarioMixinName,
                        })
                        removeElement({
                            indexes: indexes.join("x"),
                            version: props.version,
                            scenarioMixinName: props.scenarioMixinName,
                        })

                        props.setEl(newEl)
                        var parentsBefore = [...props.parents]
                        if (parentsBefore.length > 1 && newParentEl != undefined) {
                            var lastItem = parentsBefore[parentsBefore.length - 2]
                            lastItem!.elem = newParentEl
                            parentsBefore[parentsBefore.length - 2].elem = newParentEl
                            props.setParents(parentsBefore)
                        }
                        props.setUpdate(props.update + 1)
                    }}
                >
                    Right Elements
                </Button>
                <Button
                    icon="slim-arrow-right"
                    style={{
                        display:
                            props.parents.length > 1 &&
                            props.el &&
                            props.element &&
                            props.parents[props.parents.length - 2].elem.elements.filter(
                                (sibling) => sibling.sort! < props.el!.sort!,
                            ).length == 0 &&
                            props.parents[props.parents.length - 2].elem.headerSegment !=
                                undefined &&
                            props.parents[props.parents.length - 2].elem.headerSegment != props.el
                                ? "flex"
                                : "none",
                    }}
                    onClick={() => {
                        var maxSort =
                            props.parents[
                                props.parents.length - 2
                            ].elem.headerSegment?.elements.reduce((max: number, obj: Elem) => {
                                return obj.sort! > max ? obj.sort! : max
                            }, 0) ?? 0
                        var newEl = { ...props.el!, sort: maxSort + 10 }

                        var newHeaderSegment = {
                            ...props.parents[props.parents.length - 2].elem.headerSegment!,
                        }
                        newHeaderSegment.elements = [...newHeaderSegment.elements!, newEl]

                        var newTopElement = { ...props.parents[props.parents.length - 2].elem }
                        newTopElement.headerSegment = newHeaderSegment

                        editDetailData({
                            version: props.version,
                            indexes: props.element!.substring(
                                0,
                                props.element!.slice(0, -1).lastIndexOf("x") + 1,
                            ),
                            newEl: newTopElement,
                            scenarioMixinName: props.scenarioMixinName,
                        })
                        removeElement({
                            indexes: props.element!.split("x").filter((item) => item).join("x"),
                            version: props.version,
                            scenarioMixinName: props.scenarioMixinName,
                        })

                        props.setEl(newEl)
                        props.setEl(newEl)
                        var parentsBefore = [...props.parents]
                        if (parentsBefore.length > 1 && newHeaderSegment != undefined) {
                            var lastItem = parentsBefore[parentsBefore.length - 2]
                            lastItem!.elem = newHeaderSegment
                            parentsBefore[parentsBefore.length - 2].elem = newHeaderSegment
                            props.setParents(parentsBefore)
                        }
                        props.setUpdate(props.update + 1)
                    }}
                >
                    Header segment
                </Button>
            </ActionSheet>
            <Button
                icon="copy"
                disabled={props.mode == ListSelectionMode.Delete || !props.el}
                tooltip="Copy"
                className={classes.buttonTree}
                onClick={() => {
                    props.setCopiedEl(props.el)
                    toast(Severity.None, "element_copied")
                }}
            />
            <Button
                icon="paste"
                disabled={
                    props.mode == ListSelectionMode.Delete ||
                    props.copiedEl == undefined ||
                    !props.element ||
                    !isInsertAllowed(
                        props.el?.type,
                        props.copiedEl.type,
                        props
                            .element!.split("x")
                            .filter((item) => item)
                            .at(-1),
                    )
                }
                tooltip="Paste"
                className={classes.buttonTree}
                onClick={() => {
                    props.setCopyDialogOpen(true)
                }}
            />
        </div>
    )
}
