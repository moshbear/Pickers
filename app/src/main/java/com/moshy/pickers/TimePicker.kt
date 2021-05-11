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

// This is adapted from android.widget.TimePicker to add seconds.
package com.moshy.pickers

import android.content.Context
import android.content.res.Resources
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.text.format.DateFormat
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.annotation.AttrRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar

/**
 * A view for selecting the time of day, in either 24 hour or AM/PM mode. The
 * hour, each minute digit, each second digit, and AM/PM (if applicable) can be
 * controlled by vertical spinners. The hour can be entered by keyboard input.
 * Entering in two digit hours can be accomplished by hitting two digits within
 * a timeout of about a second (e.g. '1' then '2' to select 12). The minutes and
 * seconds can be entered by entering single digits. Under AM/PM mode, the user
 * can hit 'a', 'A", 'p' or 'P' to pick. For a dialog using this view, see
 * [TimePickerDialog].
 */
class TimePicker(
    context: Context, attrs: AttributeSet?, @AttrRes defStyle: Int
): FrameLayout(context, attrs, defStyle) {

    // state
    private var is24HourViewF: Boolean = false
    /* Caution: API incompatibility -
    * TimePicker uses isXxx/setXxx but Kotlin generates getXxx/setXxx
    */
    var is24HourView: Boolean
        get() = is24HourViewF
        set(value) = setIs24HourView(value)

    private var isAm = false

    // ui components
    private val hourSpinner: NumberPicker
    private val minuteSpinner: NumberPicker
    private val secondSpinner: NumberPicker
    private var amPmSpinner: NumberPicker

    private val hourSpinnerInput: EditText
    private val minuteSpinnerInput: EditText
    private val secondSpinnerInput: EditText
    private val amPmSpinnerInput: EditText

    private val dividerHM: TextView
    private val dividerMS: TextView

    private var isEnabled = DEFAULT_ENABLED_STATE

    // callbacks
    private var onTimeChangedListener: OnTimeChangedListener? = null

    @Suppress("DEPRECATION")
    private val locale = context.resources.configuration.locale
    private val tempCalendar = Calendar.getInstance(locale)

    private data class HourFormatData(val format: Char, val twoDigit: Boolean)
    private val defaultHourFormatData = HourFormatData('\u0000', false)
    private var hourFormatData: HourFormatData = defaultHourFormatData

    private val bestDateTimePattern24 by lazy {
        DateFormat.getBestDateTimePattern(locale, "Hms")
    }
    private val bestDateTimePattern12 by lazy {
        DateFormat.getBestDateTimePattern(locale, "hms")
    }
    private val bestDateTimePattern
        get() =
            when (is24HourView) {
                true -> bestDateTimePattern24
                false -> bestDateTimePattern12
            }

    private var timeFormatter: SimpleDateFormat = SimpleDateFormat(bestDateTimePattern, locale)
    /**
     * The callback interface used to indicate the time has been adjusted.
     */
    fun interface OnTimeChangedListener {
        /**
         * @param view The view associated with this listener.
         * @param hourOfDay The current hour.
         * @param minute The current minute.
         * @param second The current second.
         */
        fun onTimeChanged(view: com.moshy.pickers.TimePicker?, hourOfDay: Int, minute: Int, second: Int)
    }

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?)
    : this(context, attrs, attrRefTimePickerStyle)

    override fun setEnabled(enabled: Boolean) {
        if (isEnabled == enabled) {
            return
        }
        super.setEnabled(enabled)
         listOf(secondSpinner, dividerMS, minuteSpinner, dividerHM, hourSpinner, amPmSpinner)
        .forEach {
            it.isEnabled = enabled
        }

        isEnabled = enabled
    }

    override fun isEnabled(): Boolean = isEnabled

    /**
     * Used to save / restore state of time picker
     */
    private class SavedState : BaseSavedState {
        val hour: Int
        val minute: Int
        val second: Int

        constructor(superState: Parcelable?, hour: Int, minute: Int, second: Int) : super(superState) {
            this.hour = hour
            this.minute = minute
            this.second = second
        }

        private constructor(`in`: Parcel) : super(`in`) {
            hour = `in`.readInt()
            minute = `in`.readInt()
            second = `in`.readInt()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            dest.writeInt(hour)
            dest.writeInt(minute)
            dest.writeInt(second)
        }

        companion object {
            @Suppress("unused")
            @JvmField
            val CREATOR: Creator<SavedState?> = object : Creator<SavedState?> {
                override fun createFromParcel(`in`: Parcel): SavedState {
                    return SavedState(`in`)
                }

                override fun newArray(size: Int): Array<SavedState?> {
                    return arrayOfNulls(size)
                }
            }
        }
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        return SavedState(superState, hour, minute, second)
    }

    override fun onRestoreInstanceState(state: Parcelable) {
        val ss = state as SavedState
        super.onRestoreInstanceState(ss.superState)
        hour = ss.hour
        minute = ss.minute
        second = ss.second
    }

    /**
     * Set the callback that indicates the time has been adjusted by the user.
     *
     * @param onTimeChangedListener the callback, should not be null.
     */
    fun setOnTimeChangedListener(onTimeChangedListener: OnTimeChangedListener?) {
        this.onTimeChangedListener = onTimeChangedListener
    }

    var hour: Int
        get() {
            val currentHour = hourSpinner.value
            return when {
                is24HourView -> currentHour
                isAm -> currentHour % HOURS_IN_HALF_DAY
                else -> currentHour % HOURS_IN_HALF_DAY + HOURS_IN_HALF_DAY
            }
        }
        set(currentHour) {
            setCurrentHour(currentHour, true)
        }

    private fun setCurrentHour(currentHour_: Int, notifyTimeChanged: Boolean) {
        var currentHour: Int = currentHour_
        if (this.hour == currentHour) {
            return
        }
        if (!is24HourView) {
            // convert [0,23] ordinal to wall clock display
            if (currentHour >= HOURS_IN_HALF_DAY) {
                isAm = false
                if (currentHour > HOURS_IN_HALF_DAY) {
                    currentHour -= HOURS_IN_HALF_DAY
                }
            } else {
                isAm = true
                if (currentHour == 0) {
                    currentHour = HOURS_IN_HALF_DAY
                }
            }
            updateAmPmControl()
        }
        hourSpinner.value = currentHour
        if (notifyTimeChanged) {
            onTimeChanged()
        }
    }

    /**
     * Set whether in 24 hour or AM/PM mode.
     *
     * @param is24HourView True = 24 hour mode. False = AM/PM.
     */
    private fun setIs24HourView(is24HourView: Boolean) {
        if (this.is24HourView == is24HourView) {
            return
        }
        // cache the current hour since spinner range changes and BEFORE changing mIs24HourView!!
        val currentHour = hour
        // Order is important here.
        is24HourViewF = is24HourView
        hourFormatData = hourFormatData()
        updateHourControl()
        // set value after spinner range is updated - be aware that because mIs24HourView has
        // changed then getHour() is not equal to the hour we cached before so
        // explicitly ask for *not* propagating any onTimeChanged()
        setCurrentHour(currentHour, false)
        updateSecondControl()
        updateAmPmControl()
        timeFormatter = SimpleDateFormat(bestDateTimePattern, locale)
    }

    var minute: Int
        get() = minuteSpinner.value
        set(currentMinute) {
            if (this.minute == currentMinute) {
                return
            }
            minuteSpinner.value = currentMinute
            onTimeChanged()
        }

    var second: Int
        get() = secondSpinner.value
        set(currentSecond) {
            if (this.second == currentSecond) {
                return
            }
            secondSpinner.value = currentSecond
            onTimeChanged()
        }

    private val isAmPmAtStart: Boolean
        get() = bestDateTimePattern12.startsWith("a")

    private fun hourFormatData(): HourFormatData {
        val lengthPattern = bestDateTimePattern.length
        // Check if the returned pattern is single or double 'H', 'h', 'K', 'k'.
        // We also save the hour format that we found.
        for (i in 0 until lengthPattern) {
            val c = bestDateTimePattern[i]
            if (c == 'H' || c == 'h' || c == 'K' || c == 'k') {
                return HourFormatData(
                    c,
                    (i + 1 < lengthPattern && c == bestDateTimePattern[i + 1])
                )
            }
        }
        return defaultHourFormatData
    }

    /**
     * The time separator is defined in the Unicode CLDR and cannot be supposed to be ":".
     *
     * See http://unicode.org/cldr/trac/browser/trunk/common/main
     *
     * We pass the correct "skeleton" depending on 12 or 24 hours view and then extract the
     * hour-minute separator as the character which is just after the hour marker in the returned
     * pattern.
     * Likewise for minute-second separator and just after the minute marker.
     */
    private fun getMinutesSecondsSeparator(): Pair<String, String> {
        val bestDateTimePattern = bestDateTimePattern
        val hourIndex = when (val lastH = bestDateTimePattern.lastIndexOf('H')) {
            -1 -> bestDateTimePattern.lastIndexOf('h')
            else -> lastH
        }
        val minuteStartIndex by lazy { bestDateTimePattern.indexOf('m', hourIndex + 1) }
        val minuteEndIndex by lazy { bestDateTimePattern.lastIndexOf('m') }
        val secondStartIndex by lazy { bestDateTimePattern.indexOf('s', minuteEndIndex + 1) }

        val hoursMinutes =
            if (hourIndex == -1)
                ":"
            else {
                if (minuteStartIndex == -1)
                    bestDateTimePattern[hourIndex + 1].toString()
                else
                    bestDateTimePattern.substring(hourIndex + 1, minuteStartIndex)
            }
        val minutesSeconds =
            if (minuteEndIndex == -1)
                ":"
            else {
                if (secondStartIndex == -1)
                    bestDateTimePattern[minuteEndIndex + 1].toString()
                else
                    bestDateTimePattern.substring(minuteEndIndex + 1, secondStartIndex)

            }
        return Pair(hoursMinutes, minutesSeconds)
    }

    override fun getBaseline(): Int = hourSpinner.baseline

    override fun dispatchPopulateAccessibilityEvent(event: AccessibilityEvent): Boolean {
        onPopulateAccessibilityEvent(event)
        return true
    }

    override fun onPopulateAccessibilityEvent(event: AccessibilityEvent) {
        super.onPopulateAccessibilityEvent(event)

        @Suppress("DEPRECATION")

        tempCalendar.set(Calendar.HOUR_OF_DAY, hour)
        tempCalendar.set(Calendar.MINUTE, minute)
        tempCalendar.set(Calendar.SECOND, second)

        val selectedDateUtterance = timeFormatter.format(tempCalendar)
        event.text.add(selectedDateUtterance)
    }

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = TimePicker::class.java.name
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = TimePicker::class.java.name
    }

    private fun updateHourControl() {
        if (is24HourView) {
            // 'k' means 1-24 hour
            if (hourFormatData.format == 'k') {
                hourSpinner.minValue = 1
                hourSpinner.maxValue = 24
            } else {
                hourSpinner.minValue = 0
                hourSpinner.maxValue = 23
            }
        } else {
            // 'K' means 0-11 hour
            if (hourFormatData.format == 'K') {
                hourSpinner.minValue = 0
                hourSpinner.maxValue = 11
            } else {
                hourSpinner.minValue = 1
                hourSpinner.maxValue = 12
            }
        }

        hourSpinner.setFormatter(if (hourFormatData.twoDigit) twoDigitFormatter else null)
    }

    private fun updateSecondControl() {
        if (is24HourView) {
            secondSpinnerInput.imeOptions = EditorInfo.IME_ACTION_DONE
        } else {
            secondSpinnerInput.imeOptions = EditorInfo.IME_ACTION_NEXT
        }
    }

    private fun updateAmPmControl() {
        if (is24HourView) {
            amPmSpinner.visibility = GONE
        } else {
            val index: Int = if (isAm) Calendar.AM else Calendar.PM
            amPmSpinner.value = index
            amPmSpinner.visibility = VISIBLE

        }
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
    }

    private fun onTimeChanged() {
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED)
        onTimeChangedListener?.onTimeChanged(this, hour, minute, second)
    }

    private fun setContentDescriptions() {
        @IdRes val idIncrement = requireAndroidResource("id", "increment")
        @IdRes val idDecrement = requireAndroidResource("id", "decrement")

        trySetContentDescription(secondSpinner, idIncrement,
            R.string.time_picker_increment_second_button)
        trySetContentDescription(secondSpinner, idDecrement,
            R.string.time_picker_decrement_second_button)
        
        trySetContentDescription(minuteSpinner, idIncrement,
            R.string.time_picker_increment_minute_button)
        trySetContentDescription(minuteSpinner, idDecrement,
            R.string.time_picker_decrement_minute_button)

        trySetContentDescription(hourSpinner, idIncrement,
            R.string.time_picker_increment_hour_button)
        trySetContentDescription(hourSpinner, idDecrement,
            R.string.time_picker_decrement_hour_button)

        trySetContentDescription(amPmSpinner, idIncrement,
            R.string.time_picker_increment_set_pm_button)
        trySetContentDescription(amPmSpinner, idDecrement,
            R.string.time_picker_decrement_set_am_button)
    }

    private fun trySetContentDescription(root: View, @IdRes viewId: Int, @StringRes contDescResId: Int) {
        root.findViewById<View>(viewId)
        ?. run {
            contentDescription = context.getString(contDescResId)
        }
    }

    private fun updateInputState() {
        // Make sure that if the user changes the value and the IME is active
        // for one of the inputs if this widget, the IME is closed. If the user
        // changed the value via the IME and there is a next input the IME will
        // be shown, otherwise the user chose another means of changing the
        // value and having the IME up makes no sense.
        val inputMethodManager: InputMethodManager? = context.getSystemService(InputMethodManager::class.java)
        if (inputMethodManager != null) {
            when {
                inputMethodManager.isActive(hourSpinnerInput) -> {
                    hourSpinnerInput.clearFocus()
                    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
                }
                inputMethodManager.isActive(minuteSpinnerInput) -> {
                    minuteSpinnerInput.clearFocus()
                    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
                }
                inputMethodManager.isActive(secondSpinnerInput) -> {
                    secondSpinnerInput.clearFocus()
                    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
                }
                inputMethodManager.isActive(amPmSpinnerInput) -> {
                    amPmSpinnerInput.clearFocus()
                    inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
                }
            }
        }
    }

    private companion object {
        const val DEFAULT_ENABLED_STATE = true
        const val HOURS_IN_HALF_DAY = 12

        val twoDigitFormatter = NumberPicker.Formatter { String.format("%02d", it) }


        /**
         * A no-op callback used in the constructor to avoid null checks later in
         * the code.
         */
        val NO_OP_CHANGE_LISTENER: OnTimeChangedListener =
            OnTimeChangedListener { _, _, _, _ -> }
    }

    init {
        with (context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater) {
            inflate(R.layout.time_picker, this@TimePicker, true)
        }

        // hour
        hourSpinner = requireViewById1(R.id.hour)
        with (hourSpinner) {
            setOnValueChangedListener { _, oldVal, newVal ->
                updateInputState()
                if (!is24HourView) {
                    if (oldVal == HOURS_IN_HALF_DAY - 1 && newVal == HOURS_IN_HALF_DAY
                        || oldVal == HOURS_IN_HALF_DAY && newVal == HOURS_IN_HALF_DAY - 1
                    ) {
                        isAm = !isAm
                        updateAmPmControl()
                    }
                }
                onTimeChanged()
            }
            hourSpinnerInput = getEditText().also {
                it.imeOptions = EditorInfo.IME_ACTION_NEXT
            }
        }

        val dividerText = getMinutesSecondsSeparator()
        dividerHM = requireViewById1(R.id.divider_hm)
        dividerHM.text = dividerText.first

        minuteSpinner = requireViewById1(R.id.minute)
        // We will have to re-use hour wrap-around handling for the seconds case of
        // XX:59:59 -> (XX+1):0:0 or vice versa so move into separate function.
        fun updateMinutesSpinnerKernel(oldVal: Int, newVal: Int) {
            val minValue = minuteSpinner.minValue
            val maxValue = minuteSpinner.maxValue
            if (oldVal == maxValue && newVal == minValue) {
                val newHour = hourSpinner.value + 1
                if (!is24HourView && newHour == HOURS_IN_HALF_DAY) {
                    isAm = !isAm
                    updateAmPmControl()
                }
                hourSpinner.value = newHour
            } else if (oldVal == minValue && newVal == maxValue) {
                val newHour = hourSpinner.value - 1
                if (!is24HourView && newHour == HOURS_IN_HALF_DAY - 1) {
                    isAm = !isAm
                    updateAmPmControl()
                }
                hourSpinner.value = newHour
            }
        }
        with (minuteSpinner) {
            minValue = 0
            maxValue = 59
            setOnLongPressUpdateInterval(100)
            setFormatter(twoDigitFormatter)
            setOnValueChangedListener { _, oldVal, newVal ->
                updateInputState()
                updateMinutesSpinnerKernel(oldVal, newVal)
                onTimeChanged()
            }

            minuteSpinnerInput = getEditText().also {
                it.imeOptions = EditorInfo.IME_ACTION_NEXT
            }
        }

        dividerMS = requireViewById1(R.id.divider_ms)
        dividerMS.text = dividerText.second

        secondSpinner = requireViewById1(R.id.second)
        with (secondSpinner) {
            minValue = 0
            maxValue = 59
            setOnLongPressUpdateInterval(100)
            setFormatter(twoDigitFormatter)
            setOnValueChangedListener { _, oldVal, newVal ->
                updateInputState()
                val minValue = secondSpinner.minValue
                val maxValue = secondSpinner.maxValue
                if (oldVal == maxValue && newVal == minValue) {
                    val newMinute = (minuteSpinner.value + 1) % 60
                    updateMinutesSpinnerKernel(minuteSpinner.value, newMinute)
                    minuteSpinner.value = newMinute
                } else if (oldVal == minValue && newVal == maxValue) {
                    val newMinute = (minuteSpinner.value + 59) % 60
                    updateMinutesSpinnerKernel(minuteSpinner.value, newMinute)
                    minuteSpinner.value = newMinute
                }
                onTimeChanged()
            }

            secondSpinnerInput = getEditText().also {
                it.imeOptions = EditorInfo.IME_ACTION_NEXT
            }
        }

        amPmSpinner = requireViewById1(R.id.amPm)
        with (amPmSpinner) {
            minValue = 0
            maxValue = 1
            displayedValues = DateFormatSymbols().amPmStrings
            setOnValueChangedListener { picker, _, _ ->
                updateInputState()
                picker.requestFocus()
                isAm = !isAm
                updateAmPmControl()
                onTimeChanged()
            }

            amPmSpinnerInput = getEditText().also {
                it.imeOptions = EditorInfo.IME_ACTION_DONE
            }
        }

        if (isAmPmAtStart) {
            // Move the am/pm view to the beginning
            val amPmParent = findViewById<View>(R.id.timePickerLayout) as ViewGroup
            amPmParent.removeView(amPmSpinner)
            amPmParent.addView(amPmSpinner, 0)
            // Swap layout margins if needed. They may be not symmetrical (Old Standard Theme for
            // example and not for Holo Theme)
            val lp = amPmSpinner.layoutParams as MarginLayoutParams
            val startMargin = lp.marginStart
            val endMargin = lp.marginEnd
            if (startMargin != endMargin) {
                lp.marginStart = endMargin
                lp.marginEnd = startMargin
            }
        }
        hourFormatData = hourFormatData()
        // update controls to initial state
        updateHourControl()
        updateSecondControl()
        updateAmPmControl()
        setOnTimeChangedListener(NO_OP_CHANGE_LISTENER)
        // set to current time
        hour = tempCalendar.get(Calendar.HOUR_OF_DAY)
        minute = tempCalendar.get(Calendar.MINUTE)
        second = tempCalendar.get(Calendar.SECOND)
        if (!isEnabled) {
            isEnabled = false
        }
        // set the content descriptions
        setContentDescriptions()
        // If not explicitly specified this view is important for accessibility.
        if (importantForAccessibility == IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
            importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_YES
        }
    }
}

private val systemResources by lazy { requireNotNull(Resources.getSystem()) }

private fun getAndroidResourceByString(type: String, name: String): Int? =
    (systemResources.getIdentifier(name, type, "android"))
        .run { if (this <= 0) null else this }

private fun requireAndroidResource(type: String, name: String): Int =
    requireNotNull(getAndroidResourceByString(type, name))
    { "Could not find resource id for android:$type/$name" }

/* @IdRes */ private val idRefNumberPickerInput by lazy {
    requireAndroidResource("id", "numberpicker_input")
}

/* @AttrRes */ private val attrRefTimePickerStyle by lazy {
    requireAndroidResource("attr", "timePickerStyle")
}

private fun <T: View> View.requireViewById1(@IdRes id: Int): T =
    requireNotNull(findViewById(id))
private fun NumberPicker.getEditText(): EditText = requireViewById1(idRefNumberPickerInput)
