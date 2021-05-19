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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal data class DateFormatConfiguration(val locale: Locale, val is24Hour: Boolean)
// Singleton to cache SimpleDateFormat for given DateFormatConfiguration
private val DateFormatSettings = object {
    private lateinit var dateFormat: DateFormatConfiguration

    private lateinit var bestDateTimePattern: String

    private lateinit var formatter: DateTimeFormatter

    fun getSimpleDateFormatter(df: DateFormatConfiguration): DateTimeFormatter {
        if ((!::dateFormat.isInitialized)
            || (dateFormat != df))
        {
            dateFormat = df

            bestDateTimePattern = DateFormat.getBestDateTimePattern(dateFormat.locale,
                /* skeleton */ when (dateFormat.is24Hour) {
                    true -> "y-M-d\nHms"
                    false -> "y-M-d\nhms"
                })

            formatter = DateTimeFormatter.ofPattern(bestDateTimePattern, dateFormat.locale)
        }
        return formatter
    }
}

internal fun timestampToDateTimeString(df: DateFormatConfiguration, dt: LocalDateTime): String =
    // the formatter is thread-safe; the caching by the singleton is not
    synchronized(DateFormatSettings) {
        dt.format(DateFormatSettings.getSimpleDateFormatter(df))
    }

internal fun dtStringView24(dt: LiveData<LocalDateTime>, is24hour: LiveData<Boolean>, locale: Locale,
                          context: Context, @StringRes resId: Int): LiveData<String> =
    MediatorLiveData<String>().apply {
        val stringView = { dt: LocalDateTime, is24: Boolean ->
            context.getString(resId,
                timestampToDateTimeString(
                    DateFormatConfiguration(locale, is24),
                    dt)
            )
        }

        addSource(dt) { dt: LocalDateTime? ->
            is24hour.value?.run {
                this@apply.value = stringView(checkNotNull(dt), this)
            }
        }
        addSource(is24hour) { is24h: Boolean? ->
            dt.value?.run {
                this@apply.value = stringView(this, checkNotNull(is24h))
            }
        }
    }

internal fun updateDtDate(dt: MutableLiveData<LocalDateTime>, y: Int, m: Int, d: Int) =
    dt.value?.run {
        dt.value = withYear(y).withMonth(m).withDayOfMonth(d)
    } ?: check(false)

internal fun updateDtTime(dt: MutableLiveData<LocalDateTime>, h: Int, m: Int, s: Int) =
    dt.value?.run {
        dt.value = withHour(h).withMinute(m).withSecond(s)
    } ?: check(false)
