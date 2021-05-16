/*
 * Copyright (C) 2007 The Android Open Source Project
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

/* This is a copy paste of android.app.DatePickerDialog but using our id/datePicker
 * instead of system android:id/datePicker
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
import android.widget.DatePicker
import android.widget.DatePicker.OnDateChangedListener
import androidx.annotation.StyleRes

/**
 * A simple dialog containing an [android.widget.DatePicker].
 *
 *
 * See the [Pickers]({@docRoot}guide/topics/ui/controls/pickers.html)
 * guide.
 */
class DatePickerDialog(
    context: Context,
    @StyleRes theme: Int,
    private val callBack: OnDateSetListener?,
    year: Int,
    monthOfYear: Int,
    dayOfMonth: Int
) : AlertDialog(context, theme),
    OnClickListener, OnDateChangedListener {
    /**
     * Gets the [DatePicker] contained in this dialog.
     *
     * @return The calendar view.
     */
    private val datePicker: DatePicker =
        LayoutInflater.from(context).inflate(R.layout.date_picker_dialog, null)
        .also { setView(it) }
        .findViewById<DatePicker>(R.id.datePicker)
        .also {
            try {
                @Suppress("DEPRECATION")
                it.calendarViewShown = false
            } catch (_: UnsupportedOperationException) {
                check(false) { "The style is calendar not spinner" }
            }
            it.init(year, monthOfYear, dayOfMonth, this)
        }

    /**
     * @param context The context the dialog is to run in.
     * @param callBack How the parent is notified that the date is set.
     * @param year The initial year of the dialog.
     * @param monthOfYear The initial month of the dialog.
     * @param dayOfMonth The initial day of the dialog.
     */
    constructor(
        context: Context,
        callBack: OnDateSetListener?,
        year: Int,
        monthOfYear: Int,
        dayOfMonth: Int
    ) : this(context, 0, callBack, year, monthOfYear, dayOfMonth)

    companion object {
        private const val bkYear = "year"
        private const val bkMonth = "month"
        private const val bkDay = "day"
    }

    fun interface OnDateSetListener: Parcelable {
        fun onDateSet(view: DatePicker, year: Int, month: Int, day: Int)
        override fun describeContents(): Int = 0
        override fun writeToParcel(dest: Parcel, flags: Int) { /* no-op */ }
    }

    init {
        setTitle(R.string.date_picker_dialog_title)
        setButton(BUTTON_POSITIVE, context.getText(R.string.date_time_done), this)
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        tryNotifyDateSet()
    }

    override fun onDateChanged(
        view: DatePicker, year: Int,
        month: Int, day: Int
    ) {
        datePicker.init(year, month, day, this)
    }

    /**
     * Sets the current date.
     *
     * @param year The date year.
     * @param monthOfYear The date month.
     * @param dayOfMonth The date day of month.
     */
    fun updateDate(year: Int, monthOfYear: Int, dayOfMonth: Int) {
        datePicker.updateDate(year, monthOfYear, dayOfMonth)
    }

    private fun tryNotifyDateSet() {
        if (callBack != null) {
            datePicker.clearFocus()
            callBack.onDateSet(
                datePicker, datePicker.year,
                datePicker.month, datePicker.dayOfMonth
            )
        }
    }

    override fun onStop() {
        tryNotifyDateSet()
        super.onStop()
    }

    override fun onSaveInstanceState(): Bundle {
        val state: Bundle = super.onSaveInstanceState()
        state.putInt(bkYear, datePicker.year)
        state.putInt(bkMonth, datePicker.month)
        state.putInt(bkDay, datePicker.dayOfMonth)
        return state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val year = savedInstanceState.getInt(bkYear)
        val month = savedInstanceState.getInt(bkMonth)
        val day = savedInstanceState.getInt(bkDay)
        datePicker.init(year, month, day, this)
    }

}
