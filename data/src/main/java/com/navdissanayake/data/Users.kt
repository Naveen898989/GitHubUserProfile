package com.navdissanayake.data

import android.content.Context
import com.google.gson.Gson
import com.navdissanayake.data.constants.CACHED_USER_SHARED_PREFERENCE_NAME
import com.navdissanayake.data.networking.GraphQlQuery
import com.navdissanayake.data.networking.GraphQlUserResponse
import com.navdissanayake.data.networking.RetrofitClient
import com.navdissanayake.domain.model.CachedUser
import com.navdissanayake.domain.model.User
import io.reactivex.Observable
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.Exception
import java.lang.IllegalStateException

const val DAY_IN_MILLIS: Long = 24 * 60 * 60 * 1000

const val QUERY = "query {\n" +
        "  user(login:\"%s\") {\n" +
        "    name\n" +
        "    login\n" +
        "    email\n" +
        "    followers{\n" +
        "      totalCount\n" +
        "    }\n" +
        "    following {\n" +
        "      totalCount\n" +
        "    }\n" +
        "    avatarUrl\n" +
        "    pinnedItems(first: 3, types: [REPOSITORY]) {\n" +
        "       totalCount\n" +
        "       nodes {\n" +
        "          ... on Repository {\n" +
        "          name\n" +
        "          description\n" +
        "          stargazerCount\n" +
        "          languages(first: 3){\n" +
        "            nodes {\n" +
        "              color\n" +
        "              name\n" +
        "            }\n" +
        "          }\n" +
        "          owner{\n" +
        "            login\n" +
        "            avatarUrl\n" +
        "          }\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "    topRepositories(first: 10, orderBy: {field: CREATED_AT, direction: ASC} ) {\n" +
        "      totalCount\n" +
        "        nodes {\n" +
        "        name\n" +
        "        description\n" +
        "        stargazerCount\n" +
        "        languages(first: 3){\n" +
        "          nodes {\n" +
        "            color\n" +
        "            name\n" +
        "          }\n" +
        "        }\n" +
        "      owner{\n" +
        "          login\n" +
        "          avatarUrl\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +
        "    starredRepositories(first: 10, orderBy: {field: STARRED_AT, direction: DESC}){\n" +
        "      totalCount\n" +
        "      nodes{\n" +
        "        name\n" +
        "        description\n" +
        "        stargazerCount\n" +
        "        languages(first: 3){\n" +
        "          nodes {\n" +
        "            color\n" +
        "            name\n" +
        "          }\n" +
        "        }\n" +
        "        owner{\n" +
        "          login\n" +
        "          avatarUrl\n" +
        "         }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}"

class Users(private val context: Context) {

    @Throws(Exception::class)
    public fun retrieveUser(userLogin: String): Observable<User> {
        return Observable.create<User> { emitter ->
            val userRepository = UserRepository(
                context.getSharedPreferences(
                    CACHED_USER_SHARED_PREFERENCE_NAME,
                    Context.MODE_PRIVATE
                )
            )

            val cachedUser: CachedUser? = userRepository.getUser()

            if (cachedUser == null || cachedUser.cachedTimestamp < System.currentTimeMillis() - DAY_IN_MILLIS) {
                val response: Response<ResponseBody> =
                    RetrofitClient.githubGraphqlApi.retrieveGraphQlResponse<User>(
                        GraphQlQuery(
                            QUERY.format(
                                userLogin
                            )
                        )
                    )
                        .execute()

                if (response.isSuccessful) {
                    val body: String? = response.body()!!.string()

                    val graphQlUserResponse: GraphQlUserResponse =
                        Gson().fromJson(body, GraphQlUserResponse::class.java)

                    val userObject: User = graphQlUserResponse.data.user

                    userRepository.saveUser(userObject)

                    emitter.onNext(userObject)
                } else {
                    emitter.onError(IllegalStateException(response.errorBody()?.string()))
                }
            } else {
                emitter.onNext(cachedUser.user)
            }

            emitter.onComplete()
        }

    }

    public fun invalidateCachedData() {
        UserRepository(
            context.getSharedPreferences(
                CACHED_USER_SHARED_PREFERENCE_NAME,
                Context.MODE_PRIVATE
            )
        ).deleteUser()
    }

}