package com.navdissanayake.data.networking

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface GithubGraphqlRepository {

    @POST("/graphql")
    fun <T>retrieveGraphQlResponse(@Body body: GraphQlQuery): Call<ResponseBody>

}