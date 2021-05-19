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
package com.moshy.pickers.ui

import android.app.Application
import android.text.format.DateFormat
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.time.LocalDateTime
import java.util.Calendar
import com.moshy.pickers.R

// TODO: Would `java.time.Instant.ofEpochSecond()` be better for timestamp?
internal class MainViewModel(
    app: Application,
    initialTimestamp: Long,
    initialIs24HourView: Boolean
) : ViewModel() {

    class Factory(
        private val app: Application, private val initialTimestamp: Long,
        private val is24Hour: Boolean = DateFormat.is24HourFormat(app)
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(app, initialTimestamp, is24Hour) as T
        }
    }

    private val _is24Hour =
        MutableLiveData<Boolean>().also { it.value = initialIs24HourView }
    val is24Hour: LiveData<Boolean>
        get() = _is24Hour

    private val _currentDT =
        MutableLiveData<LocalDateTime>().also { it.value = timestampToLocalDT(initialTimestamp) }
    val currentDT: LiveData<LocalDateTime>
        get() = _currentDT
    @Suppress("DEPRECATION")
    private val locale = app.resources.configuration.locale
    val dtString = dtStringView24(currentDT, is24Hour, locale, app, R.string.datetime_message)

    enum class OpState {
        INACTIVE, ACTIVE,
        ;
    }
    private val state = MutableLiveData<OpState>().also { it.value = OpState.INACTIVE }
    val pickerVisibility = Transformations.map(state) {
            when (it) {
                OpState.ACTIVE -> View.VISIBLE
                else -> View.GONE
            }
        }
    val detailsVisibility = Transformations.map(state) {
            when (it) {
                OpState.INACTIVE -> View.VISIBLE
                else -> View.GONE
            }
        }


    fun onClickButton() {
        when (state.value) {
            OpState.INACTIVE -> state.value = OpState.ACTIVE
            OpState.ACTIVE -> state.value = OpState.INACTIVE
            else -> require(false) { "Unexpected state transition" }
        }
    }

    fun onToggle24Hour() {
        when (is24Hour.value) {
            true -> _is24Hour.value = false
            false -> _is24Hour.value = true
            null -> require(false) { "Unexpected null" }
        }
    }


    fun setDateFromPickerResult(y: Int, m: Int, d: Int) = updateDtDate(_currentDT, y, m, d)

    fun setTimeFromPickerResult(h: Int, m: Int, s: Int) = updateDtTime(_currentDT, h, m, s)

    fun setDateTimeFromPickerResult(dt: LocalDateTime) {
        _currentDT.value = dt
    }

}
