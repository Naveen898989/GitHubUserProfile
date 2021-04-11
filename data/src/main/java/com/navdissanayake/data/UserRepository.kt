package com.navdissanayake.data

import android.content.SharedPreferences
import com.google.gson.Gson
import com.navdissanayake.domain.model.CachedUser
import com.navdissanayake.domain.model.User
import com.navdissanayake.domain.repository.IUserRepository
import io.reactivex.Completable

class UserRepository(private val preferences: SharedPreferences) : IUserRepository {

    override fun getUser(): CachedUser? {
        val cachedData: String? = preferences.getString("CachedUser", null)

        return if (cachedData != null)
            Gson().fromJson(cachedData, CachedUser::class.java)
        else
            null
    }

    override fun deleteUser(): Completable {
        return Completable.fromAction { preferences.edit().remove("CachedUser").apply() }
    }

    override fun saveUser(user: User): Completable {
        val cacheData: String = Gson().toJson(CachedUser(user, System.currentTimeMillis()))

        return Completable.fromAction {
            preferences.edit().putString("CachedUser", cacheData).apply()
        }
    }

}