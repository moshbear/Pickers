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

        @JvmStatic
        fun newInstance(listener: DatePickerDialog.OnDateSetListener): DatePickerFragment =
            DatePickerFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable(bundleListener, listener)
                }
            }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current date as the default date in the picker
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val listener = checkNotNull(arguments).getParcelable<DatePickerDialog.OnDateSetListener>(bundleListener)
        // Create a new instance of DatePickerDialog and return it
        return DatePickerDialog(requireActivity(), listener, year, month, day)
    }
}

class TimePickerFragment(
): DialogFragment() {

    companion object {
        private const val bundleListener = "listener"
        @JvmStatic
        fun newInstance(listener: TimePickerDialog.OnTimeSetListener): TimePickerFragment =
            TimePickerFragment().apply {
                this.arguments = Bundle().apply {
                    putParcelable(bundleListener, listener)
                }
            }
        }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        // Use the current time as the default values for the picker
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val second = c.get(Calendar.SECOND)

        val is24Hour = DateFormat.is24HourFormat(requireActivity())

        val listener = checkNotNull(arguments).getParcelable<TimePickerDialog.OnTimeSetListener>(bundleListener)
        // Create a new instance of TimePickerDialog and return it
        return TimePickerDialog(requireActivity(), listener, hour, minute, second, is24Hour)

    }

}
