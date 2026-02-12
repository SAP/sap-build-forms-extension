import { ControlProps } from "./Control"
import ControlGridContainer from "./ControlGridContainer"

interface Props extends ControlProps {}

export default function (props: Props) {
    return <ControlGridContainer {...props} />
}
