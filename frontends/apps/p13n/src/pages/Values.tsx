import {
    Bar,
    Breadcrumbs,
    BreadcrumbsItem,
    Button,
    DynamicPage,
    DynamicPageHeader,
    DynamicPageTitle,
    FlexBox,
    FlexibleColumnLayout,
    Icon,
    Input,
    InputDomRef,
    List,
    ListDomRef,
    ListItemStandard,
    MessageBox,
    MessageBoxAction,
    MessageBoxType,
    Option,
    Select,
    Table,
    TableHeaderCell,
    TableHeaderRow,
    Title,
    Toolbar,
    ToolbarButton,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"
import { JSX, useEffect, useState } from "react"
import { useIntl } from "react-intl"
import { ThemingParameters } from "@ui5/webcomponents-react-base"
import { MessageBoxParams, Value } from "../features/personalizationDefinitions"
import { backendDispatch } from "../utils/backend"
import { ListItemClickEventDetail } from "@ui5/webcomponents/dist/List"
import { createUseStyles } from "react-jss"
import DialogAddValue from "../components/layout/DialogAddValue"
import DialogAddLocale from "../components/layout/DialogAddLocale"
import TableRowKeyValue from "../components/layout/TableRowKeyValue"
import FCLLayout from "@ui5/webcomponents-fiori/dist/types/FCLLayout"
import ListSelectionMode from "@ui5/webcomponents/dist/types/ListSelectionMode"
import ButtonDesign from "@ui5/webcomponents/dist/types/ButtonDesign"

export default function () {
    const useStyles = createUseStyles({
        headerButton: {
            marginInline: 5,
        },
    })

    useEffect(() => {
        const p = backendDispatch("/v1/p13n/values/keys", "GET", undefined, undefined)
        p.then((action: any) => {
            if (action.status == 200) {
                setValueKeys(action.data)
            } else {
                openMessageBoxLoadError()
            }
        })
    }, [])

    const intl = useIntl()
    const classes = useStyles()
    const [valueKeys, setValueKeys] = useState<string[]>([])
    const [values, setValues] = useState<Value[]>([])
    const [keySelected, setKeySelected] = useState<string>()
    const [searchKey, setSearchKey] = useState<string>("")
    const [layout, setLayout] = useState(FCLLayout.OneColumn)
    const [edit, setEdit] = useState(false)
    const [locale, setLocale] = useState("_")

    const [messageBoxParams, setMessageBoxParams] = useState<MessageBoxParams>({
        type: undefined,
        id: "",
        text: <></>,
    })
    const [messageBoxOpen, setMessageBoxOpen] = useState(false)
    const [dialogAddValueOpen, setDialogAddValueOpen] = useState(false)
    const [dialogAddLocaleOpen, setDialogAddLocaleOpen] = useState(false)

    function loadValueKeys(requestParams: object | undefined) {
        const p = backendDispatch("/v1/p13n/values/keys", "GET", undefined, requestParams)
        p.then((action: any) => {
            if (action.status == 200) {
                setValueKeys(action.data)
            } else {
                openMessageBoxLoadError()
            }
        })
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

    function openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element, mBoxId: string) {
        setMessageBoxParams({ type: mBoxType, id: mBoxId, text: mBoxText })
        setMessageBoxOpen(true)
    }

    function openMessageBoxSave(success: boolean) {
        openMessageBox(
            success ? MessageBoxType.Success : MessageBoxType.Error,
            <>
                {intl.formatMessage({
                    id: success ? "p13n_save_success" : "p13n_save_error",
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
                        id: "p13n_title_values",
                    })}
                </Title>
            </Bar>
            <div style={{ height: `${window.innerHeight - 44}px` }}>
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
                                        paddingTop: 50,
                                        paddingBottom: 20,
                                        paddingLeft: 30,
                                    }}
                                    direction="Row"
                                    alignItems="Center"
                                >
                                    <Input
                                        style={{ marginInline: 10 }}
                                        value={searchKey}
                                        icon={<Icon name="search" />}
                                        placeholder={intl.formatMessage({
                                            id: "p13n_search_placeholder",
                                        })}
                                        showClearIcon={true}
                                        onInput={(e: Ui5CustomEvent<InputDomRef, never>) => {
                                            setSearchKey(
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
                                            if (searchKey != "" && searchKey != undefined) {
                                                loadValueKeys({ search: searchKey })
                                            } else {
                                                loadValueKeys(undefined)
                                            }
                                        }}
                                    >
                                        {intl.formatMessage({
                                            id: "p13n_search_button",
                                        })}
                                    </Button>
                                </FlexBox>
                            </FlexBox>

                            <List
                                headerText={intl.formatMessage({ id: "p13n_values" })}
                                selectionMode={ListSelectionMode.Single}
                                onItemClick={(
                                    e: Ui5CustomEvent<ListDomRef, ListItemClickEventDetail>,
                                ) => {
                                    setKeySelected(e.detail.item.id)
                                    const p = backendDispatch(
                                        `/v1/p13n/values/keys/${e.detail.item.id}`,
                                        "GET",
                                        undefined,
                                        undefined,
                                    )
                                    p.then((action: any) => {
                                        if (action.status == 200) {
                                            setValues(action.data)
                                            setLocale("_")
                                        } else {
                                            openMessageBoxLoadError()
                                        }
                                    })
                                    setLayout(FCLLayout.TwoColumnsMidExpanded)
                                }}
                            >
                                {valueKeys.sort().map((key: string) => (
                                    <ListItemStandard
                                        key={key}
                                        id={key}
                                        icon={"navigation-right-arrow"}
                                        iconEnd={true}
                                        navigated={keySelected == key}
                                        selected={keySelected == key}
                                    >
                                        {key}
                                    </ListItemStandard>
                                ))}
                            </List>
                        </>
                    }
                    midColumn={
                        <DynamicPage
                            slot="midColumn"
                            headerArea={
                                <DynamicPageHeader>
                                    <FlexBox wrap="Wrap">
                                        <FlexBox alignItems="Center">
                                            {(
                                                values.find((value) => value.locale == locale)
                                                    ?.values || []
                                            ).length > 0 &&
                                                values
                                                    .find((value) => value.locale == locale)
                                                    ?.values.every(
                                                        (item) =>
                                                            item.substring(
                                                                item.indexOf("(") + 1,
                                                                item.lastIndexOf(")"),
                                                            ).length > 0,
                                                    ) && (
                                                    <>
                                                        <Select
                                                            onChange={(e) => {
                                                                setLocale(
                                                                    e.detail.selectedOption.id,
                                                                )
                                                            }}
                                                        >
                                                            <Option key={"_"} id={"_"}>
                                                                {intl.formatMessage({
                                                                    id: "form_default",
                                                                })}
                                                            </Option>
                                                            {values
                                                                .filter((v) => {
                                                                    return v.locale != "_"
                                                                })
                                                                .sort((a: Value, b: Value) =>
                                                                    a.locale.localeCompare(
                                                                        b.locale,
                                                                    ),
                                                                )
                                                                .map((a) => {
                                                                    return (
                                                                        <Option
                                                                            key={a.locale}
                                                                            id={a.locale}
                                                                            selected={
                                                                                a.locale == locale
                                                                            }
                                                                        >
                                                                            {a.locale}
                                                                        </Option>
                                                                    )
                                                                })}
                                                        </Select>
                                                        <Button
                                                            design="Transparent"
                                                            className={classes.headerButton}
                                                            onClick={() => {
                                                                setDialogAddLocaleOpen(true)
                                                            }}
                                                        >
                                                            {intl.formatMessage({
                                                                id: "p13n_button_new_locale",
                                                            })}
                                                        </Button>
                                                        <Button
                                                            design="Transparent"
                                                            className={classes.headerButton}
                                                            disabled={locale == "_"}
                                                            onClick={() => {
                                                                if (locale != "_") {
                                                                    setLocale("_")
                                                                    setValues(
                                                                        values.filter(
                                                                            (v) =>
                                                                                v.locale != locale,
                                                                        ),
                                                                    )
                                                                }
                                                            }}
                                                        >
                                                            {intl.formatMessage({
                                                                id: "p13n_button_delete_locale",
                                                            })}
                                                        </Button>
                                                    </>
                                                )}
                                            <Button
                                                design={
                                                    (
                                                        values.find(
                                                            (value) => value.locale == locale,
                                                        )?.values || []
                                                    ).length > 0 &&
                                                    values
                                                        .find((value) => value.locale == locale)
                                                        ?.values.every(
                                                            (item) =>
                                                                item.substring(
                                                                    item.indexOf("(") + 1,
                                                                    item.lastIndexOf(")"),
                                                                ).length > 0,
                                                        )
                                                        ? "Transparent"
                                                        : "Emphasized"
                                                }
                                                className={classes.headerButton}
                                                onClick={() => {
                                                    setDialogAddValueOpen(true)
                                                }}
                                            >
                                                {intl.formatMessage({
                                                    id: "p13n_button_new_value",
                                                })}
                                            </Button>
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
                                                onClick={() => {
                                                    const p2 = backendDispatch(
                                                        `/v1/p13n/admin/values/${keySelected}`,
                                                        "PUT",
                                                        values,
                                                        undefined,
                                                    )
                                                    p2.then((action: any) => {
                                                        if (action.status == 200) {
                                                            setValues(action.data)
                                                            openMessageBoxSave(true)
                                                        } else {
                                                            openMessageBoxSave(false)
                                                        }
                                                    })
                                                }}
                                                design="Emphasized"
                                                text={intl.formatMessage({ id: "common_save" })}
                                            />
                                        </Toolbar>
                                    }
                                    heading={<Title>{keySelected}</Title>}
                                    breadcrumbs={
                                        <Breadcrumbs>
                                            <BreadcrumbsItem>Home</BreadcrumbsItem>
                                        </Breadcrumbs>
                                    }
                                    navigationBar={
                                        <Button
                                            icon="decline"
                                            design={ButtonDesign.Transparent}
                                            onClick={() => {
                                                toListView()
                                            }}
                                        />
                                    }
                                ></DynamicPageTitle>
                            }
                        >
                            <Table
                                headerRow={
                                    <TableHeaderRow sticky>
                                        <TableHeaderCell width="100px">
                                            {intl.formatMessage({ id: "p13n_new_setting_key" })}
                                        </TableHeaderCell>
                                        <TableHeaderCell>
                                            {intl.formatMessage({ id: "p13n_new_setting_value" })}
                                        </TableHeaderCell>
                                        <TableHeaderCell width="100px"></TableHeaderCell>
                                    </TableHeaderRow>
                                }
                            >
                                {values
                                    .find((value) => value.locale == locale)
                                    ?.values.map((item: string) => {
                                        return (
                                            <TableRowKeyValue
                                                item={item}
                                                locale={locale}
                                                edit={edit}
                                                values={values}
                                                setValues={setValues}
                                                key={item}
                                            />
                                        )
                                    })}
                            </Table>
                        </DynamicPage>
                    }
                />
            </div>
            <MessageBox
                onClose={(action, escPressed) => {
                    if (
                        messageBoxParams.type === MessageBoxType.Confirm &&
                        action === MessageBoxAction.OK
                    ) {
                    }
                    setMessageBoxOpen(false)
                }}
                type={messageBoxParams.type}
                open={messageBoxOpen}
            >
                {messageBoxParams.text}
            </MessageBox>
            <DialogAddValue
                dialogOpen={dialogAddValueOpen}
                values={values}
                id={keySelected}
                setDialogOpen={setDialogAddValueOpen}
                setValues={setValues}
                openMessageBox={openMessageBox}
            />
            <DialogAddLocale
                dialogOpen={dialogAddLocaleOpen}
                values={values}
                id={keySelected}
                setDialogOpen={setDialogAddLocaleOpen}
                setValues={setValues}
                setLocale={setLocale}
                openMessageBox={openMessageBox}
            />
        </div>
    )
}
