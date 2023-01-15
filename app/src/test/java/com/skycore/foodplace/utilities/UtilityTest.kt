package com.skycore.foodplace.utilities

import com.google.common.truth.Truth
import org.junit.Test

class UtilityTest {

    @Test
    fun validLocation() {
        val latitude = ""
        val longitude = ""

        val result = Utility.latLongValidation(latitude, longitude)
        Truth.assertThat(result).isEqualTo(true)
    }
}