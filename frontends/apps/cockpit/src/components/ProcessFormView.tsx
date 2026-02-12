import { useVisualStore } from "../state/visual"

export default function () {
    const process = useVisualStore((state) => state.selectedProcess)
    return (
        <iframe
            is="x-frame-bypass"
            src={`${process?.scenarioUrl}/show/${process?.id}/${process?.showState}`}
            style={{ width: "100%", height: "calc(100vh - 200px)", border: "none" }}
        ></iframe>
    )
}
