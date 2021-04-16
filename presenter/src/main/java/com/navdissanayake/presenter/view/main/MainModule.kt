package com.navdissanayake.presenter.view.main

import android.app.Application
import com.navdissanayake.data.networking.GithubGraphQlApi
import com.navdissanayake.data.repository.UsersRepository
import com.navdissanayake.domain.repository.IUsersRepository
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideUsersRepository(
        application: Application,
        githubGraphQlApi: GithubGraphQlApi
    ): IUsersRepository {
        return UsersRepository(application, githubGraphQlApi)
    }

    @MainScope
    @Provides
    fun provideMainPresenter(
        application: Application,
        usersRepository: IUsersRepository
    ): MainPresenter {
        return MainPresenter(application, usersRepository)
    }

}