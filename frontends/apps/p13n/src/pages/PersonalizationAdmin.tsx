import {
    Bar,
    FlexibleColumnLayout,
    Input,
    List,
    ListDomRef,
    MessageBox,
    MessageBoxAction,
    MessageBoxType,
    Title,
    Ui5CustomEvent,
    InputDomRef,
    Button,
    FlexBox,
    Icon,
    ListItemStandard,
} from "@ui5/webcomponents-react"
import { ThemingParameters } from "@ui5/webcomponents-react-base"
import { JSX, useEffect, useRef, useState } from "react"
import { backendDispatch } from "../utils/backend"
import { ListItemClickEventDetail } from "@ui5/webcomponents/dist/List"
import { MessageBoxParams, Personalization, Value } from "../features/personalizationDefinitions"
import Settings from "../components/layout/Settings"
import DialogAddUser from "../components/layout/DialogAddUser"
import { useIntl } from "react-intl"
import DialogAddApplication from "../components/layout/DialogAddApplication"
import DialogAddSetting from "../components/layout/DialogAddSetting"
import FCLLayout from "@ui5/webcomponents-fiori/dist/types/FCLLayout"
import ListSelectionMode from "@ui5/webcomponents/dist/types/ListSelectionMode"

export default function () {
    useEffect(() => {
        loadUsers(undefined)
        loadStaticValues()
        loadApplications()
    }, [])

    const intl = useIntl()

    useEffect(() => {
        document.title = intl.formatMessage({ id: "p13n_title_admin" })
    }, [intl])

    const [users, setUsers] = useState<string[]>([])
    const [values, setValues] = useState<Value[]>([])
    const [searchUser, setSearchUser] = useState<string>("")
    const [userSelected, setUserSelected] = useState<String | null>(null)
    const [personalizationsOfUser, setPersonalizationsOfUser] = useState<Personalization[]>([])
    const [deletedPersonalizations, setDeletedPersonalizations] = useState<Personalization[]>([])

    const [applications, setApplications] = useState<string[]>([])
    const [application, setApplication] = useState("_")

    const [layout, setLayout] = useState(FCLLayout.OneColumn)

    const [messageBoxParams, setMessageBoxParams] = useState<MessageBoxParams>({
        type: undefined,
        id: "",
        text: <></>,
    })

    const [messageBoxOpen, setMessageBoxOpen] = useState(false)
    const [dialogAddUserOpen, setDialogAddUserOpen] = useState(false)
    const [dialogAddApplicationOpen, setDialogAddApplicationOpen] = useState(false)
    const [dialogAddSettingOpen, setDialogAddSettingOpen] = useState(false)

    const confirmCallbackRef = useRef<(() => void) | null>(null)

    function loadUsers(requestParams: object | undefined) {
        const p = backendDispatch("/v1/p13n/admin/user", "GET", undefined, requestParams)
        p.then((action: any) => {
            if (action.status == 200) {
                setUsers(action.data)
            } else {
                openMessageBoxLoadError()
            }
        })
    }

    function loadApplications() {
        const p = backendDispatch("/v1/p13n/apps", "GET", undefined, undefined)
        p.then((action: any) => {
            if (action.status == 200) {
                setApplications(action.data)
            } else {
                openMessageBoxLoadError()
            }
        })
    }

    function loadStaticValues() {
        const p = backendDispatch("/v1/p13n/values", "GET", undefined, {
            locale: intl.locale.split("-")[0],
        })
        p.then((action: any) => {
            if (action.status == 200) {
                const o: { [key: string]: string[] } = action.data
                setValues(
                    Object.entries(o).map(([key, value]) => {
                        return {
                            id: key,
                            locale: intl.locale.split("-")[0],
                            values: value,
                        }
                    }),
                )
            } else if (action.status == 404) {
                openMessageBoxNotDefaults()
            } else {
                openMessageBoxLoadError()
            }
        })
    }

    function openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string, onConfirm?: () => void) {
        setMessageBoxParams({ type: mBoxType, id: mBoxId, text: mBoxText })
        confirmCallbackRef.current = onConfirm ?? null
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

    function toListView() {
        setLayout(FCLLayout.OneColumn)
    }

    return (
        <div>
            <Bar style={{ height: 44 }}>
                <Title level="H1" style={{ fontSize: ThemingParameters.sapFontHeader3Size }}>
                    {intl.formatMessage({
                        id: "p13n_title_admin",
                    })}
                </Title>
            </Bar>
            <FlexibleColumnLayout
                layout={layout}
                style={{ height: `${window.innerHeight - 44}px` }}
                startColumn={
                    <>
                        <FlexBox
                            direction="Column"
                            style={{ position: "sticky", top: 0, zIndex: 1 }}
                        >
                            <FlexBox
                                style={{
                                    background: "White",
                                    paddingTop: 70,
                                    paddingBottom: 40,
                                    paddingLeft: 30,
                                }}
                                direction="Row"
                                alignItems="Center"
                            >
                                <Input
                                    style={{ marginInline: 10 }}
                                    value={searchUser}
                                    icon={<Icon name="search" />}
                                    placeholder={intl.formatMessage({
                                        id: "p13n_search_placeholder",
                                    })}
                                    showClearIcon={true}
                                    onInput={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                        setSearchUser(
                                            e.target.attributes
                                                .getNamedItem("value")!
                                                .nodeValue!.trim(),
                                        )
                                    }}
                                />
                                <Button
                                    style={{ marginInline: 10 }}
                                    design="Emphasized"
                                    onClick={() => {
                                        if (searchUser != "" && searchUser != undefined) {
                                            loadUsers({ search: searchUser })
                                        } else {
                                            loadUsers(undefined)
                                        }
                                    }}
                                >
                                    {intl.formatMessage({
                                        id: "p13n_search_button",
                                    })}
                                </Button>
                            </FlexBox>
                            <FlexBox
                                direction="Row"
                                justifyContent="Start"
                                alignItems="Center"
                                style={{ background: "#F0F0F0", paddingBlock: 3 }}
                            >
                                <Button
                                    icon="add"
                                    style={{ paddingLeft: 30 }}
                                    design="Transparent"
                                    onClick={() => {
                                        setDialogAddUserOpen(true)
                                    }}
                                >
                                    {intl.formatMessage({
                                        id: "p13n_button_new",
                                    })}
                                </Button>
                                <Button
                                    icon="delete"
                                    design="Transparent"
                                    disabled={!userSelected || userSelected == "_"}
                                    style={{ paddingInline: 10 }}
                                    onClick={() => {
                                        var parts = intl
                                            .formatMessage({
                                                id: "p13n_delete_information",
                                            })
                                            .split("<>")
                                        const currentUser = userSelected
                                        openMessageBox(
                                            MessageBoxType.Confirm,
                                            <>
                                                {parts[0]}
                                                <b>
                                                    <i>{currentUser}</i>{" "}
                                                </b>
                                                {parts[1]}
                                                <br />
                                                {parts[2]}
                                            </>,
                                            "Delete",
                                            () => {
                                                const p = backendDispatch(
                                                    `/v1/p13n/admin/user/${currentUser}`,
                                                    "DELETE",
                                                    undefined,
                                                    undefined,
                                                )
                                                p.then((action: any) => {
                                                    if (action.status == 204) {
                                                        setUsers(
                                                            users.filter(
                                                                (user) => user !== currentUser,
                                                            ),
                                                        )
                                                        setUserSelected("")
                                                        setPersonalizationsOfUser([])
                                                        setLayout(FCLLayout.OneColumn)
                                                    } else {
                                                        openMessageBox(
                                                            MessageBoxType.Error,
                                                            <>
                                                                {intl.formatMessage({
                                                                    id: "p13n_delete_error",
                                                                })}
                                                            </>,
                                                            "",
                                                        )
                                                    }
                                                })
                                            },
                                        )
                                    }}
                                >
                                    {intl.formatMessage({
                                        id: "p13n_button_delete",
                                    })}
                                </Button>
                            </FlexBox>
                        </FlexBox>

                        <List
                            headerText={intl.formatMessage({
                                id: "p13n_users",
                            })}
                            selectionMode={ListSelectionMode.Single}
                            onItemClick={(
                                e: Ui5CustomEvent<ListDomRef, ListItemClickEventDetail>,
                            ) => {
                                setUserSelected(e.detail.item.id)
                                setDeletedPersonalizations([])
                                const p = backendDispatch(
                                    `/v1/p13n/admin/user/${e.detail.item.id}${
                                        application != "_" ? "/" + application : ""
                                    }`,
                                    "GET",
                                    undefined,
                                    undefined,
                                )
                                p.then((action: any) => {
                                    if (action.status == 200) {
                                        const data = action.data
                                        setPersonalizationsOfUser(data)
                                    } else {
                                        openMessageBoxLoadError()
                                    }
                                })
                                setLayout(FCLLayout.TwoColumnsMidExpanded)
                            }}
                        >
                            <ListItemStandard
                                key="_"
                                id="_"
                                icon="navigation-right-arrow"
                                iconEnd={true}
                                navigated={userSelected == "_"}
                                selected={userSelected == "_"}
                            >
                                {intl.formatMessage({ id: "form_user_default" })}
                            </ListItemStandard>
                            {users.sort().map((item: string) => (
                                <ListItemStandard
                                    key={item}
                                    id={item}
                                    icon={"navigation-right-arrow"}
                                    iconEnd={true}
                                    navigated={userSelected == item}
                                    selected={userSelected == item}
                                >
                                    {item}
                                </ListItemStandard>
                            ))}
                        </List>
                    </>
                }
                midColumn={
                    <>
                        <Settings
                            userName={userSelected}
                            applications={applications}
                            application={application}
                            setApplication={setApplication}
                            toListView={toListView}
                            isAdminView={true}
                            values={values}
                            personalizationsOfUser={personalizationsOfUser}
                            deletedPersonalizations={deletedPersonalizations}
                            setPersonalizationsOfUser={setPersonalizationsOfUser}
                            setDeletedPersonalizations={setDeletedPersonalizations}
                            setDialogAddApplicationOpen={setDialogAddApplicationOpen}
                            setDialogAddSettingOpen={setDialogAddSettingOpen}
                            openMessageBox={openMessageBox}
                        />
                    </>
                }
            />
            <DialogAddUser
                dialogOpen={dialogAddUserOpen}
                username={userSelected}
                users={users}
                setDialogOpen={setDialogAddUserOpen}
                setUsername={setUserSelected}
                setUsers={setUsers}
                setPersonalizationsOfUser={setPersonalizationsOfUser}
                setApplication={setApplication}
                openMessageBox={openMessageBox}
                setLayout={setLayout}
            />
            <DialogAddApplication
                dialogOpen={dialogAddApplicationOpen}
                username={userSelected}
                applications={applications}
                setDialogOpen={setDialogAddApplicationOpen}
                setApplications={setApplications}
                setPersonalizationsOfUser={setPersonalizationsOfUser}
                openMessageBox={openMessageBox}
            />
            <DialogAddSetting
                dialogOpen={dialogAddSettingOpen}
                username={userSelected}
                application={application}
                personalizationsOfUser={personalizationsOfUser}
                setDialogOpen={setDialogAddSettingOpen}
                setPersonalizationsOfUser={setPersonalizationsOfUser}
                openMessageBox={openMessageBox}
            />
            <MessageBox
                onClose={(action, escPressed) => {
                    if (
                        messageBoxParams.type === MessageBoxType.Confirm &&
                        action === MessageBoxAction.OK
                    ) {
                        confirmCallbackRef.current?.()
                    }
                    confirmCallbackRef.current = null
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
