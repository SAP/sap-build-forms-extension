import { Dispatch, PayloadAction, ThunkDispatch, UnknownAction } from "@reduxjs/toolkit"
import { AxiosResponse } from "axios"

import { MessageIntf } from "commons"

import { Definition, UIElement, UserEventType } from "../../features/sessions/definitions"
import {
    CurrencyAmount,
    DateRange,
    DocFormData,
    FormService,
    TableInfo,
} from "../../features/sessions/forms"
import { useAppSelector } from "../../features/store"
import { SessionResponse, triggerEvent } from "../../features/sessions/sessionActions"

import ButtonControl from "./ButtonControl"
import CurrencyControl from "./CurrencyControl"
import FormControl from "./FormControl"
import GroupControl from "./GroupControl"
import InputControl from "./InputControl"
import SelectControl from "./SelectControl"
import TableControl from "./TableControl"
import ToolbarControl from "./ToolbarControl"
import CheckboxControl from "./CheckboxControl"
import DummyControl from "./DummyControl"
import MultiSelectControl from "./MultiSelectControl"
import EditorControl from "./EditorControl"
import RadioButtonsControl from "./RadioButtonsControl"
import SearchHelpControl from "./SearchHelpControl"
import TextControl from "./TextControl"
import WizardControl from "./WizardControl"
import AlertControl from "./AlertControl"
import SegmentControl from "./SegmentControl"
import AttachmentControl from "./AttachmentControl"
import { SessionState, ValuehelpsState } from "../../features/states"
import { update } from "../../features/sessions/sessionSlice"
import { ElementProp } from "../../features/sessions/journal"
import DateRangeControl from "./DateRangeControl"
import DialogControl from "./DialogControl"
import AutocompleteControl from "./AutoCompleteControl"
import IconControl from "./IconControl"
import ImageControl from "./ImageControl"
import LinkControl from "./LinkControl"
import DocFormControl from "./DocFormControl"
import { ValuehelpsService, ValueName } from "../../features/valuehelps/logic"

/**
 *
 * @param dispatch
 * @param def
 * @param rowId
 * @param messages
 */
export function handleLeaveFocus(
    dispatch: ThunkDispatch<
        {
            session: SessionState
            valuehelps: ValuehelpsState
        },
        undefined,
        UnknownAction
    > &
        Dispatch<UnknownAction>,
    def: Definition,
    rowId: string | undefined,
    messages: MessageIntf,
): Promise<any> {
    return dispatch(triggerEvent({ type: UserEventType.LeaveFocus, def, rowId, messages }))
}

/**
 *
 * @param dispatch
 * @param def
 * @param rowId
 * @param messages
 */
export function handleEnterFocus(
    dispatch: ThunkDispatch<
        {
            session: SessionState
            valuehelps: ValuehelpsState
        },
        undefined,
        UnknownAction
    > &
        Dispatch<UnknownAction>,
    def: Definition,
    rowId: string | undefined,
    messages: MessageIntf,
): Promise<any> {
    return dispatch(triggerEvent({ type: UserEventType.EnterFocus, def, rowId, messages }))
}

/**
 *
 * @param value
 */
export function handleChange(
    dispatch: ThunkDispatch<
        {
            session: SessionState
            valuehelps: ValuehelpsState
        },
        undefined,
        UnknownAction
    > &
        Dispatch<UnknownAction>,
    def: Definition,
    rowId: string | undefined,
    messages: MessageIntf,
    value:
        | string
        | boolean
        | DateRange
        | TableInfo
        | CurrencyAmount
        | DocFormData
        | number
        | undefined,
): Promise<any> {
    dispatch(
        update({
            def,
            rowId,
            prop: ElementProp.Message,
            value: undefined,
        }),
    )
    dispatch(
        update({
            def,
            rowId,
            prop: ElementProp.Value,
            value,
        }),
    )
    return dispatch(triggerEvent({ type: UserEventType.Change, def, rowId, messages }))
}

/**
 *
 * @param dispatch
 * @param def
 * @param rowId
 * @param messages
 */
export function handleAction(
    dispatch: ThunkDispatch<
        {
            session: SessionState
            valuehelps: ValuehelpsState
        },
        undefined,
        UnknownAction
    > &
        Dispatch<UnknownAction>,
    def: Definition,
    rowId: string | undefined,
    messages: MessageIntf,
): Promise<any> {
    return dispatch(triggerEvent({ type: UserEventType.Action, def, rowId, messages }))
}

/**
 *
 * @param dispatch
 * @param def
 * @param rowId
 * @param messages
 * @returns
 */
export function handleOpen(
    dispatch: ThunkDispatch<
        {
            session: SessionState
            valuehelps: ValuehelpsState
        },
        undefined,
        UnknownAction
    > &
        Dispatch<UnknownAction>,
    def: Definition,
    rowId: string | undefined,
    messages: MessageIntf,
): Promise<any> {
    return dispatch(triggerEvent({ type: UserEventType.Open, def, rowId, messages }))
}

