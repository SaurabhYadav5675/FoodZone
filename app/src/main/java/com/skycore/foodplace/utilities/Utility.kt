package com.skycore.foodplace.utilities

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.text.TextUtils
import android.widget.Toast

object Utility {
    fun checkInternetConnection(context: Context): Boolean {
        val connectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.M) {
            val activeNetwork =
                connectivity.getNetworkCapabilities(connectivity.activeNetwork) ?: return false
            return when {
                // check wiFi network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                // check phone network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                // check internet Capability
                activeNetwork.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> true
                // else return no internet
                else -> false
            }
        } else {
            // if android version is below M
            @Suppress("DEPRECATION") return connectivity.activeNetworkInfo?.isConnected ?: false
        }

    }

    fun getRadiusText(progress: Int): String {
        val radius = if (progress < 10) {
            "${progress * 100} M"
        } else {
            val quotient = progress / 10
            val remainder = progress % 10
            if (remainder > 0) "$quotient.$remainder KM" else "$quotient KM"
        }
        return radius
    }

    fun latLongValidation(latitude: String, longitude: String): Boolean {
        val isValid: Boolean = if (TextUtils.isEmpty(latitude) || TextUtils.isEmpty(longitude)) {
            false
        } else if (latitude == "0.0" || longitude == "0.0") {
            false
        } else if (latitude.toDouble() < -90 || latitude.toDouble() > 90) {
            false
        } else !(longitude.toDouble() < -180 || longitude.toDouble() > 180)
        return isValid
    }

    fun shortToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}