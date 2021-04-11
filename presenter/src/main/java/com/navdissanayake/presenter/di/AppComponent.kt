package com.navdissanayake.presenter.di

import android.app.Application
import com.navdissanayake.presenter.GithubUserProfileApplication
import com.navdissanayake.presenter.view.main.MainModule
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import dagger.android.AndroidInjector
import javax.inject.Singleton

@Singleton
@Component(modules = [AndroidInjectionModule::class, ActivityBuildersModule::class, AppModule::class, MainModule::class])
interface AppComponent : AndroidInjector<GithubUserProfileApplication> {

    @Component.Builder
    interface Builder {
        @BindsInstance
        fun application(application: Application): Builder
        fun build(): AppComponent
    }
}