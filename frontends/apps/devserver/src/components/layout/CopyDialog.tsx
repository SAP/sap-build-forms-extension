import { Bar, Button, Dialog, Form, FormItem, Input, Label, Title } from "@ui5/webcomponents-react"
import { createUseStyles } from "react-jss"
import { useState } from "react"
import { useIntl } from "react-intl"
import useElementsStore from "../../state/elements"
import { changeName } from "../../utils/formUtils"
import { Elem, Scenario } from "../../utils/scenarioDefinitions"
import { useMessages, Severity } from "commons"

interface Props {
    dialogOpen: boolean
    version: number
    el: Elem | undefined
    element: string | undefined
    parentEl: Elem | undefined
    copiedEl: Elem | undefined
    treeItemsShown: Scenario | null | undefined
    scenarioMixinName: string
    update: number
    setUpdate: (e: any) => void
    setDialogOpen: (e: any) => void
}

const useStyles = createUseStyles({
    button: {
        marginLeft: 20,
        marginRight: 20,
        fontSize: "medium",
    },
})

export default function CopyDialog(props: Props) {
    const classes = useStyles()
    const intl = useIntl()
    const addElement = useElementsStore((state) => state.addElement)
    const [prefix, setPrefix] = useState("")
    const [postfix, setPostfix] = useState("")
    const { toast } = useMessages()

    return (
        <Dialog
            header={
                <Bar>
                    <Title>{intl.formatMessage({ id: "copy_dialog_title" })}</Title>
                </Bar>
            }
            footer={
                <Bar
                    design="Footer"
                    endContent={
                        <Button
                            design="Positive"
                            className={classes.button}
                            disabled={prefix.trim().length == 0 && postfix.trim().length == 0}
                            onClick={function Ta() {
                                if (prefix.trim().length != 0 || postfix.trim().length != 0) {
                                    var maxSort
                                    if (props.el) {
                                        maxSort =
                                            props.el.elements.reduce((max: number, obj: Elem) => {
                                                return obj.sort! > max ? obj.sort! : max
                                            }, 0) ?? 0
                                    } else {
                                        if (
                                            props.parentEl &&
                                            props
                                                .element!.split("x")
                                                .filter((item) => item)
                                                .at(-1) == "l"
                                        ) {
                                            maxSort =
                                                props.parentEl.leftElements!.reduce(
                                                    (max: number, obj: Elem) => {
                                                        return obj.sort! > max ? obj.sort! : max
                                                    },
                                                    0,
                                                ) ?? 0
                                        } else if (
                                            props.parentEl &&
                                            props
                                                .element!.split("x")
                                                .filter((item) => item)
                                                .at(-1) == "r"
                                        ) {
                                            maxSort =
                                                props.parentEl.rightElements!.reduce(
                                                    (max: number, obj: Elem) => {
                                                        return obj.sort! > max ? obj.sort! : max
                                                    },
                                                    0,
                                                ) ?? 0
                                        } else {
                                            maxSort =
                                                props.treeItemsShown?.elements?.reduce(
                                                    (prev, current) =>
                                                        (prev.sort ? prev.sort : -1) >
                                                            (current.sort ? current.sort : -1)
                                                            ? prev
                                                            : current,
                                                ).sort ?? 0
                                        }
                                    }

                                    if (props.copiedEl != undefined) {
                                        addElement({
                                            indexes: props
                                                .element!.split("x")
                                                .filter((item) => item)
                                                .join("x"),
                                            version: props.version,
                                            newEl: {
                                                ...changeName(props.copiedEl, prefix, postfix),
                                                sort: maxSort + 10,
                                            },
                                            scenarioMixinName: props.scenarioMixinName,
                                        })

                                        // Show toast notification
                                        toast(Severity.None, "element_pasted")

                                        props.setUpdate(props.update + 1)
                                    }
                                    props.setDialogOpen(false)
                                }
                            }}
                        >
                            {intl.formatMessage({ id: "copy_dialog_button_paste" })}
                        </Button>
                    }
                    startContent={
                        <Button
                            design="Negative"
                            className={classes.button}
                            onClick={function Ta() {
                                props.setDialogOpen(false)
                            }}
                        >
                            {intl.formatMessage({ id: "copy_dialog_button_close" })}
                        </Button>
                    }
                ></Bar>
            }
            onBeforeOpen={function Ta() {
                setPrefix("")
                setPostfix(`${(Math.random() + 1).toString(36).substring(7)}`)
            }}
            open={props.dialogOpen}
            onClose={() => {
                props.setDialogOpen(false)
            }}
            style={{ padding: 3, margin: 3, minWidth: 400, width: "20%" }}
        >
            <Form layout="S1 M1 L1 XL1" labelSpan="S1 M1 L1 XL1">
                <FormItem labelContent={<Label>{intl.formatMessage({ id: "copy_dialog_label_prefix" })}</Label>}>
                    <Input
                        value={prefix}
                        onInput={(e) => {
                            setPrefix(e.target.attributes.getNamedItem("value")!.nodeValue!)
                        }}
                    />
                </FormItem>
                <FormItem labelContent={<Label>{intl.formatMessage({ id: "copy_dialog_label_postfix" })}</Label>}>
                    <Input
                        value={postfix}
                        valueState={
                            prefix.trim().length == 0 && postfix.trim().length == 0
                                ? "Negative"
                                : "None"
                        }
                        valueStateMessage={<span>{intl.formatMessage({ id: "copy_dialog_prefix_postfix_required" })}</span>}
                        onInput={(e) => {
                            setPostfix(e.target.attributes.getNamedItem("value")!.nodeValue!)
                        }}
                    />
                </FormItem>
            </Form>
        </Dialog>
    )
}
