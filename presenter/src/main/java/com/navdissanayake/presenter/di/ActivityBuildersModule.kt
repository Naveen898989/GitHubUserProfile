package com.navdissanayake.presenter.di

import com.navdissanayake.presenter.view.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {

    @ContributesAndroidInjector
    abstract fun provideMainActivity(): MainActivity

}