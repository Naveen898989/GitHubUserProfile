package com.navdissanayake.data.networking

/**
 * Generic Graph QL response class
 */
data class GraphQlResponse<T>(val data: T)