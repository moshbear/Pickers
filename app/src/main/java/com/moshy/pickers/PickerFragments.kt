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
import java.util.Calendar

class DatePickerFragment(): DialogFragment() {

    companion object {
        private const val bkListener = "listener"
        private const val bkInitDate = "initDate"

        @JvmStatic
        internal fun newInstance(
            initDate: Calendar = Calendar.getInstance(),
            listener: DatePickerDialog.OnDateSetListener
        ): DatePickerFragment =
            DatePickerFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable(bkListener, listener)
                    putLong(bkInitDate, initDate.timeInMillis)
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker

        val args = checkNotNull(arguments)
        val listener = checkNotNull(args.getParcelable<DatePickerDialog.OnDateSetListener>(bkListener))
        val initTsMillis = checkNotNull(args.getLong(bkInitDate))

        val c = Calendar.getInstance().apply { this.timeInMillis = initTsMillis }

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireActivity(), listener,
            c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DAY_OF_MONTH])
    }
}

class TimePickerFragment(
): DialogFragment() {

    companion object {
        private const val bkListener = "listener"
        private const val bkInitTime = "initTime"
        private const val bkIs24Hour = "is24Hour"

        @JvmStatic
        internal fun newInstance(
            initTime: Calendar = Calendar.getInstance(),
            is24Hour: Boolean? = null,
            listener: TimePickerDialog.OnTimeSetListener
        ): TimePickerFragment =
            TimePickerFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable(bkListener, listener)
                    putLong(bkInitTime, initTime.timeInMillis)
                    if (is24Hour != null)
                        putBoolean(bkIs24Hour, is24Hour)
                }
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker

        val args = checkNotNull(arguments)
        val listener = checkNotNull(args.getParcelable<TimePickerDialog.OnTimeSetListener>(bkListener))
        val initTsMillis = checkNotNull(args.getLong(bkInitTime))

        val is24Hour = (args.get(bkIs24Hour) as Boolean?) ?: DateFormat.is24HourFormat(requireActivity())

        val c = Calendar.getInstance().apply { this.timeInMillis = initTsMillis }

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(requireActivity(), listener,
            c[Calendar.HOUR_OF_DAY], c[Calendar.MINUTE], c[Calendar.SECOND], is24Hour)

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
