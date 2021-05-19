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

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.DatePicker
import android.widget.FrameLayout
import androidx.annotation.AttrRes
import java.time.LocalDateTime

class DateTimePicker(
    context: Context, attrs: AttributeSet?, @AttrRes defStyle: Int
): FrameLayout(context, attrs, defStyle),
    DatePicker.OnDateChangedListener, TimePicker.OnTimeChangedListener
{
    private var onDateTimeChangedListener: OnDateTimeChangedListener? = null

    // if true, updateDate / updateTime was called, so the callback is not invoked.
    private var internalCall: Boolean = false
    private fun doInternalCall(block: () -> Unit) {
        internalCall = true
        block()
        internalCall = false
    }
    private fun ifNotInternalCall(block: () -> Unit) {
        if (!internalCall)
            block()
    }
    private fun requireInternalCall(block: () -> Unit) {
        check(internalCall)
        block()
    }

    fun interface OnDateTimeChangedListener {
        fun onDateTimeChanged(
            view: DateTimePicker?,
            dt: LocalDateTime
        )
    }
    /**
     * Set the callback that indicates the date and/or time have been adjusted by the user.
     *
     * @param onDateTimeChangedListener the callback
     */
    fun setOnDateTimeChangedListener(onDateTimeChangedListener: OnDateTimeChangedListener?) {
        this.onDateTimeChangedListener = onDateTimeChangedListener
    }

    // Internal LocalDateTime updaters
    private fun updateInternalDate(year: Int, month: Int, day: Int) =
        dateTime.withYear(year).withMonth(month).withDayOfMonth(day).apply {
            dateTime = this
        }

    private fun updateInternalTime(hourOfDay: Int, minute: Int, second: Int) =
        dateTime.withHour(hourOfDay).withMinute(minute).withSecond(second).apply {
            dateTime = this
        }
    // Picker state updaters
    private fun updatePickerDateToCurrent() =
        requireInternalCall {
            // DatePicker month is 0 indexed but DateTime month is 1-indexed
            datePicker.init(dateTime.year, dateTime.monthValue - 1, dateTime.dayOfMonth, this)
        }

    private fun updatePickerTimeToCurrent() =
        requireInternalCall {
            with(timePicker) {
                hour = dateTime.hour
                minute = dateTime.minute
                second = dateTime.second
            }
        }


    // Picker callbacks
    // Caution: DatePicker month is 0-indexed. java.util.Calendar[MONTH] semantics?
    override fun onDateChanged(view: DatePicker?, year: Int, month: Int, day: Int) =
        ifNotInternalCall {
            updateInternalDate(year, month + 1, day)
            invokeCallback()
        }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int, second: Int) =
        ifNotInternalCall {
            updateInternalTime(hourOfDay, minute, second)
            invokeCallback()
        }

    private fun invokeCallback() =
        onDateTimeChangedListener?.onDateTimeChanged(this, dateTime)


    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?)
            : this(context, attrs, android.R.attr.datePickerStyle)

    private var isEnabled: Boolean = true
    override fun setEnabled(enabled: Boolean) {
        if (isEnabled == enabled) {
            return
        }
        super.setEnabled(enabled)
        listOf(timePicker, datePicker)
            .forEach {
                it.isEnabled = enabled
            }

        isEnabled = enabled
    }
    override fun isEnabled(): Boolean = isEnabled

    private val datePicker: DatePicker
    private val timePicker: TimePicker

    var dateTime: LocalDateTime = LocalDateTime.now()
        private set

    fun setDate(year: Int, month: Int, dayOfMonth: Int) =
        /* DatePicker.init is safe to call without the doInternalCall guard
         * because it won't invoke the onDateChanged proxy callback but keep the
         * pattern in case this doesn't hold (eg for setTime()).
         */
        doInternalCall {
            updateInternalDate(year, month, dayOfMonth)
            updatePickerDateToCurrent()
        }

    fun setTime(hour: Int, minute: Int, second: Int) =
        /* The public API of TimePicker does not provide a way to set the
         * time without invoking the onTimeChanged proxy callback.
         * The guard is thus necessary here.
         * We also need the guard so that the hour rollover handler doesn't
         * handle a case of set to 0:xx:xx as transition from ??:xx:xx (or
         * likewise for 23:xx:xx).
         */
        doInternalCall {
            updateInternalTime(hour, minute, second)
            updatePickerTimeToCurrent()
        }

    init {
        with(context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater) {
            inflate(R.layout.date_time_picker, this@DateTimePicker, true)
        }
        datePicker = requireViewById1(R.id.datePicker)
        timePicker = requireViewById1(R.id.timePicker)

        with (datePicker) {
            try {
                @Suppress("DEPRECATION")
                calendarViewShown = false
            } catch (_: UnsupportedOperationException) {
                check(false) { "The style is calendar not spinner" }
            }
        }

        with(timePicker) {
            setOnTimeChangedListener(this@DateTimePicker)
            setDayChangeCallback {
                if (it == 0)
                    return@setDayChangeCallback
                ifNotInternalCall {
                    doInternalCall {
                        dateTime = dateTime.plusDays(it.toLong())
                        updatePickerDateToCurrent()
                    }
                    invokeCallback()
                }
            }
        }

        doInternalCall {
            updatePickerDateToCurrent()
            updatePickerTimeToCurrent()
        }
    }

    var is24HourView by timePicker::is24HourView
    val year
        get() = datePicker.year
    val month
        get() = datePicker.month
    val dayOfMonth
        get() = datePicker.dayOfMonth
    val hour
        get() = timePicker.hour
    val minute
        get() = timePicker.minute
    val second
        get() = timePicker.second

}