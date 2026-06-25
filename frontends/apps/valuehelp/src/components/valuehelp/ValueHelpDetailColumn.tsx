import { useIntl } from "react-intl"

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
import FCLLayout from "@ui5/webcomponents-fiori/dist/types/FCLLayout"

import { ValueHelpDef, ValueHelpValue } from "../../features/model"
import ConfigTab from "./TabConfig"
import CurrentValuesTab from "./TabCurrentValues"

interface ValueHelpDetailColumnProps {
    slot?: string
    currentValueHelpDef: ValueHelpDef | undefined
    valueHelpValue: ValueHelpValue | undefined
    language: string | undefined
    availableLanguages: string[]
    edit: boolean
    fullscreen: boolean
    onEdit(): void
    onSave(): void
    onDelete(): void
    onClose(): void
    onFullscreen(): void
    onChangeLanguage(language: string, def: ValueHelpDef): void
    onChangeValueHelpValue(v: ValueHelpValue): void
    onSetDialogAddValueOpen(o: boolean): void
    onChangeLanguages(def: ValueHelpDef): void
    onSetCurrentValueHelpDef(def: ValueHelpDef): void
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
    onSetCurrentValueHelpDef,
}: ValueHelpDetailColumnProps) {
    const intl = useIntl()

    return (
        <div slot={slot} style={{ height: "100%" }}>
        <DynamicPage
            headerArea={
                <DynamicPageHeader>
                    <FlexBox wrap="Wrap">
                        <FlexBox direction="Column">
                            <FlexBox style={{ paddingBlock: 2 }}>
                                <Label>Description:</Label>
                                <Text style={{ marginLeft: "2px", wordBreak: "break-all" }}>
                                    {currentValueHelpDef?.description}
                                </Text>
                            </FlexBox>
                            <FlexBox style={{ paddingBlock: 2 }}>
                                <Label>TTL:</Label>
                                {currentValueHelpDef?.ttl === -1 && (
                                    <Text style={{ marginLeft: "2px", wordBreak: "break-all" }}>
                                        static
                                    </Text>
                                )}
                                {currentValueHelpDef?.ttl === 0 && (
                                    <Text style={{ marginLeft: "2px", wordBreak: "break-all" }}>
                                        refresh
                                    </Text>
                                )}
                                {currentValueHelpDef && currentValueHelpDef.ttl > 0 && (
                                    <Text style={{ marginLeft: "2px", wordBreak: "break-all" }}>
                                        {currentValueHelpDef.ttl} min
                                    </Text>
                                )}
                            </FlexBox>
                            <FlexBox style={{ paddingBlock: 2 }}>
                                <Label>Adapter:</Label>
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
                                onClick={onSave}
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
                <ConfigTab
                    currentValueHelpDef={currentValueHelpDef}
                    setCurrentValueHelpDef={onSetCurrentValueHelpDef}
                    edit={edit}
                    changeLanguages={onChangeLanguages}
                    availableLanguages={availableLanguages}
                    openMessageBox={() => {}}
                />
                {currentValueHelpDef?.adapter === "local" && (
                    <CurrentValuesTab
                        edit={edit}
                        currentValueHelpDef={currentValueHelpDef}
                        valueHelpValue={valueHelpValue}
                        language={language}
                        setCurrentValueHelpDef={onSetCurrentValueHelpDef}
                        changeValueHelpValue={onChangeValueHelpValue}
                        changeLanguage={onChangeLanguage}
                        setDialogAddValueOpen={onSetDialogAddValueOpen}
                    />
                )}
            </TabContainer>
        </DynamicPage>
        </div>
    )
}
