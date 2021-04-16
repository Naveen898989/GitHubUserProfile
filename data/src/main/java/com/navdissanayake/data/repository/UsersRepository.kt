package com.navdissanayake.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.VisibleForTesting
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import com.navdissanayake.data.constants.CACHED_USER_SHARED_PREFERENCE_NAME
import com.navdissanayake.data.constants.CACHE_DURATION
import com.navdissanayake.data.networking.GithubGraphQlApi
import com.navdissanayake.data.networking.GraphQlQuery
import com.navdissanayake.data.networking.GraphQlResponse
import com.navdissanayake.data.networking.user.UserResponse
import com.navdissanayake.data.util.IllegalResponseException
import com.navdissanayake.domain.model.Cache
import com.navdissanayake.domain.model.User
import com.navdissanayake.domain.repository.IUsersRepository
import okhttp3.ResponseBody
import retrofit2.Response
import java.lang.reflect.Type

const val QUERY = "query {\n" +
        // User data
        "  user(login:\"%s\") {\n" +
        "    name\n" +
        "    login\n" +
        "    email\n" +
        "    followers {\n" +
        "      totalCount\n" +
        "    }\n" +
        "    following {\n" +
        "      totalCount\n" +
        "    }\n" +
        "    avatarUrl\n" +

        // Pinned repositories
        "    pinnedItems(first: 3, types: [REPOSITORY]) {\n" +
        "       totalCount\n" +
        "       nodes {\n" +
        "          ... on Repository {\n" +
        "          name\n" +
        "          description\n" +
        "          stargazerCount\n" +
        "          languages(first: 3) {\n" +
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

        // Top repositories
        "    topRepositories(first: 10, orderBy: {field: CREATED_AT, direction: ASC} ) {\n" +
        "      totalCount\n" +
        "      nodes {\n" +
        "        name\n" +
        "        description\n" +
        "        stargazerCount\n" +
        "        languages(first: 3) {\n" +
        "          nodes {\n" +
        "            color\n" +
        "            name\n" +
        "          }\n" +
        "        }\n" +
        "      owner {\n" +
        "          login\n" +
        "          avatarUrl\n" +
        "        }\n" +
        "      }\n" +
        "    }\n" +

        // Starred repositories
        "    starredRepositories(first: 10, orderBy: {field: STARRED_AT, direction: DESC}) {\n" +
        "      totalCount\n" +
        "      nodes {\n" +
        "        name\n" +
        "        description\n" +
        "        stargazerCount\n" +
        "        languages(first: 3) {\n" +
        "          nodes {\n" +
        "            color\n" +
        "            name\n" +
        "          }\n" +
        "        }\n" +
        "        owner {\n" +
        "          login\n" +
        "          avatarUrl\n" +
        "         }\n" +
        "      }\n" +
        "    }\n" +
        "  }\n" +
        "}"

/**
 * Repository class used to fetch user response and caching
 */
class UsersRepository(
    private val context: Context,
    private val githubGraphQlApi: GithubGraphQlApi
) : IUsersRepository {

    /**
     * Retrieve user from Github using the given login ID. May return cached data if cache is less
     * than one day old. See [CACHE_DURATION] for duration. Use
     * [invalidateCachedData()][invalidateCachedData] to clear cache and retrieve clean data
     *
     * @param userLogin User login ID
     * @return User object
     * @throws JsonSyntaxException If response is not formatted properly
     * @throws IllegalResponseException If response was not successful (Response not between 200
     * and 300)
     * @throws IllegalStateException If unexpected exceptions occur
     */
    @Throws(
        JsonSyntaxException::class,
        IllegalResponseException::class,
        IllegalStateException::class
    )
    override suspend fun retrieveUser(userLogin: String): User {
        try {
            // Return cache if cache is available. Cache already validated in retrieveCachedUser
            // method
            val cachedUser: User? = retrieveCachedUser(userLogin)
            if (cachedUser != null) {
                return cachedUser
            }

            return fetchUserFromApi(userLogin)
        } catch (e: Exception) {
            throw IllegalStateException("Unknown error occurred", e)
        }
    }

    /**
     * Retrieve cached user using given user login.
     * @param userLogin Login ID of user to retrieve.
     * @return Cached user if cache is available and has not expired. _Null_ if not
     */
    override suspend fun retrieveCachedUser(userLogin: String): User? {
        val sharedPreferences: SharedPreferences =
            context.getSharedPreferences(CACHED_USER_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
        val cacheString: String? = sharedPreferences.getString(userLogin, null)

        if (cacheString != null) {
            val type: Type = object : TypeToken<Cache<User>>() {}.type
            val cachedUser: Cache<User> = Gson().fromJson<Cache<User>>(cacheString, type)

            // Check if cache has expired
            return if (cachedUser.isCacheValid(CACHE_DURATION)) {
                cachedUser.cachedData
            } else {
                null
            }
        }

        return null
    }

    /**
     * Clear all user cache.
     * @return true if successful
     */
    override suspend fun invalidateCachedData(): Boolean {
        val editor: SharedPreferences.Editor =
            context.getSharedPreferences(CACHED_USER_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
                .edit()
        editor.clear()
        return editor.commit()
    }

    /**
     * Fetch user from Github Graph QL API and return User object
     */
    @Throws(
        JsonSyntaxException::class,
        IllegalResponseException::class,
        IllegalStateException::class
    )
    private fun fetchUserFromApi(userLogin: String): User {
        val response: Response<ResponseBody> =
            githubGraphQlApi.retrieveGraphQlResponse(
                GraphQlQuery(
                    QUERY.format(
                        userLogin
                    )
                )
            ).execute()

        if (response.isSuccessful) {
            val body: String? = response.body()?.string()

            if (body != null) {
                try {
                    val type: Type = object : TypeToken<GraphQlResponse<UserResponse>>() {}.type
                    val graphQlResponse: GraphQlResponse<UserResponse> =
                        Gson().fromJson(body, type)

                    val user: User = graphQlResponse.data.user

                    // Save cache
                    saveCache(userLogin, user)

                    return user
                } catch (e: JsonSyntaxException) {
                    throw e
                }
            }
        } else {
            throw IllegalResponseException(
                "Response non-successful: ${response.code()}",
                response.errorBody()?.string()
            )
        }

        throw IllegalStateException("Unknown error occurred")
    }

    /**
     * Caches data to be use again.
     * @return true if successful
     */
    private fun saveCache(userLogin: String, user: User): Boolean {
        val editor: SharedPreferences.Editor =
            context.getSharedPreferences(CACHED_USER_SHARED_PREFERENCE_NAME, Context.MODE_PRIVATE)
                .edit()

        val cacheString: String = Gson().toJson(Cache(user, System.currentTimeMillis()))

        editor.putString(userLogin, cacheString)
        return editor.commit()
    }

    /* ------------- TESTING ------------- */

    /**
     * Expose [fetchUserFromApi()][fetchUserFromApi] method for testing
     */
    @VisibleForTesting
    fun fetchUserFromApiForTesting(userLogin: String): User {
        return fetchUserFromApi(userLogin)
    }

    /**
     * Expose [saveCacheForTesting()][saveCacheForTesting] method for testing
     */
    @VisibleForTesting
    fun saveCacheForTesting(userLogin: String, user: User): Boolean {
        return saveCache(userLogin, user)
    }

}