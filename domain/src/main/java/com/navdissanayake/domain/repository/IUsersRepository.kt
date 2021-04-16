package com.navdissanayake.domain.repository

import com.navdissanayake.domain.model.User

interface IUsersRepository {

    /**
     * Retrieve user with the given user login ID
     */
    suspend fun retrieveUser(userLogin: String): User

    /**
     * Retrieve user with given user ID from cache if available
     */
    suspend fun retrieveCachedUser(userLogin: String): User?

    /**
     * Clear all user caches
     */
    suspend fun invalidateCachedData(): Boolean

}