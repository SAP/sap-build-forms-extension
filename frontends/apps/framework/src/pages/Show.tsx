import { useEffect, useState } from "react"
import { useParams, useSearchParams } from "react-router-dom"

import { useMessages } from "commons"

import { useAppDispatch, useAppSelector } from "../features/store"
import { createSession, SessionResponse } from "../features/sessions/sessionActions"
import Form from "../components/layout/FormPage"
import { resetFlag, setGlobalReadonly } from "../features/sessions/sessionSlice"

/**
 *
 * @returns
 */
export default function () {
    const params = useParams()
    const [searchParams] = useSearchParams()
    const formsId = params.formsId ?? undefined
    const state = params.state ?? ""
    const editable = searchParams.get("editable") === "true"
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const session = useAppSelector((state) => state.session)
    const shouldReset = useAppSelector((state) => state.session.shouldReset)
    const [response, setResponse] = useState<SessionResponse>()

    const doCreateSession = () => {
        dispatch(createSession({ messages, formsId, state })).then((action: any) => {
            // console.log(action)
            if (action.meta.requestStatus === "fulfilled") {
                // console.log("Receiving data from backend!")
                setResponse(action.payload.data)
                if (editable) {
                    dispatch(setGlobalReadonly(false))
                }
            }
        })
    }

    useEffect(() => {
            doCreateSession()
        }, [])
    
        useEffect(() => {
            if (shouldReset) {
                dispatch(resetFlag())
                doCreateSession()
            }
        }, [shouldReset])
    

    useEffect(() => {
        document.title = session.pageTitle
    }, [session.pageTitle])

    if (response) {
        return <Form response={response} />
    }
    return <></>
}
