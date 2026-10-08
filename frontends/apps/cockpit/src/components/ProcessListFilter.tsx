import { useState } from "react"
import { useIntl } from "react-intl"

import {
    DateRangePicker,
    FlexBox,
    Grid,
    Icon,
    Input,
    InputDomRef,
    Label,
    MultiComboBox,
    MultiComboBoxItem,
    Option,
    Select,
    Ui5CustomEvent,
} from "@ui5/webcomponents-react"

import "@ui5/webcomponents-icons/dist/value-help.js"

import { useMessages } from "commons"
import { PROCESS_STATES, useProcessStore } from "../state/processes"
import { useVisualStore } from "../state/visual"
import SearchDialog from "./SearchDialog"

const INPUT_TYPES = ["equals", "contains", "begins_with", "ends_with"] as const
type InputType = typeof INPUT_TYPES[number]

function FilterField({ label, required, children }: { label: string; required?: boolean; children: React.ReactNode }) {
    return (
        <FlexBox direction="Column" style={{ width: "100%", gap: "0.25rem" }}>
            <Label required={required}>{label}</Label>
            {children}
        </FlexBox>
    )
}

function SearchHelpInput({
    field,
    value,
    inputType,
    onValueChange,
    onTypeChange,
    dialogTitle,
}: {
    field: string
    value: string[]
    inputType: InputType
    onValueChange: (v: string[]) => void
    onTypeChange: (t: InputType) => void
    dialogTitle: string
}) {
    const intl = useIntl()
    const messages = useMessages()
    const loadSuggestions = useProcessStore((state) => state.loadSuggestions)

    const [showDialog, setShowDialog] = useState(false)
    const [isHovered, setHovered] = useState(false)

    const handleOpen = () => setShowDialog(true)

    return (
        <>
            <FlexBox style={{ width: "100%", gap: "0.25rem" }}>
                <Select
                    style={{ width: "40%", flexShrink: 0 }}
                    onChange={(e) => onTypeChange(e.detail.selectedOption.dataset.key as InputType)}
                >
                    {INPUT_TYPES.map((t) => (
                        <Option key={t} data-key={t} selected={inputType === t}>
                            {intl.formatMessage({ id: "input_type_" + t })}
                        </Option>
                    ))}
                </Select>
                <Input
                    style={{ flex: 1 }}
                    value={value.length <= 1 ? (value[0] ?? "") : `${value[0]} (+${value.length - 1})`}
                    readonly={value.length > 1}
                    onInput={(e: Ui5CustomEvent<InputDomRef>) =>
                        onValueChange(e.target.value ? [e.target.value] : [])
                    }
                    icon={
                        <Icon
                            name="value-help"
                            onClick={handleOpen}
                            style={{ boxShadow: isHovered ? "var(--sapField_Hover_Shadow)" : "none" }}
                        />
                    }
                    onMouseOver={() => setHovered(true)}
                    onMouseLeave={() => setHovered(false)}
                    onKeyDown={(e) => {
                        if (e.key === "F4") {
                            e.preventDefault()
                            handleOpen()
                        }
                    }}
                />
            </FlexBox>
            {showDialog && (
                <SearchDialog
                    title={dialogTitle}
                    onSearch={(search) => loadSuggestions(messages, field, search)}
                    onSelect={(values) => onValueChange(values)}
                    onClose={() => setShowDialog(false)}
                />
            )}
        </>
    )
}

