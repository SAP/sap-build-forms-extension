import { useEffect } from "react"

import { useIntl } from "react-intl"
import { useForm } from "react-hook-form"

import {
    DynamicPage,
    DynamicPageHeader,
    DynamicPageTitle,
    FlexBox,
    Label,
    TabContainer,
    Text,
    Title,
    Toolbar,
    ToolbarButton,
} from "@ui5/webcomponents-react"
import ButtonDesign from "@ui5/webcomponents/dist/types/ButtonDesign"

import { ValueHelpDef, ValueHelpValue } from "../../features/model"
import { useValueHelpState } from "../../features/valuehelpstate"
import ValueHelpDefinitionForm from "./ValueHelpDefinitionForm"
import CurrentValuesTab from "./TabCurrentValues"
import { Tab } from "@ui5/webcomponents-react"

interface ValueHelpDetailColumnProps {
    slot?: string
    currentValueHelpDef: ValueHelpDef | undefined
    valueHelpValue: ValueHelpValue | undefined
    language: string | undefined
    availableLanguages: string[]
    edit: boolean
    fullscreen: boolean
    onEdit(): void
    onSave(def: ValueHelpDef): void
    onDelete(): void
    onClose(): void
    onFullscreen(): void
    onChangeLanguage(language: string, def: ValueHelpDef): void
    onChangeValueHelpValue(v: ValueHelpValue): void
    onSetDialogAddValueOpen(o: boolean): void
    onChangeLanguages(def: ValueHelpDef): void
}

export default function ({
    slot,
    currentValueHelpDef,
    valueHelpValue,
    language,
    availableLanguages,
    edit,
    fullscreen,
    onEdit,
    onSave,
    onDelete,
    onClose,
    onFullscreen,
    onChangeLanguage,
    onChangeValueHelpValue,
    onSetDialogAddValueOpen,
    onChangeLanguages,
}: ValueHelpDetailColumnProps) {
    const intl = useIntl()
    const state = useValueHelpState()

    const form = useForm<ValueHelpDef>({ defaultValues: currentValueHelpDef ?? {} as ValueHelpDef })

    // Sync form values whenever the selected definition changes.
    useEffect(() => {
        if (currentValueHelpDef) {
            form.reset(currentValueHelpDef)
        }
    }, [currentValueHelpDef])

    return (
        <div slot={slot} style={{ height: "100%" }}>
        <DynamicPage
            headerArea={
                <DynamicPageHeader>
                    <FlexBox wrap="Wrap">
                        <FlexBox direction="Column">
                            <FlexBox style={{ paddingBlock: 2 }}>
                                <Label>{intl.formatMessage({ id: "lbl_description" })}:</Label>
                                <Text style={{ marginLeft: "2px", wordBreak: "break-all" }}>
                                    {currentValueHelpDef?.description}
                                </Text>
                            </FlexBox>
                            <FlexBox style={{ paddingBlock: 2 }}>
                                <Label>{intl.formatMessage({ id: "lbl_ttl" })}:</Label>
                                {currentValueHelpDef?.ttl === -1 && (
                                    <Text style={{ marginLeft: "2px", wordBreak: "break-all" }}>
                                        {intl.formatMessage({ id: "lbl_ttl_static" })}
                                    </Text>
                                )}
                                {currentValueHelpDef?.ttl === 0 && (
                                    <Text style={{ marginLeft: "2px", wordBreak: "break-all" }}>
                                        {intl.formatMessage({ id: "lbl_ttl_refresh" })}
                                    </Text>
                                )}
                                {currentValueHelpDef && currentValueHelpDef.ttl > 0 && (
                                    <Text style={{ marginLeft: "2px", wordBreak: "break-all" }}>
                                        {currentValueHelpDef.ttl} min
                                    </Text>
                                )}
                            </FlexBox>
                            <FlexBox style={{ paddingBlock: 2 }}>
                                <Label>{intl.formatMessage({ id: "lbl_adapter" })}:</Label>
                                <Text style={{ marginLeft: "2px", wordBreak: "break-all" }}>
                                    {currentValueHelpDef?.adapter}
                                </Text>
                            </FlexBox>
                        </FlexBox>
                    </FlexBox>
                </DynamicPageHeader>
            }
            titleArea={
                <DynamicPageTitle
                    actionsBar={
                        <Toolbar design="Transparent">
                            <ToolbarButton
                                design="Transparent"
                                onClick={onEdit}
                                text={intl.formatMessage({
                                    id: edit ? "common_show" : "common_edit",
                                })}
                            />
                            <ToolbarButton
                                design="Transparent"
                                onClick={onDelete}
                                text={intl.formatMessage({ id: "common_delete" })}
                            />
                            <ToolbarButton
                                icon="save"
                                onClick={() => form.handleSubmit(onSave)()}
                                design="Emphasized"
                                text={intl.formatMessage({ id: "common_save" })}
                            />
                        </Toolbar>
                    }
                    heading={<Title>{currentValueHelpDef?.id}</Title>}
                    snappedHeading={<Title>{currentValueHelpDef?.id}</Title>}
                    navigationBar={
                        <Toolbar design="Transparent">
                            <ToolbarButton
                                icon={fullscreen ? "exit-full-screen" : "full-screen"}
                                design={ButtonDesign.Transparent}
                                onClick={onFullscreen}
                            />
                            <ToolbarButton
                                icon="decline"
                                design={ButtonDesign.Transparent}
                                onClick={onClose}
                            />
                        </Toolbar>
                    }
                />
            }
        >
            <TabContainer
                contentBackgroundDesign="Solid"
                headerBackgroundDesign="Solid"
                style={{ width: "100%" }}
                tabLayout="Standard"
            >
                <Tab icon="settings" selected text="Config">
                    <ValueHelpDefinitionForm
                        isNew={false}
                        editMode={edit}
                        availableLanguages={availableLanguages}
                        availableAdapters={state.adapters}
                        changeLanguages={onChangeLanguages}
                        form={form}
                    />
                </Tab>
                {currentValueHelpDef?.adapter === "local" && (
                    <CurrentValuesTab
                            edit={edit}
                            currentValueHelpDef={currentValueHelpDef}
                            valueHelpValue={valueHelpValue}
                            language={language}
                            changeValueHelpValue={onChangeValueHelpValue}
                            changeLanguage={onChangeLanguage}
                            setDialogAddValueOpen={onSetDialogAddValueOpen}                  />
                )}
            </TabContainer>
        </DynamicPage>
        </div>
    )
}
