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
    isReadOnly: boolean
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

    // Helper function to get element from store based on index path
    const getElementFromStore = (indexPath: string): any => {
        const indexes = indexPath.split("x").filter((item) => item)

        const scenarioIndex =
            props.scenarioMixinName === "Scenario"
                ? elements.findIndex((el) => el.version === props.version && !("kind" in el))
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

    // Check if the element is in leftElements or rightElements
    const isInLeftOrRightElements = (): boolean => {
        if (!props.element) return false
        const indexes = props.element.split("x").filter((item) => item)
        // Find l/r marker before the leaf index.
        for (let i = 0; i < indexes.length - 1; i++) {
            if (indexes[i] === "l" || indexes[i] === "r") {
                return true
            }
        }
        return false
    }

    // Return active container marker: l or r.
    const getContainerType = (): string | null => {
        if (!props.element) return null
        const indexes = props.element.split("x").filter((item) => item)
        for (let i = 0; i < indexes.length - 1; i++) {
            if (indexes[i] === "l" || indexes[i] === "r") {
                return indexes[i]
            }
        }
        return null
    }

    // Return siblings from current container.
    const getSiblings = (): Elem[] => {
        if (!props.element || !props.el) return []

        if (isInLeftOrRightElements()) {
            const containerType = getContainerType()
            const parent = props.parents[props.parents.length - 2]?.elem
            if (containerType === "l") {
                return parent?.leftElements || []
            } else if (containerType === "r") {
                return parent?.rightElements || []
            }
        }
        const parent = props.parents[props.parents.length - 2]?.elem
        return parent?.elements || []
    }

    // Check if can move up
    const canMoveUp = (): boolean => {
        if (!props.el || !props.element) return false
        const siblings = getSiblings()
        return siblings.filter((sibling) => sibling.sort! < props.el!.sort!).length > 0
    }

    // Check if can move down
    const canMoveDown = (): boolean => {
        if (!props.el || !props.element) return false
        const siblings = getSiblings()
        return siblings.filter((sibling) => sibling.sort! > props.el!.sort!).length > 0
    }

    // Move up inside left/right container.
    const handleMoveUpInContainer = () => {
        if (!props.el || !props.element) return

        const containerType = getContainerType()
        const toolbarParent = props.parents[props.parents.length - 2]?.elem

        if (!toolbarParent) return

        const currentContainer =
            containerType === "l" ? toolbarParent.leftElements : toolbarParent.rightElements

        if (!currentContainer) return

        // Find the element directly before
        const filteredBefore = currentContainer.filter(
            (sibling: Elem) => sibling.sort! < props.el!.sort!
        )
        if (filteredBefore.length === 0) return

        const elBefore: Elem = filteredBefore.reduce((max: Elem, sibling: Elem) =>
            sibling.sort! > max.sort! ? sibling : max
        )

        // Swap sort values
        const updatedCurrentEl = { ...props.el, sort: elBefore.sort }
        const updatedBeforeEl = { ...elBefore, sort: props.el.sort }

        // Write updated container.
        const updatedContainer = currentContainer.map((el: Elem) => {
            if (el.name === props.el!.name && el.sort === props.el!.sort && el.id === props.el!.id) {
                return updatedCurrentEl
            }
            if (el.name === elBefore.name && el.sort === elBefore.sort && el.id === elBefore.id) {
                return updatedBeforeEl
            }
            return el
        })

        // Create updated toolbar
        const updatedToolbar = { ...toolbarParent }
        if (containerType === "l") {
            updatedToolbar.leftElements = updatedContainer
        } else if (containerType === "r") {
            updatedToolbar.rightElements = updatedContainer
        }

        // Build path back to toolbar parent.
        const indexes = props.element.split("x").filter((item) => item)
        let toolbarParentPath = ""
        for (let i = 0; i < indexes.length; i++) {
            if (indexes[i] === "l" || indexes[i] === "r") {
                toolbarParentPath = indexes.slice(0, i).join("x")
                break
            }
        }

        editDetailData({
            version: props.version,
            indexes: toolbarParentPath,
            newEl: updatedToolbar,
            scenarioMixinName: props.scenarioMixinName,
        })

        props.setEl(updatedCurrentEl)
        props.setUpdate(props.update + 1)
    }

    // Move down inside left/right container.
    const handleMoveDownInContainer = () => {
        if (!props.el || !props.element) return

        const containerType = getContainerType()
        const toolbarParent = props.parents[props.parents.length - 2]?.elem

        if (!toolbarParent) return

        const currentContainer =
            containerType === "l" ? toolbarParent.leftElements : toolbarParent.rightElements

        if (!currentContainer) return

        // Find the element directly after
        const filteredAfter = currentContainer.filter(
            (sibling: Elem) => sibling.sort! > props.el!.sort!
        )
        if (filteredAfter.length === 0) return

        const elAfter: Elem = filteredAfter.reduce((min: Elem, sibling: Elem) =>
            sibling.sort! < min.sort! ? sibling : min
        )

        // Swap sort values
        const updatedCurrentEl = { ...props.el, sort: elAfter.sort }
        const updatedAfterEl = { ...elAfter, sort: props.el.sort }

        // Write updated container.
        const updatedContainer = currentContainer.map((el: Elem) => {
            if (el.name === props.el!.name && el.sort === props.el!.sort && el.id === props.el!.id) {
                return updatedCurrentEl
            }
            if (el.name === elAfter.name && el.sort === elAfter.sort && el.id === elAfter.id) {
                return updatedAfterEl
            }
            return el
        })

        // Create updated toolbar
        const updatedToolbar = { ...toolbarParent }
        if (containerType === "l") {
            updatedToolbar.leftElements = updatedContainer
        } else if (containerType === "r") {
            updatedToolbar.rightElements = updatedContainer
        }

        // Get the path to the toolbar parent
        const indexes = props.element.split("x").filter((item) => item)
        let toolbarParentPath = ""
        for (let i = 0; i < indexes.length; i++) {
            if (indexes[i] === "l" || indexes[i] === "r") {
                toolbarParentPath = indexes.slice(0, i).join("x")
                break
            }
        }

        editDetailData({
            version: props.version,
            indexes: toolbarParentPath,
            newEl: updatedToolbar,
            scenarioMixinName: props.scenarioMixinName,
        })

        props.setEl(updatedCurrentEl)
        props.setUpdate(props.update + 1)
    }

    // Handle moving element out of leftElements/rightElements to toolbar's main elements
    const handleMoveOutOfLeftRightElements = () => {
        if (!props.el || !props.element) return

        const containerType = getContainerType()
        const toolbarParent = props.parents[props.parents.length - 2]?.elem

        if (!toolbarParent) return

        const currentContainer =
            containerType === "l" ? toolbarParent.leftElements : toolbarParent.rightElements

        if (!currentContainer) return

        // Calculate new sort value for the toolbar's elements
        const maxSort =
            toolbarParent.elements?.reduce((max: number, obj: Elem) => {
                return obj.sort! > max ? obj.sort! : max
            }, 0) ?? 0

        const newEl = { ...props.el, sort: maxSort + 10 }

        // Remove from current container
        const updatedContainer = currentContainer.filter(
            (el: Elem) =>
                !(el.name === props.el!.name && el.sort === props.el!.sort && el.id === props.el!.id)
        )

        // Write toolbar with updated containers.
        const updatedToolbar = { ...toolbarParent }
        if (containerType === "l") {
            updatedToolbar.leftElements = updatedContainer
        } else if (containerType === "r") {
            updatedToolbar.rightElements = updatedContainer
        }
        updatedToolbar.elements = [...(updatedToolbar.elements || []), newEl]

        // Build path back to toolbar parent.
        const indexes = props.element.split("x").filter((item) => item)
        let toolbarParentPath = ""
        for (let i = 0; i < indexes.length; i++) {
            if (indexes[i] === "l" || indexes[i] === "r") {
                toolbarParentPath = indexes.slice(0, i).join("x")
                break
            }
        }

        editDetailData({
            version: props.version,
            indexes: toolbarParentPath,
            newEl: updatedToolbar,
            scenarioMixinName: props.scenarioMixinName,
        })

        props.setEl(newEl)
        props.setElement(undefined)
        props.setUpdate(props.update + 1)
    }

    const [onSortButton, setOnSortButton] = useState<boolean>(false)

    return (
        <div className={classes.buttonsTree}
            style={props.isReadOnly ? { pointerEvents: "none", opacity: 0.5 } : undefined}
        >
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
                    disabled={props.mode == ListSelectionMode.Delete || props.search.trim() != ""}
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
                                        (sibling) => sibling.sort! < props.el!.sort!
                                    ).length == 0 &&
                                    props.parents[props.parents.length - 2].elem.headerSegment !=
                                    props.el))
                            ? 160
                            : 10,
                }}
            >
                <Button
                    icon="slim-arrow-up"
                    disabled={!canMoveUp()}
                    onClick={() => {
                        if (isInLeftOrRightElements()) {
                            handleMoveUpInContainer()
                        } else {
                            const indexes = props.element!.split("x").filter((item) => item)
                            const parentIndexPath = indexes.slice(0, -1).join("x")

                            // Load current parent.
                            const currentParent = getElementFromStore(parentIndexPath)
                            if (!currentParent || !currentParent.elements) return

                            // Find the element directly before
                            const filteredBefore = currentParent.elements.filter(
                                (sibling: Elem) => sibling.sort! < props.el!.sort!
                            )
                            if (filteredBefore.length === 0) return

                            const elBefore: Elem = filteredBefore.reduce(
                                (max: Elem, sibling: Elem) =>
                                    sibling.sort! > max.sort! ? sibling : max
                            )

                            // Swap sort values
                            const updatedCurrentEl = { ...props.el!, sort: elBefore.sort }
                            const updatedBeforeEl = { ...elBefore, sort: props.el!.sort }

                            // Update parent with both swapped elements
                            const updatedParent = {
                                ...currentParent,
                                elements: currentParent.elements.map((el: Elem) => {
                                    if (
                                        el.name === props.el!.name &&
                                        el.sort === props.el!.sort &&
                                        el.id === props.el!.id
                                    ) {
                                        return updatedCurrentEl
                                    }
                                    if (
                                        el.name === elBefore.name &&
                                        el.sort === elBefore.sort &&
                                        el.id === elBefore.id
                                    ) {
                                        return updatedBeforeEl
                                    }
                                    return el
                                }),
                            }

                            editDetailData({
                                version: props.version,
                                indexes: parentIndexPath,
                                newEl: updatedParent,
                                scenarioMixinName: props.scenarioMixinName,
                            })

                            props.setEl(updatedCurrentEl)
                            props.setUpdate(props.update + 1)
                        }
                    }}
                >
                    Up
                </Button>
                <Button
                    icon="slim-arrow-down"
                    disabled={!canMoveDown()}
                    onClick={() => {
                        if (isInLeftOrRightElements()) {
                            handleMoveDownInContainer()
                        } else {
                            const indexes = props.element!.split("x").filter((item) => item)
                            const parentIndexPath = indexes.slice(0, -1).join("x")

                            // Load current parent.
                            const currentParent = getElementFromStore(parentIndexPath)
                            if (!currentParent || !currentParent.elements) return

                            // Find the element directly after
                            const filteredAfter = currentParent.elements.filter(
                                (sibling: Elem) => sibling.sort! > props.el!.sort!
                            )
                            if (filteredAfter.length === 0) return

                            const elAfter: Elem = filteredAfter.reduce(
                                (min: Elem, sibling: Elem) =>
                                    sibling.sort! < min.sort! ? sibling : min
                            )

                            // Swap sort values
                            const updatedCurrentEl = { ...props.el!, sort: elAfter.sort }
                            const updatedAfterEl = { ...elAfter, sort: props.el!.sort }

                            // Update parent with both swapped elements
                            const updatedParent = {
                                ...currentParent,
                                elements: currentParent.elements.map((el: Elem) => {
                                    if (
                                        el.name === props.el!.name &&
                                        el.sort === props.el!.sort &&
                                        el.id === props.el!.id
                                    ) {
                                        return updatedCurrentEl
                                    }
                                    if (
                                        el.name === elAfter.name &&
                                        el.sort === elAfter.sort &&
                                        el.id === elAfter.id
                                    ) {
                                        return updatedAfterEl
                                    }
                                    return el
                                }),
                            }

                            editDetailData({
                                version: props.version,
                                indexes: parentIndexPath,
                                newEl: updatedParent,
                                scenarioMixinName: props.scenarioMixinName,
                            })

                            props.setEl(updatedCurrentEl)
                            props.setUpdate(props.update + 1)
                        }
                    }}
                >
                    Down
                </Button>
                <Button
                    icon="slim-arrow-left"
                    disabled={props.parents.length < 2 || !props.element}
                    onClick={() => {
                        // Check if element is in leftElements or rightElements
                        if (isInLeftOrRightElements()) {
                            // Move out of leftElements/rightElements to toolbar's main elements
                            handleMoveOutOfLeftRightElements()
                        } else {
                            // Original left movement logic for regular elements
                            const indexes = props.element!.split("x").filter((item) => item)
                            const parentIndexPath = indexes.slice(0, -1).join("x")
                            const grandParentIndexPath = indexes.slice(0, -2).join("x")

                            // Get elements from store
                            const currentParent = getElementFromStore(parentIndexPath)
                            const grandParent = grandParentIndexPath
                                ? getElementFromStore(grandParentIndexPath)
                                : elements.find((el) =>
                                    props.scenarioMixinName === "Scenario"
                                        ? el.version === props.version && !("kind" in el)
                                        : el.version === props.version &&
                                        el.name === props.scenarioMixinName
                                )

                            if (!currentParent || !grandParent) return

                            // Create moved element with new sort position
                            const newEl = { ...props.el!, sort: (currentParent.sort || 0) + 5 }

                            // Updated parent without the element being moved
                            const updatedParent = {
                                ...currentParent,
                                elements: currentParent.elements.filter(
                                    (el: Elem) =>
                                        !(
                                            el.name === props.el!.name &&
                                            el.sort === props.el!.sort &&
                                            el.id === props.el!.id
                                        )
                                ),
                            }

                            // Replace parent and append moved node.
                            const updatedGrandParent = {
                                ...grandParent,
                                elements: [
                                    ...(grandParent.elements || []).map((el: Elem) =>
                                        el.name === currentParent.name &&
                                            el.sort === currentParent.sort &&
                                            el.id === currentParent.id
                                            ? updatedParent
                                            : el
                                    ),
                                    newEl,
                                ],
                            }

                            editDetailData({
                                version: props.version,
                                indexes: grandParentIndexPath,
                                newEl: updatedGrandParent,
                                scenarioMixinName: props.scenarioMixinName,
                            })

                            // Select next sibling element or undefined
                            const nextSibling = updatedParent.elements
                                .filter((sibling: Elem) => sibling.sort! > props.el!.sort!)
                                .reduce(
                                    (min: Elem | null, sibling: Elem) =>
                                        !min || sibling.sort! < min.sort! ? sibling : min,
                                    null
                                )

                            props.setEl(nextSibling || undefined)
                            props.setUpdate(props.update + 1)
                        }
                    }}
                >
                    Left
                </Button>
                <Button
                    icon="slim-arrow-right"
                    disabled={
                        props.parents[props.parents.length - 2] == undefined ||
                        props.parents[props.parents.length - 2].elem.elements.filter(
                            (sibling) =>
                                sibling != undefined &&
                                props.el != undefined &&
                                sibling.sort! < props.el.sort!
                        ).length == 0 ||
                        !props.element ||
                        isInLeftOrRightElements()
                    }
                    onClick={() => {
                        const indexes = props.element!.split("x").filter((item) => item)
                        const parentIndexPath = indexes.slice(0, -1).join("x")

                        // Get Parent from store
                        const currentParent = getElementFromStore(parentIndexPath)
                        if (!currentParent || !currentParent.elements) return

                        // Pick previous sibling as new parent.
                        const filteredBefore = currentParent.elements.filter(
                            (sibling: Elem) => sibling.sort! < props.el!.sort!
                        )
                        if (filteredBefore.length === 0) return

                        const elBefore: Elem = filteredBefore.reduce(
                            (max: Elem, sibling: Elem) => (sibling.sort! > max.sort! ? sibling : max)
                        )

                        const minSort =
                            elBefore.elements && elBefore.elements.length > 0
                                ? elBefore.elements.reduce(
                                    (min: number, obj: Elem) =>
                                        obj.sort! < min ? obj.sort! : min,
                                    elBefore.elements[0].sort || 0
                                )
                                : 0

                        const newEl = { ...props.el!, sort: minSort > 0 ? minSort - 10 : 5 }

                        // Remove selected and insert as first child.
                        const updatedParent = {
                            ...currentParent,
                            elements: currentParent.elements
                                .filter(
                                    (el: Elem) =>
                                        !(
                                            el.name === props.el!.name &&
                                            el.sort === props.el!.sort &&
                                            el.id === props.el!.id
                                        )
                                )
                                .map((el: Elem) => {
                                    // Insert moved item at start.
                                    if (
                                        el.name === elBefore.name &&
                                        el.sort === elBefore.sort &&
                                        el.id === elBefore.id
                                    ) {
                                        return {
                                            ...el,
                                            elements: [newEl, ...(el.elements || [])],
                                        }
                                    }
                                    return el
                                }),
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
                    icon="slim-arrow-left"
                    style={{
                        display:
                            props.parents.length > 1 &&
                                props.parents[props.parents.length - 2].elem.type == "toolbar" &&
                                props.element &&
                                !["l", "r"].includes(
                                    props.element!.split("x").filter((item) => item).at(-2) ?? ""
                                )
                                ? "flex"
                                : "none",
                    }}
                    onClick={() => {
                        var indexes = props.element!.split("x").filter((item) => item)
                        var indexes2 = props.element!.slice(
                            0,
                            props.element!.lastIndexOf("x", props.element!.lastIndexOf("x") - 1) + 1
                        )

                        var maxSort =
                            props.parents[props.parents.length - 2].elem.leftElements?.reduce(
                                (max: number, obj: Elem) => {
                                    return obj.sort! > max ? obj.sort! : max
                                },
                                0
                            ) ?? 0

                        var newEl = { ...props.el!, sort: maxSort + 10 }
                        var newParentEl = { ...props.parents[props.parents.length - 2].elem }
                        newParentEl!.leftElements = [...(newParentEl!.leftElements || []), newEl]

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
                                    props.element!.split("x").filter((item) => item).at(-2) ?? ""
                                )
                                ? "flex"
                                : "none",
                    }}
                    onClick={() => {
                        var indexes = props.element!.split("x").filter((item) => item)
                        var indexes2 = props.element!.slice(
                            0,
                            props.element!.lastIndexOf("x", props.element!.lastIndexOf("x") - 1) + 1
                        )

                        var maxSort =
                            props.parents[props.parents.length - 2].elem.rightElements?.reduce(
                                (max: number, obj: Elem) => {
                                    return obj.sort! > max ? obj.sort! : max
                                },
                                0
                            ) ?? 0

                        var newEl = { ...props.el!, sort: maxSort + 10 }
                        var newParentEl = { ...props.parents[props.parents.length - 2].elem }
                        newParentEl!.rightElements = [...(newParentEl!.rightElements || []), newEl]

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
                    icon="slim-arrow-up"
                    style={{
                        display:
                            props.parents.length > 1 &&
                                props.el &&
                                props.element &&
                                props.parents[props.parents.length - 2].elem.elements.filter(
                                    (sibling) => sibling.sort! < props.el!.sort!
                                ).length == 0 &&
                                props.parents[props.parents.length - 2].elem.headerSegment != undefined &&
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
                                props.element!.slice(0, -1).lastIndexOf("x") + 1
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
                    Header
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
                        props.element!.split("x").filter((item) => item).at(-1)
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
