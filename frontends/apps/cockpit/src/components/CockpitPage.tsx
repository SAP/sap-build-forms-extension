import { useEffect, useState, useRef, useReducer, JSX } from "react"

import { useIntl } from "react-intl"

import {
    DynamicPage,
    DynamicPageHeader,
    DynamicPageTitle,
    MessageBoxType,
    Title,
    Toolbar,
    ToolbarButton,
    VariantItemPropTypes,
} from "@ui5/webcomponents-react"

import { getLanguage, Margin, useMessages } from "commons"

import { FilterParams, FILTER_INPUI_TYPES, useProcessStore } from "../state/processes"
import { Settings, useVisualStore } from "../state/visual"
import ProcessListView from "./ProcessListView"
import ProcessListFilter from "./ProcessListFilter"
import ProcessDetailsView from "./ProcessDetailsView"
import ProcessFormView from "./ProcessFormView"

export default function () {
    const intl = useIntl()
    const messages = useMessages()
    const visualState = useVisualStore((state) => state)
    const processState = useProcessStore((state) => state)
    const loadSettings = useVisualStore((state) => state.loadSettings)
    const findProcesses = useProcessStore((state) => state.findProcesses)

    const [messageBoxOpen, setMessageBoxOpen] = useState(false)
    const [messageBoxType, setMessageBoxType] = useState<MessageBoxType>()
    const [messageBoxText, setMessageBoxText] = useState<JSX.Element>(<></>)

    const [searchParametersError, setSearchParametersError] = useState<boolean>(false)
    const [startedByError, setStartedByError] = useState<boolean>(false)
    const [endedOnError, setEndedOnError] = useState<boolean>(false)

    const [selectedVariant, setSelectedVariant] = useState("Standard")
    const [defaultVariant, setDefaultVariant] = useState("Standard")
    const [customVariants, setCustomVariants] = useState<VariantItemPropTypes[]>([])
    const [isDirty, setIsDirty] = useState(false)
    const [checkIfDiry, setCheckIfDirty] = useState(false)

    useEffect(() => {
        loadSettings(messages, getLanguage()).then((data) => {
            // initialize the filter with the settings returned
            processState.initFilter((data as any).data as Settings)
            // after loading settings we can load the processes
            findProcesses(messages, processState.filter)
        })
    }, [])

    const initialVariantValues = useRef<any>({
        Standard: {
            searchParameters: [],
            description: "",
            descriptionType: FILTER_INPUI_TYPES[0],
            functionalId: "",
            functionalIdType: FILTER_INPUI_TYPES[0],
            states: [],
            additionalInformation: "",
            additionalInformationType: FILTER_INPUI_TYPES[0],
            user: "",
            roleUser: [],
            startedBy: "",
            endedOn: "",
            scenario: "",
            visibleFilters: new Set([
                "search_parameter",
                "description",
                "functional_id",
                "status",
                "additional_information",
                "user",
                "role_user",
                "started_by",
                "ended_on",
                "scenario",
            ]),
        },
    })

    const filterReducer = (state: any, action: any) => {
        const { payload, type } = action
        setCheckIfDirty(true)
        switch (type) {
            case "searchParameters":
                return { ...state, searchParameters: payload }
            case "description":
                return { ...state, description: payload }
            case "descriptionType":
                return { ...state, descriptionType: payload }
            case "functionalId":
                return { ...state, functionalId: payload }
            case "functionalIdType":
                return { ...state, functionalIdType: payload }
            case "states":
                return { ...state, states: payload }
            case "additionalInformation":
                return { ...state, additionalInformation: payload }
            case "additionalInformationType":
                return { ...state, additionalInformationType: payload }
            case "user":
                return { ...state, user: payload }
            case "roleUser":
                return { ...state, roleUser: payload }
            case "startedBy":
                return { ...state, startedBy: payload }
            case "endedOn":
                return { ...state, endedOn: payload }
            case "scenario":
                return { ...state, scenario: payload }
            case "reset":
                return {
                    ...state,
                    searchParameters:
                        initialVariantValues.current[selectedVariant].searchParameters,
                    description: initialVariantValues.current[selectedVariant].description,
                    descriptionType: initialVariantValues.current[selectedVariant].descriptionType,
                    functionalId: initialVariantValues.current[selectedVariant].functionalId,
                    functionalIdType:
                        initialVariantValues.current[selectedVariant].functionalIdType,
                    states: initialVariantValues.current[selectedVariant].states,
                    additionalInformation:
                        initialVariantValues.current[selectedVariant].additionalInformation,
                    additionalInformationType:
                        initialVariantValues.current[selectedVariant].additionalInformationType,
                    user: initialVariantValues.current[selectedVariant].user,
                    roleUser: initialVariantValues.current[selectedVariant].roleUser,
                    startedBy: initialVariantValues.current[selectedVariant].startedBy,
                    endedOn: initialVariantValues.current[selectedVariant].endedOn,
                    scenario: initialVariantValues.current[selectedVariant].scenario,
                }
            case "visibility":
                return { ...state, visibleFilters: payload }
            case "changeVariant":
                return payload
            default:
                console.warn("Unknown action type!")
                return state
        }
    }

    const [filters, dispatchFiltersChange] = useReducer(
        filterReducer,
        initialVariantValues.current.Standard,
    )
    const {
        searchParameters,
        description,
        descriptionType,
        functionalId,
        functionalIdType,
        states,
        additionalInformation,
        additionalInformationType,
        user,
        roleUser,
        startedBy,
        endedOn,
        scenario,
        visibleFilters,
    } = filters

    useEffect(() => {
        if (checkIfDiry) {
            const hasChanged = Object.entries(initialVariantValues.current[selectedVariant]).some(
                ([key, val]) => {
                    if (key === "searchParameters") {
                        const searchParametersLength = Object.keys(filters.searchParameters).length
                        if (
                            searchParametersLength > 0 &&
                            Object.keys(val as string).length !== searchParametersLength
                        ) {
                            return true
                        }
                        return Object.entries(filters.searchParameters).some(([code, bool]) => {
                            return (val as any)[code] !== bool
                        })
                    }
                    if (key === "states") {
                        const statesLength = Object.keys(filters.states).length
                        if (
                            statesLength > 0 &&
                            Object.keys(val as string).length !== statesLength
                        ) {
                            return true
                        }
                        return Object.entries(filters.states).some(([code, bool]) => {
                            return (val as any)[code] !== bool
                        })
                    }
                    if (key === "roleUser") {
                        const roleUserLength = Object.keys(filters.roleUser).length
                        if (
                            roleUserLength > 0 &&
                            Object.keys(val as string).length !== roleUserLength
                        ) {
                            return true
                        }
                        return Object.entries(filters.roleUser).some(([code, bool]) => {
                            return (val as any)[code] !== bool
                        })
                    }
                    return filters[key] !== val
                },
            )
            setCheckIfDirty(false)
            setIsDirty(hasChanged)
        }
    }, [checkIfDiry, selectedVariant])

    function refresh(parameterObject: object | undefined) {
        // dispatch(removeProcesses())
        // const p = backendDispatch("/v1/cockpit", "GET", undefined, parameterObject)
        // p.then((action: any) => {
        //     if (action.status == 200) {
        //         dispatch(insertProcesses(action.data))
        //     } else {
        //         openMessageBox(MessageBoxType.Error, <>An error occurred while loading data.</>)
        //     }
        // })
    }

    function filter() {
        var parameters: FilterParams = {}
        if (searchParameters && searchParameters.length > 0) {
            parameters["profiles"] = searchParameters
        }
        if (visibleFilters.has("description") && description && description.trim().length > 0) {
            parameters["descriptionType"] = descriptionType
            parameters["descriptionValue"] = description
        }
        if (visibleFilters.has("functional_id") && functionalId && functionalId.trim().length > 0) {
            parameters["functionalIdType"] = functionalIdType
            parameters["functionalIdValue"] = functionalId
        }
        if (visibleFilters.has("status") && states && states.length > 0) {
            parameters["status"] = states
        }

        if (
            visibleFilters.has("additional_information") &&
            additionalInformation &&
            additionalInformation.trim().length > 0
        ) {
            parameters["additionalInformationType"] = additionalInformationType
            parameters["additionalInformationValue"] = additionalInformation
        }
        if (visibleFilters.has("role_user") && roleUser && roleUser.length > 0) {
            parameters["roleUser"] = roleUser
        }
        if (visibleFilters.has("started_by") && startedBy && startedBy.length > 0) {
            parameters["startedBy"] = startedBy
        }
        if (visibleFilters.has("ended_on") && endedOn && endedOn.length > 0) {
            parameters["endedOn"] = endedOn
        }
        if (visibleFilters.has("scenario") && scenario) {
            parameters["scenario"] = scenario
        }

        refresh(parameters)
    }

    function openMessageBox(mBoxType: MessageBoxType, mBoxText: JSX.Element) {
        setMessageBoxType(mBoxType)
        setMessageBoxText(mBoxText)
        setMessageBoxOpen(true)
    }

    function formatDate(date: Date) {
        function prepend0(number: Number) {
            return number.toString().padStart(2, "0")
        }
        return (
            prepend0(date.getDate()) +
            "." +
            prepend0(date.getMonth() + 1) +
            "." +
            date.getFullYear() +
            " " +
            date.getHours() +
            ":" +
            prepend0(date.getMinutes())
        )
    }

    return (
        <DynamicPage
            style={{ width: "100vw", height: "100vh" }}
            titleArea={
                <DynamicPageTitle
                    heading={<Title level="H1">{intl.formatMessage({ id: "app_title" })}</Title>}
                    subheading={
                        <Title level="H2">
                            {intl.formatMessage(
                                { id: "app_subtitle_" + visualState.view },
                                { ...visualState.selectedProcess },
                            )}
                        </Title>
                    }
                    snappedHeading={
                        <Title level="H1">{intl.formatMessage({ id: "app_title" })}</Title>
                    }
                    snappedSubheading={
                        <Title level="H2">
                            {intl.formatMessage(
                                { id: "app_subtitle_" + visualState.view },
                                { ...visualState.selectedProcess },
                            )}
                        </Title>
                    }
                    navigationBar={
                        <>
                            {visualState.view !== "list" && (
                                <Toolbar design="Transparent">
                                    <ToolbarButton
                                        design="Transparent"
                                        icon="decline"
                                        onClick={() => {
                                            visualState.setView("list")
                                        }}
                                    />
                                </Toolbar>
                            )}
                        </>
                    }
                />
            }
            headerArea={
                <DynamicPageHeader>
                    {visualState.view === "list" && <ProcessListFilter />}
                </DynamicPageHeader>
            }
        >
            <div style={{ marginTop: Margin.MEDIUM }}>
                {visualState.view === "list" && <ProcessListView />}
                {visualState.view === "details" && <ProcessDetailsView />}
                {visualState.view === "form" && <ProcessFormView />}
            </div>
        </DynamicPage>
    )
}
