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
    const state = params.state ?? "start"
    const dispatch = useAppDispatch()
    const messages = useMessages()
    const session = useAppSelector((state) => state.session)
    const [response, setResponse] = useState<SessionResponse>()

    useEffect(() => {
        dispatch(createSession({ state, messages })).then((action: any) => {
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
