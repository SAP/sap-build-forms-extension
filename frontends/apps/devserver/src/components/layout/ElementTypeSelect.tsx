import { Select, Option } from "@ui5/webcomponents-react"
import { Elem, ElementPart, Message, Scenario } from "../../utils/scenarioDefinitions"
import { elementInfo2ValueState, getHighestSeverity } from "../../utils/formUtils"

interface Props {
    parentType: String | undefined
    type: String | undefined
    treeItemsShown: Scenario | null | undefined
    changeItem: boolean
    isScenario: boolean
    messages: Message[]
    className: string
    onChange: (e: any) => void
}

export default function ElementTypeSelect(props: Props) {
    var formOrWizard: "form" | "wizard" | "docform" | null = null

    if (props.treeItemsShown) {
        var elements: Elem[] = props.treeItemsShown!.elements!
        for (var x of elements) {
            if (x.type == "form") {
                formOrWizard = "form"
                break
            } else if (x.type == "wizard") {
                formOrWizard = "wizard"
                break
            } else if (x.type == "docform") {
                formOrWizard = "docform"
                break
            }
        }
    }

    if (props.isScenario == true) {
        return (
            <Select
                className={props.className}
                valueState={
                    props.messages.length > 0 &&
                    props.messages.filter((a: any) => a.elementPart == ElementPart.UiElementType)
                        .length > 0
                        ? elementInfo2ValueState(
                              props.messages.filter(
                                  (a: any) => a.elementPart == ElementPart.UiElementType,
                              )[0].severity,
                          )
                        : "None"
                }
                valueStateMessage={
                    props.messages.length > 0 &&
                    props.messages.filter((a: any) => a.elementPart == ElementPart.UiElementType)
                        .length > 0 ? (
                        <span>
                            {props.messages
                                .filter((a: any) => a.elementPart == ElementPart.UiElementType)
                                .map((e: Message) => e.message)
                                .join(", ")}
                        </span>
                    ) : (
                        <span></span>
                    )
                }
                onChange={function Ta(e) {
                    props.onChange(e)
                }}
            >
                <Option selected={true}>{props.type}</Option>
                {props.parentType == undefined &&
                    ((props.changeItem == false && formOrWizard == null) ||
                        (props.changeItem == true &&
                            ((formOrWizard != null &&
                                (props.type == "form" || props.type == "wizard" || props.type == "docform")) ||
                                formOrWizard == null))) && (
                        <>{props.type != "form" && <Option selected={false}>form</Option>}</>
                    )}
                {props.parentType == undefined &&
                    ((props.changeItem == false && formOrWizard == null) ||
                        (props.changeItem == true &&
                            ((formOrWizard != null &&
                                (props.type == "form" || props.type == "wizard" || props.type == "docform")) ||
                                formOrWizard == null))) && (
                        <>{props.type != "wizard" && <Option selected={false}>wizard</Option>}</>
                    )}
                {props.parentType == undefined &&
                    ((props.changeItem == false && formOrWizard == null) ||
                        (props.changeItem == true &&
                            ((formOrWizard != null &&
                                (props.type == "form" || props.type == "wizard" || props.type == "docform")) ||
                                formOrWizard == null))) && (
                        <>{props.type != "docform" && <Option selected={false}>docform</Option>}</>
                    )}
                {(props.parentType == "form" || props.parentType == "wizard" || props.parentType == "docform") &&
                    props.type != "toolbar" && (
                        <>{props.type != "segment" && <Option selected={false}>segment</Option>}</>
                    )}
                {(props.parentType == "segment" ||
                    props.parentType == "dialog" ||
                    props.parentType == "searchhelp" ||
                    props.parentType == "form" ||
                    props.parentType == "wizard") &&
                    props.type != "toolbar" && (
                        <>{props.type != "group" && <Option selected={false}>group</Option>}</>
                    )}
                {(props.parentType == "segment" ||
                    props.parentType == "group" ||
                    props.parentType == "table" ||
                    props.parentType == "searchhelp" ||
                    props.parentType == "dialog") && (
                    <>
                        {props.type != "alert" && <Option selected={false}>alert</Option>}
                        {props.type != "attachment" && <Option selected={false}>attachment</Option>}
                        {props.type != "autocomplete" && (
                            <Option selected={false}>autocomplete</Option>
                        )}
                        {props.type != "checkbox" && <Option selected={false}>checkbox</Option>}
                        {props.type != "currency" && <Option selected={false}>currency</Option>}
                        {props.type != "daterange" && <Option selected={false}>daterange</Option>}
                        {props.type != "dialog" && <Option selected={false}>dialog</Option>}
                        {props.type != "edit" && <Option selected={false}>edit</Option>}
                        {props.type != "icon" && <Option selected={false}>icon</Option>}
                        {props.type != "image" && <Option selected={false}>image</Option>}
                        {props.type != "input" && <Option selected={false}>input</Option>}
                        {props.type != "link" && <Option selected={false}>link</Option>}
                        {props.type != "multiselect" && (
                            <Option selected={false}>multiselect</Option>
                        )}
                        {props.type != "radio" && <Option selected={false}>radio</Option>}
                        {props.type != "select" && <Option selected={false}>select</Option>}
                        {props.type != "table" && <Option selected={false}>table</Option>}
                        {props.type != "text" && <Option selected={false}>text</Option>}
                        {props.type != "dummy" && <Option selected={false}>dummy</Option>}
                    </>
                )}
                {(props.parentType == "segment" ||
                    props.parentType == "group" ||
                    props.parentType == "table" ||
                    props.parentType == "searchhelp" ||
                    props.parentType == "toolbar" ||
                    props.parentType == "dialog") && (
                    <>{props.type != "button" && <Option selected={false}>button</Option>}</>
                )}
                {(props.parentType == "segment" ||
                    props.parentType == "group" ||
                    props.parentType == "table" ||
                    props.parentType == "dialog") && (
                    <>
                        {props.type != "searchhelp" && <Option selected={false}>searchhelp</Option>}
                    </>
                )}
                {props.type != "toolbar" && (
                    <>{props.type != "mixin" && <Option selected={false}>mixin</Option>}</>
                )}
            </Select>
        )
    } else {
        return (
            <Select
                onChange={function Ta(e) {
                    props.onChange(e)
                }}
                className={props.className}
                valueState={
                    props.messages.length > 0 &&
                    props.messages.filter((a: any) => a.elementPart == ElementPart.UiElementType)
                        .length > 0
                        ? elementInfo2ValueState(
                              getHighestSeverity(
                                  props.messages
                                      .filter(
                                          (a: any) => a.elementPart == ElementPart.UiElementType,
                                      )
                                      .map((e: Message) => e.severity),
                              ),
                          )
                        : "None"
                }
                valueStateMessage={
                    props.messages.length > 0 &&
                    props.messages.filter((a: any) => a.elementPart == ElementPart.UiElementType)
                        .length > 0 ? (
                        <span>
                            {props.messages
                                .filter((a: any) => a.elementPart == ElementPart.UiElementType)
                                .map((e: Message) => e.message)
                                .join(", ")}
                        </span>
                    ) : (
                        <span></span>
                    )
                }
            >
                <Option selected={true}>{props.type}</Option>

                {(props.parentType == "form" ||
                    props.parentType == "wizard" ||
                    props.parentType == "docform" ||
                    props.parentType == undefined) && (
                    <>{props.type != "segment" && <Option selected={false}>segment</Option>}</>
                )}
                {(props.parentType == "segment" ||
                    props.parentType == "dialog" ||
                    props.parentType == "searchhelp" ||
                    props.parentType == "form" ||
                    props.parentType == "wizard" ||
                    props.parentType == undefined) && (
                    <>{props.type != "group" && <Option selected={false}>group</Option>}</>
                )}
                {(props.parentType == "segment" ||
                    props.parentType == "group" ||
                    props.parentType == "table" ||
                    props.parentType == "searchhelp" ||
                    props.parentType == "dialog" ||
                    props.parentType == undefined) && (
                    <>
                        {props.type != "alert" && <Option selected={false}>alert</Option>}
                        {props.type != "attachment" && <Option selected={false}>attachment</Option>}
                        {props.type != "autocomplete" && (
                            <Option selected={false}>autocomplete</Option>
                        )}
                        {props.type != "checkbox" && <Option selected={false}>checkbox</Option>}
                        {props.type != "currency" && <Option selected={false}>currency</Option>}
                        {props.type != "daterange" && <Option selected={false}>daterange</Option>}
                        {props.type != "dialog" && <Option selected={false}>dialog</Option>}
                        {props.type != "edit" && <Option selected={false}>edit</Option>}
                        {props.type != "icon" && <Option selected={false}>icon</Option>}
                        {props.type != "image" && <Option selected={false}>image</Option>}
                        {props.type != "input" && <Option selected={false}>input</Option>}
                        {props.type != "link" && <Option selected={false}>link</Option>}
                        {props.type != "multiselect" && (
                            <Option selected={false}>multiselect</Option>
                        )}
                        {props.type != "radio" && <Option selected={false}>radio</Option>}
                        {props.type != "select" && <Option selected={false}>select</Option>}
                        {props.type != "table" && <Option selected={false}>table</Option>}
                        {props.type != "text" && <Option selected={false}>text</Option>}
                        {props.type != "dummy" && <Option selected={false}>dummy</Option>}
                    </>
                )}
                {(props.parentType == "segment" ||
                    props.parentType == "group" ||
                    props.parentType == "table" ||
                    props.parentType == "searchhelp" ||
                    props.parentType == "toolbar" ||
                    props.parentType == "dialog" ||
                    props.parentType == undefined) && (
                    <>{props.type != "button" && <Option selected={false}>button</Option>} </>
                )}
                {(props.parentType == "segment" ||
                    props.parentType == "group" ||
                    props.parentType == "table" ||
                    props.parentType == "dialog" ||
                    props.parentType == undefined) && (
                    <>
                        {props.type != "searchhelp" && <Option selected={false}>searchhelp</Option>}{" "}
                    </>
                )}
                <>{props.type != "mixin" && <Option selected={false}>mixin</Option>}</>
            </Select>
        )
    }
}
