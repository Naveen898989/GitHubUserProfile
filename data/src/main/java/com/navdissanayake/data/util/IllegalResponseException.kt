package com.navdissanayake.data.util

/**
 * Exception class used to throw exceptions when response code from API is non-success
 */
class IllegalResponseException(message: String, val response: String?) : Exception(message)