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

import android.app.Dialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.DialogFragment
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset

@Deprecated(
    "The use case for this was in conjunction with TimePickerFragment." +
            "Its use case is deprecated with the standalone DateTimePickerFragment" +
            " and will be removed in a future commit",
    ReplaceWith("DateTimePickerFragment")
)
class DatePickerFragment(): DialogFragment() {

    companion object {
        private const val bkListener = "listener"
        private const val bkInitDate = "initEpochDate"

        @JvmStatic
        internal fun newInstance(
            initDate: LocalDate = LocalDate.now(),
            listener: DatePickerDialog.OnDateSetListener
        ): DatePickerFragment =
            DatePickerFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable(bkListener, listener)
                    putLong(bkInitDate, initDate.toEpochDay())
                }
            }
        @JvmStatic
        internal fun newInstance(
            initDate: LocalDateTime,
            listener: DatePickerDialog.OnDateSetListener
        ): DatePickerFragment =
            newInstance(initDate.toLocalDate(), listener)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker

        val args = checkNotNull(arguments)
        val listener = checkNotNull(args.getParcelable<DatePickerDialog.OnDateSetListener>(bkListener))
        val initEpoch = checkNotNull(args.getLong(bkInitDate))

        val date = LocalDate.ofEpochDay(initEpoch)

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireActivity(), listener,
            date.year, date.monthValue - 1, date.dayOfMonth)
    }
}

@Deprecated(
    "The use case for this was in conjunction with DatePickerFragment." +
            "Its use case is deprecated with the standalone DateTimePickerFragment" +
            " and will be removed in a future commit",
    ReplaceWith("DateTimePickerFragment")
)
class TimePickerFragment(
): DialogFragment() {

    companion object {
        private const val bkListener = "listener"
        private const val bkInitTime = "initSecondTime"
        private const val bkIs24Hour = "is24Hour"

        @JvmStatic
        internal fun newInstance(
            initTime: LocalTime = LocalTime.now(),
            is24Hour: Boolean? = null,
            listener: TimePickerDialog.OnTimeSetListener
        ): TimePickerFragment =
            TimePickerFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable(bkListener, listener)
                    putInt(bkInitTime, initTime.toSecondOfDay())
                    if (is24Hour != null)
                        putBoolean(bkIs24Hour, is24Hour)
                }
            }

        @JvmStatic
        internal fun newInstance(
            initDateTime: LocalDateTime,
            is24Hour: Boolean? = null,
            listener: TimePickerDialog.OnTimeSetListener
        ): TimePickerFragment =
            newInstance(initDateTime.toLocalTime(), is24Hour, listener)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker

        val args = checkNotNull(arguments)
        val listener = checkNotNull(args.getParcelable<TimePickerDialog.OnTimeSetListener>(bkListener))
        val initSeconds = checkNotNull(args.getInt(bkInitTime))

        val is24Hour = (args.get(bkIs24Hour) as Boolean?) ?: DateFormat.is24HourFormat(requireActivity())

        // LocalTime.toSecondOfDay() returns an int yet LocalTime.ofSecondOfDay() takes a long.
        // Odd API quirk.
        val time = LocalTime.ofSecondOfDay(initSeconds.toLong())

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(requireActivity(), listener,
            time.hour, time.minute, time.second, is24Hour)

    }

}

class DateTimePickerFragment(
): DialogFragment() {

    companion object {
        private const val bkListener = "listener"
        private const val bkInitDateTime = "initDT"
        private const val bkIs24Hour = "is24Hour"

        @JvmStatic
        internal fun newInstance(
            initDateTime: LocalDateTime = LocalDateTime.now(),
            is24Hour: Boolean? = null,
            listener: DateTimePickerDialog.OnDateTimeSetListener
        ): DateTimePickerFragment =
            DateTimePickerFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable(bkListener, listener)
                    putLong(bkInitDateTime, initDateTime.toEpochSecond(ZoneOffset.UTC))
                    if (is24Hour != null)
                        putBoolean(bkIs24Hour, is24Hour)
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker

        val args = checkNotNull(arguments)
        val listener = checkNotNull(args.getParcelable<DateTimePickerDialog.OnDateTimeSetListener>(bkListener))
        val initTsEpochSecs = checkNotNull(args.getLong(bkInitDateTime))

        val is24Hour = (args.get(bkIs24Hour) as Boolean?) ?: DateFormat.is24HourFormat(requireActivity())

        val dt = LocalDateTime.ofEpochSecond(initTsEpochSecs, 0, ZoneOffset.UTC)

        // Create a new instance of DateTimePickerDialog and return it
        return DateTimePickerDialog(requireActivity(), listener,
            dt.year, dt.monthValue, dt.dayOfMonth,
            dt.hour, dt.minute, dt.second, is24Hour)
    }

}
