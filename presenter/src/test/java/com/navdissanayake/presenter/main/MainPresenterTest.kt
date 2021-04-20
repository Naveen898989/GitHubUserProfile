package com.navdissanayake.presenter.main

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkInfo
import com.navdissanayake.data.util.NoInternetException
import com.navdissanayake.domain.model.*
import com.navdissanayake.domain.repository.IUsersRepository
import com.navdissanayake.domain.usecase.user.InvalidateCachedData
import com.navdissanayake.domain.usecase.user.RetrieveCachedUser
import com.navdissanayake.domain.usecase.user.RetrieveUser
import com.navdissanayake.presenter.view.main.MainPresenter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runBlockingTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
import org.mockito.internal.stubbing.answers.AnswersWithDelay
import org.mockito.internal.stubbing.answers.Returns

@ExperimentalCoroutinesApi
@Suppress("DEPRECATION")
class MainPresenterTest {

    private val userLogin = "Naveen898989"

    private lateinit var mockedContext: Context
    private lateinit var mockedConnectivityManager: ConnectivityManager
    private lateinit var mockedUsersRepository: IUsersRepository
    private lateinit var mockedMainPresenter: MainPresenter
    private lateinit var mockedView: MainPresenter.View

    private val testDispatcher = TestCoroutineDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        mockedContext = mock(Context::class.java)
        mockedConnectivityManager = mock(ConnectivityManager::class.java)
        `when`(mockedContext.getSystemService(Context.CONNECTIVITY_SERVICE))
            .thenReturn(mockedConnectivityManager)

        mockedUsersRepository = mock(IUsersRepository::class.java)

        val retrieveUser = RetrieveUser(mockedUsersRepository)
        val retrieveCachedUser = RetrieveCachedUser(mockedUsersRepository)
        val invalidateCachedData = InvalidateCachedData(mockedUsersRepository)

        mockedMainPresenter = MainPresenter(
            mockedContext,
            retrieveUser,
            retrieveCachedUser,
            invalidateCachedData,
            testDispatcher
        )

        mockedView = mock(MainPresenter.View::class.java)

        mockedMainPresenter.attachView(mockedView)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        testDispatcher.cleanupTestCoroutines()
    }

    @Test
    fun loadUser_internetAvailable_noCache() = testDispatcher.runBlockingTest {
        mockInternetAvailable()
        `when`(mockedUsersRepository.retrieveUser(userLogin))
            .thenReturn(mockedApiUser)
        `when`(mockedUsersRepository.retrieveCachedUser(userLogin))
            .thenReturn(null)

        mockedMainPresenter.loadUser(userLogin)

        verify(mockedView, times(1)).showLoading()
        verify(mockedView, times(1)).onLoadUserComplete(mockedApiUser)
        verify(mockedView, times(1)).hideLoading()
    }

    @Test
    fun loadUserMultipleCalls_OnlyRespondOnce() = testDispatcher.runBlockingTest {
        mockInternetAvailable()
        `when`(mockedUsersRepository.retrieveUser(userLogin))
            .thenAnswer(AnswersWithDelay(1000, Returns(mockedApiUser)))
        `when`(mockedUsersRepository.retrieveCachedUser(userLogin))
            .thenReturn(null)

        mockedMainPresenter.loadUser(userLogin)
        mockedMainPresenter.loadUser(userLogin)

        verify(mockedView, times(2)).showLoading()
        verify(mockedView, times(1)).onLoadUserComplete(mockedApiUser)
        verify(mockedView, times(1)).hideLoading()
    }

    @Test
    fun loadUser_noInternetAvailable_noCache() = testDispatcher.runBlockingTest {
        mockInternetNotAvailable()
        `when`(mockedUsersRepository.retrieveUser(userLogin))
            .thenReturn(mockedApiUser)
        `when`(mockedUsersRepository.retrieveCachedUser(userLogin))
            .thenReturn(null)

        mockedMainPresenter.loadUser(userLogin)

        verify(mockedView, times(1)).showLoading()
        verify(mockedView, times(1))
            .onLoadUserError(ArgumentMatchers.any(NoInternetException::class.java))
        verify(mockedView, times(1)).hideLoading()
    }

    @Test
    fun loadUser_noInternetAvailable_cacheAvailable() = testDispatcher.runBlockingTest {
        mockInternetNotAvailable()
        `when`(mockedUsersRepository.retrieveUser(userLogin))
            .thenReturn(mockedApiUser)
        `when`(mockedUsersRepository.retrieveCachedUser(userLogin))
            .thenReturn(mockedCachedUser)

        mockedMainPresenter.loadUser(userLogin)

        verify(mockedView, times(1)).showLoading()
        verify(mockedView, times(1)).onLoadUserComplete(mockedCachedUser)
        verify(mockedView, times(1)).hideLoading()
    }

    private fun mockInternetAvailable() {
        val mockedNetworkInfo: NetworkInfo = mock(NetworkInfo::class.java)
        `when`(mockedNetworkInfo.isConnected)
            .thenReturn(true)

        `when`(mockedConnectivityManager.activeNetworkInfo)
            .thenReturn(mockedNetworkInfo)
    }

    private fun mockInternetNotAvailable() {
        val mockedNetworkInfo: NetworkInfo? = null

        `when`(mockedConnectivityManager.activeNetworkInfo)
            .thenReturn(mockedNetworkInfo)
    }

    private val mockedApiUser = User(
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
    private val mockedCachedUser = User(
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

}