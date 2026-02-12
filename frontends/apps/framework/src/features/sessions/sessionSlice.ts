import { PayloadAction, createSlice } from "@reduxjs/toolkit"

import { FrontendJournal, ElementProp, JournalService } from "./journal"
import { DataTypes, Form, FormService, TableInfo } from "./forms"
import { SessionState } from "../states"
import { createSession, deleteRow, handleSessionResponse, triggerEvent } from "./sessionActions"
import { DownloadAttachmentInput, deleteAttachment, uploadAttachment } from "./attachmentActions"
import { Definition } from "./definitions"
import { getRouterBaseName } from "commons"

/**
 *
 */
interface UpdateValueInput {
    def: Definition
    rowId?: string
    selectedRowIds?: Array<string>
    value: DataTypes | undefined
    forced?: boolean
    prop?: ElementProp
}

/**
 *
 */
interface DeleteRowInput {
    rowId: string
    key: string
    deleteRowId: string
}

/**
 *
 */
const initialState: SessionState = {
    form: new Form("", {}),
    headerTitle: "...",
    journal: new FrontendJournal(),
    locale: "en",
    pageTitle: "...",
    vhs: {},
    ignore: true,
}

/**
 *
 */
export const sessionSlice = createSlice({
    name: "session",
    initialState,
    reducers: {
        update: (state, action: PayloadAction<UpdateValueInput>) => {
            const element = FormService.findElementByRowAndKey(
                action.payload.rowId,
                action.payload.def.key,
                state.form,
            )

            // console.log(
            //     `Update element for ${action.payload.key} on ${action.payload.rowId} is ${element}`,
            // )

            if (element) {
                // update journal and value
                switch (action.payload.prop) {
                    case ElementProp.Position:
                    case ElementProp.PageSize:
                    case ElementProp.SortField:
                    case ElementProp.SortOrder:
                    case ElementProp.Value:
                        JournalService.update(
                            state.journal,
                            action.payload.def,
                            element!,
                            action.payload.value,
                            action.payload.rowId,
                            action.payload.forced,
                            action.payload.prop,
                        )
                        state.form = new Form("", state.form.values)
                        break
                    case ElementProp.Selected:
                        JournalService.updateSelected(
                            state.journal,
                            action.payload.def,
                            element,
                            action.payload.selectedRowIds!,
                        )
                        break
                    case ElementProp.Visible:
                        JournalService.updateVisible(
                            state.journal,
                            action.payload.def,
                            element!,
                            action.payload.value as boolean,
                            action.payload.rowId,
                        )
                        state.form = new Form("", state.form.values)
                        break
                }
                // console.log(state.journal)
            }
        },
        // deleteRow: (state, action: PayloadAction<DeleteRowInput>) => {
        //     const element = FormService.findElementByRowAndKey(
        //         action.payload.rowId,
        //         action.payload.key,
        //         state.form,
        //     )
        //     if (element) {
        //         JournalService.addDeleted(
        //             state.journal,
        //             action.payload.rowId,
        //             action.payload.key,
        //             action.payload.deleteRowId,
        //         )

        //         // remove the row from the table
        //         const table = element.va as TableInfo
        //         // remove the row from the "rows" array
        //         console.log(`Delete ${action.payload.deleteRowId} => ${table.r!}`)
        //         console.log(table.d)
        //         table.r!.splice(
        //             table.r!.findIndex((it) => it === action.payload.deleteRowId),
        //             1,
        //         )
        //         // remove row from the "data" object (named "d")
        //         delete table.d![action.payload.deleteRowId]
        //         console.log(`Deleted! r: ${table.r!}`)
        //         console.log(table.d)
        //         // decrease size of the table
        //         table.s--
        //         // this is a little hack to indicate a state change
        //         state.ignore = !state.ignore
        //     }
        // },
        downloadAttachment: (state, action: PayloadAction<DownloadAttachmentInput>) => {
            window.open(
                getRouterBaseName() +
                `api/v1/attachments/${state.id}/${action.payload.key}/${action.payload.id}`,
            )
        },
    },
    extraReducers: (builder) => {
        builder.addCase(createSession.fulfilled, (state, action) =>
            handleSessionResponse(state, action, true),
        )
        builder.addCase(triggerEvent.fulfilled, (state, action) =>
            handleSessionResponse(state, action, false),
        )
        builder.addCase(uploadAttachment.fulfilled, (state, action) =>
            handleSessionResponse(state, action, false),
        )
        builder.addCase(deleteAttachment.fulfilled, (state, action) =>
            handleSessionResponse(state, action, false),
        )
        builder.addCase(deleteRow.fulfilled, (state, action) =>
            handleSessionResponse(state, action, false),
        )
    },
})

export const { update, downloadAttachment } = sessionSlice.actions
