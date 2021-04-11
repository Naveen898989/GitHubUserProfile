package com.navdissanayake.presenter.view.main

import android.app.Application
import com.navdissanayake.data.Users
import com.navdissanayake.presenter.di.PerScreen
import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @PerScreen
    @Provides
    fun providesUsers(application: Application) = Users(application)

    @PerScreen
    @Provides
    fun provideMainView(mainActivity: MainActivity): MainPresenter.View = mainActivity

    @PerScreen
    @Provides
    fun provideMainPresenter(users: Users, view: MainPresenter.View) = MainPresenter(users, view)

}