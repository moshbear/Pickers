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

import android.os.Parcelable
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlinx.parcelize.Parcelize

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


@Parcelize
data class DateTriple(val year: Int, val month: Int, val day: Int) : Parcelable
internal fun defaultDateTriple(): DateTriple =
    with (Calendar.getInstance()) {
        DateTriple(get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH))

    }

@Parcelize
data class TimeTriple(val hour: Int, val minute: Int, val second: Int) : Parcelable
internal fun defaultTimeTriple(): TimeTriple =
    with (Calendar.getInstance()) {
        TimeTriple(get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE), get(Calendar.SECOND))
    }


internal data class DateTimeTuple(
    val dY: Int = -1, val dM: Int = -1, val dD: Int = -1, // date params
    val tH: Int = -1, val tM: Int = -1, val tS: Int = -1 // time params
) {
    fun hasDate(): Boolean = dY >= 0 && dM >= 0 && dD >= 0
    fun hasTime(): Boolean = tH >= 0 && tM >= 0 && tS >= 0
    fun toBoolean(): Boolean = hasDate() || hasTime()
    @Suppress("DEPRECATION") // Date((int,)*6), Date().timezoneOffset
    fun pack(): Long {
        check(hasDate())
        check(hasTime())

        check(dY >= 1970)
        // All I care about is converting the unpacked time back to seconds past Unix epoch.
        // The deprecated Date class and Date(int,int,int,int,int,int) constructor works fine for this.
        val d = Date(dY - 1900, dM, dD, tH, tM, tS)
        return d.time / 1000
    }
    /// Copy with existing date and new time.
    fun copyDate(tH_: Int, tM_: Int, tS_: Int) = DateTimeTuple(dY, dM, dD, tH_, tM_, tS_)
    /// Copy with new date and existing time.
    fun copyTime(dY_: Int, dM_: Int, dD_: Int) = DateTimeTuple(dY_, dM_, dD_, tH, tM, tS)

    fun extractDate(): DateTriple = DateTriple(dY, dM, dD)
    fun extractTime(): TimeTriple = TimeTriple(tH, tM, tS)

    companion object {
        fun unpack(t: Long?): DateTimeTuple? {
            t ?: return null
            with (Calendar.getInstance()) {
                time = Date(t * 1000)
                return DateTimeTuple(
                    get(Calendar.YEAR), get(Calendar.MONTH), get(Calendar.DAY_OF_MONTH),
                    get(Calendar.HOUR_OF_DAY), get(Calendar.MINUTE), get(Calendar.SECOND))
            }
        }
    }
}
