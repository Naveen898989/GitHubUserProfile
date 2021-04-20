package com.navdissanayake.presenter.view.main

import android.content.Context
import com.navdissanayake.data.util.Helper
import com.navdissanayake.data.util.NoInternetException
import com.navdissanayake.domain.model.User
import com.navdissanayake.domain.usecase.user.InvalidateCachedData
import com.navdissanayake.domain.usecase.user.RetrieveCachedUser
import com.navdissanayake.domain.usecase.user.RetrieveUser
import com.navdissanayake.presenter.base.BasePresenter
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Presenter for main screen which holds user profile info
 * @param context Context to use
 * @param retrieveUser [RetrieveUser] use case class
 * @param retrieveCachedUser [RetrieveCachedUser] use case class
 * @param invalidateCachedData [invalidateCachedData] use case class
 */
class MainPresenter constructor(
    private val context: Context,
    private val retrieveUser: RetrieveUser,
    private val retrieveCachedUser: RetrieveCachedUser,
    private val invalidateCachedData: InvalidateCachedData,
    defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
) :
    BasePresenter<MainPresenter.View>(defaultDispatcher) {

    private var isLoadingUser = false

    /**
     * Method to retrieve user data.
     *
     * Will call [View.showLoading] at the start. After retrieve will call [View.hideLoading].
     *
     * Once loading completes will call [View.onLoadUserComplete] if request was a success. If user
     * has no internet and has no cache will send [NoInternetException] as error.
     *
     * @param userLogin Users login id.
     */
    fun loadUser(userLogin: String) {
        view?.showLoading()

        if (!isLoadingUser) {
            scope.launch {
                isLoadingUser = true

                var user: User? = null
                var error: Throwable? = null
                try {
                    if (Helper.isNetworkConnected(context)) {
                        user = retrieveUser.execute(RetrieveUser.RequestValues(userLogin))
                    } else {
                        user =
                            retrieveCachedUser.execute(RetrieveCachedUser.RequestValues(userLogin))

                        // Only throw no internet error if no cache is found
                        if (user == null) {
                            error =
                                NoInternetException()
                        }
                    }
                } catch (e: Exception) {
                    error = e
                }

                notifyView(error, user)

                isLoadingUser = false
            }
        }
    }

    private suspend fun notifyView(error: Throwable?, user: User?) {
        withContext(Dispatchers.Main) {
            when {
                error != null -> {
                    view?.onLoadUserError(error)
                }
                user != null -> {
                    view?.onLoadUserComplete(user)
                }
                else -> {
                    // Shouldn't happen
                    view?.onLoadUserError(IllegalStateException("Unknown error occurred"))
                }
            }

            view?.hideLoading()
        }
    }

    /**
     * Clear all cache in order to retrieve data again.
     */
    fun invalidateCache() {
        scope.launch {
            invalidateCachedData.execute(InvalidateCachedData.RequestValues())
        }
    }

    interface View {
        /**
         * Show progress bar for user
         */
        fun showLoading()

        /**
         * Hide progress bar from user
         */
        fun hideLoading()

        /**
         * Called when user data was successfully retrieved.
         * @param user User object
         */
        fun onLoadUserComplete(user: User)

        /**
         * Call when user data retrieval caused error out. Will show general message to user.
         */
        fun onLoadUserError(e: Throwable?)
    }

}
