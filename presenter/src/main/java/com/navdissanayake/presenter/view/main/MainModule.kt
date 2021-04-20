package com.navdissanayake.presenter.view.main

import android.app.Application
import com.navdissanayake.data.networking.GithubGraphQlApi
import com.navdissanayake.data.repository.UsersRepository
import com.navdissanayake.domain.repository.IUsersRepository
import com.navdissanayake.domain.usecase.user.InvalidateCachedData
import com.navdissanayake.domain.usecase.user.RetrieveCachedUser
import com.navdissanayake.domain.usecase.user.RetrieveUser
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideUsersRepository(
        application: Application,
        githubGraphQlApi: GithubGraphQlApi
    ): IUsersRepository =
        UsersRepository(application, githubGraphQlApi)

    @MainScope
    @Provides
    fun provideRetrieveUser(usersRepository: IUsersRepository): RetrieveUser =
        RetrieveUser(usersRepository)

    @MainScope
    @Provides
    fun provideRetrieveCachedUser(usersRepository: IUsersRepository): RetrieveCachedUser =
        RetrieveCachedUser(usersRepository)

    @MainScope
    @Provides
    fun provideInvalidateCachedDate(usersRepository: IUsersRepository): InvalidateCachedData =
        InvalidateCachedData(usersRepository)

    @MainScope
    @Provides
    fun provideMainPresenter(
        application: Application,
        retrieveUser: RetrieveUser,
        retrieveCachedUser: RetrieveCachedUser,
        invalidateCachedData: InvalidateCachedData
    ): MainPresenter {
        return MainPresenter(application, retrieveUser, retrieveCachedUser, invalidateCachedData)
    }

}