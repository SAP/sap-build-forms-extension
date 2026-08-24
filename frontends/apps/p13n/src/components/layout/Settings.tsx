import { Personalization, Value } from "../../features/personalizationDefinitions"
import { backendDispatch } from "../../utils/backend"
import {
    Breadcrumbs,
    BreadcrumbsItem,
    Button,
    DynamicPage,
    DynamicPageHeader,
    DynamicPageTitle,
    FlexBox,
    Form,
    FormGroup,
    FormItem,
    Label,
    MessageBoxType,
    Option,
    Select,
    Text,
    Title,
    Toolbar,
    ToolbarButton,
} from "@ui5/webcomponents-react"
import { JSX, useState } from "react"
import StaticFormItem from "./StaticFormItem"
import { formatCurrentDate, formatCurrentTime } from "../../utils/DataFormatUtils"
import { useIntl } from "react-intl"
import AdditionalFormItem from "./AdditionalFormItem"
import { createUseStyles } from "react-jss"
import ButtonDesign from "@ui5/webcomponents/dist/types/ButtonDesign"

interface SettingsProps {
    userName: String | null
    applications: string[]
    application: string
    personalizationsOfUser: Personalization[]
    deletedPersonalizations: Personalization[]
    isAdminView: boolean
    values: Value[]
    setPersonalizationsOfUser(personalizations: Personalization[]): void
    setDeletedPersonalizations(personalizations: Personalization[]): void
    setApplication(application: string): void
    openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string): void
    setDialogAddApplicationOpen(o: boolean): void
    setDialogAddSettingOpen(o: boolean): void
    toListView(): void
}

