import { Button, FlexBox, Input, TableCell, Text } from "@ui5/webcomponents-react"
import { useState } from "react"

interface Props {
    value: string
    setValue: (e: any) => void
}

export default function TextEdit(props: Props) {
    const [edit, setEdit] = useState(false)

    return (
        <>
            <TableCell>
                <>
                    {edit && (
                        <Input
                            style={{ width: "100%" }}
                            placeholder={props.value}
                            value={props.value}
                            onInput={(e) => {
                                props.setValue(
                                    e.target.attributes.getNamedItem("value")!.nodeValue!,
                                )
                            }}
                        />
                    )}
                    {!edit && <Text>{props.value}</Text>}
                </>
            </TableCell>
            <TableCell>
                <FlexBox justifyContent="End" style={{ width: "100%" }}>
                    <Button
                        style={{ marginLeft: 10 }}
                        onClick={() => {
                            setEdit(!edit)
                        }}
                        icon={edit ? "display" : "edit"}
                        design="Transparent"
                    />
                </FlexBox>
            </TableCell>
        </>
    )
}
