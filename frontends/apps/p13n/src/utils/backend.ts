import { AxiosResponse } from "axios"
import { api } from "commons"

/**
 *
 * @param messages
 * @param dispatch
 * @param action
 * @returns
 */
export async function backendDispatch(
    url: string,
    method: "GET" | "POST" | "PATCH" | "DELETE" | "PUT",
    params?: object,
    requestParams?: object,
): Promise<AxiosResponse<string>> {
    const requestQueue: {
        url: string
        method: "GET" | "POST" | "PATCH" | "DELETE" | "PUT"
        requestParams: any | undefined
        params: object | undefined
        resolve: (
            value: AxiosResponse<string, any> | PromiseLike<AxiosResponse<string, any>>,
        ) => void
        reject: (reason?: any) => void
    }[] = []

    // create a new promise that first just puts the request into a queue
    const p = new Promise<AxiosResponse<string>>((resolve, reject) => {
        requestQueue.push({
            url,
            method,
            requestParams,
            params,
            resolve,
            reject,
        })
    })

    // trigger execution of queued requests
    // extract the first element of the queue
    const request = requestQueue.shift()

    // prepare data
    let data: any = request?.params ?? {}

    try {
        let res: any = undefined
        if (request?.requestParams) {
            for (const [key, value] of Object.entries(request.requestParams)) {
                if (Array.isArray(value)) {
                    request.requestParams[key] = value.join()
                }
            }
        }

        switch (request?.method) {
            case "GET":
                res = await api.get(request.url, { params: request.requestParams })
                break
            case "POST":
                res = await api.post(request.url, data, { params: request.requestParams })
                break
            case "PUT":
                res = await api.put(request.url, data, { params: request.requestParams })
                break
            case "PATCH":
                res = await api.patch(request.url, data, { params: request.requestParams })
                break
            case "DELETE":
                res = await api.delete(request.url, { params: request.requestParams })
        }
        request?.resolve(res)
    } catch (err) {
        request?.reject(err)
    }

    return p
}
