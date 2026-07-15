import { ReactNode, useState } from "react"
import { useIntl } from "react-intl"
import { TreeItemCustom } from "@ui5/webcomponents-react"
import useElementsStore from "../../state/elements"
import { useMessages, Severity } from "commons"

interface Props {
    id: string
    title: string
    selected: boolean
    navigated: boolean
    content: any
    children?: ReactNode
    version: number
    scenarioMixinName: string
    setUpdate?: (value: any) => void
    expanded?: boolean  
    dimmed?: boolean
}

export default function DraggableTreeItem(props: Props) {
    const [isDragging, setIsDragging] = useState(false)
    const [isOver, setIsOver] = useState(false)
    const [dropPosition, setDropPosition] = useState<'before' | 'inside' | 'after'>('inside')
    const editDetailData = useElementsStore((state) => state.editDetailData)
    const elements = useElementsStore((state) => state.elements)
    const { toast } = useMessages()
    const intl = useIntl()

    type CollectionKey = "elements" | "leftElements" | "rightElements"

    // Resolve parent path and sibling collection from a tree id.
    const resolveCollectionContext = (itemPath: string): {
        parentPath: string
        collectionKey: CollectionKey
    } => {
        const tokens = itemPath.split("x").filter((item) => item)

        if (tokens.length <= 1) {
            return { parentPath: "", collectionKey: "elements" }
        }

        const parentTokens = tokens.slice(0, -1)
        const marker = parentTokens[parentTokens.length - 1]

        if (marker === "l") {
            return {
                parentPath: parentTokens.slice(0, -1).join("x"),
                collectionKey: "leftElements",
            }
        }

        if (marker === "r") {
            return {
                parentPath: parentTokens.slice(0, -1).join("x"),
                collectionKey: "rightElements",
            }
        }

        return {
            parentPath: parentTokens.join("x"),
            collectionKey: "elements",
        }
    }

    // Read any node/array using ids with f/h/t/l/r markers.
    const getNodeAtPath = (root: any, path: string): any => {
        if (!root) return null

        const tokens = path.split("x").filter((item) => item)
        if (tokens.length === 0) return root

        let current: any = root

        for (const token of tokens) {
            if (!current) return null

            if (token === "f") {
                current = current.footer
                continue
            }
            if (token === "h") {
                current = current.headerSegment
                continue
            }
            if (token === "t") {
                current = current.toolbar
                continue
            }
            if (token === "l") {
                current = current.leftElements
                continue
            }
            if (token === "r") {
                current = current.rightElements
                continue
            }

            const index = Number(token)
            if (Number.isNaN(index)) {
                return null
            }

            current = Array.isArray(current) ? current[index] : current.elements?.[index]
        }

        return current
    }

    // Immutably update a node/array at a marker-aware path.
    const updateNodeAtPath = (root: any, path: string, updater: (node: any) => any): any => {
        const tokens = path.split("x").filter((item) => item)

        const applyUpdate = (node: any, tokenIndex: number): any => {
            if (tokenIndex >= tokens.length) {
                return updater(node)
            }

            const token = tokens[tokenIndex]

            if (token === "f") {
                return {
                    ...node,
                    footer: applyUpdate(node?.footer, tokenIndex + 1),
                }
            }

            if (token === "h") {
                return {
                    ...node,
                    headerSegment: applyUpdate(node?.headerSegment, tokenIndex + 1),
                }
            }

            if (token === "t") {
                return {
                    ...node,
                    toolbar: applyUpdate(node?.toolbar, tokenIndex + 1),
                }
            }

            if (token === "l" || token === "r") {
                const key = token === "l" ? "leftElements" : "rightElements"
                return {
                    ...node,
                    [key]: applyUpdate(node?.[key] || [], tokenIndex + 1),
                }
            }

            const index = Number(token)
            if (Number.isNaN(index)) {
                return node
            }

            if (Array.isArray(node)) {
                const updatedArray = [...node]
                updatedArray[index] = applyUpdate(updatedArray[index], tokenIndex + 1)
                return updatedArray
            }

            const updatedElements = [...(node?.elements || [])]
            updatedElements[index] = applyUpdate(updatedElements[index], tokenIndex + 1)
            return {
                ...node,
                elements: updatedElements,
            }
        }

        return applyUpdate(root, 0)
    }

    // Read an element from store by path id.
    const getElementFromStore = (indexPath: string): any => {
        // Find active scenario or mixin root.
        const scenarioIndex = props.scenarioMixinName === "Scenario"
            ? elements.findIndex((el) => el.version === props.version && "defaultLanguage" in el)
            : elements.findIndex((el) => el.version === props.version && el.name === props.scenarioMixinName)
        
        if (scenarioIndex < 0) return null
        const root = elements[scenarioIndex]
        return getNodeAtPath(root, indexPath)
    }

    // Block moves into own descendants.
    const isDescendant = (parentPath: string, childPath: string): boolean => {
        if (!parentPath || !childPath) return false
        return childPath.startsWith(parentPath + "x")
    }

    // Start native drag operation.
    const handleDragStart = (e: any) => {
        e.stopPropagation()
        setIsDragging(true)
        e.dataTransfer.effectAllowed = "move"
        e.dataTransfer.setData("text/plain", props.id)
    }

    // Reset drag visuals after drop/cancel.
    const handleDragEnd = () => {
        setIsDragging(false)
        setDropPosition('inside')
    }

    // Determines drop position based on mouse position: 
    // - Top 40%: drop before
    // - Bottom 40%: drop after
    // - Middle 20%: drop inside
    const handleDragOver = (e: any) => {
        e.preventDefault()
        e.stopPropagation()
        
        const rect = e.currentTarget.getBoundingClientRect()
        const y = e.clientY - rect.top
        const height = rect.height
        
        // Calculate drop position based on cursor location
        if (y < height * 0.40) {
            setDropPosition('before')
        } else if (y > height * 0.60) {
            setDropPosition('after')
        } else {
            setDropPosition('inside')
        }
        e.dataTransfer.dropEffect = "move"
        setIsOver(true)
    }

    // Clear hover state when pointer leaves.
    const handleDragLeave = (e: any) => {
        // Only reset if actually leaving the element
        if (!e.currentTarget.contains(e.relatedTarget)) {
            setIsOver(false)
            setDropPosition('inside')
        }
    }

    // Handle drop and route to reorder or move.
    const handleDrop = (e: any) => {
        e.preventDefault()
        e.stopPropagation()
        setIsOver(false)

        const draggedId = e.dataTransfer.getData("text/plain")
        const droppedOnId = props.id

        // Ignore self-drop.
        if (draggedId === droppedOnId) {
            setDropPosition('inside')
            return
        }

        // Prevent invalid parent->child drop.
        if (isDescendant(draggedId, droppedOnId)) {
            toast(Severity.Warning, intl.formatMessage({ id: "element_cannot_move_into_child" }))
            setDropPosition('inside')
            return
        }

        const draggedElement = getElementFromStore(draggedId)
        const droppedElement = getElementFromStore(droppedOnId)

        if (!draggedElement || !droppedElement) {
            setDropPosition('inside')
            return
        }

        const draggedContext = resolveCollectionContext(draggedId)
        const droppedContext = resolveCollectionContext(droppedOnId)

        // Determine if this is a same-level reorder or cross-level move
        if (
            draggedContext.parentPath === droppedContext.parentPath &&
            draggedContext.collectionKey === droppedContext.collectionKey &&
            dropPosition !== 'inside'
        ) {
            reorderOnSameLevel(
                draggedElement,
                droppedElement,
                draggedContext.parentPath,
                draggedContext.collectionKey,
            )
        } else {
            moveElementToNewParent(draggedId, droppedOnId, draggedElement, droppedElement)
        }

        setDropPosition('inside')
    }

    // Reorder within the same sibling collection.
    const reorderOnSameLevel = (
        draggedElement: any,
        droppedElement: any,
        parentPath: string,
        collectionKey: CollectionKey,
    ) => {
        const root = getElementFromStore("")
        
        const updatedRoot = updateNodeAtPath(root, parentPath, (parent) => {
            const siblings = parent?.[collectionKey] || []
            if (!Array.isArray(siblings)) return parent

            // Find indices of dragged and dropped elements
            const draggedIndex = siblings.findIndex(
                (el: any) => el.name === draggedElement.name && el.sort === draggedElement.sort && el.id === draggedElement.id
            )
            const droppedIndex = siblings.findIndex(
                (el: any) => el.name === droppedElement.name && el.sort === droppedElement.sort && el.id === droppedElement.id
            )

            if (draggedIndex === -1 || droppedIndex === -1) return parent

            // Compute final insert index.
            let targetIndex = droppedIndex
            if (dropPosition === 'after') targetIndex++
            if (draggedIndex < targetIndex) targetIndex--

            // Move element in copied array.
            const elementsCopy = [...siblings]
            const [movedElement] = elementsCopy.splice(draggedIndex, 1)
            elementsCopy.splice(targetIndex, 0, movedElement)

            // Keep existing sort slots, only swap order.
            const sortValues = siblings.map((el: any) => el.sort).sort((a: number, b: number) => a - b)
            const updatedElements = elementsCopy.map((el: any, index: number) => ({
                ...el,
                sort: sortValues[index]
            }))

            return { ...parent, [collectionKey]: updatedElements }
        })

        // Persist full tree in one store update.
        editDetailData({
            scenarioMixinName: props.scenarioMixinName,
            version: props.version,
            indexes: "",
            newEl: updatedRoot,
        })
        
        toast(Severity.None, "element_moved")
        if (props.setUpdate) props.setUpdate(Date.now())
    }

    // Move across parents or collections.
    const moveElementToNewParent = (
        draggedId: string,
        droppedOnId: string,
        draggedElement: any,
        droppedElement: any
    ) => {
        const draggedContext = resolveCollectionContext(draggedId)
        const droppedContext = resolveCollectionContext(droppedOnId)

        let root = getElementFromStore("")
        let movedElement: any = null

        // Remove element from source parent
        root = updateNodeAtPath(root, draggedContext.parentPath, (parent) => {
            const siblings = parent?.[draggedContext.collectionKey] || []
            if (!Array.isArray(siblings)) return parent

            const draggedIndex = siblings.findIndex(
                (el: any) => el.name === draggedElement.name && el.sort === draggedElement.sort && el.id === draggedElement.id
            )

            if (draggedIndex === -1) return parent

            const elementsCopy = [...siblings]
            const [removed] = elementsCopy.splice(draggedIndex, 1)
            movedElement = removed

            // Reassign sort values after removal
            const sortValues = elementsCopy.map((el: any) => el.sort).sort((a: number, b: number) => a - b)
            const updatedElements = elementsCopy.map((el: any, index: number) => ({
                ...el,
                sort: sortValues[index]
            }))

            return { ...parent, [draggedContext.collectionKey]: updatedElements }
        })

        if (!movedElement) return

        // Insert at target location.
        if (dropPosition === 'inside') {
            // Append as child of dropped element.
            root = updateNodeAtPath(root, droppedOnId, (target) => {
                const targetElements = target.elements || []
                const newSort = targetElements.length > 0 
                    ? Math.max(...targetElements.map((el: any) => el.sort)) + 1 
                    : 0
                return {
                    ...target,
                    elements: [...targetElements, { ...movedElement, sort: newSort }]
                }
            })
        } else {
            // Insert as sibling before/after dropped item.
            root = updateNodeAtPath(root, droppedContext.parentPath, (parent) => {
                const siblings = parent?.[droppedContext.collectionKey] || []
                if (!Array.isArray(siblings)) return parent

                const droppedIndex = siblings.findIndex(
                    (el: any) => el.name === droppedElement.name && el.sort === droppedElement.sort && el.id === droppedElement.id
                )

                if (droppedIndex === -1) return parent

            const insertIndex = dropPosition === 'after' ? droppedIndex + 1 : droppedIndex
            const elementsCopy = [...siblings]
            elementsCopy.splice(insertIndex, 0, movedElement)

            // Rebuild ordered sort values.
            const sortValues = elementsCopy.map((el: any) => el.sort).sort((a: number, b: number) => a - b)

            const updatedElements = elementsCopy.map((el: any, index: number) => ({
                ...el,
                sort: sortValues[index]
            }))

            return { ...parent, [droppedContext.collectionKey]: updatedElements }
            })
        }

        // Single atomic update to prevent duplication
        editDetailData({
            scenarioMixinName: props.scenarioMixinName,
            version: props.version,
            indexes: "",
            newEl: root,
        })

        toast(Severity.None, "element_moved")
        if (props.setUpdate) props.setUpdate(Date.now())
    }

    const getDropIndicatorStyle = () => {
        if (!isOver) return {}
        
        if (dropPosition === 'before') {
            return { borderTop: '2px solid #0078d4' }
        } else if (dropPosition === 'after') {
            return { borderBottom: '2px solid #0078d4' }
        } else {
            return { backgroundColor: 'rgba(0, 120, 215, 0.1)' }
        }
    }

    // Merge base item style with drop visuals.
    const itemStyle = {
        opacity: isDragging ? 0.5 : props.dimmed ? 0.45 : 1,
        cursor: "grab",
        transition: 'all 0.2s ease',
        ...getDropIndicatorStyle()
    }

    return (
        <TreeItemCustom
            id={props.id}
            title={props.title}
            selected={props.selected}
            navigated={props.navigated}
            content={props.content}
            expanded={props.expanded} 
            draggable
            onDragStart={handleDragStart}
            onDragEnd={handleDragEnd}
            onDragOver={handleDragOver}
            onDragLeave={handleDragLeave}
            onDrop={handleDrop}
            style={itemStyle}
        >
            {props.children}
        </TreeItemCustom>
    )
}