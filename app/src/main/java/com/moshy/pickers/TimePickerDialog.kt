/*
*
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
package com.moshy.pickers

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import android.view.LayoutInflater
import androidx.annotation.StyleRes

class TimePickerDialog(
    _context: Context, @StyleRes themeResId: Int,
    private val callback: OnTimeSetListener?,
    initialHourOfDay: Int, initialMinute: Int, initialSecond: Int, is24HourView: Boolean
): AlertDialog(_context, themeResId), OnClickListener, TimePicker.OnTimeChangedListener {

    fun interface OnTimeSetListener: Parcelable {
        fun onTimeSet(view: TimePicker, hour: Int, minute: Int, second: Int)
        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) { /* no-op */ }
    }

    constructor(
        context: Context, callback: OnTimeSetListener?,
        hourOfDay: Int, minute: Int, second: Int, is24HourView: Boolean
    )
    : this(context, 0, callback, hourOfDay, minute, second, is24HourView)

    companion object {
        private const val bkHour = "hour"
        private const val bkMinute = "minute"
        private const val bkSecond = "second"
        private const val bkIs24Hour = "is24hour"
    }

    private val timePicker: TimePicker

    init {
        setTitle(R.string.time_picker_dialog_title)
        setButton(BUTTON_POSITIVE, context.getString(R.string.date_time_done), this)
        timePicker =
            LayoutInflater.from(context).inflate(R.layout.time_picker_dialog, null)
            .also { setView(it) }
            .findViewById(R.id.timePicker)

        timePicker.is24HourView = is24HourView
        timePicker.hour = initialHourOfDay
        timePicker.minute = initialMinute
        timePicker.second = initialSecond
        timePicker.setOnTimeChangedListener(this)
    }

    private fun tryCallback() {
        if (callback != null) {
            timePicker.clearFocus()
            callback.onTimeSet(timePicker,
                timePicker.hour, timePicker.minute, timePicker.second)
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        require(which == BUTTON_POSITIVE) {
            "$which is unexpected because the listener was only declared for BUTTON_POSITIVE"
        }
        tryCallback()
    }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int, second: Int) {
        /* no-op */
    }

    /**
     * Sets the current time.
     *
     * @param hourOfDay The current hour within the day.
     * @param minuteOfHour The current initialMinute within the hour.
     * @param secondOfMinute The current initialSecond within the minute.
     */
    fun updateTime(hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int) {
        timePicker.hour = hourOfDay
        timePicker.minute = minuteOfHour
        timePicker.second = secondOfMinute
    }

    override fun onStop() {
        tryCallback()
        super.onStop()
    }

    override fun onSaveInstanceState(): Bundle {
        val state = super.onSaveInstanceState()
        state.putInt(bkHour, timePicker.hour)
        state.putInt(bkMinute, timePicker.minute)
        state.putInt(bkSecond, timePicker.second)
        state.putBoolean(bkIs24Hour, timePicker.is24HourView)
        return state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val hour = savedInstanceState.getInt(bkHour)
        val minute = savedInstanceState.getInt(bkMinute)
        val second = savedInstanceState.getInt(bkSecond)
        timePicker.is24HourView = savedInstanceState.getBoolean(bkIs24Hour)
        timePicker.hour = hour
        timePicker.minute = minute
        timePicker.second = second
    }
}

