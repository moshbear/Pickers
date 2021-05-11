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
package com.moshy.pickers

import android.content.Context
import android.text.format.DateFormat
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

internal data class DateFormatConfiguration(val locale: Locale, val is24Hour: Boolean)
// Singleton to cache SimpleDateFormat for given DateFormatConfiguration
private val DateFormatSettings = object {
    private lateinit var dateFormat: DateFormatConfiguration

    private lateinit var bestDateTimePattern: String

    private lateinit var formatter: SimpleDateFormat

    fun getSimpleDateFormatter(df: DateFormatConfiguration): SimpleDateFormat {
        if ((!::dateFormat.isInitialized)
            || (dateFormat != df))
        {
            dateFormat = df

            bestDateTimePattern = DateFormat.getBestDateTimePattern(dateFormat.locale,
                /* skeleton */ when (dateFormat.is24Hour) {
                    true -> "y-M-d\nHms"
                    false -> "y-M-d\nhms"
                })

            formatter = SimpleDateFormat(bestDateTimePattern, dateFormat.locale)
        }
        return formatter
    }
}

internal fun timestampToDateTimeString(df: DateFormatConfiguration, time: Long): String =
    synchronized(DateFormatSettings) {
        DateFormatSettings.getSimpleDateFormatter(df).format(Date(time * 1000))
    }

internal fun dtStringView(dt: LiveData<Calendar>, df: DateFormatConfiguration,
                          context: Context, @StringRes resId: Int) =
    Transformations.map(dt) {
        context.getString(resId, timestampToDateTimeString(df, it.timeInMillis / 1000))
    }

internal fun updateDtDate(dt: MutableLiveData<Calendar>, y: Int, m: Int, d: Int) =
    dt.value?.run {
        val c = this.clone() as Calendar // force copy
        c[Calendar.YEAR] = y
        c[Calendar.MONTH] = m
        c[Calendar.DAY_OF_MONTH] = d
        dt.value = c
    } ?: check(false)

internal fun updateDtTime(dt: MutableLiveData<Calendar>, h: Int, m: Int, s: Int) =
    dt.value?.run {
        val c = this.clone() as Calendar // force copy
        c[Calendar.HOUR_OF_DAY] = h
        c[Calendar.MINUTE] = m
        c[Calendar.SECOND] = s
        dt.value = c
    } ?: check(false)