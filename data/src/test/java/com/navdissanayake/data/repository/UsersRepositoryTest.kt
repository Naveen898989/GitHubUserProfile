package com.navdissanayake.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.navdissanayake.data.MockedRetrofitResponseCall
import com.navdissanayake.data.constants.CACHED_USER_SHARED_PREFERENCE_NAME
import com.navdissanayake.data.networking.GithubGraphQlApi
import com.navdissanayake.data.networking.GraphQlQuery
import com.navdissanayake.data.util.IllegalResponseException
import com.navdissanayake.domain.model.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*

@ExperimentalCoroutinesApi
class UsersRepositoryTest {

    private val userLogin = "Naveen898989"

    private lateinit var usersRepository: UsersRepository

    private lateinit var mockedPreferences: SharedPreferences
    private lateinit var mockedPreferencesEditor: SharedPreferences.Editor
    private lateinit var mockedContext: Context
    private lateinit var mockedGithubGraphQlApi: GithubGraphQlApi

    @Before
    fun setup() {
        mockedPreferencesEditor = mock(SharedPreferences.Editor::class.java)
        `when`(
            mockedPreferencesEditor.putString(anyString(), anyString())
        ).thenReturn(mockedPreferencesEditor)

        `when`(
            mockedPreferencesEditor.commit()
        ).thenReturn(true)

        mockedPreferences = mock(SharedPreferences::class.java)
        `when`(mockedPreferences.edit())
            .thenReturn(mockedPreferencesEditor)

        mockedContext = mock(Context::class.java)
        `when`(
            mockedContext.getSharedPreferences(
                CACHED_USER_SHARED_PREFERENCE_NAME,
                Context.MODE_PRIVATE
            )
        ).thenReturn(mockedPreferences)

        mockedGithubGraphQlApi = mock(GithubGraphQlApi::class.java)

        usersRepository = UsersRepository(mockedContext, mockedGithubGraphQlApi)
    }

    /**
     * - Fetch from mocked API which returns success response
     * - Must save in cache
     * - Must return valid user object
     */
    @Test
    fun fetchUserFromApi_success() {
        mockApiResponseSuccess()

        val fetchedUser: User = usersRepository.fetchUserFromApiForTesting(userLogin)

        verify(mockedPreferencesEditor, times(1))
            .putString(anyString(), anyString())
        assertThat(fetchedUser).isEqualTo(mockedUser)
    }

    /**
     * - Fetch from mocked API which returns a 404 error response
     * - Must throw IllegalResponseException
     */
    @Test(expected = IllegalResponseException::class)
    fun fetchUserFromApi_errorResponse() {
        mockApiResponseError()

        usersRepository.fetchUserFromApiForTesting(userLogin)
    }

    /**
     * - Fetch from mocked API which returns an invalid json with success response
     * - Must throw JsonSyntaxException
     */
    @Test(expected = JsonSyntaxException::class)
    fun fetchUserFromApi_errorJson() {
        `when`(
            mockedGithubGraphQlApi.retrieveGraphQlResponse(
                GraphQlQuery(
                    anyString()
                )
            )
        ).thenReturn(
            MockedRetrofitResponseCall.buildSuccessResponse(
                ResponseBody.create(
                    MediaType.parse("application/json"),
                    "{"
                )
            )
        )

        usersRepository.fetchUserFromApiForTesting(userLogin)
    }

