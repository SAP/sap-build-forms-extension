import { configureStore } from "@reduxjs/toolkit"
import { TypedUseSelectorHook, useDispatch, useSelector } from "react-redux"

import { sessionSlice } from "./sessions/sessionSlice"
import { valuehelpsSlice } from "./valuehelps/valuehelpsSlice"
import { environmentSlice } from "./environment/environmentSlice"

/**
 *
 * For non-serializable objects the serializableCheck is switched off. See
 * https://stackoverflow.com/questions/61704805/getting-an-error-a-non-serializable-value-was-detected-in-the-state-when-using
 * for details!
 */
const store = configureStore({
    reducer: {
        session: sessionSlice.reducer,
        valuehelps: valuehelpsSlice.reducer,
        environment: environmentSlice.reducer,
    },
    middleware: (getDefaultMiddleware) =>
        getDefaultMiddleware({
            serializableCheck: false,
        }),
})

// Infer the 'RootState' and 'AppDispatch' types from the store itself
export type RootState = ReturnType<typeof store.getState>
export type AppDispatch = typeof store.dispatch

// export of store
export default store

// Use throughout your app instead of plain `useDispatch` and `useSelector`
export const useAppDispatch = () => useDispatch<AppDispatch>()
export const useAppSelector: TypedUseSelectorHook<RootState> = useSelector
