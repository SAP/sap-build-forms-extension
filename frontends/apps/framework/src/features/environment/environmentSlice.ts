import { createSlice, PayloadAction } from "@reduxjs/toolkit"
import { EnvironmentState } from "../states"

const initialState: EnvironmentState = {
    screenWidth: window.innerWidth,
    screenHeight: window.innerHeight,
    isPortraitMode: window.innerHeight > window.innerWidth,
}

export const environmentSlice = createSlice({
    name: "environment",
    initialState,
    reducers: {
        updateScreen: (state, action: PayloadAction<undefined>) => {
            state.screenWidth = window.innerWidth
            state.screenHeight = window.innerHeight
            state.isPortraitMode = window.innerHeight > window.innerWidth
            // console.log(`Updated screen size to ${state.screenWidth}x${state.screenHeight}`)
        },
    },
})

export const { updateScreen } = environmentSlice.actions
