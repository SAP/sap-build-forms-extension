import { useEffect, useState } from "react"
import { ElemForTable } from "../../utils/scenarioDefinitions"
import { BreadcrumbsItem } from "@ui5/webcomponents-react"

interface Props {
    item: ElemForTable
}

export default function BreadcrumbItemParents(props: Props) {
    const [items, setItems] = useState<ElemForTable[]>([])

    useEffect(() => {
        const collectItems = (item: ElemForTable) => {
            let items = []
            let current = item
            items.push(current)
            while (current.parent) {
                current = current.parent
                items.push(current)
            }
            return items
        }

        setItems(collectItems(props.item))
    }, [props.item])

    return (
        <>
            {items.toReversed().map((item) => {
                return (
                    <BreadcrumbsItem key={item.index} id={item.index}>
                        {item.name}
                    </BreadcrumbsItem>
                )
            })}
        </>
    )
}
