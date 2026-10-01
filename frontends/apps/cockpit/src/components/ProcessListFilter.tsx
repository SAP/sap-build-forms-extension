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
    onValueChange,
    dialogTitle,
}: {
    field: string
    value: string[]
    onValueChange: (v: string[]) => void
    dialogTitle: string
}) {
    const messages = useMessages()
    const loadSuggestions = useProcessStore((state) => state.loadSuggestions)

    const [showDialog, setShowDialog] = useState(false)
    const [isHovered, setHovered] = useState(false)

    const handleOpen = () => setShowDialog(true)

    return (
        <>
            <Input
                style={{ width: "100%" }}
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
                        onValueChange={(v) => mergeFilter({ descriptionValue: v })}
                        dialogTitle={intl.formatMessage({ id: "label_description" })}
                    />
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_functional_id" })}>
                    <SearchHelpInput
                        field="functionalId"
                        value={filter.functionalIdValue ?? []}
                        onValueChange={(v) => mergeFilter({ functionalIdValue: v })}
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
                        onValueChange={(v) => mergeFilter({ additionalInformationValue: v })}
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