/**
 *
 * @param dispatch
 * @param def
 * @param rowId
 * @param messages
 * @returns
 */
export async function handleChangeTablePageSize(
    dispatch: ThunkDispatch<
        {
            session: SessionState
            valuehelps: ValuehelpsState
        },
        undefined,
        UnknownAction
    > &
        Dispatch<UnknownAction>,
    def: Definition,
    rowId: string | undefined,
    messages: MessageIntf,
    value: number,
): Promise<any> {
    dispatch(
        update({
            def,
            rowId,
            value,
            forced: true,
            prop: ElementProp.PageSize,
        }),
    )
    return dispatch(triggerEvent({ type: UserEventType.Browse, def, rowId, messages }))
}

/**
 *
 * @param dispatch
 * @param def
 * @param rowId
 * @param messages
 * @returns
 */
export async function handleBrowseTable(
    dispatch: ThunkDispatch<
        {
            session: SessionState
            valuehelps: ValuehelpsState
        },
        undefined,
        UnknownAction
    > &
        Dispatch<UnknownAction>,
    def: Definition,
    rowId: string | undefined,
    messages: MessageIntf,
    value: number,
): Promise<any> {
    dispatch(
        update({
            def,
            rowId,
            value,
            forced: true,
            prop: ElementProp.Position,
        }),
    )
    return dispatch(triggerEvent({ type: UserEventType.Browse, def, rowId, messages }))
}

/**
 *
 * @param dispatch
 * @param def
 * @param rowId
 * @param messages
 * @param value
 * @returns
 */
export async function handleSortTable(
    dispatch: ThunkDispatch<
        {
            session: SessionState
            valuehelps: ValuehelpsState
        },
        undefined,
        UnknownAction
    > &
        Dispatch<UnknownAction>,
    def: Definition,
    rowId: string | undefined,
    messages: MessageIntf,
    sortField: string,
    sortOrder: "asc" | "desc",
): Promise<any> {
    dispatch(
        update({
            def,
            rowId,
            value: sortField,
            forced: true,
            prop: ElementProp.SortField,
        }),
    )
    return dispatch(triggerEvent({ type: UserEventType.Sort, def, rowId, messages }))
}

/**
 * handleValueHelp is a function that handles value helps for a given definition. It checks if the
 * definition has a value help and if it exists in the provided vhs record. If so, it loads the value help options
 * from the local store and updates the state accordingly.
 *
 * @param def The definition object containing metadata about the element.
 * @param vhs A record of value helps.
 * @param locale The locale to use for loading value help options.
 * @param setOptions A function to update the options for the value help.
 * @param setElementDisabled A function to update the disabled state of the element.
 */
export function handleValueHelp(
    def: Definition,
    vhs: Record<string, boolean>,
    locale: string,
    setOptions: React.Dispatch<React.SetStateAction<ValueName[]>>,
    setElementDisabled: React.Dispatch<React.SetStateAction<boolean>>,
): void {
    // console.log(`SelectControl: def=${def.id} with vh=${def.vh?.name} and locale=${locale}`)
    if (def.vh && vhs[def.vh.name]) {
        const p = ValuehelpsService.loadFormLocalstore(def.vh.name, locale)
        p.then((values) => {
            setOptions(ValuehelpsService.createVHOptions(values, def.vh))
            setElementDisabled(false)
        })
    }
}

/**
 * handleDynamicValueHelp is a function that handles dynamic value helps for a given definition. It checks if the
 * definition has a value help and if it exists in the provided dvhs record. If so, it creates options for the value
 * help and updates the state accordingly.
 *
 * @param def The definition object containing metadata about the element.
 * @param dvhs A record of dynamic value helps.
 * @param setOptions A function to update the options for the value help.
 * @param setElementDisabled A function to update the disabled state of the element.
 */
export function handleDynamicValueHelp(
    def: Definition,
    dvhs: Record<string, Record<string, string>>,
    setOptions: React.Dispatch<React.SetStateAction<ValueName[]>>,
    setElementDisabled: React.Dispatch<React.SetStateAction<boolean>>,
): void {
    if (def.vh && dvhs[def.vh.name]) {
        setOptions(ValuehelpsService.createVHOptions(dvhs[def.vh.name], def.vh))
        setElementDisabled(false)
    }

    // console.log(`SelectControl: def=${def.id} with vh=${def.vh?.name} and locale=${locale}`)
    if (def.vh && dvhs[def.vh.name]) {
        console.log(`SelectControl: def=${def.id} with vh=${def.vh?.name} has value-help in state`)
        setOptions(ValuehelpsService.createVHOptions(dvhs[def.vh.name], def.vh))
        setElementDisabled(false)
    }
}

/**
 *
 * @param texts
 * @param def
 */
