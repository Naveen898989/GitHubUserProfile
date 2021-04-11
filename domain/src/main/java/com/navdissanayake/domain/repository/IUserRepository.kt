package com.navdissanayake.domain.repository

import com.navdissanayake.domain.model.CachedUser
import com.navdissanayake.domain.model.User
import io.reactivex.Completable

interface IUserRepository {

    fun getUser(): CachedUser?

    fun deleteUser(): Completable

    fun saveUser(user: User): Completable

}