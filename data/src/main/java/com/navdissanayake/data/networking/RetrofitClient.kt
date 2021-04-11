package com.navdissanayake.data.networking

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    private var apiBaseUrl: String? = null
    private var apiAccessToken: String? = null

    private fun getRetrofitClient(): Retrofit {
        if (apiBaseUrl == null || apiAccessToken == null) {
            throw IllegalStateException("You must call init() before accessing RetrofitClient")
        }

        return retrofit
    }

    fun init(apiBaseUrl: String, apiAccessToken: String) {
        this.apiBaseUrl = apiBaseUrl
        this.apiAccessToken = apiAccessToken
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(apiBaseUrl!!)
            .client(
                OkHttpClient.Builder()
                    .addInterceptor { chain ->
                        val request = chain.request()
                            .newBuilder()
                            .addHeader(
                                "Authorization",
                                "Bearer " + apiAccessToken
                            )

                            .build()

                        chain.proceed(request)
                    }
                    .build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val githubGraphqlApi: GithubGraphqlRepository by lazy {
        getRetrofitClient().create(
            GithubGraphqlRepository::class.java
        )
    }

}