export default function (props: SettingsProps) {
    const useStyles = createUseStyles({
        headerButton: {
            marginInline: 5,
        },
    })

    const intl = useIntl()
    const classes = useStyles()
    const [edit, setEdit] = useState(false)

    function openMessageBoxSave(success: boolean) {
        props.openMessageBox(
            success ? MessageBoxType.Success : MessageBoxType.Error,
            <>
                {intl.formatMessage({
                    id: success ? "p13n_save_success" : "p13n_save_error",
                })}
            </>,
            "",
        )
    }

    if (props.values.length > 0 && props.personalizationsOfUser.length > 0) {
        return (
            <DynamicPage
                slot="midColumn"
                headerArea={
                    <DynamicPageHeader>
                        <FlexBox wrap="Wrap">
                            <FlexBox alignItems="Center">
                                <Select
                                    onChange={(e) => {
                                        props.setApplication(e.detail.selectedOption.id)

                                        const p = backendDispatch(
                                            `/v1/p13n/${props.isAdminView ? "admin/" : ""}user/${props.userName
                                            }${e.detail.selectedOption.id == "_"
                                                ? ""
                                                : "/" + e.detail.selectedOption.id
                                            }`,
                                            "GET",
                                            undefined,
                                            undefined,
                                        )

                                        p.then((action: any) => {
                                            if (action.status == 200) {
                                                props.setPersonalizationsOfUser(action.data)
                                                props.setDeletedPersonalizations([])
                                            } else {
                                                props.openMessageBox(
                                                    MessageBoxType.Error,
                                                    <>
                                                        {intl.formatMessage({
                                                            id: "p13n_load_error",
                                                        })}
                                                    </>,
                                                    "",
                                                )
                                            }
                                        })
                                    }}
                                    valueState="None"
                                >
                                    <Option key={"_"} id={"_"} selected={props.application == "_"}>
                                        {intl.formatMessage({ id: "form_application_default" })}
                                    </Option>
                                    {props.applications.sort().map((a) => {
                                        return (
                                            <Option
                                                key={a}
                                                id={a}
                                                selected={a == props.application}
                                            >
                                                {a}
                                            </Option>
                                        )
                                    })}
                                </Select>
                                <Button
                                    className={classes.headerButton}
                                    design="Transparent"
                                    onClick={() => {
                                        props.setDialogAddApplicationOpen(true)
                                    }}
                                >
                                    {intl.formatMessage({
                                        id: "p13n_button_new_application",
                                    })}
                                </Button>
                                <Button
                                    className={classes.headerButton}
                                    design="Transparent"
                                    disabled={props.userName == "_" && props.application == "_"}
                                    onClick={() => {
                                        props.openMessageBox(
                                            MessageBoxType.Confirm,
                                            <>
                                                {intl.formatMessage({
                                                    id: "p13n_delete_setting_information",
                                                })}
                                            </>,
                                            "DeleteSettings",
                                        )
                                    }}
                                >
                                    {intl.formatMessage({
                                        id: "p13n_button_setting_delete",
                                    })}
                                </Button>
                                {props.isAdminView && (
                                    <Button
                                        design="Transparent"
                                        className={classes.headerButton}
                                        onClick={() => {
                                            props.setDialogAddSettingOpen(true)
                                        }}
                                    >
                                        {intl.formatMessage({
                                            id: "p13n_button_add_setting",
                                        })}
                                    </Button>
                                )}
                            </FlexBox>
                        </FlexBox>
                    </DynamicPageHeader>
                }
                titleArea={
                    <DynamicPageTitle
                        actionsBar={
                            <Toolbar>
                                <ToolbarButton
                                    design="Transparent"
                                    onClick={() => {
                                        setEdit(!edit)
                                    }}
                                    text={
                                        edit
                                            ? intl.formatMessage({ id: "common_show" })
                                            : intl.formatMessage({ id: "common_edit" })
                                    }
                                />
                                <ToolbarButton
                                    icon="save"
                                    text={intl.formatMessage({ id: "common_save" })}
                                    onClick={() => {
                                        if (props.deletedPersonalizations.length > 0) {
                                            const p = backendDispatch(
                                                `/v1/p13n/${props.isAdminView ? "admin/" : ""
                                                }user/${props.userName}/${props.application}`,
                                                "DELETE",
                                                undefined,
                                                {
                                                    ids: props.deletedPersonalizations.map((e) => {
                                                        return e.id
                                                    }),
                                                },
                                            )
                                            p.then((action: any) => {
                                                if (action.status == 204) {
                                                    props.setDeletedPersonalizations([])
                                                    if (
                                                        props.personalizationsOfUser.filter((p) => {
                                                            return (
                                                                p.app == props.application &&
                                                                p.user == props.userName
                                                            )
                                                        }).length > 0
                                                    ) {
                                                        const p2 = backendDispatch(
                                                            `/v1/p13n/${props.isAdminView ? "admin/" : ""
                                                            }user/${props.userName}`,
                                                            "PUT",
                                                            props.personalizationsOfUser.filter(
                                                                (p) => {
                                                                    return (
                                                                        p.app ==
                                                                        props.application &&
                                                                        p.user == props.userName
                                                                    )
                                                                },
                                                            ),
                                                            undefined,
                                                        )
                                                        p2.then((action: any) => {
                                                            if (action.status == 200) {
                                                                props.setPersonalizationsOfUser(
                                                                    action.data,
                                                                )
                                                                openMessageBoxSave(true)
                                                            } else {
                                                                openMessageBoxSave(false)
                                                            }
                                                        })
                                                    } else {
                                                        openMessageBoxSave(true)
                                                    }
                                                } else {
                                                    openMessageBoxSave(false)
                                                }
                                            })
                                        } else if (
                                            props.personalizationsOfUser.filter((p) => {
                                                return (
                                                    p.app == props.application &&
                                                    p.user == props.userName
                                                )
                                            }).length > 0
                                        ) {
                                            const p = backendDispatch(
                                                `/v1/p13n/${props.isAdminView ? "admin/" : ""
                                                }user/${props.userName}`,
                                                "PUT",
                                                props.personalizationsOfUser.filter((p) => {
                                                    return (
                                                        p.app == props.application &&
                                                        p.user == props.userName
                                                    )
                                                }),
                                                undefined,
                                            )
                                            p.then((action: any) => {
                                                if (action.status == 200) {
                                                    props.setPersonalizationsOfUser(action.data)
                                                    openMessageBoxSave(true)
                                                } else {
                                                    openMessageBoxSave(false)
                                                }
                                            })
                                        } else {
                                            openMessageBoxSave(true)
                                        }
                                    }}
                                    design="Emphasized"
                                />
                            </Toolbar>
                        }
                        heading={
                            <Title>{props.userName == "_" ? "Default user" : props.userName}</Title>
                        }
                        snappedHeading={
                            <Title>{props.userName === "_" ? "Default user" : props.userName}</Title>
                        }
                        breadcrumbs={
                            <Breadcrumbs>
                                <BreadcrumbsItem>Home</BreadcrumbsItem>
                            </Breadcrumbs>
                        }
                        navigationBar={
                            <>
                                {props.isAdminView && (
                                    <Button
                                        icon="decline"
                                        design={ButtonDesign.Transparent}
                                        onClick={() => {
                                            props.toListView()
                                        }}
                                    />
                                )}
                            </>
                        }
                    ></DynamicPageTitle>
                }
            >
                <Form
                    layout="S1 M1 L1 XL1"
                    labelSpan="S10 M4 L4 XL 4"
                    style={{
                        alignItems: "center",
                        padding: 30,
                    }}
                >
                    <FormGroup
                        headerText={intl.formatMessage({
                            id: "form_group_general",
                        })}
                    >
                        {props.userName != "_" && (
                            <FormItem
                                labelContent={
                                    <Label>{intl.formatMessage({ id: "form_name" })}</Label>
                                }
                            >
                                <Label style={{ marginBlock: 10 }}>{props.userName}</Label>
                            </FormItem>
                        )}

                        <StaticFormItem
                            settingKey="_lang"
                            userName={props.userName}
                            currentValue={
                                props.values
                                    .find((k) => {
                                        return k.id == "_lang"
                                    })
                                    ?.values.find((v: string) => {
                                        return v.includes(
                                            "(" +
                                            props.personalizationsOfUser.find(
                                                (e: Personalization) => {
                                                    return e.key == "_lang"
                                                },
                                            )?.value +
                                            ")",
                                        )
                                    }) || ""
                            }
                            edit={edit}
                            isAdminView={props.isAdminView}
                            application={props.application}
                            personalization={props.personalizationsOfUser.find((e) => {
                                return e.key == "_lang"
                            })}
                            personalizationsOfUser={props.personalizationsOfUser}
                            options={(() => {
                                return props.values
                                    .find((e) => {
                                        return e.id == "_lang"
                                    })
                                    ?.values.map((oldValue) => {
                                        return {
                                            key: oldValue.substring(
                                                oldValue.indexOf("(") + 1,
                                                oldValue.lastIndexOf(")"),
                                            ),
                                            optionDisplayed: oldValue,
                                        }
                                    })!
                            })()}
                            setPersonalizationsOfUser={props.setPersonalizationsOfUser}
                        />
                        <StaticFormItem
                            settingKey="_date"
                            userName={props.userName}
                            currentValue={(() => {
                                var value = props.personalizationsOfUser.find((e) => {
                                    return e.key == "_date"
                                })?.value
                                if (!value) {
                                    return ""
                                }

                                return formatCurrentDate(new Date(), value) + " (" + value + ")"
                            })()}
                            edit={edit}
                            isAdminView={props.isAdminView}
                            application={props.application}
                            personalizationsOfUser={props.personalizationsOfUser}
                            personalization={props.personalizationsOfUser.find((e) => {
                                return e.key == "_date"
                            })}
                            options={(() => {
                                return props.values
                                    .find((e) => {
                                        return e.id == "_date"
                                    })
                                    ?.values.map((oldValue) => {
                                        return {
                                            key: oldValue,
                                            optionDisplayed:
                                                formatCurrentDate(
                                                    new Date(2024, 11, 31),
                                                    oldValue,
                                                ) +
                                                " (" +
                                                oldValue +
                                                ")",
                                        }
                                    })!
                            })()}
                            setPersonalizationsOfUser={props.setPersonalizationsOfUser}
                        />
                        <StaticFormItem
                            settingKey="_time"
                            userName={props.userName}
                            currentValue={(() => {
                                var value = props.personalizationsOfUser.find((e) => {
                                    return e.key == "_time"
                                })?.value
                                if (!value) {
                                    return ""
                                }

                                return formatCurrentTime(new Date(), value) + " (" + value + ")"
                            })()}
                            edit={edit}
                            isAdminView={props.isAdminView}
                            application={props.application}
                            personalizationsOfUser={props.personalizationsOfUser}
                            personalization={props.personalizationsOfUser.find(
                                (e: Personalization) => {
                                    return e.key == "_time"
                                },
                            )}
                            options={(() => {
                                var date = new Date()
                                date.setHours(12)
                                date.setMinutes(5)

                                return props.values
                                    .find((e) => {
                                        return e.id == "_time"
                                    })
                                    ?.values.map((oldValue) => {
                                        return {
                                            key: oldValue,
                                            optionDisplayed:
                                                formatCurrentTime(date, oldValue) +
                                                " (" +
                                                oldValue +
                                                ")",
                                        }
                                    })!
                            })()}
                            setPersonalizationsOfUser={props.setPersonalizationsOfUser}
                        />
                        <StaticFormItem
                            settingKey="_number"
                            userName={props.userName}
                            currentValue={
                                props.values
                                    .find((k) => {
                                        return k.id == "_number"
                                    })
                                    ?.values.find((v: string) => {
                                        return v.includes(
                                            "(" +
                                            props.personalizationsOfUser.find(
                                                (e: Personalization) => {
                                                    return e.key == "_number"
                                                },
                                            )?.value +
                                            ")",
                                        )
                                    }) || ""
                            }
                            edit={edit}
                            isAdminView={props.isAdminView}
                            application={props.application}
                            personalizationsOfUser={props.personalizationsOfUser}
                            personalization={props.personalizationsOfUser.find((e) => {
                                return e.key == "_number"
                            })}
                            options={(() => {
                                return props.values
                                    .find((e) => {
                                        return e.id == "_number"
                                    })
                                    ?.values.map((oldValue) => {
                                        return {
                                            key: oldValue.substring(
                                                oldValue.indexOf("(") + 1,
                                                oldValue.lastIndexOf(")"),
                                            ),
                                            optionDisplayed: oldValue,
                                        }
                                    })!
                            })()}
                            setPersonalizationsOfUser={props.setPersonalizationsOfUser}
                        />
                    </FormGroup>
                    <FormGroup
                        headerText={intl.formatMessage({
                            id: "form_group_additional",
                        })}
                    >
                        {props.personalizationsOfUser
                            .filter((personalization) => {
                                return !personalization.key.startsWith("_")
                            })
                            .map((personalization) => {
                                return (
                                    <AdditionalFormItem
                                        userName={props.userName}
                                        key={personalization.id}
                                        edit={edit}
                                        isAdminView={props.isAdminView}
                                        personalization={personalization}
                                        application={props.application}
                                        deletedPersonalizations={props.deletedPersonalizations}
                                        personalizationsOfUser={props.personalizationsOfUser}
                                        setDeletedPersonalizations={
                                            props.setDeletedPersonalizations
                                        }
                                        setPersonalizationsOfUser={props.setPersonalizationsOfUser}
                                    />
                                )
                            })}
                        {props.personalizationsOfUser.filter((personalization) => {
                            return !personalization.key.startsWith("_")
                        }).length == 0 && (
                                <FormItem>
                                    <Text>
                                        {intl.formatMessage({
                                            id: "form_no_additional_settings",
                                        })}
                                    </Text>
                                </FormItem>
                            )}
                    </FormGroup>
                </Form>
            </DynamicPage>
        )
    } else {
        return <></>
    }
}
