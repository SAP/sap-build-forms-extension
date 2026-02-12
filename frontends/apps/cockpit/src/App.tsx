import { BrowserRouter, Route, Routes } from "react-router-dom"

import { NotFound } from "commons"

import CockpitPage from "./components/CockpitPage"

function App() {
    return (
        <BrowserRouter>
            <Routes>
                <Route path="" element={<CockpitPage />} />
                <Route path="/cockpit/*" element={<CockpitPage />} />

                <Route path="*" element={<NotFound />} />
            </Routes>
        </BrowserRouter>
    )
}

export default App
