import { create } from "zustand"
import { immer } from "zustand/middleware/immer"
import { Mixin, Scenario } from "../utils/scenarioDefinitions"

type State = {
    elements: Array<Scenario | Mixin>
}

type Action = {
    insertElements: (payload: Record<string, Scenario | Mixin>) => void
    insertElementsMixin: (payload: Record<string, Record<string, Scenario | Mixin>>) => void
    removeElement: (payload: { scenarioMixinName: string; version: number; indexes: string }) => void
    removeElements: () => void
    addElement: (payload: { 
        scenarioMixinName: string; 
        version: number; 
        indexes: string; 
        newEl: any;
        position?: string 
    }) => void
    editBaseData: (payload: {
        scenarioMixinName: string;
        version: number;
        name?: string;
        accessObject?: string;
        basePackage?: string;
        root?: string;
        defaultLanguage?: string;
        active?: boolean;
    }) => void
    editDetailData: (payload: {
        scenarioMixinName: string;
        version: number;
        indexes: string;
        newEl: any;
    }) => void
    editTexts: (payload: {
        scenarioMixinName: string;
        version: number;
        texts: any;
    }) => void
}

const useElementsStore = create<State & Action>()(
    immer((set) => ({
        elements: [],

        insertElements: (payload: Record<string, Scenario | Mixin>) => {
            const array = Object.keys(payload).map((key) => payload[key])
            set((state) => {
                for (let i = 0; i < array.length; i++) {
                    if (
                        !state.elements.find(
                            (element: Scenario | Mixin) =>
                                element.name === array[i].name && element.version === array[i].version,
                        )
                    ) {
                        state.elements.push(array[i])
                    }
                }
            })
        },

        insertElementsMixin: (payload: Record<string, Record<string, Scenario | Mixin>>) => {
            const array = Object.keys(payload).map((key) => payload[key])
            set((state) => {
                for (let i = 0; i < array.length; i++) {
                    const innerArray = Object.keys(array[i]).map((key) => array[i][key])
                    for (let j = 0; j < innerArray.length; j++) {
                        if (
                            !state.elements.find(
                                (element) =>
                                    element.name === innerArray[j].name &&
                                    element.version === innerArray[j].version,
                            )
                        ) {
                            state.elements.push(innerArray[j])
                        }
                    }
                }
            })
        },

        removeElement: (payload: { scenarioMixinName: string; version: number; indexes: string }) => {
        set((state) => {
            let scenarioIndex
            if (payload.scenarioMixinName == "Scenario") {
                scenarioIndex = state.elements.findIndex(
                    (element) =>
                        element.version === payload.version &&
                        "defaultLanguage" in element,
                )
            } else {
                scenarioIndex = state.elements.findIndex(
                    (element) =>
                        element.version === payload.version &&
                        element.name === payload.scenarioMixinName,
                )
            }

            if (scenarioIndex < 0) return

            const indexes = payload.indexes.split("x").filter((item: any) => item)
            let current: any = state.elements[scenarioIndex]
            let x = 0
            
            while (x < indexes.length - 2) {
                if (indexes[x + 1] == "f") {
                    if (indexes[x + 2] == "l") {
                        if (x + 3 < indexes.length - 1) {
                            current = current.elements![indexes[x]].footer!.leftElements![indexes[x + 3]]
                            x += 3
                        } else {
                            current.elements![indexes[x]].footer!.leftElements!.splice(indexes[x + 3], 1)
                            return
                        }
                    } else if (indexes[x + 2] == "r") {
                        if (x + 3 < indexes.length - 1) {
                            current = current.elements![indexes[x]].footer!.rightElements![indexes[x + 3]]
                            x += 3
                        } else {
                            current.elements![indexes[x]].footer!.rightElements!.splice(indexes[x + 3], 1)
                            return
                        }
                    } else {
                        current = current.elements![indexes[x]].footer!
                        x += 1
                    }
                } else if (indexes[x + 1] == "h") {
                    current = current.elements![indexes[x]].headerSegment!
                    x += 1
                } else if (indexes[x + 1] == "t") {
                    if (indexes[x + 2] == "l") {
                        if (x + 3 < indexes.length - 1) {
                            current = current.elements![indexes[x]].toolbar!.leftElements![indexes[x + 3]]
                            x += 3
                        } else {
                            current.elements![indexes[x]].toolbar!.leftElements!.splice(indexes[x + 3], 1)
                            return
                        }
                    } else if (indexes[x + 2] == "r") {
                        if (x + 3 < indexes.length - 1) {
                            current = current.elements![indexes[x]].toolbar!.rightElements![indexes[x + 3]]
                            x += 3
                        } else {
                            current.elements![indexes[x]].toolbar!.rightElements!.splice(indexes[x + 3], 1)
                            return
                        }
                    } else {
                        current = current.elements![indexes[x]].toolbar!
                        x += 1
                    }
                } else {
                    current = current.elements![indexes[x]]
                }
                x += 1
            }

            if (x == indexes.length - 1) {
                current.elements!.splice(indexes[x], 1)
            } else {
                if (indexes[x + 1] == "f") {
                    delete current.elements![indexes[x]].footer
                } else if (indexes[x + 1] == "h") {
                    delete current.elements![indexes[x]].headerSegment
                } else if (indexes[x + 1] == "t") {
                    delete current.elements![indexes[x]].toolbar
                } else {
                    current.elements![indexes[x]].elements.splice(indexes[x + 1], 1)
                }
            }
        })
    },

        removeElements: () => set((state) => { state.elements = [] }),

        addElement: (payload) => {
        set((state) => {
            let scenarioIndex
            if (payload.scenarioMixinName == "Scenario") {
                scenarioIndex = state.elements.findIndex(
                    (element) =>
                        element.version === payload.version &&
                        "defaultLanguage" in element,
                )
            } else {
                scenarioIndex = state.elements.findIndex(
                    (element) =>
                        element.version === payload.version &&
                        element.name === payload.scenarioMixinName,
                )
            }

            if (scenarioIndex < 0) return

            const indexes = payload.indexes.split("x").filter((item: any) => item)
            let current: any = state.elements[scenarioIndex]
            let x = 0

            while (x < indexes.length) {
                if (indexes[x + 1] == "f") {
                    if (indexes[x + 2] == "l") {
                        if (x + 2 == indexes.length - 1) {
                            current.elements![indexes[x]].footer!.leftElements!.push(payload.newEl)
                            return
                        } else {
                            current = current.elements![indexes[x]].footer!.leftElements![indexes[x + 3]]
                            x += 2
                        }
                    } else if (indexes[x + 2] == "r") {
                        if (x + 2 == indexes.length - 1) {
                            current.elements![indexes[x]].footer!.rightElements!.push(payload.newEl)
                            return
                        } else {
                            current = current.elements![indexes[x]].footer!.rightElements![indexes[x + 3]]
                            x += 3
                        }
                    } else {
                        current = current.elements![indexes[x]].footer!
                        x += 1
                    }
                } else if (indexes[x + 1] == "h") {
                    current = current.elements![indexes[x]].headerSegment!
                    x += 1
                } else if (indexes[x + 1] == "t") {
                    if (indexes[x + 2] == "l") {
                        if (x + 3 < indexes.length - 1) {
                            current = current.elements![indexes[x]].toolbar!.leftElements![indexes[x + 3]]
                            x += 3
                        } else {
                            current.elements![indexes[x]].toolbar!.leftElements!.push(payload.newEl)
                            return
                        }
                    } else if (indexes[x + 2] == "r") {
                        if (x + 3 < indexes.length - 1) {
                            current = current.elements![indexes[x]].toolbar!.rightElements![indexes[x + 3]]
                            x += 3
                        } else {
                            current.elements![indexes[x]].toolbar!.rightElements!.push(payload.newEl)
                            return
                        }
                    } else {
                        current = current.elements![indexes[x]].toolbar!
                        x += 2
                    }
                } else {
                    current = current.elements![indexes[x]]
                }
                x += 1
            }

            current.elements!.push(payload.newEl)
        })
    },

        editBaseData: (payload) => {
        set((state) => {
            let i
            if (payload.scenarioMixinName == "Scenario") {
                i = state.elements.findIndex(
                    (element) =>
                        element.version === payload.version && "defaultLanguage" in element,
                )
            } else {
                i = state.elements.findIndex(
                    (element) =>
                        element.version === payload.version &&
                        element.name === payload.scenarioMixinName,
                )
            }
            
            if (i < 0) return

            if ("defaultLanguage" in state.elements[i]) {
                if (payload.name !== undefined) state.elements[i].name = payload.name
                if (payload.accessObject !== undefined) state.elements[i].accessObject = payload.accessObject
                if (payload.basePackage !== undefined) state.elements[i].basePackage = payload.basePackage
                if (payload.root !== undefined) (state.elements[i] as any).root = payload.root
                if (payload.defaultLanguage !== undefined) (state.elements[i] as any).defaultLanguage = payload.defaultLanguage
                if (payload.active !== undefined) (state.elements[i] as any).active = payload.active
            } else {
                if (payload.name !== undefined) state.elements[i].name = payload.name
                if (payload.accessObject !== undefined) state.elements[i].accessObject = payload.accessObject
                if (payload.basePackage !== undefined) state.elements[i].basePackage = payload.basePackage
            }
        })
    },

        editDetailData: (payload) => {
        set((state) => {
            let scenarioIndex
            if (payload.scenarioMixinName == "Scenario") {
                scenarioIndex = state.elements.findIndex(
                    (element) =>
                        element.version === payload.version &&
                        "defaultLanguage" in element,
                )
            } else {
                scenarioIndex = state.elements.findIndex(
                    (element) =>
                        element.version === payload.version &&
                        element.name === payload.scenarioMixinName,
                )
            }

            if (scenarioIndex < 0) return

            const indexes = payload.indexes.split("x").filter((item: any) => item)
            
            // Handle root-level update (empty indexes)
            if (indexes.length === 0) {
                state.elements[scenarioIndex] = {
                    ...state.elements[scenarioIndex],
                    ...payload.newEl
                }
                return
            }
            
            let current: any = state.elements[scenarioIndex]
            let x = 0

            while (x < indexes.length - 1) {
                if (indexes[x + 1] == "f") {
                    if (indexes[x + 2] == "l") {
                        if (x + 3 < indexes.length - 1) {
                            current = current.elements![indexes[x]].footer!.leftElements![indexes[x + 3]]
                            x += 3
                        } else {
                            current.elements![indexes[x]].footer!.leftElements!.splice(
                                indexes[x + 3],
                                1,
                                payload.newEl,
                            )
                            return
                        }
                    } else if (indexes[x + 2] == "r") {
                        if (x + 3 < indexes.length - 1) {
                            current = current.elements![indexes[x]].footer!.rightElements![indexes[x + 3]]
                            x += 3
                        } else {
                            current.elements![indexes[x]].footer!.rightElements!.splice(
                                indexes[x + 3],
                                1,
                                payload.newEl,
                            )
                            return
                        }
                    } else {
                        if (x + 1 == indexes.length - 1) {
                            current.elements![indexes[x]].footer! = payload.newEl
                            return
                        } else {
                            current = current.elements![indexes[x]].footer!
                            x += 1
                        }
                    }
                } else if (indexes[x + 1] == "h") {
                    if (x + 1 == indexes.length - 1) {
                        current.elements![indexes[x]].headerSegment! = payload.newEl
                        return
                    } else {
                        current = current.elements![indexes[x]].headerSegment!
                        x += 1
                    }
                } else if (indexes[x + 1] == "t") {
                    if (indexes[x + 2] == "l") {
                        if (x + 3 < indexes.length - 1) {
                            current = current.elements![indexes[x]].toolbar!.leftElements![indexes[x + 3]]
                            x += 3
                        } else {
                            current.elements![indexes[x]].toolbar!.leftElements!.splice(
                                indexes[x + 3],
                                1,
                                payload.newEl,
                            )
                            return
                        }
                    } else if (indexes[x + 2] == "r") {
                        if (x + 3 < indexes.length - 1) {
                            current = current.elements![indexes[x]].toolbar!.rightElements![indexes[x + 3]]
                            x += 3
                        } else {
                            current.elements![indexes[x]].toolbar!.rightElements!.splice(
                                indexes[x + 3],
                                1,
                                payload.newEl,
                            )
                            return
                        }
                    } else {
                        if (x + 1 == indexes.length - 1) {
                            current.elements![indexes[x]].toolbar! = payload.newEl
                            return
                        } else {
                            current = current.elements![indexes[x]].toolbar!
                            x += 2
                        }
                    }
                } else {
                    current = current.elements![indexes[x]]
                }
                x += 1
            }

            current.elements!.splice(indexes[x], 1, payload.newEl)
        })
    },

        editTexts: (payload) => {
        set((state) => {
            let i
            if (payload.scenarioMixinName == "Scenario") {
                i = state.elements.findIndex(
                    (element) =>
                        element.version === payload.version && "defaultLanguage" in element,
                )
            } else {
                i = state.elements.findIndex(
                    (element) =>
                        element.version === payload.version &&
                        element.name === payload.scenarioMixinName,
                )
            }

            if (i < 0) return

            state.elements[i].texts = payload.texts
        })
    },
    }))
)

export default useElementsStore
