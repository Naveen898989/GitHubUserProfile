package com.navdissanayake.data.util

import android.content.Context
import android.net.ConnectivityManager

/**
 * Helper class which holds convenience methods
 */
object Helper {

    /**
     * Convenience method for checking users network connectivity
     */
    fun isNetworkConnected(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val activeNetwork = cm.activeNetworkInfo
        return activeNetwork?.isConnected == true
    }

}