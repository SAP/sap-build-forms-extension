import { Input } from "@ui5/webcomponents-react"
import { createUseStyles } from "react-jss"
import useElementsStore from "../../state/elements"

interface Props {
    postfix: string
    texts: any
    defaultLanguage: string | undefined
    currentName: string | undefined
    scenarioMixinName: string
    version: number
    setUpdate: (e: any) => void
}

const useStyles = createUseStyles({
    largeInput: {
        width: "100%",
    },
})

export default function StructureTabTextsInput(props: Props) {
    const classes = useStyles()
    const editTexts = useElementsStore((state) => state.editTexts)

    return (
        <Input
            value={
                props.texts![
                    props.defaultLanguage ?? (Object.keys(props.texts! ?? {}).sort()[0] as any)
                ]?.[`${props.currentName}${props.postfix}` as any] ?? ""
            }
            className={classes.largeInput}
            onInput={(e) => {
                var texts: any = JSON.parse(JSON.stringify(props.texts!))
                if (props.defaultLanguage) {
                    if (!texts![props.defaultLanguage as any]) {
                        texts![props.defaultLanguage as any] = {}
                    }
                    texts![props.defaultLanguage as any][`${props.currentName}${props.postfix}`] =
                        e.target.attributes.getNamedItem("value")!.nodeValue!
                } else {
                    texts![Object.keys(props.texts!).sort()[0]][
                        `${props.currentName}${props.postfix}`
                    ] = e.target.attributes.getNamedItem("value")!.nodeValue!
                }
                Object.keys(props.texts!).map((l) => {
                    if (texts![l][`${props.currentName}${props.postfix}`] == undefined) {
                        texts![l][`${props.currentName}${props.postfix}`] = ""
                    }
                })
                editTexts({
                    version: props.version,
                    texts: texts,
                    scenarioMixinName: props.scenarioMixinName,
                })
                props.setUpdate((prev: number) => prev + 1)
            }}
        />
    )
}
