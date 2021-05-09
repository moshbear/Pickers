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
package com.moshy.pickersdemo

import android.text.format.DateFormat
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

internal fun timestampToCalendar(ts: Long): Calendar =
    Calendar.getInstance().apply {
        timeInMillis = ts * 1000
    }
