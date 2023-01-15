package com.skycore.foodplace.utilities

import android.annotation.SuppressLint
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.common.truth.Truth

import org.junit.Test

class UtilityTest {


    @SuppressLint("CheckResult")
    @Test
    fun checkInternetConnection() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val utility = Utility
        val result = utility.checkInternetConnection(context)
        Truth.assertThat(result).isEqualTo(true)
    }
}