/*
* Copyright 2021 Andrey V
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.moshy.pickers.ui

import android.content.Context
import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

internal class DateTimeFormatter(locale: Locale, is24Hour: Boolean) {
    private val bestDateTimePattern =
        DateFormat.getBestDateTimePattern(locale,
        /* skeleton */ when (is24Hour) {
            true -> "yMd Hms"
            false -> "yMd hms"
        })
    private val formatter = DateTimeFormatter.ofPattern(bestDateTimePattern, locale)

    fun formatLocalDateTime(localDateTime: LocalDateTime): String = localDateTime.format(formatter)
}

internal fun dtStringView24(dt: LiveData<LocalDateTime>, df: LiveData<com.moshy.pickers.ui.DateTimeFormatter>,
                            context: Context, @StringRes resId: Int): LiveData<String> =
    MediatorLiveData<String>().apply {
        val stringView = { dt: LocalDateTime, df: com.moshy.pickers.ui.DateTimeFormatter ->
            context.getString(resId, df.formatLocalDateTime(dt))
        }

        addSource(dt) { dt: LocalDateTime? ->
            df.value?.run {
                this@apply.value = stringView(checkNotNull(dt), this)
            }
        }
        addSource(df) { df: com.moshy.pickers.ui.DateTimeFormatter? ->
            dt.value?.run {
                this@apply.value = stringView(this, checkNotNull(df))
            }
        }
    }