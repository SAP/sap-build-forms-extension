import { ControlProps } from "./Control"
import { calculateColspan } from "./utils"

export default function (props: ControlProps) {
    const { def } = props

    return <div className={" " + calculateColspan(def)} aria-hidden="true"></div>
}
