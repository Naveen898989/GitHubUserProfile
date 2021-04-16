package com.navdissanayake.presenter.di

import com.navdissanayake.presenter.view.main.MainActivity
import com.navdissanayake.presenter.view.main.MainModule
import com.navdissanayake.presenter.view.main.MainScope
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @MainScope
    @ContributesAndroidInjector(modules = [MainModule::class])
    abstract fun provideMainActivity(): MainActivity

}