export function getLabel(texts: Record<string, string>, def: Definition): string {
    let labelText = undefined

    if (texts) {
        labelText = texts[def.id + ".title"]
    }

    if (typeof labelText !== "string" || labelText.length === 0) {
        labelText = def.id
    }

    return labelText
}

/**
 *
 * @param texts
 * @param def
 * @returns
 */
export function getLong(texts: Record<string, string>, def: Definition): string {
    let text = undefined

    if (texts) {
        text = texts[def.id + ".long"]
    }

    if (typeof text !== "string" || text.length === 0) {
        text = def.id
    }

    return text
}

/**
 *
 * @param texts
 * @param def
 * @returns
 */
export function getDoc(texts: Record<string, string>, def: Definition): string {
    let text = undefined

    if (texts) {
        text = texts[def.id + ".doc"]
    }

    if (typeof text !== "string" || text.length === 0) {
        text = def.id
    }

    return text
}

/**
 *
 * @param texts
 * @param def
 * @returns
 */
export function getPlaceholder(texts: Record<string, string>, def: Definition): string | undefined {
    if (texts) {
        const text = texts[def.id + ".placeholder"]
        if (typeof text === "string" && text.length > 0) {
            return text
        }
    }
    return undefined
}

/**
 *

 */
export interface ResultHandlerIntf {
    handleResult(result: PayloadAction<AxiosResponse<SessionResponse | string>>): boolean
}

/**
 *
 */
export interface ControlProps {
    def: Definition
    texts: Record<string, string>
    rowId: string
    globalEd: boolean
    // resultHandler: ResultHandlerIntf
    asTableCell: boolean
    withContainer: boolean
    slot?: string
    vhs: Record<string, number>
    design?: string
    onAfterAction?: () => Promise<void> | void
}

/**
 *
 * @param props
 * @returns
 */
export default function (props: ControlProps) {
    const { def, rowId } = props
    const form = useAppSelector((state) => state.session.form)
    const element = FormService.findElementByRowAndKey(rowId, def.key, form)

    // console.log(`Element for ${def.id} is '${element === null ? "null" : element}'`)
    // if (element !== null && element.vi) {
    // console.log(def)
    // console.log(element)
    // console.log(`def.uiElement === ${def.uiElement}, visible == '${element?.vi}'`)
    // }

    if (element && element.vi) {
        // only visible elements are created
        if (def.uiElement === UIElement.Alert) {
            return <AlertControl {...props} />
        } else if (def.uiElement === UIElement.Attachment) {
            return <AttachmentControl {...props} />
        } else if (def.uiElement === UIElement.Autocomplete) {
            return <AutocompleteControl {...props} />
        } else if (def.uiElement === UIElement.Button) {
            return <ButtonControl {...props} />
        } else if (def.uiElement === UIElement.Checkbox) {
            return <CheckboxControl {...props} />
        } else if (def.uiElement === UIElement.Currency) {
            return <CurrencyControl {...props} />
        } else if (def.uiElement === UIElement.Dialog) {
            return <DialogControl {...props} />
        } else if (def.uiElement === UIElement.DateRange) {
            return <DateRangeControl {...props} />
        } else if (def.uiElement === UIElement.DocForm) {
            return <DocFormControl {...props} />
        } else if (def.uiElement === UIElement.Dummy) {
            return <DummyControl {...props} />
        } else if (def.uiElement === UIElement.Form) {
            return <FormControl {...props} />
        } else if (def.uiElement === UIElement.Group) {
            return <GroupControl {...props} />
        } else if (def.uiElement === UIElement.Icon) {
            return <IconControl {...props} />
        } else if (def.uiElement === UIElement.Image) {
            return <ImageControl {...props} />
        } else if (def.uiElement === UIElement.Link) {
            return <LinkControl {...props} />
        } else if (def.uiElement === UIElement.Input) {
            return <InputControl {...props} />
        } else if (def.uiElement === UIElement.MultiSelect) {
            return <MultiSelectControl {...props} />
        } else if (def.uiElement === UIElement.RadioButtons) {
            return <RadioButtonsControl {...props} />
        } else if (def.uiElement === UIElement.SearchHelp) {
            return <SearchHelpControl {...props} />
        } else if (def.uiElement === UIElement.Select) {
            return <SelectControl {...props} />
        } else if (def.uiElement == UIElement.Segment) {
            return <SegmentControl {...props} />
        } else if (def.uiElement === UIElement.Table) {
            return <TableControl {...props} />
        } else if (def.uiElement === UIElement.Text) {
            return <TextControl {...props} />
        } else if (def.uiElement === UIElement.Toolbar) {
            return <ToolbarControl {...props} />
        } else if (def.uiElement === UIElement.Editor) {
            return <EditorControl {...props} />
        } else if (def.uiElement === UIElement.Wizard) {
            return <WizardControl {...props} />
        } else {
            throw Error(`Unkown UI-Element ${def.uiElement}!`)
        }
    }

    return <></>
}
