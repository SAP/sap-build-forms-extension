import { ReactNode, createContext, useContext } from "react"

type VariantFilterContextValue = {
    selectedVariants: string[]
}

const VariantFilterContext = createContext<VariantFilterContextValue>({
    selectedVariants: [],
})

interface ProviderProps {
    selectedVariants: string[]
    children: ReactNode
}

export function VariantFilterProvider({ selectedVariants, children }: ProviderProps) {
    return (
        <VariantFilterContext.Provider value={{ selectedVariants }}>
            {children}
        </VariantFilterContext.Provider>
    )
}

export function useVariantFilter() {
    return useContext(VariantFilterContext)
}
