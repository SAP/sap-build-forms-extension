import { useEffect, useState } from "react"
import { useParams } from "react-router-dom"

import { useMessages } from "commons"

import { useAppDispatch, useAppSelector } from "../features/store"
import { createSession, SessionResponse } from "../features/sessions/sessionActions"
import Form from "../components/layout/FormPage"

/**
 *
 * @returns
 */
export default function () {
    const params = useParams()
    const formsId = params.formsId ?? undefined
    const state = params.state ?? ""
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const session = useAppSelector((state) => state.session)
    const [response, setResponse] = useState<SessionResponse>()

    useEffect(() => {
        dispatch(createSession({ messages, formsId, state })).then((action: any) => {
            // console.log(action)
            if (action.meta.requestStatus === "fulfilled") {
                // console.log("Receiving data from backend!")
                setResponse(action.payload.data)
            }
        })
    }, [])

    useEffect(() => {
        document.title = session.pageTitle
    }, [session.pageTitle])

    if (response) {
        return <Form response={response} />
    }
    return <></>
}
