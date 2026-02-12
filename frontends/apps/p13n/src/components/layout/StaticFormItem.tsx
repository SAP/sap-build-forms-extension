import { CheckBox, FlexBox, FormItem, Label, Option, Select, Text } from "@ui5/webcomponents-react"
import { Personalization } from "../../features/personalizationDefinitions"
import { useIntl } from "react-intl"

interface DialogAddUserProps {
    settingKey: string
    currentValue: string
    userName: String | null
    edit: boolean
    isAdminView: boolean
    personalization: Personalization | undefined
    application: string
    personalizationsOfUser: Personalization[]
    options: { key: string; optionDisplayed: string }[]
    setPersonalizationsOfUser(personalizations: Personalization[]): void
}

export default function (props: DialogAddUserProps) {
    const intl = useIntl()
    return (
        <FormItem
            labelContent={<Label>{intl.formatMessage({ id: "form" + props.settingKey })}</Label>}
        >
            <FlexBox direction="Row" alignItems="Center" style={{ height: 40 }}>
                {!props.edit && (
                    <Text style={{ marginBlock: 10, width: 210 }}>{props.currentValue}</Text>
                )}
                {props.edit && (
                    <Select
                        onChange={(e) => {
                            if (
                                props.personalization &&
                                props.personalization.user === props.userName &&
                                props.personalization.app === props.application
                            ) {
                                props.setPersonalizationsOfUser(
                                    props.personalizationsOfUser.map(
                                        (personalization: Personalization) =>
                                            personalization.key == props.settingKey
                                                ? {
                                                      ...personalization,
                                                      value: e.detail.selectedOption.id,
                                                  }
                                                : personalization,
                                    ),
                                )
                            } else {
                                props.setPersonalizationsOfUser(
                                    props.personalizationsOfUser.map((p) => {
                                        if (p === props.personalization) {
                                            return {
                                                ...p,
                                                id: null,
                                                user: String(props.userName!),
                                                app: props.application,
                                                value: e.detail.selectedOption.id,
                                            }
                                        } else {
                                            return p
                                        }
                                    }),
                                )
                            }
                        }}
                        valueState="None"
                        disabled={
                            (!props.isAdminView && !props.personalization?.editable) ||
                            (props.personalization &&
                                (props.userName != props.personalization?.user ||
                                    props.application != props.personalization.app) &&
                                props.personalization.editable == false)
                        }
                    >
                        {props.options.map((option) => {
                            return (
                                <Option
                                    key={option.key}
                                    id={option.key}
                                    selected={option.key == props.personalization?.value}
                                >
                                    {option.optionDisplayed}
                                </Option>
                            )
                        })}
                    </Select>
                )}
                {props.isAdminView && (
                    <>
                        <FlexBox direction="Row" alignItems="Center" style={{ paddingInline: 20 }}>
                            <Label>{intl.formatMessage({ id: "form_visible" })}: </Label>
                            <CheckBox
                                onChange={(e) => {
                                    if (
                                        props.personalization &&
                                        props.personalization.user === props.userName &&
                                        props.personalization.app == props.application
                                    ) {
                                        props.setPersonalizationsOfUser(
                                            props.personalizationsOfUser.map(
                                                (personalization: Personalization) =>
                                                    personalization.key == props.settingKey
                                                        ? {
                                                              ...personalization,
                                                              visible: e.target.checked,
                                                          }
                                                        : personalization,
                                            ),
                                        )
                                    } else {
                                        props.setPersonalizationsOfUser(
                                            props.personalizationsOfUser.map((p) => {
                                                if (p === props.personalization) {
                                                    return {
                                                        ...p,
                                                        id: null,
                                                        user: String(props.userName!),
                                                        app: props.application,
                                                        visible: e.target.checked,
                                                    }
                                                } else {
                                                    return p
                                                }
                                            }),
                                        )
                                    }
                                }}
                                valueState="None"
                                readonly={!props.edit}
                                checked={props.personalization?.visible}
                                disabled={
                                    props.personalization &&
                                    (props.userName != props.personalization?.user ||
                                        props.application != props.personalization.app) &&
                                    props.personalization.visible == false
                                }
                            />
                        </FlexBox>
                        <FlexBox direction="Row" alignItems="Center" style={{ paddingInline: 20 }}>
                            <Label>{intl.formatMessage({ id: "form_editable" })}: </Label>
                            <CheckBox
                                onChange={(e) => {
                                    if (
                                        props.personalization &&
                                        props.personalization.user === props.userName &&
                                        props.personalization.app == props.application
                                    ) {
                                        props.setPersonalizationsOfUser(
                                            props.personalizationsOfUser.map(
                                                (personalization: Personalization) =>
                                                    personalization.key == props.settingKey
                                                        ? {
                                                              ...personalization,
                                                              editable: e.target.checked,
                                                          }
                                                        : personalization,
                                            ),
                                        )
                                    } else {
                                        props.setPersonalizationsOfUser(
                                            props.personalizationsOfUser.map((p) => {
                                                if (p === props.personalization) {
                                                    return {
                                                        ...p,
                                                        id: null,
                                                        user: String(props.userName!),
                                                        app: props.application,
                                                        editable: e.target.checked,
                                                    }
                                                } else {
                                                    return p
                                                }
                                            }),
                                        )
                                    }
                                }}
                                valueState="None"
                                readonly={!props.edit}
                                checked={props.personalization?.editable}
                                disabled={
                                    props.personalization &&
                                    (props.userName != props.personalization?.user ||
                                        props.application != props.personalization.app) &&
                                    props.personalization.editable == false
                                }
                            />
                        </FlexBox>
                    </>
                )}
            </FlexBox>
        </FormItem>
    )
}
