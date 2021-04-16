package com.navdissanayake.data.networking

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface GithubGraphQlApi {

    /**
     * Retrieve graph QL response from Graph QL API
     * @param body Query to send
     * @return [ResponseBody] object containing String response
     */
    @POST("/graphql")
    fun retrieveGraphQlResponse(@Body body: GraphQlQuery): Call<ResponseBody>

}