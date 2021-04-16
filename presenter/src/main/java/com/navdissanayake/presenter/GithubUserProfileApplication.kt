package com.navdissanayake.presenter

import com.navdissanayake.data.networking.RetrofitClient
import com.navdissanayake.presenter.di.DaggerAppComponent
import com.navdissanayake.presenter.util.API_BASE_URL
import dagger.android.AndroidInjector
import dagger.android.DaggerApplication

class GithubUserProfileApplication : DaggerApplication() {

    override fun applicationInjector(): AndroidInjector<out DaggerApplication> {
        // Init retrofit client with context, access token and API base url
        RetrofitClient.init(API_BASE_URL, BuildConfig.API_ACCESS_TOKEN)

        return DaggerAppComponent.builder().application(this).build()
    }

}