export default function () {
    const intl = useIntl()
    const filter = useProcessStore((state) => state.filter)
    const mergeFilter = useProcessStore((state) => state.mergeFilter)
    const settings = useVisualStore((state) => state.settings)

    const [startedByError, setStartedByError] = useState(false)
    const [endedOnError, setEndedOnError] = useState(false)

    return (
        <Grid>
                <FilterField label={intl.formatMessage({ id: "label_profiles" })} required>
                    <MultiComboBox
                        style={{ width: "100%" }}
                        onSelectionChange={(e) =>
                            mergeFilter({ profiles: e.detail.items.map((item) => item.dataset.key!) })
                        }
                        valueState={filter.profiles && filter.profiles.length > 0 ? "None" : "Negative"}
                        valueStateMessage={
                            filter.profiles && filter.profiles.length > 0 ? undefined : (
                                <span>{intl.formatMessage({ id: "common_error_required" }, { name: "" })}</span>
                            )
                        }
                    >
                        {settings?.profiles.map((p) => (
                            <MultiComboBoxItem key={p.id} data-key={p.id} text={p.name} selected={p.selected} />
                        ))}
                    </MultiComboBox>
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_description" })}>
                    <SearchHelpInput
                        field="description"
                        value={filter.descriptionValue ?? []}
                        inputType={(filter.descriptionType as InputType) ?? "contains"}
                        onValueChange={(v) => mergeFilter({ descriptionValue: v })}
                        onTypeChange={(t) => mergeFilter({ descriptionType: t })}
                        dialogTitle={intl.formatMessage({ id: "label_description" })}
                    />
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_functional_id" })}>
                    <SearchHelpInput
                        field="functionalId"
                        value={filter.functionalIdValue ?? []}
                        inputType={(filter.functionalIdType as InputType) ?? "contains"}
                        onValueChange={(v) => mergeFilter({ functionalIdValue: v })}
                        onTypeChange={(t) => mergeFilter({ functionalIdType: t })}
                        dialogTitle={intl.formatMessage({ id: "label_functional_id" })}
                    />
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_status" })}>
                    <MultiComboBox
                        style={{ width: "100%" }}
                        onSelectionChange={(e) =>
                            mergeFilter({ status: e.detail.items.map((item) => item.dataset.key!) })
                        }
                    >
                        {PROCESS_STATES.map((s) => (
                            <MultiComboBoxItem
                                key={s.id}
                                data-key={s.id}
                                selected={filter.status?.includes(s.id) ?? false}
                                text={intl.formatMessage({ id: "process_state_" + s.id })}
                            />
                        ))}
                    </MultiComboBox>
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_additional_information" })}>
                    <SearchHelpInput
                        field="additionalInformation"
                        value={filter.additionalInformationValue ?? []}
                        inputType={(filter.additionalInformationType as InputType) ?? "contains"}
                        onValueChange={(v) => mergeFilter({ additionalInformationValue: v })}
                        onTypeChange={(t) => mergeFilter({ additionalInformationType: t })}
                        dialogTitle={intl.formatMessage({ id: "label_additional_information" })}
                    />
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_user" })}>
                    <Input
                        style={{ width: "100%" }}
                        value={filter.user ?? ""}
                        onInput={(e: Ui5CustomEvent<InputDomRef>) =>
                            mergeFilter({ user: e.target.value })
                        }
                    />
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "role_user" })}>
                    <MultiComboBox
                        style={{ width: "100%" }}
                        onSelectionChange={(e) =>
                            mergeFilter({ roleUser: e.detail.items.map((item) => item.dataset.key!) })
                        }
                    >
                        <MultiComboBoxItem
                            text={intl.formatMessage({ id: "role_user_started" })}
                            data-key="role_user_started"
                            selected={filter.roleUser?.includes("role_user_started") ?? false}
                        />
                        <MultiComboBoxItem
                            text={intl.formatMessage({ id: "role_user_involved" })}
                            data-key="role_user_involved"
                            selected={filter.roleUser?.includes("role_user_involved") ?? false}
                        />
                    </MultiComboBox>
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_started_at" })}>
                    <DateRangePicker
                        style={{ width: "100%" }}
                        value={filter.startedBy ?? ""}
                        onChange={(e) => {
                            setStartedByError(!e.detail.valid)
                            mergeFilter({ startedBy: e.detail.value })
                        }}
                        primaryCalendarType="Gregorian"
                        valueState={startedByError ? "Negative" : "None"}
                        valueStateMessage={
                            startedByError ? (
                                <span>{intl.formatMessage({ id: "common_error_date" })}</span>
                            ) : undefined
                        }
                    />
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_finished_at" })}>
                    <DateRangePicker
                        style={{ width: "100%" }}
                        value={filter.endedOn ?? ""}
                        onChange={(e) => {
                            setEndedOnError(!e.detail.valid)
                            mergeFilter({ endedOn: e.detail.value })
                        }}
                        primaryCalendarType="Gregorian"
                        valueState={endedOnError ? "Negative" : "None"}
                        valueStateMessage={
                            endedOnError ? (
                                <span>{intl.formatMessage({ id: "common_error_date" })}</span>
                            ) : undefined
                        }
                    />
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "scenario" })}>
                    <Select
                        style={{ width: "100%" }}
                        onChange={(e) =>
                            mergeFilter({ scenario: e.detail.selectedOption.dataset.key ?? undefined })
                        }
                    >
                        <Option data-key="..." selected={!filter.scenario || filter.scenario === "..."} />
                        {settings?.scenarios.map((s) => (
                            <Option key={s} data-key={s} selected={filter.scenario === s}>{s}</Option>
                        ))}
                    </Select>
                </FilterField>
        </Grid>
    )
}
