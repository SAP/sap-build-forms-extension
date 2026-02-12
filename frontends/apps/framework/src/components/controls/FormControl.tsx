import { ReactNode } from "react"

import {
    FlexBox,
    FlexBoxAlignItems,
    FlexBoxDirection,
    Tab,
    TabContainer,
    TabContainerDomRef,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import "@ui5/webcomponents-fiori/dist/illustrations/ErrorScreen"
import { TabContainerTabSelectEventDetail } from "@ui5/webcomponents/dist/TabContainer"

import { Card2, Severity, useMessages } from "commons"

import { ElementInfo, FormService, ROOT_ROW } from "../../features/sessions/forms"
import { ControlProps, getLabel, handleChange } from "./Control"
import { useAppDispatch, useAppSelector } from "../../features/store"
import SegmentControl from "./SegmentControl"
import { UIElement, UserEventType } from "../../features/sessions/definitions"
import { update } from "../../features/sessions/sessionSlice"
import { ElementProp } from "../../features/sessions/journal"
import { isEventValid } from "../../features/sessions/sessionActions"

/**
 *
 * @param ei
 * @returns
 */
function calcDesign(ei: ElementInfo | boolean | undefined): "Default" | "Positive" | "Negative" {
    if (typeof ei === "object") {
        if (ei.severity === Severity.Error) {
            return "Negative"
        }
        if (ei.severity === Severity.Success) {
            return "Positive"
        }
    }

    return "Default"
}

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { def, texts, rowId } = props
    const dispatch = useAppDispatch()
    const form = useAppSelector((state) => state.session.form)
    const messages = useMessages()
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    // console.log(
    //     `Selected segment is ${typeof element?.va}, value: '${element?.va}', len: ${
    //         (element?.va as string).length
    //     }`,
    // )

    /**
     *
     * @param evt
     */
    const handleTabSelect = async (
        evt: Ui5CustomEvent<TabContainerDomRef, TabContainerTabSelectEventDetail>,
    ) => {
        const key = evt.detail.tab.getAttribute("data-key")
        if (key) {
            // if it's a valid event there is a callback on backend side. In this case
            // we skip any default processing. Otherwise we let default handling change the tab
            const isValidEvent = isEventValid(UserEventType.Action, def)
            await handleChange(dispatch, def, rowId, messages, key)
            if (isValidEvent) {
                evt.preventDefault()
            }
        }
    }

    // create tabs for each (visible) segment
    let tabs: ReactNode[] = []
    def.elements!.forEach((it, i) => {
        const segment = FormService.findElementByRowAndKey(rowId, it.key, form)

        // if the is no current segment defined we implicitly set the first segment as selected
        if (i === 0 && (element?.va as string).length === 0) {
            // need to do this delayed/async because it will triffer a re-render of all components
            // and we cannot render within render in React
            setTimeout(
                () =>
                    dispatch(
                        update({
                            def,
                            rowId,
                            prop: ElementProp.Value,
                            value: segment?.key,
                        }),
                    ),
                0,
            )
            return <></>
        }

        if (it.uiElement === UIElement.Segment && segment?.vi) {
            const isSelected = it.key === element?.va
            tabs.push(
                <Tab
                    key={it.id}
                    data-key={it.key}
                    text={getLabel(texts, it)}
                    design={calcDesign(segment?.msg)}
                    selected={isSelected}
                >
                    {isSelected && (
                        <FlexBox
                            alignItems={FlexBoxAlignItems.Stretch}
                            direction={FlexBoxDirection.Column}
                            fitContainer
                            style={{ rowGap: ".5rem", overflowX: "scroll", overflowY: "auto" }}
                        >
                            <SegmentControl {...props} def={it} rowId={ROOT_ROW} />
                        </FlexBox>
                    )}
                    {!isSelected && <></>}
                </Tab>,
            )
        }
    })

    return (
        <>
            <FlexBox direction="Column" style={{ gap: ".5rem" }}>
                {def.header && (
                    <Card2
                        style={{
                            padding: ".5rem",
                            marginLeft: "2rem",
                            marginRight: "2rem",
                            marginTop: ".5rem",
                        }}
                    >
                        <SegmentControl {...props} def={def.header} rowId={ROOT_ROW} />
                    </Card2>
                )}
                <TabContainer
                    collapsed={false}
                    contentBackgroundDesign="Solid"
                    onTabSelect={handleTabSelect}
                >
                    {tabs}
                </TabContainer>
            </FlexBox>
        </>
    )
}
