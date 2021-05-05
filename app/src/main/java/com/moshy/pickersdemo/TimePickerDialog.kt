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
package com.moshy.pickersdemo

import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.DialogInterface.OnClickListener
import android.content.res.Resources
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.TimePicker
import android.widget.TimePicker.OnTimeChangedListener
import androidx.annotation.IdRes
import androidx.annotation.StyleRes
import androidx.core.view.get
import java.util.Locale

class TimePickerDialog(
    _context: Context, @StyleRes themeResId: Int,
    private val callback: OnTimeSetListener?,
    initialHourOfDay: Int, initialMinute: Int, initialSecond: Int, is24HourView: Boolean
): AlertDialog(_context, themeResId), OnClickListener, OnTimeChangedListener {

    fun interface OnTimeSetListener {
        fun onTimeSet(view: TimePicker, hour: Int, minute: Int, second: Int)
    }

    constructor(
        context: Context, callback: OnTimeSetListener?,
        hourOfDay: Int, minute: Int, second: Int, is24HourView: Boolean
    )
    : this(context, 0, callback, hourOfDay, minute, second, is24HourView)

    private companion object {
        const val idRefSeparator = "divider"
        const val idRefMinute = "minute"
        const val idRefLayout = "timePickerLayout"
        const val idRefAmPm = "amPm"

        const val bundleHour = "hour"
        const val bundleMinute = "minute"
        const val bundleSecond = "second"
        const val bundleIs24Hour = "is24hour"
        const val missingSecond = -1

    }

    private val timePicker: TimePicker
    private val secondsPicker: NumberPicker

    init {
        setTitle(R.string.time_picker_dialog_title)
        setButton(BUTTON_POSITIVE, context.getString(R.string.time_picker_done), this)

        timePicker =
            LayoutInflater.from(context).inflate(R.layout.time_picker_dialog, null)
            .also { setView(it) }
            .findViewById(R.id.timePicker)

        val hmDivider =
            getAndroidIdByString(idRefSeparator)
            ?.run { timePicker.findViewById<TextView>(this) }

        val minutesPicker =
            requireAndroidIdentifier(idRefMinute)
            .run { timePicker.requireNotNullViewById<NumberPicker>(this, idRefMinute) }
        // [inner] layout: h,m(,s) widgets
        // outer layout: inner layout + am/pm widget
        val layout =
            requireAndroidIdentifier(idRefLayout)
            .run { timePicker.requireNotNullViewById<LinearLayout>(this, idRefLayout) }
            .run { requireNotNull(getChildAt(0)) { "could not access inner layout" } }
            .run { this as LinearLayout }

        requireAndroidIdentifier(idRefAmPm)
        .run { timePicker.requireNotNullViewById<View>(this, idRefAmPm) }
        .run {
            check(parent == layout.parent)
            { "The parent of the amPm selector is not the outer layout. " +
              "Must remove before adding seconds picker"
            }
        }

        if (hmDivider != null && layout[1] === hmDivider) {
            with(TextView(context)) {
                layout.addView(this)
                this.layoutParams = hmDivider.layoutParams
                text = getMinutesSecondsSeparator(is24HourView)
            }
        }

        /* TODO: Manipulation is incomplete because we do not update minute on over/under-flow.
         *       Nor is IME-based accessibility implemented.
         *       As this will modify a substantial amount of the picker inputs, a custom
         *       TimePicker itself will have to be created due to an absurd amount of effective
         *       code duplication otherwise.
         */
        secondsPicker = createSecondsPicker(context, initialSecond)
        with (secondsPicker) {
            layout.addView(this)
            this.layoutParams = minutesPicker.layoutParams
        }

        timePicker.setIs24HourView(is24HourView)
        timePicker.hour = initialHourOfDay
        timePicker.minute = initialMinute
        timePicker.setOnTimeChangedListener(this)

    }

    private var hour by timePicker::hour
    private var minute by timePicker::minute

    private val secondsView
        get() = secondsPicker

    private var second by secondsView::value

    private var is24Hour
        get() = timePicker.is24HourView
        set(value) { timePicker.setIs24HourView(value) }

    private fun tryCallback() {
        if (callback != null) {
            timePicker.clearFocus()
            callback.onTimeSet(timePicker, hour, minute, second)
        }
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        require(which == BUTTON_POSITIVE) {
            "$which is unexpected because the listener was only declared for BUTTON_POSITIVE"
        }
        tryCallback()
    }

    override fun onTimeChanged(view: TimePicker?, hourOfDay: Int, minute: Int) {
        /* no-op */
    }

    /**
     * @param hourOfDay The current hour within the day.
     * @param minuteOfHour The current initialMinute within the hour.
     */
    fun updateTime(hourOfDay: Int, minuteOfHour: Int) {
        hour = hourOfDay
        minute = minuteOfHour
    }
    /**
     * Sets the current time.
     *
     * @param hourOfDay The current hour within the day.
     * @param minuteOfHour The current initialMinute within the hour.
     * @param secondOfMinute The current initialSecond within the minute.
     */
    fun updateTime(hourOfDay: Int, minuteOfHour: Int, secondOfMinute: Int) {
        hour = hourOfDay
        minute = minuteOfHour
        second = secondOfMinute
    }

    override fun onStop() {
        tryCallback()
        super.onStop()
    }

    override fun onSaveInstanceState(): Bundle {
        val state = super.onSaveInstanceState()
        state.putInt(bundleHour, hour)
        state.putInt(bundleMinute, minute)
        state.putInt(bundleSecond, second)
        state.putBoolean(bundleIs24Hour, is24Hour)
        return state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val hour = savedInstanceState.getInt(bundleHour)
        val minute = savedInstanceState.getInt(bundleMinute)
        val second = savedInstanceState.getInt(bundleSecond, missingSecond)
        is24Hour = savedInstanceState.getBoolean(bundleIs24Hour)
        this.hour = hour
        this.minute = minute
        if (second != missingSecond)
            this.second = second
    }
}

private fun getMinutesSecondsSeparator(is24hView: Boolean): String {

    val bestDateTimePattern = DateFormat.getBestDateTimePattern(Locale.getDefault(),
        when (is24hView) {
            true -> "Hms"
            false -> "hms"
        })
    val minuteIndex = bestDateTimePattern.lastIndexOf('m')
    return if (minuteIndex == -1) {
        ":"
    } else {
        val secondIndex = bestDateTimePattern.indexOf('s')
        if (secondIndex == -1) {
            bestDateTimePattern[minuteIndex + 1].toString()
        } else {
            bestDateTimePattern.substring(minuteIndex + 1, secondIndex)
        }
    }
}

private fun createSecondsPicker(context: Context, initialValue: Int) =
    NumberPicker(context).apply {
        id = R.id.second
        setFormatter { String.format("%02d", it) }
        minValue = 0
        maxValue = 59
        setOnLongPressUpdateInterval(100)
        value = initialValue
    }

private val systemResources by lazy { requireNotNull(Resources.getSystem()) }

private fun getAndroidIdByString(name: String): Int? =
    (systemResources.getIdentifier(name, "id", "android"))
    .run { if (this <= 0) null else this }

private fun requireAndroidIdentifier(name: String): Int =
    requireNotNull(getAndroidIdByString(name))
    { "Could not find resource id for android:id/$name" }

private fun <T : View> TimePicker.requireNotNullViewById(@IdRes id: Int, exHint: String = "") =
    requireNotNull(findViewById<T>(id))
    { "Could not find view for resource" + (if (exHint.isNotBlank()) " $exHint" else "") }
