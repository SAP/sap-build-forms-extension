import axios, { AxiosRequestConfig, AxiosResponse } from "axios"
import { MessageIntf } from "./messages"

const WAIT_FOR_BLOCK = 1000

// used if application has a prefix. defined in index.html
declare var ROUTER_BASE_NAME: string

// for direct axios calls
export const api = axios.create({
  baseURL: (ROUTER_BASE_NAME + "/api").replace("//", "/"),
  timeout: 60000,
  // @ts-ignore
  validateStatus: (status: number) => true,
})
// if (sessionStorage["accessToken"]) {
//   api.defaults.headers["Authorization"] = "Bearer " + sessionStorage.getItem("accessToken")
// }

/**
 *
 * @returns
 */
export function getRouterBaseName() {
  return ROUTER_BASE_NAME
}

/**
 *
 * @param status
 * @returns
 */
export function apiOk(status: number) {
  return status >= 200 && status < 400
}

/**
 *
 * @returns
 */
export function apiCsrfToken(): string {
  const csrfToken = document.cookie.replace(/(?:(?:^|.*;\s*)XSRF-TOKEN\s*\=\s*([^;]*).*$)|^.*$/, '$1')
  // console.log(`CSRF-Token is ${csrfToken}`)
  return csrfToken
}

/**
 *  Function to handle errors in API calls.
 * It logs the error to the console and returns a rejected promise with the error message.
 *
 * @param response
 * @returns
 */
export function handleError(response: AxiosResponse<any, any>, actionName: string, messages: MessageIntf): Promise<Error> {
  const errData = response.data as ErrorInfo
  console.error(`Error in API call '${actionName}': ${errData.error_code} :'${errData.message}', (guid: ${errData.guid})`)
  setTimeout(() => messages.fatal("common_error_backend", { "guid": errData.guid }), 10)
  return Promise.reject(Error(response.status + ": " + response.statusText))
}

/**
 * Interface for error information returned by the backend API.
 */
export interface ErrorInfo {
  error_code: number
  message: string
  guid: string
  user: string
}

/**
 * HTTP methods supported by the backend API.
 */
type HTTP_METHOD = "GET" | "POST" | "PATCH" | "PUT" | "DELETE"

/**
 *
 */
export interface RequestInfo<TResponse> {
  url: string
  method: HTTP_METHOD
  data?: object
  preSendData?: object
  resolve: (value: AxiosResponse<TResponse> | PromiseLike<AxiosResponse<TResponse>>) => void
  reject: (reason?: any) => void
  config?: AxiosRequestConfig<any> | undefined
}

/**
 *
 */
export class Backend<TResponse> {
  requestQueue: RequestInfo<TResponse>[]
  waitCount: number
  preSendProcessor: ((data: any, request: RequestInfo<TResponse>) => void) | undefined

  /**
   *
   * @param aPreSendProcessor
   */
  constructor(aPreSendProcessor?: (data: any, request: RequestInfo<TResponse>) => void) {
    this.requestQueue = []
    this.waitCount = 0
    this.preSendProcessor = aPreSendProcessor
  }

  /**
   *
   */
  private async executeQueued() {
    // check if there is a request waiting and we are not waiting for response
    // from server. If not, then return method
    if (this.requestQueue.length === 0 || this.waitCount != 0) {
      return
    }
    // extract the first element of the queue
    const request = this.requestQueue.shift()
    if (!request) {
      console.error("Request is undefined in executeQueued")
      return
    }

    // repare data
    let data: any = request?.data ?? {}
    // data["journal"] = request?.sessionState.journal
    if (this.preSendProcessor) {
      this.preSendProcessor(data, request!)
    }

    // set xsrf token
    request.config ??= {}
    request.config.headers ??= {}
    const csrfToken = apiCsrfToken()
    if (csrfToken) {
      request.config.headers["X-XSRF-TOKEN"] = csrfToken
    }
    // set CORS handler to allow all origins (for development purposes)
    // request.config.headers["Access-Control-Allow-Origin"] = "*"

    try {
      this.waitCount++
      let res: any = undefined
      switch (request?.method) {
        case "GET":
          res = await api.get(request.url, request.config)
          break
        case "POST":
          res = await api.post(request.url, data, request.config)
          break
        case "PATCH":
          res = await api.patch(request.url, data, request.config)
          break
        case "PUT":
          res = await api.put(request.url, data, request.config)
          break
        case "DELETE":
          res = await api.delete(request.url, request.config)
      }
      request?.resolve(res)
    } catch (err) {
      request?.reject(err)
    } finally {
      this.waitCount--
      this.executeQueued()
    }
  }

  /**
   *
   * @returns
   */
  public getWaitCount(): number {
    return this.waitCount
  }

  /**
   *
   * @param info
   * @returns
   */
  public callQueued(
    messages: MessageIntf,
    url: string,
    method: HTTP_METHOD,
    data?: object,
    preSendData?: object,
    config?: AxiosRequestConfig<any> | undefined,
  ): Promise<AxiosResponse<TResponse>> {
    setTimeout(() => {
      if (this.waitCount !== 0) {
        messages.block(true)
      }
    }, WAIT_FOR_BLOCK)

    // create a new promise that first just puts the request into a queue
    const p = new Promise<AxiosResponse<TResponse>>((resolve, reject) => {
      this.requestQueue.push({
        url,
        method,
        data,
        preSendData,
        resolve,
        reject,
        config,
      })
    })
    p.finally(() => {
      messages.block(false)
    })

    // trigger execution of queued requests
    this.executeQueued()

    return p
  }

  /**
   * callDirect is a method to make direct API calls without queuing.
   * It takes a message interface, URL, HTTP method, and optional data,
   * returning a promise that resolves with the API response.
   *
   * @param messages
   * @param url
   * @param method
   * @param data
   * @returns
   */
  public async callDirect(
    messages: MessageIntf,
    url: string,
    method: HTTP_METHOD,
    data?: object,
    config?: AxiosRequestConfig<any> | undefined,
  ): Promise<AxiosResponse<TResponse>> {
    setTimeout(() => {
      if (this.waitCount !== 0) {
        messages.block(true)
      }
    }, WAIT_FOR_BLOCK)

    //
    try {
      this.waitCount++
      switch (method) {
        case "GET":
          return await api.get(url, config)
        case "POST":
          return await api.post(url, data, config)
        case "PATCH":
          return await api.patch(url, data, config)
        case "PUT":
          return await api.put(url, data, config)
        case "DELETE":
          return await api.delete(url, config)
        default:
          throw new Error(`Unsupported method: ${method}`)
      }
    } catch (err) {
      Promise.reject(err)
    } finally {
      this.waitCount--
      if (this.waitCount === 0) {
        messages.block(false)
      }
    }

    return Promise.reject(Error("unsupported call"))
  }
}
