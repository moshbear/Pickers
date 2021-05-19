/*
 * Copyright (C) 2021 Andrey V
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.time.LocalDateTime

/**
 * A simple dialog containing a [DateTimePicker].
 *
 *
 * See the [Pickers]({@docRoot}guide/topics/ui/controls/pickers.html)
 * guide.
 */
class DateTimePickerDialog(
    context: Context,
    @StyleRes theme: Int,
    private val callback: OnDateTimeSetListener?,
    year: Int, monthOfYear: Int, dayOfMonth: Int,
    initialHour: Int, initialMinute: Int, initialSecond: Int, is24Hour: Boolean
) : AlertDialog(context, theme), OnClickListener {


    fun interface OnDateTimeSetListener: Parcelable {
        fun onDateTimeSet(
            view: DateTimePicker,
            dateTime: LocalDateTime
        )
        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) { /* no-op */ }
    }

    @Suppress("DEPRECATION")
    private var dateTime = LocalDateTime.now()

    /**
     * Gets the [DateTimePicker] contained in this dialog.
     *
     * @return The calendar view.
     */
    private val picker =
        LayoutInflater.from(context).inflate(R.layout.date_time_picker_dialog, null)
        .also { setView(it) }
        .requireViewById1<DateTimePicker>(R.id.dateTimePicker)
        .also {
            it.setDate(year, monthOfYear, dayOfMonth)
            it.setTime(initialHour, initialMinute, initialSecond)
            it.is24HourView = is24Hour
            it.setOnDateTimeChangedListener { _, dt -> dateTime = dt }
        }

    /**
     * @param context The context the dialog is to run in.
     * @param callBack How the parent is notified that the date is set.
     * @param initialDateTime Initial date-time state.
     * @param is24Hour Whether or not the time picker is 24-hour.
     */
    constructor(
        context: Context,
        callBack: OnDateTimeSetListener?,
        initialDateTime: LocalDateTime, is24Hour: Boolean
    ) : this(context, 0, callBack,
            initialDateTime.year, initialDateTime.monthValue, initialDateTime.dayOfMonth,
            initialDateTime.hour, initialDateTime.minute, initialDateTime.second,
            is24Hour)
    /**
     * @param context The context the dialog is to run in.
     * @param callBack How the parent is notified that the date is set.
     * @param year The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth The initial day of the dialog.
     * @param initialHour The initial hour of the dialog.
     * @param initialMinute The initial minute of the dialog.
     * @param initialSecond The initial second of the dialog.
     * @param is24Hour Whether or not the time picker is 24-hour.
     */
    constructor(
        context: Context,
        callBack: DateTimePickerDialog.OnDateTimeSetListener?,
        year: Int, monthOfYear: Int, dayOfMonth: Int,
        initialHour: Int, initialMinute: Int, initialSecond: Int, is24Hour: Boolean
    ) : this(context, 0, callBack,
        year, monthOfYear, dayOfMonth,
        initialHour, initialMinute, initialSecond, is24Hour)

    companion object {
        private const val bkYear = "year"
        private const val bkMonth = "month"
        private const val bkDay = "day"
        private const val bkHour = "hour"
        private const val bkMinute = "minute"
        private const val bkSecond = "second"
        private const val bkIs24Hour = "is24hour"
    }

    init {
        setTitle(R.string.date_time_picker_dialog_title)
        setButton(BUTTON_POSITIVE, context.getText(R.string.date_time_done), this)
    }

    override fun onClick(dialog: DialogInterface, which: Int) = tryCallback()
    
    /**
     * Sets the current date.
     *
     * @param year The date year.
     * @param monthOfYear The date month.
     * @param dayOfMonth The date day of month.
     */
    fun updateDate(year: Int, monthOfYear: Int, dayOfMonth: Int) =
        picker.setDate(year, monthOfYear, dayOfMonth)
    /**
     * Sets the current time.
     *
     * @param hourOfDay The current hour within the day.
     * @param minuteOfHour The current initialMinute within the hour.
     * @param secondOfMinute The current initialSecond within the minute.
     */
    fun updateTime(hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int) =
        picker.setTime(hourOfDay, minuteOfHour, secondOfMinute)
    

    private fun tryCallback() {
        if (callback != null) {
            picker.clearFocus()
            callback.onDateTimeSet(picker, picker.dateTime)
        }
    }

    override fun onStop() {
        tryCallback()
        super.onStop()
    }

    override fun onSaveInstanceState(): Bundle {
        val state: Bundle = super.onSaveInstanceState()
        state.putInt(bkYear, picker.year)
        state.putInt(bkMonth, picker.month)
        state.putInt(bkDay, picker.dayOfMonth)
        state.putInt(bkHour, picker.hour)
        state.putInt(bkMinute, picker.minute)
        state.putInt(bkSecond, picker.second)
        state.putBoolean(bkIs24Hour, picker.is24HourView)
        return state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val year = savedInstanceState.getInt(bkYear)
        val month = savedInstanceState.getInt(bkMonth)
        val day = savedInstanceState.getInt(bkDay)
        val hour = savedInstanceState.getInt(bkHour)
        val minute = savedInstanceState.getInt(bkMinute)
        val second = savedInstanceState.getInt(bkSecond)
        picker.setDate(year, month, day)
        picker.is24HourView = savedInstanceState.getBoolean(bkIs24Hour)
        picker.setTime(hour, minute, second)
    }
}