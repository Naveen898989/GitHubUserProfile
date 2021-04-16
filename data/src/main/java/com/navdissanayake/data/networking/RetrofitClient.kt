package com.navdissanayake.data.networking

import com.navdissanayake.data.networking.RetrofitClient.init
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * Retrofit client object used for Restful API calls.
 *
 * **IMPORTANT:** Please call [init()][init] before using this methods in this object.
 */
object RetrofitClient {

    private var apiBaseUrl: String? = null
    private var apiAccessToken: String? = null

    /**
     * Initialize retrofit object.
     *
     * **NOTE:** Only pass application context so as not to leak.
     *
     * @param apiBaseUrl Base URL of the API
     * @param apiAccessToken Authorization taken used to authenticate request. Will be send as a
     * bearer token.
     */
    fun init(apiBaseUrl: String, apiAccessToken: String) {
        this.apiBaseUrl = apiBaseUrl
        this.apiAccessToken = apiAccessToken
    }

    val githubGraphQlApi: GithubGraphQlApi by lazy {
        retrofit.create(GithubGraphQlApi::class.java)
    }

    private val retrofit: Retrofit by lazy {
        if (apiBaseUrl == null || apiAccessToken == null) {
            throw IllegalStateException("You must call init() before accessing RetrofitClient")
        }

        Retrofit.Builder()
            .baseUrl(apiBaseUrl!!)
            .client(
                OkHttpClient.Builder()
                    // Authentication interceptor
                    .addInterceptor { chain ->
                        val request: Request = chain.request()
                            .newBuilder()
                            .addHeader(
                                "Authorization",
                                "Bearer $apiAccessToken"
                            )

                            .build()

                        chain.proceed(request)
                    }
                    .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

}