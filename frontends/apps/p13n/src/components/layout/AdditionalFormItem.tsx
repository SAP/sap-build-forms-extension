import { useIntl } from "react-intl"
import { Personalization } from "../../features/personalizationDefinitions"
import {
    Button,
    CheckBox,
    FlexBox,
    FormItem,
    Label,
    Text,
    TextArea,
} from "@ui5/webcomponents-react"

interface DialogAddUserProps {
    userName: String | null
    edit: boolean
    isAdminView: boolean
    personalization: Personalization
    application: string
    deletedPersonalizations: Personalization[]
    personalizationsOfUser: Personalization[]
    setPersonalizationsOfUser(personalizations: Personalization[]): void
    setDeletedPersonalizations(personalizations: Personalization[]): void
}

export default function (props: DialogAddUserProps) {
    const intl = useIntl()
    return (
        <FormItem labelContent={<Label>{props.personalization.key}</Label>}>
            <FlexBox direction="Row" alignItems="Center" style={{ minHeight: 40 }}>
                {!props.edit && (
                    <Text style={{ marginBlock: 10, width: 210 }} maxLines={10}>
                        {props.personalization.value}
                    </Text>
                )}
                {props.edit && (
                    <TextArea
                        style={{
                            width: 210,
                            height: props.personalization.value.length > 50 ? 150 : 50,
                        }}
                        disabled={
                            (!props.isAdminView && !props.personalization?.editable) ||
                            ((props.userName != props.personalization?.user ||
                                props.application != props.personalization.app) &&
                                props.personalization.editable == false)
                        }
                        onInput={(e) => {
                            if (
                                props.personalization.user === props.userName &&
                                props.personalization.app === props.application
                            ) {
                                props.setPersonalizationsOfUser(
                                    props.personalizationsOfUser.map((p: Personalization) =>
                                        p.key == props.personalization.key
                                            ? {
                                                  ...p,
                                                  value: e.target.value.trim(),
                                              }
                                            : p,
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
                                                value: e.target.value.trim(),
                                            }
                                        } else {
                                            return p
                                        }
                                    }),
                                )
                            }
                        }}
                        valueState="None"
                        value={props.personalization.value}
                    />
                )}
                {props.isAdminView && (
                    <>
                        <FlexBox direction="Row" alignItems="Center" style={{ paddingInline: 20 }}>
                            <Label>
                                {intl.formatMessage({
                                    id: "form_visible",
                                })}
                                :{" "}
                            </Label>
                            <CheckBox
                                onChange={(e) => {
                                    if (
                                        props.personalization.user === props.userName &&
                                        props.personalization.app == props.application
                                    ) {
                                        props.setPersonalizationsOfUser(
                                            props.personalizationsOfUser.map((p: Personalization) =>
                                                p.key == props.personalization.key
                                                    ? {
                                                          ...p,
                                                          visible: e.target.checked,
                                                      }
                                                    : p,
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
                                    (props.userName != props.personalization?.user ||
                                        props.application != props.personalization.app) &&
                                    props.personalization.visible == false
                                }
                            />
                        </FlexBox>
                        <FlexBox direction="Row" alignItems="Center" style={{ paddingInline: 20 }}>
                            <Label>
                                {intl.formatMessage({
                                    id: "form_editable",
                                })}
                                :{" "}
                            </Label>
                            <CheckBox
                                onChange={(e) => {
                                    if (
                                        props.personalization.user === props.userName &&
                                        props.personalization.app == props.application
                                    ) {
                                        props.setPersonalizationsOfUser(
                                            props.personalizationsOfUser.map((p: Personalization) =>
                                                p.key == props.personalization.key
                                                    ? {
                                                          ...p,
                                                          editable: e.target.checked,
                                                      }
                                                    : p,
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
                                    (props.userName != props.personalization.user ||
                                        props.application != props.personalization.app) &&
                                    props.personalization.editable == false
                                }
                            />
                        </FlexBox>
                        {props.edit &&
                            props.isAdminView &&
                            props.userName == props.personalization?.user &&
                            props.application == props.personalization.app &&
                            props.personalization.id != null && (
                                <FlexBox
                                    direction="Row"
                                    alignItems="Center"
                                    style={{ paddingInline: 20 }}
                                >
                                    <Button
                                        design="Transparent"
                                        onClick={() => {
                                            props.setDeletedPersonalizations([
                                                ...props.deletedPersonalizations,
                                                props.personalization,
                                            ])
                                            props.setPersonalizationsOfUser(
                                                props.personalizationsOfUser.filter((p) => {
                                                    return p != props.personalization
                                                }),
                                            )
                                        }}
                                    >
                                        {intl.formatMessage({
                                            id: "form_delete_setting",
                                        })}
                                    </Button>
                                </FlexBox>
                            )}
                    </>
                )}
            </FlexBox>
        </FormItem>
    )
}
