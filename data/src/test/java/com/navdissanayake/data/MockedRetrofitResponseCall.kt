package com.navdissanayake.data

import okhttp3.MediaType
import okhttp3.Request
import okhttp3.ResponseBody
import okio.Timeout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MockedRetrofitResponseCall<T>(private val response: Response<T>) : Call<T> {

    companion object {
        fun <T> buildSuccessResponse(body: T): Call<T> {
            return MockedRetrofitResponseCall<T>(Response.success(body))
        }

        fun <T> buildErrorResponse(responseCode: Int, errorBody: String): Call<T> {
            return MockedRetrofitResponseCall(
                Response.error(
                    responseCode,
                    ResponseBody.create(MediaType.parse("application/text"), errorBody)
                )
            )
        }
    }

    override fun enqueue(callback: Callback<T>) {
    }

    override fun isExecuted(): Boolean {
        return false
    }

    override fun timeout(): Timeout {
        return Timeout()
    }

    override fun clone(): Call<T> {
        return MockedRetrofitResponseCall(response)
    }

    override fun isCanceled(): Boolean {
        return false
    }

    override fun cancel() {
    }

    override fun execute(): Response<T> {
        return response
    }

    override fun request(): Request? {
        return null
    }

}