import { Bar, MessageBox, MessageBoxAction, MessageBoxType, Title } from "@ui5/webcomponents-react"
import { ThemingParameters } from "@ui5/webcomponents-react-base"
import { JSX, useEffect, useState } from "react"
import { backendDispatch } from "../utils/backend"
import { MessageBoxParams, Personalization, Value } from "../features/personalizationDefinitions"
import Settings from "../components/layout/Settings"
import { useIntl } from "react-intl"
import DialogAddApplication from "../components/layout/DialogAddApplication"

export default function () {
    const intl = useIntl()

    const [user, setUser] = useState<string>("")
    const [personalizationsOfUser, setPersonalizationsOfUser] = useState<Personalization[]>([])
    const [values, setValues] = useState<Value[]>([])

    const [applications, setApplications] = useState<string[]>([])
    const [application, setApplication] = useState("_")
    const [dialogAddApplicationOpen, setDialogAddApplicationOpen] = useState(false)
    const [messageBoxOpen, setMessageBoxOpen] = useState(false)

    const [messageBoxParams, setMessageBoxParams] = useState<MessageBoxParams>({
        type: undefined,
        id: "",
        text: <></>,
    })

    useEffect(() => {
        backendDispatch("/v1/p13n/me", "GET", undefined, undefined).then((action: any) => {
            if (action.status === 200) {
                setUser(action.data.username)
            } else {
                openMessageBoxLoadError()
            }
        })
    }, [])

    useEffect(() => {
        if (!user) return

        backendDispatch("/v1/p13n/values", "GET", undefined, {
            locale: intl.locale.split("-")[0],
        }).then((action: any) => {
            if (action.status == 200) {
                const o: { [key: string]: string[] } = action.data
                setValues(
                    Object.entries(o).map(([key, value]) => ({
                        id: key,
                        locale: intl.locale.split("-")[0],
                        values: value,
                    })),
                )
            } else if (action.status == 404) {
                openMessageBoxNotDefaults()
            } else {
                openMessageBoxLoadError()
            }
        })
        backendDispatch("/v1/p13n/user/" + user, "GET", undefined, undefined).then(
            (action: any) => {
                if (action.status == 200) {
                    setPersonalizationsOfUser(action.data)
                } else {
                    openMessageBoxLoadError()
                }
            },
        )
        backendDispatch("/v1/p13n/apps", "GET", undefined, undefined).then((action: any) => {
            if (action.status == 200) {
                setApplications(action.data)
            } else {
                openMessageBoxLoadError()
            }
        })
    }, [user])

    function openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string) {
        setMessageBoxParams({ type: mBoxType, text: mBoxText, id: mBoxId })
        setMessageBoxOpen(true)
    }

    function openMessageBoxLoadError() {
        openMessageBox(
            MessageBoxType.Error,
            <>
                {intl.formatMessage({
                    id: "p13n_load_error",
                })}
            </>,
            "",
        )
    }

    function openMessageBoxNotDefaults() {
        openMessageBox(
            MessageBoxType.Error,
            <>
                {intl.formatMessage({
                    id: "p13n_load_defaults_error",
                })}
            </>,
            "",
        )
    }

    return (
        <div>
            <Bar style={{ height: 44 }}>
                <Title level="H1" style={{ fontSize: ThemingParameters.sapFontHeader3Size }}>
                    {intl.formatMessage({
                        id: "p13n_title_user",
                    })}
                </Title>
            </Bar>
            <div style={{ height: `${window.innerHeight - 44}px` }}>
                <Settings
                    userName={user}
                    applications={applications}
                    application={application}
                    setApplication={setApplication}
                    isAdminView={false}
                    values={values}
                    toListView={() => {}}
                    personalizationsOfUser={personalizationsOfUser}
                    deletedPersonalizations={[]}
                    setPersonalizationsOfUser={setPersonalizationsOfUser}
                    setDeletedPersonalizations={() => {}}
                    setDialogAddApplicationOpen={setDialogAddApplicationOpen}
                    setDialogAddSettingOpen={() => {}}
                    openMessageBox={openMessageBox}
                />
            </div>
            <DialogAddApplication
                dialogOpen={dialogAddApplicationOpen}
                username={user}
                applications={applications}
                setDialogOpen={setDialogAddApplicationOpen}
                setApplications={setApplications}
                setPersonalizationsOfUser={setPersonalizationsOfUser}
                openMessageBox={openMessageBox}
            />
            <MessageBox
                onClose={(action, escPressed) => {
                    if (
                        messageBoxParams.type === MessageBoxType.Confirm &&
                        action === MessageBoxAction.OK
                    ) {
                        if (messageBoxParams.id == "DeleteSettings") {
                            const p = backendDispatch(
                                `/v1/p13n/user/${user}/${application}`,
                                "DELETE",
                                undefined,
                                undefined,
                            )
                            p.then((action: any) => {
                                if (action.status == 204) {
                                    const p = backendDispatch(
                                        `/v1/p13n/user/${user}${
                                            application != "_" ? "/" + application : ""
                                        }`,
                                        "GET",
                                        undefined,
                                        undefined,
                                    )
                                    p.then((action: any) => {
                                        if (action.status == 200) {
                                            setPersonalizationsOfUser(action.data)
                                        } else {
                                            openMessageBoxLoadError()
                                        }
                                    })
                                } else {
                                    openMessageBox(
                                        MessageBoxType.Error,
                                        <>
                                            {intl.formatMessage({
                                                id: "p13n_delete_settings_error",
                                            })}
                                        </>,
                                        "",
                                    )
                                }
                            })
                        }
                    }
                    setMessageBoxOpen(false)
                }}
                type={messageBoxParams.type}
                open={messageBoxOpen}
            >
                {messageBoxParams.text}
            </MessageBox>
        </div>
    )
}
