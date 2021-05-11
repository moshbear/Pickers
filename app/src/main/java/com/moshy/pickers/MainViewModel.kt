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

import android.app.Application
import android.text.format.DateFormat
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.util.Calendar

internal class MainViewModel(
    app: Application,
    initialTimestamp: Long,
    is24HourView: Boolean
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

    private val _currentDT = MutableLiveData<Calendar>()
    val currentDT: LiveData<Calendar>
        get() = _currentDT
    @Suppress("DEPRECATION")
    private val locale = app.resources.configuration.locale
    val dtString =
        dtStringView(currentDT, DateFormatConfiguration(locale, is24HourView),
            app, R.string.datetime_message)

    enum class OpState {
        INACTIVE, ACTIVE,
        ;
    }
    private val state = MutableLiveData<OpState>()
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

    init {
        _currentDT.value = timestampToCalendar(initialTimestamp)
        state.value = OpState.INACTIVE
    }

    fun onClickButton() {
        when (state.value) {
            OpState.INACTIVE -> state.value = OpState.ACTIVE
            OpState.ACTIVE -> state.value = OpState.INACTIVE
            else -> require(false) { "Unexpected state transition" }
        }
    }


    fun setDateFromPickerResult(y: Int, m: Int, d: Int) = updateDtDate(_currentDT, y, m, d)

    fun setTimeFromPickerResult(h: Int, m: Int, s: Int) = updateDtTime(_currentDT, h, m, s)

}
