import { useIntl } from "react-intl"

import {
    FilterBar,
    FilterGroupItem,
    FlexBox,
    Input,
    MultiComboBox,
    MultiComboBoxItem,
    Option,
    Select,
} from "@ui5/webcomponents-react"

import { useMessages } from "commons"

import { useProcessStore } from "../state/processes"
import { useVisualStore } from "../state/visual"

export default function () {
    const intl = useIntl()
    const messages = useMessages()
    const filter = useProcessStore((state) => state.filter)
    const setFilter = useProcessStore((state) => state.setFilter)
    const findProcesses = useProcessStore((state) => state.findProcesses)
    const settings = useVisualStore((state) => state.settings)

    return (
        <FilterBar
            onClear={() => {
                setFilter({})
            }}
            onGo={() => {
                findProcesses(messages, filter)
            }}
            showResetButton
            showClearOnFB
            showGoOnFB
            hideFilterConfiguration={true}
            hideToggleFiltersButton={true}
        >
            <FilterGroupItem
                filterKey="profiles"
                active={true}
                hiddenInFilterBar={false}
                label={intl.formatMessage({
                    id: "label_profiles",
                })}
                required={true}
            >
                <MultiComboBox
                    onSelectionChange={(e) => {
                        setFilter({
                            ...filter,
                            profiles: e.detail.items.map((item) => item.id),
                        })
                    }}
                    valueState={filter.profiles && filter.profiles.length > 0 ? "None" : "Negative"}
                    valueStateMessage={
                        filter.profiles && filter.profiles.length > 0 ? undefined : (
                            <span>
                                {intl.formatMessage({ id: "common_error_required" }, { name: "" })}
                            </span>
                        )
                    }
                    style={{ width: "40em" }}
                >
                    {settings?.profiles.map((p) => (
                        <MultiComboBoxItem key={p.id} text={p.name} selected={p.selected} />
                    ))}
                </MultiComboBox>
            </FilterGroupItem>

            {/* <FilterGroupItem
                filterKey="description"
                active={!!description}
                hiddenInFilterBar={!visibleFilters.has("description")}
                label={intl.formatMessage({ id: "description" })}
                style={{ minWidth: "20em" }}
            >
                <FlexBox>
                    <Select
                        style={{ minWidth: "9em" }}
                        onChange={(e) => {
                            dispatchFiltersChange({
                                type: "descriptionType",
                                payload: e.detail.selectedOption.id,
                            })
                        }}
                    >
                        {FILTER_INPUI_TYPES.map((input_type) => {
                            return (
                                <Option
                                    key={input_type}
                                    id={input_type}
                                    selected={descriptionType === input_type}
                                >
                                    {intl.formatMessage({
                                        id: "input_type_" + input_type,
                                    })}
                                </Option>
                            )
                        })}
                    </Select>
                    <Input
                        style={{ width: "100%" }}
                        value={description}
                        onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            dispatchFiltersChange({
                                type: "description",
                                payload: e.target.attributes.getNamedItem("value")!.nodeValue!,
                            })
                        }}
                    />
                </FlexBox>
            </FilterGroupItem> */}

            {/* <FilterGroupItem
                filterKey="functional_id"
                active={!!functionalId}
                hiddenInFilterBar={!visibleFilters.has("functional_id")}
                label={intl.formatMessage({ id: "functional_id" })}
                style={{ minWidth: "20em" }}
            >
                <FlexBox>
                    <Select
                        style={{ minWidth: "9em" }}
                        onChange={(e) => {
                            dispatchFiltersChange({
                                type: "functionalIdType",
                                payload: e.detail.selectedOption.id,
                            })
                        }}
                    >
                        {FILTER_INPUI_TYPES.map((input_type) => {
                            return (
                                <Option
                                    key={input_type}
                                    id={input_type}
                                    selected={functionalIdType === input_type}
                                >
                                    {intl.formatMessage({
                                        id: "input_type_" + input_type,
                                    })}
                                </Option>
                            )
                        })}
                    </Select>
                    <Input
                        style={{ width: "100%" }}
                        value={functionalId}
                        onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            dispatchFiltersChange({
                                type: "functionalId",
                                payload: e.target.attributes.getNamedItem("value")!.nodeValue!,
                            })
                        }}
                    />
                </FlexBox>
            </FilterGroupItem> */}

            {/* <FilterGroupItem
                filterKey="status"
                active={states.length > 0}
                hiddenInFilterBar={!visibleFilters.has("status")}
                label={intl.formatMessage({ id: "status" })}
            >
                <MultiComboBox
                    onSelectionChange={(e) => {
                        dispatchFiltersChange({
                            type: "states",
                            payload: e.detail.items.map((item) => item.id),
                        })
                    }}
                >
                    {PROCESS_STATES.map((process_state) => {
                        return (
                            <MultiComboBoxItem
                                key={process_state.id}
                                id={process_state.id}
                                selected={states.includes(process_state.id)}
                                text={intl.formatMessage({
                                    id: "process_state_" + process_state.id,
                                })}
                            />
                        )
                    })}
                </MultiComboBox>
            </FilterGroupItem> */}

            {/* <FilterGroupItem
                filterKey="additional_information"
                active={!!additionalInformation}
                hiddenInFilterBar={!visibleFilters.has("additional_information")}
                label={intl.formatMessage({
                    id: "additional_information",
                })}
                style={{ minWidth: "20em" }}
            >
                <FlexBox>
                    <Select
                        style={{ minWidth: "9em" }}
                        onChange={(e) => {
                            dispatchFiltersChange({
                                type: "additionalInformationType",
                                payload: e.detail.selectedOption.id,
                            })
                        }}
                    >
                        {FILTER_INPUI_TYPES.map((input_type) => {
                            return (
                                <Option
                                    key={input_type}
                                    id={input_type}
                                    selected={additionalInformationType === input_type}
                                >
                                    {intl.formatMessage({
                                        id: "input_type_" + input_type,
                                    })}
                                </Option>
                            )
                        })}
                    </Select>
                    <Input
                        style={{ width: "100%" }}
                        value={additionalInformation}
                        onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                            dispatchFiltersChange({
                                type: "additionalInformation",
                                payload: e.target.attributes.getNamedItem("value")!.nodeValue!,
                            })
                        }}
                    />
                </FlexBox>
            </FilterGroupItem> */}

            {/* <FilterGroupItem
                filterKey="user"
                active={!!user}
                hiddenInFilterBar={!visibleFilters.has("user")}
                label={intl.formatMessage({ id: "user" })}
            >
                <Input
                    value={user}
                    onChange={(e: Ui5CustomEvent<InputDomRef, never>) => {
                        dispatchFiltersChange({
                            type: "user",
                            payload: e.target.attributes.getNamedItem("value")!.nodeValue!,
                        })
                    }}
                />
            </FilterGroupItem> */}

            {/* <FilterGroupItem
                filterKey="role_user"
                active={roleUser.length > 0}
                hiddenInFilterBar={!visibleFilters.has("role_user")}
                label={intl.formatMessage({ id: "role_user" })}
            >
                <MultiComboBox
                    onSelectionChange={(e) => {
                        dispatchFiltersChange({
                            type: "roleUser",
                            payload: e.detail.items.map((item) => item.id),
                        })
                    }}
                >
                    <MultiComboBoxItem
                        text={intl.formatMessage({
                            id: "role_user_started",
                        })}
                        id="role_user_started"
                        selected={roleUser.includes("role_user_started")}
                    />
                    <MultiComboBoxItem
                        text={intl.formatMessage({
                            id: "role_user_involved",
                        })}
                        id="role_user_involved"
                        selected={roleUser.includes("role_user_involved")}
                    />
                </MultiComboBox>
            </FilterGroupItem> */}

            {/* <FilterGroupItem
                filterKey="started_by"
                active={!!startedBy}
                hiddenInFilterBar={!visibleFilters.has("started_by")}
                label={intl.formatMessage({ id: "started_by" })}
                style={{ minWidth: "15em" }}
            >
                <DateRangePicker
                    style={{ minWidth: "auto" }}
                    value={startedBy}
                    onChange={function _a(e) {
                        if (e.detail.valid == true) {
                            setStartedByError(false)
                            dispatchFiltersChange({
                                type: "startedBy",
                                payload: e.detail.value,
                            })
                        } else {
                            setStartedByError(true)
                            dispatchFiltersChange({
                                type: "startedBy",
                                payload: e.detail.value,
                            })
                        }
                    }}
                    primaryCalendarType="Gregorian"
                    valueState={startedByError ? "Negative" : "None"}
                    valueStateMessage={
                        startedByError && (
                            <span>
                                {intl.formatMessage({
                                    id: "common_error_date",
                                })}
                            </span>
                        )
                    }
                />
            </FilterGroupItem> */}

            {/* <FilterGroupItem
                filterKey="ended_on"
                active={!!endedOn}
                hiddenInFilterBar={!visibleFilters.has("ended_on")}
                label={intl.formatMessage({ id: "ended_on" })}
                style={{ minWidth: "15em" }}
            >
                <DateRangePicker
                    style={{ minWidth: "auto" }}
                    value={endedOn}
                    onChange={function _a(e) {
                        if (e.detail.valid == true) {
                            setEndedOnError(false)
                            dispatchFiltersChange({
                                type: "endedOn",
                                payload: e.detail.value,
                            })
                        } else {
                            setEndedOnError(true)
                            dispatchFiltersChange({
                                type: "endedOn",
                                payload: e.detail.value,
                            })
                        }
                    }}
                    primaryCalendarType="Gregorian"
                    valueState={endedOnError ? "Negative" : "None"}
                    valueStateMessage={
                        endedOnError && (
                            <span>
                                {intl.formatMessage({
                                    id: "common_error_date",
                                })}
                            </span>
                        )
                    }
                />
            </FilterGroupItem> */}

            {/* <FilterGroupItem
                filterKey="scenario"
                active={!!scenario}
                hiddenInFilterBar={!visibleFilters.has("scenario")}
                label={intl.formatMessage({ id: "scenario" })}
            >
                <Select
                    onChange={(e) => {
                        const { selectedOption } = e.detail
                        dispatchFiltersChange({
                            type: "scenario",
                            payload: selectedOption.textContent,
                        })
                    }}
                >
                    <Option id="..." selected={scenario == "..."} />
                </Select>
            </FilterGroupItem> */}
        </FilterBar>
    )
}
