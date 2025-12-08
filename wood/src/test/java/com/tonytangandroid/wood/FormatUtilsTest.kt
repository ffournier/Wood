package com.tonytangandroid.wood

import com.google.common.truth.Truth
import com.tonytangandroid.wood.FormatUtils.indexOf
import org.junit.Test

class FormatUtilsTest {
    @Test
    fun onHandleIntent() {
        Truth.assertThat(indexOf("abcd", "c")).isEqualTo(mutableListOf<Int?>(2))
    }
}
