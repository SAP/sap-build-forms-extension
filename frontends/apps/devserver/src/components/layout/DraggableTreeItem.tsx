import { ReactNode, useState } from "react"
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
}

export default function DraggableTreeItem(props: Props) {
    const [isDragging, setIsDragging] = useState(false)
    const [isOver, setIsOver] = useState(false)
    const [dropPosition, setDropPosition] = useState<'before' | 'inside' | 'after'>('inside')
    const editDetailData = useElementsStore((state) => state.editDetailData)
    const elements = useElementsStore((state) => state.elements)
    const { toast } = useMessages()

    // Retrieves an element from the store by navigating through the tree structure
    // using the provided index path (e.g., "0x1x2" means element[0].elements[1].elements[2])
    const getElementFromStore = (indexPath: string): any => {
        const indexes = indexPath.split("x").filter((item) => item)

        // Find the root scenario/mixin
        const scenarioIndex = props.scenarioMixinName === "Scenario"
            ? elements.findIndex((el) => el.version === props.version && "defaultLanguage" in el)
            : elements.findIndex((el) => el.version === props.version && el.name === props.scenarioMixinName)
        
        if (scenarioIndex < 0) return null

        let current: any = elements[scenarioIndex]
        
        // Empty path means return the root scenario itself
        if (indexes.length === 0) return current

        // Navigate through the tree using the index path
        for (let i = 0; i < indexes.length; i++) {
            if (!current) return null
            
            const index = indexes[i]
            const nextMarker = indexes[i + 1]
            
            // Handle special markers (f=footer, h=header, t=toolbar, l=left, r=right)
            if (nextMarker === "f") {
                current = current.elements?.[index]?.footer
                i++ 
            } else if (nextMarker === "h") {
                current = current.elements?.[index]?.headerSegment
                i++
            } else if (nextMarker === "t") {
                current = current.elements?.[index]?.toolbar
                i++
            } else if (nextMarker === "l") {
                current = current.elements?.[index]?.leftElements
                i++
            } else if (nextMarker === "r") {
                current = current.elements?.[index]?.rightElements
                i++
            } else if (nextMarker !== undefined) {
                current = current.elements?.[index]
            } else {
                return current.elements?.[index]
            }
        }

        return current
    }

    // Deep clones the tree and updates a nested element at the specified path
    const updateNestedElement = (root: any, path: string, updater: (element: any) => any): any => {
        if (!path) return updater(root)

        const indexes = path.split("x").filter((item) => item)
        let current = { ...root }
        let parent = current

        // Navigate through the path, cloning each level
        for (let i = 0; i < indexes.length; i++) {
            const indexStr = indexes[i]
            const index = parseInt(indexStr, 10)
            const nextMarker = indexes[i + 1]

            // Handle special markers with deep cloning
            if (nextMarker === "f") {
                const newElements = [...(parent.elements || [])]
                const newElement = { ...newElements[index] }
                newElement.footer = { ...newElement.footer }
                newElements[index] = newElement
                parent.elements = newElements
                parent = newElement.footer
                i++
            } else if (nextMarker === "h") {
                const newElements = [...(parent.elements || [])]
                const newElement = { ...newElements[index] }
                newElement.headerSegment = { ...newElement.headerSegment }
                newElements[index] = newElement
                parent.elements = newElements
                parent = newElement.headerSegment
                i++
            } else if (nextMarker === "t") {
                const newElements = [...(parent.elements || [])]
                const newElement = { ...newElements[index] }
                newElement.toolbar = { ...newElement.toolbar }
                newElements[index] = newElement
                parent.elements = newElements
                parent = newElement.toolbar
                i++
            } else if (nextMarker === "l") {
                const newElements = [...(parent.elements || [])]
                const newElement = { ...newElements[index] }
                newElement.leftElements = { ...newElement.leftElements }
                newElements[index] = newElement
                parent.elements = newElements
                parent = newElement.leftElements
                i++
            } else if (nextMarker === "r") {
                const newElements = [...(parent.elements || [])]
                const newElement = { ...newElements[index] }
                newElement.rightElements = { ...newElement.rightElements }
                newElements[index] = newElement
                parent.elements = newElements
                parent = newElement.rightElements
                i++
            } else {
                const newElements = [...(parent.elements || [])]
                if (i === indexes.length - 1) {
                    newElements[index] = updater(newElements[index])
                    parent.elements = newElements
                } else {
                    // Continue navigating deeper
                    newElements[index] = { ...newElements[index] }
                    parent.elements = newElements
                    parent = newElements[index]
                }
            }
        }

        return current
    }

    // Checks if parent element should be dropped into its own child
    const isDescendant = (parentPath: string, childPath: string): boolean => {
        if (!parentPath || !childPath) return false
        return childPath.startsWith(parentPath + "x")
    }

    const handleDragStart = (e: any) => {
        e.stopPropagation()
        setIsDragging(true)
        e.dataTransfer.effectAllowed = "move"
        e.dataTransfer.setData("text/plain", props.id)
    }

    const handleDragEnd = () => {
        setIsDragging(false)
        setDropPosition('inside')
    }

    // Determines drop position based on mouse position: 
    // - Top 25%: drop before
    // - Bottom 25%: drop after
    // - Middle 50%: drop inside
    const handleDragOver = (e: any) => {
        e.preventDefault()
        e.stopPropagation()
        
        const rect = e.currentTarget.getBoundingClientRect()
        const y = e.clientY - rect.top
        const height = rect.height
        
        // Calculate drop position based on cursor location
        if (y < height * 0.25) {
            setDropPosition('before')
        } else if (y > height * 0.75) {
            setDropPosition('after')
        } else {
            setDropPosition('inside')
        }
        e.dataTransfer.dropEffect = "move"
        setIsOver(true)
    }

    const handleDragLeave = (e: any) => {
        // Only reset if actually leaving the element
        if (!e.currentTarget.contains(e.relatedTarget)) {
            setIsOver(false)
            setDropPosition('inside')
        }
    }

    // Drop Handler: determines whether to reorder on same level or move to a different level
    const handleDrop = (e: any) => {
        e.preventDefault()
        e.stopPropagation()
        setIsOver(false)

        const draggedId = e.dataTransfer.getData("text/plain")
        const droppedOnId = props.id

        // Can't drop on itself
        if (draggedId === droppedOnId) {
            setDropPosition('inside')
            return
        }

        // Prevent dropping parent into its own descendant
        if (isDescendant(draggedId, droppedOnId)) {
            toast(Severity.Warning, "Cannot move element into its own child")
            setDropPosition('inside')
            return
        }

        const draggedElement = getElementFromStore(draggedId)
        const droppedElement = getElementFromStore(droppedOnId)

        if (!draggedElement || !droppedElement) {
            setDropPosition('inside')
            return
        }

        const draggedIndexes = draggedId.split("x").filter((item: string) => item)
        const droppedIndexes = droppedOnId.split("x").filter((item: string) => item)
        const draggedParentPath = draggedIndexes.length <= 1 ? "" : draggedIndexes.slice(0, -1).join("x")
        const droppedParentPath = droppedIndexes.length <= 1 ? "" : droppedIndexes.slice(0, -1).join("x")

        // Determine if this is a same-level reorder or cross-level move
        if (draggedParentPath === droppedParentPath && dropPosition !== 'inside') {
            reorderOnSameLevel(draggedId, droppedOnId, draggedElement, droppedElement, draggedParentPath)
        } else {
            moveElementToNewParent(draggedId, droppedOnId, draggedElement, droppedElement)
        }

        setDropPosition('inside')
    }

    // Reorders elements on the same level (same parent)
    const reorderOnSameLevel = (
        draggedId: string,
        droppedOnId: string,
        draggedElement: any,
        droppedElement: any,
        parentPath: string
    ) => {
        const root = getElementFromStore("")
        
        const updatedRoot = updateNestedElement(root, parentPath, (parent) => {
            if (!parent.elements) return parent

            // Find indices of dragged and dropped elements
            const draggedIndex = parent.elements.findIndex(
                (el: any) => el.name === draggedElement.name && el.sort === draggedElement.sort && el.id === draggedElement.id
            )
            const droppedIndex = parent.elements.findIndex(
                (el: any) => el.name === droppedElement.name && el.sort === droppedElement.sort && el.id === droppedElement.id
            )

            if (draggedIndex === -1 || droppedIndex === -1) return parent

            // Calculate target position
            let targetIndex = droppedIndex
            if (dropPosition === 'after') targetIndex++
            if (draggedIndex < targetIndex) targetIndex--

            // Move element to new position
            const elementsCopy = [...parent.elements]
            const [movedElement] = elementsCopy.splice(draggedIndex, 1)
            elementsCopy.splice(targetIndex, 0, movedElement)

            // Reassign sort values to maintain order
            const sortValues = parent.elements.map((el: any) => el.sort).sort((a: number, b: number) => a - b)
            const updatedElements = elementsCopy.map((el: any, index: number) => ({
                ...el,
                sort: sortValues[index]
            }))

            return { ...parent, elements: updatedElements }
        })

        // Single update to the store
        editDetailData({
            scenarioMixinName: props.scenarioMixinName,
            version: props.version,
            indexes: "",
            newEl: updatedRoot,
        })
        
        toast(Severity.None, "element_moved")
        if (props.setUpdate) props.setUpdate(Date.now())
    }

    const moveElementToNewParent = (
        draggedId: string,
        droppedOnId: string,
        draggedElement: any,
        droppedElement: any
    ) => {
        const draggedIndexes = draggedId.split("x").filter((item: string) => item)
        const droppedIndexes = droppedOnId.split("x").filter((item: string) => item)
        const draggedParentPath = draggedIndexes.length <= 1 ? "" : draggedIndexes.slice(0, -1).join("x")
        const droppedParentPath = droppedIndexes.length <= 1 ? "" : droppedIndexes.slice(0, -1).join("x")

        let root = getElementFromStore("")
        let movedElement: any = null

        // Remove element from source parent
        root = updateNestedElement(root, draggedParentPath, (parent) => {
            if (!parent.elements) return parent

            const draggedIndex = parent.elements.findIndex(
                (el: any) => el.name === draggedElement.name && el.sort === draggedElement.sort && el.id === draggedElement.id
            )

            if (draggedIndex === -1) return parent

            const elementsCopy = [...parent.elements]
            const [removed] = elementsCopy.splice(draggedIndex, 1)
            movedElement = removed

            // Reassign sort values after removal
            const sortValues = parent.elements.map((el: any) => el.sort).sort((a: number, b: number) => a - b)
            const updatedElements = elementsCopy.map((el: any, index: number) => ({
                ...el,
                sort: sortValues[index] !== undefined ? sortValues[index] : index
            }))

            return { ...parent, elements: updatedElements }
        })

        if (!movedElement) return

        // Add element to target location
        if (dropPosition === 'inside') {
            // Add as a child of the dropped element
            root = updateNestedElement(root, droppedOnId, (target) => {
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
            // Add as a sibling (before or after the dropped element)
            root = updateNestedElement(root, droppedParentPath, (parent) => {
                if (!parent.elements) return parent

                const droppedIndex = parent.elements.findIndex(
                    (el: any) => el.name === droppedElement.name && el.sort === droppedElement.sort && el.id === droppedElement.id
                )

                if (droppedIndex === -1) return parent

                const insertIndex = dropPosition === 'after' ? droppedIndex + 1 : droppedIndex
                const elementsCopy = [...parent.elements]
                elementsCopy.splice(insertIndex, 0, movedElement)

                // Reassign sort values with the new element included
                const sortValues = parent.elements.map((el: any) => el.sort).sort((a: number, b: number) => a - b)
                const newSortValue = sortValues.length > 0 ? Math.max(...sortValues) + 1 : 0
                sortValues.push(newSortValue)
                sortValues.sort((a: number, b: number) => a - b)

                const updatedElements = elementsCopy.map((el: any, index: number) => ({
                    ...el,
                    sort: sortValues[index]
                }))

                return { ...parent, elements: updatedElements }
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

    const itemStyle = {
        opacity: isDragging ? 0.5 : 1,
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