    /**
     * - Retrieve user from UserRepository
     * - No cache available
     * - API returns success response
     * - Must check cache
     * - Must save in cache
     * - Must return valid user object
     */
    @Test
    fun retrieveUser_noCache_apiCalledSuccessfully() = runBlockingTest {
        mockApiResponseSuccess()
        mockSharedPreferenceCache(null)

        val fetchedUser: User = usersRepository.retrieveUser(userLogin)

        verify(mockedPreferences, times(1))
            .getString(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
        verify(mockedPreferencesEditor, times(1))
            .putString(anyString(), anyString())
        assertThat(fetchedUser).isEqualTo(mockedUser)
    }

    /**
     * - Retrieve user from UserRepository
     * - Valid cache available
     * - API returns error
     * - Must check cache
     * - Must return cached user object
     */
    @Test
    fun retrieveUser_validCache() = runBlockingTest {
        val currentTimestamp: Long = System.currentTimeMillis()

        mockApiResponseError()
        mockSharedPreferenceCache(Gson().toJson(Cache(mockedUser2, currentTimestamp)))

        val fetchedUser: User = usersRepository.retrieveUser(userLogin)

        verify(mockedPreferences, times(1))
            .getString(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
        assertThat(fetchedUser).isEqualTo(mockedUser2)
    }

    /**
     * - Retrieve user from UserRepository
     * - Cache expired
     * - API returns success response
     * - Must check cache
     * - Must save in cache
     * - Must return valid user object
     */
    @Test
    fun retrieveUser_expiredCache() = runBlockingTest {
        val yesterdayTimestamp: Long = System.currentTimeMillis() - (25 * 60 * 60 * 1000)

        mockApiResponseSuccess()
        mockSharedPreferenceCache(Gson().toJson(Cache(mockedUser2, yesterdayTimestamp)))

        val fetchedUser: User = usersRepository.retrieveUser(userLogin)

        verify(mockedPreferences, times(1))
            .getString(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())
        verify(mockedPreferencesEditor, times(1))
            .putString(anyString(), anyString())
        assertThat(fetchedUser).isEqualTo(mockedUser)
    }

    /**
     * Mock API response to return responseBodySuccess
     */
    private fun mockApiResponseSuccess() {
        `when`(
            mockedGithubGraphQlApi.retrieveGraphQlResponse(
                GraphQlQuery(
                    anyString()
                )
            )
        ).thenReturn(
            MockedRetrofitResponseCall.buildSuccessResponse(
                ResponseBody.create(
                    MediaType.parse("application/json"),
                    responseBodySuccess
                )
            )
        )
    }

    /**
     * Mock API response to return 404 error
     */
    private fun mockApiResponseError() {
        `when`(
            mockedGithubGraphQlApi.retrieveGraphQlResponse(
                GraphQlQuery(
                    anyString()
                )
            )
        ).thenReturn(
            MockedRetrofitResponseCall
                .buildErrorResponse(404, "File not found")
        )
    }

    /**
     * Mock shared preference with the given cache
     */
    private fun mockSharedPreferenceCache(cache: String?) {
        `when`(
            mockedPreferences.getString(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString()
            )
        ).thenReturn(
            cache
        )
    }

    private val mockedUser = User(
        "Naveen Dissanayake",
        "Naveen898989",
        "test@example.com",
        "https://avatars.githubusercontent.com/u/25893105?v=4",
        Followers(5),
        Following(11),
        RepositoryNodes(
            listOf(
                RepositoryNode(
                    "PinnedRepository1",
                    "Android application which displays a users profile information using GraphQL",
                    0,
                    LanguageNodes(listOf(LanguageNode("#f1e05a", "Kotlin"))),
                    RepositoryOwner(
                        "Naveen898989",
                        "https://avatars.githubusercontent.com/u/25893105?v=4"
                    )
                )
            )
        ),
        RepositoryNodes(
            listOf(
                RepositoryNode(
                    "TopRepository1",
                    "Android application which displays a users profile information using GraphQL",
                    0,
                    LanguageNodes(listOf(LanguageNode("#f1e05a", "Kotlin"))),
                    RepositoryOwner(
                        "User00111",
                        "https://avatars.githubusercontent.com/u/25893105?v=4"
                    )
                )
            )
        ),
        RepositoryNodes(
            listOf(
                RepositoryNode(
                    "StarredRepository1",
                    "Android application which displays a users profile information using GraphQL",
                    0,
                    LanguageNodes(listOf(LanguageNode("#f1e05a", "Kotlin"))),
                    RepositoryOwner(
                        "Test11134",
                        "https://avatars.githubusercontent.com/u/25893105?v=4"
                    )
                )
            )
        )
    )
    private val mockedUser2 = User(
        "TestUser2",
        "TestTest2",
        "test2@example.com",
        "https://avatars.githubusercontent.com/u/25893105?v=4",
        Followers(6),
        Following(42),
        RepositoryNodes(
            listOf(
                RepositoryNode(
                    "PinnedRepository2",
                    "Android application which displays a users profile information using GraphQL",
                    0,
                    LanguageNodes(listOf(LanguageNode("#f1e05a", "Kotlin"))),
                    RepositoryOwner(
                        "TestUser2",
                        "https://avatars.githubusercontent.com/u/25893105?v=4"
                    )
                )
            )
        ),
        RepositoryNodes(
            listOf(
                RepositoryNode(
                    "TopRepository2",
                    "Android application which displays a users profile information using GraphQL",
                    0,
                    LanguageNodes(listOf(LanguageNode("#f1e05a", "Kotlin"))),
                    RepositoryOwner(
                        "User00111",
                        "https://avatars.githubusercontent.com/u/25893105?v=4"
                    )
                )
            )
        ),
        RepositoryNodes(
            listOf(
                RepositoryNode(
                    "StarredRepository2",
                    "Android application which displays a users profile information using GraphQL",
                    0,
                    LanguageNodes(listOf(LanguageNode("#f1e05a", "Kotlin"))),
                    RepositoryOwner(
                        "Test11134",
                        "https://avatars.githubusercontent.com/u/25893105?v=4"
                    )
                )
            )
        )
    )
    private val responseBodySuccess = "{\n" +
            "  \"data\": {\n" +
            "    \"user\": {\n" +
            "      \"name\": \"Naveen Dissanayake\",\n" +
            "      \"login\": \"Naveen898989\",\n" +
            "      \"email\": \"test@example.com\",\n" +
            "      \"followers\": {\n" +
            "        \"totalCount\": 5\n" +
            "      },\n" +
            "      \"following\": {\n" +
            "        \"totalCount\": 11\n" +
            "      },\n" +
            "      \"avatarUrl\": \"https://avatars.githubusercontent.com/u/25893105?v=4\",\n" +
            "      \"pinnedItems\": {\n" +
            "        \"totalCount\": 1,\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"name\": \"PinnedRepository1\",\n" +
            "            \"description\": \"Android application which displays a users profile information using GraphQL\",\n" +
            "            \"stargazerCount\": 0,\n" +
            "            \"languages\": {\n" +
            "              \"nodes\": [\n" +
            "                {\n" +
            "                  \"color\": \"#f1e05a\",\n" +
            "                  \"name\": \"Kotlin\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            \"owner\": {\n" +
            "              \"login\": \"Naveen898989\",\n" +
            "              \"avatarUrl\": \"https://avatars.githubusercontent.com/u/25893105?v=4\"\n" +
            "            }\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"topRepositories\": {\n" +
            "        \"totalCount\": 1,\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"name\": \"TopRepository1\",\n" +
            "            \"description\": \"Android application which displays a users profile information using GraphQL\",\n" +
            "            \"stargazerCount\": 0,\n" +
            "            \"languages\": {\n" +
            "              \"nodes\": [\n" +
            "                {\n" +
            "                  \"color\": \"#f1e05a\",\n" +
            "                  \"name\": \"Kotlin\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            \"owner\": {\n" +
            "              \"login\": \"User00111\",\n" +
            "              \"avatarUrl\": \"https://avatars.githubusercontent.com/u/25893105?v=4\"\n" +
            "            }\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      \"starredRepositories\": {\n" +
            "        \"totalCount\": 1,\n" +
            "        \"nodes\": [\n" +
            "          {\n" +
            "            \"name\": \"StarredRepository1\",\n" +
            "            \"description\": \"Android application which displays a users profile information using GraphQL\",\n" +
            "            \"stargazerCount\": 0,\n" +
            "            \"languages\": {\n" +
            "              \"nodes\": [\n" +
            "                {\n" +
            "                  \"color\": \"#f1e05a\",\n" +
            "                  \"name\": \"Kotlin\"\n" +
            "                }\n" +
            "              ]\n" +
            "            },\n" +
            "            \"owner\": {\n" +
            "              \"login\": \"Test11134\",\n" +
            "              \"avatarUrl\": \"https://avatars.githubusercontent.com/u/25893105?v=4\"\n" +
            "            }\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}"

}