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
    const setFilter = useProcessStore((state) => state.setFilter)
    const settings = useVisualStore((state) => state.settings)

    const [startedByError, setStartedByError] = useState(false)
    const [endedOnError, setEndedOnError] = useState(false)

    return (
        <Grid>
                <FilterField label={intl.formatMessage({ id: "label_profiles" })} required>
                    <MultiComboBox
                        style={{ width: "100%" }}
                        onSelectionChange={(e) =>
                            setFilter({ ...filter, profiles: e.detail.items.map((item) => item.id) })
                        }
                        valueState={filter.profiles && filter.profiles.length > 0 ? "None" : "Negative"}
                        valueStateMessage={
                            filter.profiles && filter.profiles.length > 0 ? undefined : (
                                <span>{intl.formatMessage({ id: "common_error_required" }, { name: "" })}</span>
                            )
                        }
                    >
                        {settings?.profiles.map((p) => (
                            <MultiComboBoxItem key={p.id} id={p.id} text={p.name} selected={p.selected} />
                        ))}
                    </MultiComboBox>
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_description" })}>
                    <SearchHelpInput
                        field="description"
                        value={filter.descriptionValue ?? []}
                        onValueChange={(v) => setFilter({ ...filter, descriptionValue: v })}
                        dialogTitle={intl.formatMessage({ id: "label_description" })}
                    />
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_functional_id" })}>
                    <SearchHelpInput
                        field="functionalId"
                        value={filter.functionalIdValue ?? []}
                        onValueChange={(v) => setFilter({ ...filter, functionalIdValue: v })}
                        dialogTitle={intl.formatMessage({ id: "label_functional_id" })}
                    />
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_status" })}>
                    <MultiComboBox
                        style={{ width: "100%" }}
                        onSelectionChange={(e) =>
                            setFilter({ ...filter, status: e.detail.items.map((item) => item.id) })
                        }
                    >
                        {PROCESS_STATES.map((s) => (
                            <MultiComboBoxItem
                                key={s.id}
                                id={s.id}
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
                        onValueChange={(v) => setFilter({ ...filter, additionalInformationValue: v })}
                        dialogTitle={intl.formatMessage({ id: "label_additional_information" })}
                    />
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_user" })}>
                    <Input
                        style={{ width: "100%" }}
                        value={filter.user ?? ""}
                        onInput={(e: Ui5CustomEvent<InputDomRef>) =>
                            setFilter({ ...filter, user: e.target.value })
                        }
                    />
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "role_user" })}>
                    <MultiComboBox
                        style={{ width: "100%" }}
                        onSelectionChange={(e) =>
                            setFilter({ ...filter, roleUser: e.detail.items.map((item) => item.id) })
                        }
                    >
                        <MultiComboBoxItem
                            text={intl.formatMessage({ id: "role_user_started" })}
                            id="role_user_started"
                            selected={filter.roleUser?.includes("role_user_started") ?? false}
                        />
                        <MultiComboBoxItem
                            text={intl.formatMessage({ id: "role_user_involved" })}
                            id="role_user_involved"
                            selected={filter.roleUser?.includes("role_user_involved") ?? false}
                        />
                    </MultiComboBox>
                </FilterField>

                <FilterField label={intl.formatMessage({ id: "label_started_by" })}>
                    <DateRangePicker
                        style={{ width: "100%" }}
                        value={filter.startedBy ?? ""}
                        onChange={(e) => {
                            setStartedByError(!e.detail.valid)
                            setFilter({ ...filter, startedBy: e.detail.value })
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

                <FilterField label={intl.formatMessage({ id: "label_ended_on" })}>
                    <DateRangePicker
                        style={{ width: "100%" }}
                        value={filter.endedOn ?? ""}
                        onChange={(e) => {
                            setEndedOnError(!e.detail.valid)
                            setFilter({ ...filter, endedOn: e.detail.value })
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
                            setFilter({
                                ...filter,
                                scenario: e.detail.selectedOption.textContent ?? undefined,
                            })
                        }
                    >
                        <Option id="..." selected={filter.scenario === "..."} />
                    </Select>
                </FilterField>
        </Grid>
    )
}
