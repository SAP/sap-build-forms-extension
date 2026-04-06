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

    const getInputValue = (e: any): string => {
        const target = e?.target as { value?: string; attributes?: NamedNodeMap } | undefined
        const valueFromTarget = target?.value
        if (typeof valueFromTarget === "string") {
            return valueFromTarget
        }

        const valueFromAttribute = target?.attributes
            ?.getNamedItem("value")
            ?.nodeValue
        return valueFromAttribute ?? ""
    }

    return (
        <Input
            value={
                props.texts![
                    props.defaultLanguage ?? (Object.keys(props.texts! ?? {}).sort()[0] as any)
                ]?.[`${props.currentName}${props.postfix}` as any] ?? ""
            }
            className={classes.largeInput}
            onChange={(e) => {
                var texts: any = JSON.parse(JSON.stringify(props.texts!))
                if (props.defaultLanguage) {
                    if (!texts![props.defaultLanguage as any]) {
                        texts![props.defaultLanguage as any] = {}
                    }
                    texts![props.defaultLanguage as any][`${props.currentName}${props.postfix}`] =
                        getInputValue(e)
                } else {
                    texts![Object.keys(props.texts!).sort()[0]][
                        `${props.currentName}${props.postfix}`
                    ] = getInputValue(e)
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
