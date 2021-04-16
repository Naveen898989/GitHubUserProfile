package com.navdissanayake.presenter.di

import android.app.Application
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.bumptech.glide.request.RequestOptions
import com.navdissanayake.data.networking.GithubGraphQlApi
import com.navdissanayake.data.networking.RetrofitClient
import com.navdissanayake.presenter.R
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class AppModule {

    @Singleton
    @Provides
    fun provideGithubGraphQlRepository(): GithubGraphQlApi {
        return RetrofitClient.githubGraphQlApi
    }

    @Singleton
    @Provides
    fun provideRequestOptions(): RequestOptions {
        return RequestOptions
            .placeholderOf(R.drawable.ic_avatar_placeholder_24)
            .error(R.drawable.ic_error_24)
    }

    @Singleton
    @Provides
    fun provideGlideInstance(
        application: Application,
        requestOptions: RequestOptions
    ): RequestManager {
        return Glide
            .with(application)
            .setDefaultRequestOptions(requestOptions)
    }

}