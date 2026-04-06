import { Elem } from "./scenarioDefinitions"

function normalizeVariant(value: string): string {
    return value.trim().toUpperCase()
}

function extractVariantFromExpressionPart(part: string): string[] {
    const trimmed = part.trim()
    if (!trimmed) {
        return []
    }

    if (!trimmed.includes("*")) {
        return [trimmed]
    }

    const starParts = trimmed
        .split("*")
        .map((token) => token.trim())
        .filter(Boolean)

    if (starParts.length === 0) {
        return []
    }

    // Patterns like "*BAU*" should keep the enclosed value.
    if (trimmed.startsWith("*") && trimmed.endsWith("*")) {
        return starParts
    }

    // Patterns like "Antragsteller*BAU10" carry the variant in the trailing segment.
    return [starParts[starParts.length - 1]]
}

export function extractVariantsFromVisible(visible?: string): string[] {
    if (!visible) {
        return []
    }

    const variants = visible
        .split(/[;\n\r]+/)
        .flatMap((part) => extractVariantFromExpressionPart(part))
        .map((token) => token.trim())
        .filter(Boolean)

    return Array.from(new Set(variants))
}

function walkElements(elements: Elem[] | undefined, visitor: (element: Elem) => void) {
    if (!elements) {
        return
    }

    elements.forEach((element) => {
        visitor(element)

        walkElements(element.elements, visitor)
        walkElements(element.leftElements, visitor)
        walkElements(element.rightElements, visitor)

        if (element.footer) {
            visitor(element.footer)
            walkElements(element.footer.elements, visitor)
            walkElements(element.footer.leftElements, visitor)
            walkElements(element.footer.rightElements, visitor)
        }

        if (element.headerSegment) {
            visitor(element.headerSegment)
            walkElements(element.headerSegment.elements, visitor)
        }

        if (element.toolbar) {
            visitor(element.toolbar)
            walkElements(element.toolbar.elements, visitor)
            walkElements(element.toolbar.leftElements, visitor)
            walkElements(element.toolbar.rightElements, visitor)
        }
    })
}

export function collectVariantsFromElements(elements: Elem[] | undefined): string[] {
    const variants = new Set<string>()

    walkElements(elements, (element) => {
        extractVariantsFromVisible(element.visible).forEach((variant) => variants.add(variant))
    })

    return Array.from(variants).sort((a, b) => a.localeCompare(b))
}

export function elementMatchesSelectedVariants(
    visible: string | undefined,
    selectedVariants: string[],
): boolean {
    if (!selectedVariants.length) {
        return true
    }

    const visibleVariants = extractVariantsFromVisible(visible)
    if (!visibleVariants.length) {
        return true
    }

    const normalizedVisible = visibleVariants.map(normalizeVariant)
    const normalizedSelected = selectedVariants.map(normalizeVariant)

    return normalizedSelected.some((selected) =>
        normalizedVisible.some(
            (visibleVariant) =>
                visibleVariant === selected ||
                visibleVariant.startsWith(selected) ||
                selected.startsWith(visibleVariant),
        ),
    )
}
