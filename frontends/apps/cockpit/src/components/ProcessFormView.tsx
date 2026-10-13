import { useEffect, useState } from "react"
import { useMessages } from "commons"
import { useVisualStore } from "../state/visual"
import { useProcessStore } from "../state/processes"

export default function () {
    const process = useVisualStore((state) => state.selectedProcess)
    const getDisplayVariant = useProcessStore((state) => state.getDisplayVariant)
    const messages = useMessages()
    const [variant, setVariant] = useState<string | null>(null)

    useEffect(() => {
        if (!process) return
        setVariant(null)
        getDisplayVariant(messages, process.id).then((v) => setVariant(v ?? ""))
    }, [process?.id])

    if (!process || variant === null) return <></>

    return (
        <iframe
            is="x-frame-bypass"
            src={`${process.scenarioUrl}/show/${process.id}/${variant}`}
            style={{ width: "100%", height: "calc(100vh - 150px)", border: "none" }}
        ></iframe>
    )
}
