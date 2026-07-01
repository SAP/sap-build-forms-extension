import { JSX, useEffect, useRef, useState } from "react"
import { createUseStyles } from "react-jss"

import {
    Bar,
    Button,
    FlexBox,
    Form,
    FormItem,
    Input,
    Label,
    MessageBox,
    MessageBoxAction,
    MessageBoxType,
    MultiComboBox,
    MultiComboBoxItem,
    Option,
    Panel,
    Select,
    Switch,
    Tab,
    TabContainer,
    Title,
} from "@ui5/webcomponents-react"
import TreeItemBase from "@ui5/webcomponents/dist/TreeItemBase"
import { ThemingParameters } from "@ui5/webcomponents-react-base"

import { useIntl } from "react-intl"

import { PageProvider, useMessages, Severity } from "commons"

import useElementsStore from "../state/elements"

import { backendDispatch } from "../utils/backend"
import useMessagesStore from "../state/messages"
import { Elem, Scenario, tabs, Mixin, Parent, ElementPart } from "../utils/scenarioDefinitions"
import { collectVariantsFromElements } from "../utils/variantUtils"
import { toPascalCase } from "../utils/formUtils"
import Structure from "./layout/StructureTab"
import LanguagesTab from "./layout/LanguagesTab"
import CopyDialog from "./layout/CopyDialog"
import AddLanguageDialog from "./layout/AddLanguageDialog"
import AddElementDialog from "./layout/AddElementDialog"

const useStyles = createUseStyles({
    selectScenarioMixin: {
        minWidth: 260,
        maxWidth: 420,
        marginRight: 16,
    },
    selectVersion: {
        minWidth: 260,
        maxWidth: 420,
    },
    generalDataForm: {
        alignItems: "center",
        paddingBlock: 8,
    },
    button: {
        marginLeft: 30,
        fontSize: "medium",
    },
    dropdownContainer: {
        gap: '1rem',
        padding: '0.5rem 0 0.75rem 0',
    },
    selectVariants: {
        minWidth: 260,
        maxWidth: 420,
    },
})

