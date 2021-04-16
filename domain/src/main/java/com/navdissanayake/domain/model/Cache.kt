package com.navdissanayake.domain.model

/**
 * Generic cache object which can be used to cache any data on sharedPreferences.
 */
data class Cache<T>(val cachedData: T, val timestamp: Long) {

    /**
     * Convenience method to check if cache has expired
     * @param cacheDuration Cache validity period
     * @return true if valid
     */
    fun isCacheValid(cacheDuration: Long): Boolean {
        return timestamp + cacheDuration > System.currentTimeMillis()
    }

}