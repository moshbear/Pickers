/*
* Copyright 2007-2011 The Android Open Source Project
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

import android.app.Dialog
import android.os.Bundle
import android.text.format.DateFormat
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class DatePickerFragment(): DialogFragment() {

    companion object {
        private const val bundleListener = "listener"
        private const val bundleInitDate = "initDate"

        @JvmStatic
        internal fun newInstance(
            initDate: Calendar = Calendar.getInstance(),
            listener: DatePickerDialog.OnDateSetListener
        ): DatePickerFragment =
            DatePickerFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable(bundleListener, listener)
                    putLong(bundleInitDate, initDate.timeInMillis)
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker

        val args = checkNotNull(arguments)
        val listener = checkNotNull(args.getParcelable<DatePickerDialog.OnDateSetListener>(bundleListener))
        val initTsMillis = checkNotNull(args.getLong(bundleInitDate))

        val c = Calendar.getInstance().apply { this.timeInMillis = initTsMillis }

        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireActivity(), listener,
            c[Calendar.YEAR], c[Calendar.MONTH], c[Calendar.DAY_OF_MONTH])
    }
}

class TimePickerFragment(
): DialogFragment() {

    companion object {
        private const val bundleListener = "listener"
        private const val bundleInitTime = "initTime"
        private const val bundleIs24Hour = "is24Hour"

        @JvmStatic
        internal fun newInstance(
            initTime: Calendar = Calendar.getInstance(),
            is24Hour: Boolean? = null,
            listener: TimePickerDialog.OnTimeSetListener
        ): TimePickerFragment =
            TimePickerFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable(bundleListener, listener)
                    putLong(bundleInitTime, initTime.timeInMillis)
                    if (is24Hour != null)
                        putBoolean(bundleIs24Hour, is24Hour)
                }
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker

        val args = checkNotNull(arguments)
        val listener = checkNotNull(args.getParcelable<TimePickerDialog.OnTimeSetListener>(bundleListener))
        val initTsMillis = checkNotNull(args.getLong(bundleInitTime))

        val is24Hour = (args.get(bundleIs24Hour) as Boolean?) ?: DateFormat.is24HourFormat(requireActivity())

        val c = Calendar.getInstance().apply { this.timeInMillis = initTsMillis }

        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(requireActivity(), listener,
            c[Calendar.HOUR_OF_DAY], c[Calendar.MINUTE], c[Calendar.SECOND], is24Hour)

    }

}
