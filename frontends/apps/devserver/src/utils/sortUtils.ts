import { Elem, Scenario } from "./scenarioDefinitions"

export function compare(a: Elem, b: Elem) {
    if (a.sort == undefined) {
        if (b.sort == undefined) {
            return 0
        } else {
            return -1
        }
    } else {
        if (b.sort == undefined) {
            return 1
        }
    }

    if (a.sort < b.sort) {
        return -1
    }
    if (a.sort > b.sort) {
        return 1
    }
    return 0
}

export function getNewIndex(
    parent: Elem | undefined,
    treeItemsShown: Scenario | null | undefined,
    otherEl: Elem | null,
    letter: String,
    element: string | undefined,
) {
    //Find the (unsorted) index position of the other element
    if (parent) {
        if (parent.type == "toolbar") {
            if (letter == "l") {
                var index = parent?.leftElements!.findIndex(
                    (x) => x.name === otherEl?.name && x.sort == otherEl?.sort,
                )
            } else if (letter == "r") {
                index = parent?.rightElements!.findIndex(
                    (x) => x.name === otherEl?.name && x.sort == otherEl?.sort,
                )
            } else {
                index = parent?.elements.findIndex(
                    (x) => x.name === otherEl?.name && x.sort == otherEl?.sort,
                )
            }
        } else {
            index = parent?.elements.findIndex(
                (x) => x.name === otherEl?.name && x.sort == otherEl?.sort,
            )
        }
    } else {
        index = treeItemsShown?.elements!.findIndex(
            (x) => x.name === otherEl?.name && x.sort == otherEl?.sort,
        )!
    }

    //get the indexes of the other element by switching the last index number with the previously filtered index
    var indexesBefore = /^(.+x)([^x]+x)$/.exec(element!)
    if (indexesBefore) {
        var newIndex = indexesBefore[1].toString() + index + "x"
    } else {
        newIndex = index.toString() + "x"
    }

    return newIndex
}

export function getItemsSorted(
    parent: Elem | undefined,
    treeItemsShown: Scenario | null | undefined,
    letter: String,
) {
    if (parent) {
        if (parent.type == "toolbar") {
            if (letter == "l") {
                itemsSorted2 = [...parent.leftElements!].sort(compare)
            } else if (letter == "r") {
                itemsSorted2 = [...parent.rightElements!].sort(compare)
            } else {
                itemsSorted2 = [...parent.elements].sort(compare)
            }
        } else {
            itemsSorted2 = [...parent.elements].sort(compare)
        }
    } else {
        var itemsSorted2: Elem[] = [...treeItemsShown?.elements!].sort(compare)
    }

    return itemsSorted2
}

export function sort(searchString: string, items: any[]) {
    if (searchString.trim().length > 0) {
        return items
            .map((item) => {
                if (item.original.name.toLocaleLowerCase().includes(searchString.toLowerCase())) {
                    return item
                }
                const newItem = { ...item }
                /*if (newItem.subRows) {
                    newItem.subRows = sort(searchString, newItem.subRows)
                }*/

                return containsChildSearchString(item.original, searchString) ? newItem : null
            })
            .filter(Boolean)
    } else {
        return items
    }
}

export function containsChildSearchString(child: Elem, searchString: string): boolean {
    if (child.name.toLocaleLowerCase().includes(searchString.toLowerCase())) {
        return true
    } else {
        if (
            child.elements.length > 0 ||
            child.toolbar ||
            child.footer ||
            child.leftElements ||
            child.rightElements
        ) {
            const has_search_child: boolean = child.elements.some((childElement) =>
                containsChildSearchString(childElement, searchString),
            )
            const has_toolbar_child: boolean = child.toolbar
                ? containsChildSearchString(child.toolbar, searchString)
                : false
            const has_footer_child: boolean = child.footer
                ? containsChildSearchString(child.footer, searchString)
                : false

            const has_search_left: boolean = child.leftElements
                ? child.leftElements.some((childElement) =>
                      containsChildSearchString(childElement, searchString),
                  )
                : false
            const has_search_right: boolean = child.rightElements
                ? child.rightElements.some((childElement) =>
                      containsChildSearchString(childElement, searchString),
                  )
                : false

            return (
                has_search_child ||
                has_toolbar_child ||
                has_footer_child ||
                has_search_left ||
                has_search_right
            )
        } else {
            return false
        }
    }
}
