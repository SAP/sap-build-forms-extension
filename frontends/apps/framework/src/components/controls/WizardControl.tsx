import { ReactNode, useEffect, useState } from "react"

import { useIntl } from "react-intl"
import { createUseStyles } from "react-jss"

import {
    Avatar,
    Bar,
    Button,
    FlexBox,
    FlexBoxAlignItems,
    FlexBoxDirection,
    Link,
    Title,
} from "@ui5/webcomponents-react"
import { ThemingParameters } from "@ui5/webcomponents-react-base"

import { Card2 } from "commons"

import { ControlProps, getLabel } from "./Control"
import { useAppSelector } from "../../features/store"
import { FormService, ROOT_ROW } from "../../features/sessions/forms"
import { Definition, UIElement } from "../../features/sessions/definitions"
import SegmentControl from "./SegmentControl"

const useStyles = createUseStyles({
    wizard_step_header: {
        fontSize: "var(--sapFontHeader5Size)",
        fontFamily: "var(--sapObjectHeader_Title_FontFamily)",
        color: "var(--sapTextColor)",
        wordWrap: "wrap",
        "&:hover": {
            textDecoration: "none",
            color: "var(--sapButton_Emphasized_Background)",
        },
    },
})

export default function (props: ControlProps) {
    const { def, texts, rowId } = props
    const form = useAppSelector((state) => state.session.form)
    const [collapsed, setCollapsed] = useState<boolean>(false)
    const [selected, setSelected] = useState<Definition>()

    const intl = useIntl()
    const classes = useStyles()

    useEffect(() => {
        setSelected(calcSelection("first"))
    }, [])

    /**
     *
     * @param type
     * @returns
     */
    const calcSelection = (type: "first" | "next" | "prev"): Definition | undefined => {
        let sel: Definition | undefined = undefined

        for (let i = 0; i < def.elements!.length; i++) {
            const it = def.elements![i]
            const element = FormService.findElementByRowAndKey(rowId, it.key, form)

            // skip any non-segment element
            if (it.uiElement !== UIElement.Segment) {
                continue
            }
            // handling of "first": Return after the first visible element is set
            if (type === "first" && element?.vi) {
                // console.log("Setting selection via first to " + it.id)
                return it
            }
            // handling of "next": If current selection is found, return after the next visible element
            if (type === "next" && sel && element?.vi) {
                // console.log("Setting selection via next to " + it.id)
                return it
            }
            if (type === "next" && selected?.key === it.key) {
                sel = it
                if (i + 1 === def.elements?.length) {
                    return it
                }
            }
            // handling of "prev": Remember alway the last visible element and set this
            // if current element is processed
            if (type === "prev" && selected?.key === it.key) {
                // console.log("Setting selection via prev to " + it.id)
                return sel ?? it
            }
            if (type === "prev" && element?.vi) {
                // console.log("Storing prev selection " + it.id)
                sel = it
            }
        }

        if (type === "next" && selected?.key === sel?.key) {
            // console.log("Handling selection on last element...")
            return sel
        }
    }

    /**
     *
     * @param element
     * @returns
     */
    const getText = (element: Definition): string => {
        let text = getLabel(texts, element)
        if (!text || text.length === 0) {
            text = element.id
        }
        return text
    }

    let items: ReactNode[] = []
    let ord = 0
    let nonSelectable = false
    def.elements!.forEach((it, i) => {
        const element = FormService.findElementByRowAndKey(rowId, it.key, form)
        if (element?.vi && it.uiElement === UIElement.Segment) {
            // determine if this element belongs to selectable or disabled area
            nonSelectable = nonSelectable || it.key === selected?.key
            // add the item
            items.push(
                <FlexBox
                    key={i}
                    alignItems={FlexBoxAlignItems.Center}
                    direction={FlexBoxDirection.Row}
                    fitContainer
                    style={{
                        rowGap: ".5rem",
                        columnGap: ".5rem",
                        boxShadow: "0 .125rem 0 color-mix(in srgb, #223548 8%, transparent)",
                        paddingTop: ".5rem",
                        paddingBottom: ".5rem",
                    }}
                >
                    <div
                        style={{
                            backgroundColor: "var(--ui5-v1-24-0-avatar-accent6)",
                            color: "var(--ui5-v1-24-0-avatar-accent6-color)",
                            minHeight: "3rem",
                            minWidth: "2.5rem",
                            fontSize: "var(--_ui5-v1-24-0_avatar_fontsize_S)",
                            height: "3rem",
                            width: "2.5rem",
                            borderRadius: "50%",
                            border: "2px solid var(--ui5-v1-24-0-avatar-accent6-color)",
                            display: "flex",
                            justifyContent: "center",
                            alignItems: "center",
                            marginLeft: ".5rem",
                            opacity:
                                nonSelectable && it.key !== selected?.key
                                    ? ThemingParameters.sapContent_DisabledOpacity
                                    : 1.0,
                        }}
                    >
                        <Title
                            style={{
                                fontSize: ThemingParameters.sapFontHeader3Size,
                                color: "var(--ui5-v1-24-0-avatar-accent6-color)",
                            }}
                        >
                            {(++ord).toString()}
                        </Title>
                    </div>
                    <Link
                        className={classes.wizard_step_header}
                        disabled={nonSelectable && it.key !== selected?.key}
                        onClick={() => setSelected(it)}
                    >
                        {getText(it)}
                    </Link>
                </FlexBox>,
            )
        }
    })

    return (
        <div style={{ width: "100%", display: "flex" }}>
            <Card2
                style={{
                    minHeight: "calc(100vh - 8.5em)",
                    position: "relative",
                    minWidth: collapsed ? "5px" : "20rem",
                    width: collapsed ? "25px" : "20rem",
                    transition: "width 0.3s ease 0s, min-width 0.3s ease 0s",
                    margin: "1em",
                }}
            >
                <Avatar
                    style={{ position: "absolute", right: "-10px", zIndex: 1 }}
                    icon={collapsed ? "open-command-field" : "close-command-field"}
                    onClick={() => setCollapsed(!collapsed)}
                />

                {!collapsed && (
                    <>
                        <FlexBox
                            direction={FlexBoxDirection.Column}
                            style={{
                                paddingRight: "1rem",
                            }}
                        >
                            {items}
                        </FlexBox>
                    </>
                )}
            </Card2>
            <Card2
                style={{
                    width: "100%",
                    minHeight: "calc(100vh - 9rem)",
                    marginTop: "sap-margin-medium",
                    marginLeft: "sap-margin-medium",
                    margin: "1em",
                }}
            >
                <Bar
                    design="Subheader"
                    startContent={
                        <Button
                            design="Emphasized"
                            icon="close-command-field"
                            disabled={calcSelection("prev")?.key === selected?.key}
                            onClick={() => setSelected(calcSelection("prev"))}
                        >
                            {intl.formatMessage({ id: "common_wizard_prev" })}
                        </Button>
                    }
                    endContent={
                        <Button
                            design="Emphasized"
                            disabled={calcSelection("next")?.key === selected?.key}
                            endIcon="open-command-field"
                            onClick={() => setSelected(calcSelection("next"))}
                        >
                            {intl.formatMessage({ id: "common_wizard_next" })}
                        </Button>
                    }
                >
                    <Title
                        level="H2"
                        style={{ fontSize: ThemingParameters.sapFontHeader3Size }}
                        wrappingType="Normal"
                    >
                        {selected && getText(selected)}
                    </Title>
                </Bar>
                <div
                    style={{
                        padding: ".5rem",
                        marginLeft: "2rem",
                        marginRight: "2rem",
                        marginTop: ".5rem",
                    }}
                >
                    {selected && <SegmentControl {...props} def={selected} rowId={ROOT_ROW} />}
                </div>
            </Card2>
        </div>
    )
}