export default function () {
    useEffect(() => {
        const p = backendDispatch("v1/scenarios/", "GET", undefined)
        p.then((action: any) => {
            if (action.status == 200) {
                const data = action.data
                insertElements(data)
                setTreeItemsShown(Object.keys(data).map((key) => data[key])[0])
                setVersion(Object.keys(data).map((key) => data[key])[0].version)
            } else {
                openMessageBox(
                    MessageBoxType.Error,
                    undefined,
                    <>{intl.formatMessage({ id: "editor_error_loading_data" })}</>,
                )
            }
        })

        const p2 = backendDispatch("v1/scenarios/mixins", "GET", undefined)
        p2.then((action: any) => {
            if (action.status == 200) {
                insertElementsMixin(action.data)
            } else {
                openMessageBox(
                    MessageBoxType.Error,
                    undefined,
                    <>{intl.formatMessage({ id: "editor_error_loading_mixin_data" })}</>,
                )
            }
        })
    }, [])

    const classes = useStyles()
    const treeItems: any = useElementsStore((state) => state.elements)
    const insertElements = useElementsStore((state) => state.insertElements)
    const insertElementsMixin = useElementsStore((state) => state.insertElementsMixin)
    const removeElement = useElementsStore((state) => state.removeElement)
    const removeElements = useElementsStore((state) => state.removeElements)
    const addElement = useElementsStore((state) => state.addElement)
    const editBaseData = useElementsStore((state) => state.editBaseData)
    const editDetailData = useElementsStore((state) => state.editDetailData)
    const editTexts = useElementsStore((state) => state.editTexts)
    const deleteElementMessages = useMessagesStore((state) => state.deleteElementMessages)
    const { toast } = useMessages()
    const intl = useIntl()

    const [scenarioMixin, setScenarioMixin] = useState("Scenario")
    const [version, setVersion] = useState<number>(-1)
    const [tab, setTab] = useState<string>("Structure")
    const [treeItemsShown, setTreeItemsShown] = useState<Scenario | Mixin | null>()
    const [element, setElement] = useState<string>()
    const [update, setUpdate] = useState<number>(1)
    const [renderTable, setRenderTable] = useState<number>(1)
    const [messageBoxOpen, setMessageBoxOpen] = useState(false)
    const [messageBoxType, setMessageBoxType] = useState<MessageBoxType>()
    const [messageBoxAction, setMessageBoxAction] = useState<"Delete" | undefined>(undefined)
    const [messageBoxText, setMessageBoxText] = useState<JSX.Element>(<></>)
    const [messageBoxOnConfirm, setMessageBoxOnConfirm] = useState<{ fn: () => void } | undefined>(undefined);
    const [addElementDialogOpen, setAddElementDialogOpen] = useState<boolean>(false)
    const [copyDialogOpen, setCopyDialogOpen] = useState<boolean>(false)
    const [indexesDelete, setIndexesDelete] = useState<{ indexes: string; name: string }>()
    const [el, setEl] = useState<Elem>()
    const [parents, setParents] = useState<Parent[]>([])
    const [selectedTreeItem, setSelectedTreeItem] = useState<TreeItemBase>()
    const [dialogAddLanguageOpen, setDialogAddLanguageOpen] = useState<boolean>(false)
    const [language, setLanguage] = useState<string>(
        treeItemsShown
            ? "defaultLanguage" in treeItemsShown
                ? treeItemsShown?.defaultLanguage!
                : Object.keys(treeItemsShown?.texts!).sort()[0]
            : "en",
    )
    const [copiedEl, setCopiedEl] = useState<Elem | undefined>()
    const [panelCollapsed, setPanelCollapsed] = useState<boolean>(true)
    const [selectedVariants, setSelectedVariants] = useState<string[]>([])

    const availableVariants = treeItemsShown
        ? collectVariantsFromElements(treeItemsShown.elements)
        : []

    useEffect(() => {
        setSelectedVariants((current) =>
            current.filter((variant) => availableVariants.includes(variant)),
        )
    }, [treeItemsShown])
    const flushPendingNameCommitRef = useRef<(() => void) | undefined>(undefined)

    useEffect(() => {
        setEl(undefined)
        setElement(undefined)
        setParents([])
        setSelectedTreeItem(undefined)
        const treeItems1 = treeItems.filter(
            (item: any) =>
                item.version == version &&
                (item.name == scenarioMixin ||
                    (scenarioMixin == "Scenario" && item.defaultLanguage != null)),
        )
        if (treeItems1.length > 0) {
            setTreeItemsShown(treeItems1[0])
            if ("defaultLanguage" in treeItems1[0]) {
                setLanguage(treeItems1[0].defaultLanguage!)
            } else {
                setLanguage(Object.keys(treeItems1[0].texts!).sort()[0])
            }
        } else {
            const treeItems2 = treeItems.filter(
                (item: any) =>
                    item.name == scenarioMixin ||
                    (scenarioMixin == "Scenario" && item.defaultLanguage != null),
            )
            if (treeItems2.length > 0) {
                setTreeItemsShown(treeItems2[0])
                setVersion(treeItems2[0].version)
                if ("defaultLanguage" in treeItems2[0]) {
                    setLanguage(treeItems2[0].defaultLanguage!)
                } else {
                    setLanguage(Object.keys(treeItems2[0].texts!).sort()[0])
                }
            }
        }
    }, [version, scenarioMixin])

    useEffect(() => {
        setTreeItemsShown(
            treeItems.filter(
                (item: any) =>
                    item.version == version &&
                    (item.name == scenarioMixin ||
                        (scenarioMixin == "Scenario" && item.defaultLanguage != null)),
            )[0],
        )
    }, [update])

    function setNewEl(newEl: Elem) {
        // Clear error messages for changed fields
        if (el && newEl.name) {
            const elementId = toPascalCase(newEl.name)

            // Check which fields changed and clear their specific error messages
            if (el.name !== newEl.name) {
                deleteElementMessages(elementId, ElementPart.Name)
            }
            if (el.type !== newEl.type) {
                deleteElementMessages(elementId, ElementPart.UiElementType)
            }
            if (el.dataType !== newEl.dataType) {
                deleteElementMessages(elementId, ElementPart.DataType)
            }
        }

        setEl(newEl)
        editDetailData({
            version: version,
            scenarioMixinName: scenarioMixin,
            indexes: element,
            newEl: newEl,
        })
        setUpdate(update + 1)
    }

    function openMessageBox(
        mBoxType: MessageBoxType,
        mBoxAction: "Delete" | undefined,
        mBoxText: JSX.Element,
        onConfirm?: () => void
    ) {
        setMessageBoxType(mBoxType)
        setMessageBoxAction(mBoxAction)
        setMessageBoxText(mBoxText)
        setMessageBoxOnConfirm(onConfirm ? { fn: onConfirm } : undefined)
        setMessageBoxOpen(true)
    }

    return (
        <>
            <PageProvider
                header={
                    <Bar>
                        <Title
                            level="H1"
                            style={{ fontSize: ThemingParameters.sapFontHeader3Size }}
                        >
                            {intl.formatMessage({ id: "editor_title" })}
                        </Title>                    </Bar>
                }
                footer={
                    <FlexBox direction="Row" justifyContent="Center">
                        <Bar
                            design="FloatingFooter"
                            style={{ width: "95%" }}
                            endContent={
                                <>
                                    <Button
                                        icon="validate"
                                        onClick={function Ta() {
                                            const p = backendDispatch(
                                                "v1/scenarios/check/",
                                                "PUT",
                                                Object.assign(
                                                    {},
                                                    treeItems.filter(
                                                        (item: any) => item.defaultLanguage != null,
                                                    ),
                                                ),
                                            )
                                            p.then((action: any) => {
                                                if (action.status == 200) {
                                                    useMessagesStore.getState().deleteMessages()
                                                    useMessagesStore.getState().insertMessages(action.data)
                                                    setUpdate(update + 1)
                                                    setRenderTable((prev) => prev + 1)

                                                    const messages = Array.isArray(action.data)
                                                        ? action.data
                                                        : Object.keys(action.data).map((k) => action.data[k])

                                                    const hasErrors = messages.some((msg: any) =>
                                                        msg.severity === "e" || msg.severity === "w"
                                                    )

                                                    if (hasErrors) {
                                                        openMessageBox(
                                                            MessageBoxType.Warning,
                                                            undefined,
                                                            <>{intl.formatMessage({ id: "editor_check_errors_warnings" })}</>,
                                                        )
                                                    } else if (messages.length > 0) {
                                                        openMessageBox(
                                                            MessageBoxType.Information,
                                                            undefined,
                                                            <>{intl.formatMessage({ id: "editor_check_info_messages" })}</>,
                                                        )
                                                    } else {
                                                        openMessageBox(
                                                            MessageBoxType.Success,
                                                            undefined,
                                                            <>{intl.formatMessage({ id: "editor_check_success" })}</>,
                                                        )
                                                    }
                                                } else {
                                                    openMessageBox(
                                                        MessageBoxType.Error,
                                                        undefined,
                                                        <>{intl.formatMessage({ id: "editor_check_error" })}</>,
                                                    )
                                                }
                                            })
                                        }}
                                        className={classes.button}
                                        design="Default"
                                    >
                                        {intl.formatMessage({ id: "editor_button_check" })}
                                    </Button>
                                    <Button
                                        icon="save"
                                        onClick={function Ta() {
                                            flushPendingNameCommitRef.current?.()
                                            ;(document.activeElement as HTMLElement | null)?.blur()

                                            const latestTreeItems = useElementsStore.getState().elements

                                            var newItems1: any
                                            var newItems2: any

                                            const p = backendDispatch(
                                                "v1/scenarios/",
                                                "PUT",
                                                Object.assign(
                                                    {},
                                                    latestTreeItems.filter(
                                                        (item: any) => item.defaultLanguage != null,
                                                    ),
                                                ),
                                            )
                                            p.then((action: any) => {
                                                if (action.status == 200) {
                                                    newItems1 = action.data
                                                    const p2 = backendDispatch(
                                                        "v1/scenarios/mixins/",
                                                        "PUT",
                                                        Object.assign(
                                                            {},
                                                            latestTreeItems.filter(
                                                                (item: any) =>
                                                                    item.defaultLanguage == null,
                                                            ),
                                                        ),
                                                    )
                                                    p2.then((action: any) => {
                                                        if (action.status == 200) {
                                                            openMessageBox(
                                                                MessageBoxType.Success,
                                                                undefined,
                                                                <>
                                                                    {intl.formatMessage({ id: "editor_save_success" })}
                                                                </>,
                                                            )
                                                            newItems2 = action.data
                                                            removeElements()
                                                            insertElements(newItems1)
                                                            insertElementsMixin(newItems2)
                                                            setUpdate(update + 1)
                                                            setRenderTable((prev) => prev + 1)
                                                        } else {
                                                            openMessageBox(
                                                                MessageBoxType.Error,
                                                                undefined,
                                                                <>
                                                                    {intl.formatMessage({ id: "editor_save_mixin_error" })}
                                                                </>,
                                                            )
                                                        }
                                                    })
                                                } else {
                                                    openMessageBox(
                                                        MessageBoxType.Error,
                                                        undefined,
                                                        <>
                                                            {intl.formatMessage({ id: "editor_save_scenario_error" })}
                                                        </>,
                                                    )
                                                }
                                            })
                                        }}
                                        className={classes.button}
                                        design="Positive"
                                    >
                                        {intl.formatMessage({ id: "editor_button_save" })}
                                    </Button>
                                </>
                            }
                        />
                    </FlexBox>
                }
                content={
                    <>
                        <FlexBox 
                            direction="Row" 
                            alignItems="Center" 
                            className={classes.dropdownContainer}
                        >
                            <Select
                                onChange={function Ta(e) {
                                    setScenarioMixin(e.detail.selectedOption.value)
                                }}
                                className={classes.selectScenarioMixin}
                            >
                                {[
                                    "Scenario",
                                    ...new Set(
                                        treeItems
                                            .filter(
                                                (t1: Scenario | Mixin) => !("defaultLanguage" in t1),
                                            )
                                            .map((t: Scenario | Mixin) => t.name),
                                    ),
                                ].map((item: any) => {
                                    if (item == "Scenario") {
                                        return (
                                            <Option
                                                key={item}
                                                value={item}
                                                icon="document"
                                                selected={scenarioMixin == item}
                                            >
                                                {intl.formatMessage({ id: "editor_scenario_label" })}
                                            </Option>
                                        )
                                    } else {
                                        return (
                                            <Option
                                                key={item}
                                                value={item}
                                                icon="add-document"
                                                selected={scenarioMixin == item}
                                            >
                                                {item}
                                            </Option>
                                        )
                                    }
                                })}
                            </Select>

                            <Select
                                onChange={function Ta(e) {
                                    setVersion(Number(e.detail.selectedOption.value).valueOf())
                                }}
                                className={classes.selectVersion}
                            >
                                {[
                                    ...new Set(
                                        treeItems
                                            .filter(
                                                (item: any) =>
                                                    item.name == scenarioMixin ||
                                                    (scenarioMixin == "Scenario" &&
                                                        item.defaultLanguage != null),
                                            )
                                            .map((obj: Scenario | Mixin) => obj.version),
                                    ),
                                ]
                                    .sort()
                                    .map((item: any) => {
                                        return <Option key={item} value={item.toString()}>{intl.formatMessage({ id: "editor_version_prefix" }, { version: item })}</Option>
                                    })}
                            </Select>

                            <MultiComboBox
                                className={classes.selectVariants}
                                placeholder={intl.formatMessage({ id: "editor_select_variants_placeholder" })}
                                onSelectionChange={(e: any) => {
                                    const selected = Array.from(e.target.items || [])
                                        .filter((item: any) => item.selected)
                                        .map((item: any) => item.text)
                                    setSelectedVariants(selected)
                                }}
                            >
                                {availableVariants.map((variant) => (
                                    <MultiComboBoxItem
                                        key={variant}
                                        text={variant}
                                        selected={selectedVariants.includes(variant)}
                                    />
                                ))}
                            </MultiComboBox>
                        </FlexBox>

                        <Panel 
                            headerText={intl.formatMessage({ id: "editor_header_general_information" })}
                            headerLevel="H6" 
                            collapsed={panelCollapsed}
                            onToggle={(e) => e.detail && setPanelCollapsed(e.detail.collapsed)}
                        >
                            <Form
                                layout="S1 M1 L2 XL2"
                                labelSpan="S10 M4 L4 XL2"
                                className={classes.generalDataForm}
                            >
                                <FormItem labelContent={<Label>{intl.formatMessage({ id: "editor_label_name" })}</Label>}>
                                    <Input
                                        placeholder={treeItemsShown?.name}
                                        value={treeItemsShown?.name}
                                        onChange={(e) => {
                                            editBaseData({
                                                scenarioMixinName: scenarioMixin,
                                                version: version,
                                                name: e.target.attributes.getNamedItem("value")!
                                                    .nodeValue!,
                                            })
                                            if (scenarioMixin != "Scenario") {
                                                setScenarioMixin(
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                                )
                                            }
                                        }}
                                    />
                                </FormItem>
                               {treeItemsShown && "active" in treeItemsShown && (
                                    <FormItem labelContent={<Label>{intl.formatMessage({ id: "editor_label_active" })}</Label>}>
                                        <Switch
                                            onChange={(e) => {
                                                editBaseData({
                                                    scenarioMixinName: scenarioMixin,
                                                    version: version,
                                                    active: e.target.checked!,
                                                })
                                            }}
                                            checked={treeItemsShown?.active}
                                        />
                                    </FormItem>
                                )} 
                                <FormItem labelContent={<Label>{intl.formatMessage({ id: "editor_label_access_object" })}</Label>}>
                                    <Input
                                        placeholder={treeItemsShown?.accessObject}
                                        value={treeItemsShown?.accessObject}
                                        onChange={(e) => {
                                            editBaseData({
                                                scenarioMixinName: scenarioMixin,
                                                version: version,
                                                accessObject:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                            setUpdate(update + 1)
                                        }}
                                    />
                                </FormItem>
                                <FormItem labelContent={<Label>{intl.formatMessage({ id: "editor_label_base_package" })}</Label>}>
                                    <Input
                                        placeholder={treeItemsShown?.basePackage}
                                        value={treeItemsShown?.basePackage}
                                        onChange={(e) => {
                                            editBaseData({
                                                scenarioMixinName: scenarioMixin,
                                                version: version,
                                                basePackage:
                                                    e.target.attributes.getNamedItem("value")!
                                                        .nodeValue!,
                                            })
                                            setUpdate(update + 1)
                                        }}
                                    />
                                </FormItem>
                                {treeItemsShown && "defaultLanguage" in treeItemsShown && (
                                    <FormItem labelContent={<Label>{intl.formatMessage({ id: "editor_label_default_language" })}</Label>}>
                                        <Select
                                            onChange={function Ta(e) {
                                                editBaseData({
                                                    scenarioMixinName: scenarioMixin,
                                                    version: version,
                                                    defaultLanguage:
                                                        e.detail.selectedOption.textContent!.toString(),
                                                })
                                                setUpdate(update + 1)
                                            }}
                                        >
                                            {treeItemsShown.defaultLanguage &&
                                                !(
                                                    Object.keys(treeItemsShown.texts!) as Array<string>
                                                ).includes(treeItemsShown.defaultLanguage) && (
                                                    <Option
                                                        selected={true}
                                                        key={treeItemsShown.defaultLanguage}
                                                    >
                                                        {treeItemsShown.defaultLanguage}
                                                    </Option>
                                                )}
                                            {(Object.keys(treeItemsShown.texts!) as Array<string>)
                                                .sort()
                                                .map((key) => {
                                                    return (
                                                        <Option
                                                            selected={
                                                                key == treeItemsShown?.defaultLanguage
                                                            }
                                                            key={key}
                                                        >
                                                            {key}
                                                        </Option>
                                                    )
                                                })}
                                        </Select>
                                    </FormItem>
                                )}
                            </Form>
                        </Panel>

                        <TabContainer
                            onTabSelect={function _a(e) {
                                setEl(undefined)
                                setElement(undefined)
                                setTab(e.detail.tab.id)
                            }}
                            style={{ width: "100%", marginTop: "0.5rem", marginBottom: "0.5rem" }}
                        >
                            {tabs.map((t) => {
                                return (
                                    <Tab
                                        icon={t.icon}
                                        selected={tab == t.text}
                                        text={intl.formatMessage({ id: `tab_${t.text.toLowerCase()}` })}
                                        key={t.text}
                                        id={t.text}
                                    />
                                )
                            })}
                        </TabContainer>

                        {tab == "Structure" && (
                            <Structure
                                version={version}
                                defaultLanguage={
                                    treeItemsShown && "defaultLanguage" in treeItemsShown
                                        ? treeItemsShown?.defaultLanguage
                                        : undefined
                                }
                                treeItemsShown={treeItemsShown}
                                scenarioMixinName={scenarioMixin}
                                update={update}
                                setNewEl={setNewEl}
                                setIndexesDelete={setIndexesDelete}
                                setAddDialogOpen={setAddElementDialogOpen}
                                setCopyDialogOpen={setCopyDialogOpen}
                                setUpdate={setUpdate}
                                openMessageBox={openMessageBox}
                                setSelectedTreeItem={setSelectedTreeItem}
                                el={el}
                                setEl={setEl}
                                element={element}
                                setElement={setElement}
                                parents={parents}
                                setParents={setParents}
                                copiedEl={copiedEl}
                                setCopiedEl={setCopiedEl}
                                renderTable={renderTable}
                                setRenderTable={setRenderTable}
                                selectedVariants={selectedVariants}
                                registerFlushPendingNameCommit={(fn) => {
                                    flushPendingNameCommitRef.current = fn
                                }}
                            />
                        )}

                        {tab == "Languages" && treeItemsShown && (
                            <LanguagesTab
                                defaultLanguage={
                                    "defaultLanguage" in treeItemsShown
                                        ? treeItemsShown?.defaultLanguage
                                        : undefined
                                }
                                treeItemsShown={treeItemsShown}
                                version={version}
                                update={update}
                                setUpdate={setUpdate}
                                scenarioMixinName={scenarioMixin}
                                setDialogAddLanguageOpen={setDialogAddLanguageOpen}
                                language={language}
                                setLanguage={setLanguage}
                                openMessageBox={openMessageBox}
                            />
                        )}
                    </>
                }
            />
            <AddElementDialog
                dialogOpen={addElementDialogOpen}
                update={update}
                element={element}
                el={el}
                parentEl={parents.length > 1 ? parents[parents.length - 2].elem : undefined}
                treeItemsShown={treeItemsShown}
                selectedTreeItem={selectedTreeItem}
                scenarioMixinName={scenarioMixin}
                setEl={setEl}
                setElement={setElement}
                setUpdate={setUpdate}
                setDialogOpen={setAddElementDialogOpen}
                version={version}
            />
            <AddLanguageDialog
                dialogAddLanguageOpen={dialogAddLanguageOpen}
                languages={
                    treeItemsShown && treeItemsShown.texts
                        ? Object.keys(treeItemsShown.texts).concat(
                            "defaultLanguage" in treeItemsShown &&
                                !Object.keys(treeItemsShown.texts).includes(
                                    treeItemsShown.defaultLanguage!,
                                )
                                ? [treeItemsShown.defaultLanguage!]
                                : [],
                        )
                        : treeItemsShown && "defaultLanguage" in treeItemsShown
                            ? [treeItemsShown.defaultLanguage!]
                            : []
                }
                language={language}
                treeItemsShown={treeItemsShown}
                version={version}
                scenarioMixinName={scenarioMixin}
                update={update}
                setDialogAddLanguageOpen={setDialogAddLanguageOpen}
                setLanguages={() => { }}
                setLanguage={setLanguage}
                setUpdate={setUpdate}
            />
            <CopyDialog
                dialogOpen={copyDialogOpen}
                update={update}
                setUpdate={setUpdate}
                setDialogOpen={setCopyDialogOpen}
                version={version}
                treeItemsShown={treeItemsShown}
                scenarioMixinName={scenarioMixin}
                el={el}
                element={element}
                parentEl={parents.length > 1 ? parents[parents.length - 2].elem : undefined}
                copiedEl={copiedEl}
            />
            <MessageBox
                type={messageBoxType}
                open={messageBoxOpen}
                onClose={(action, escPressed) => {
                    if (action == MessageBoxAction.OK) {
                        if (messageBoxType == MessageBoxType.Confirm) {
                            if (messageBoxAction == "Delete") {
                                removeElement({
                                    indexes: indexesDelete!.indexes
                                        .split("x")
                                        .filter((item) => item)
                                        .join("x"),
                                    version: version,
                                    scenarioMixinName: scenarioMixin,
                                })

                                var texts: any = JSON.parse(JSON.stringify(treeItemsShown?.texts!))
                                var oldName = toPascalCase(indexesDelete!.name)

                                Object.keys(texts).forEach((key) => {
                                    if (texts![key][`${oldName}.short` as any] != undefined) {
                                        delete texts![key][`${oldName}.short`]
                                    }
                                    if (texts![key][`${oldName}.long` as any] != undefined) {
                                        delete texts![key][`${oldName}.long`]
                                    }
                                    if (texts![key][`${oldName}.title` as any] != undefined) {
                                        delete texts![key][`${oldName}.title`]
                                    }
                                    if (texts![key][`${oldName}.doc` as any] != undefined) {
                                        delete texts[key][`${oldName}.doc`]
                                    }
                                })
                                editTexts({
                                    version: version,
                                    texts: texts,
                                    scenarioMixinName: scenarioMixin,
                                })

                                toast(Severity.None, "element_deleted")

                                setUpdate(update + 1)

                            } else if (messageBoxOnConfirm) {
                                messageBoxOnConfirm.fn();
                            }
                        }
                    }
                    setMessageBoxOpen(false)
                    setMessageBoxOnConfirm(undefined)
                }}
            >
                {messageBoxText}
            </MessageBox>
        </>
    )
}