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
package com.moshy.pickersdemo

import android.app.Application
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal class MainViewModel(
    private val app: Application,
    initialTimestamp: Long,
    is24HourView: Boolean = DateFormat.is24HourFormat(app)
) : ViewModel() {

    class Factory(private val app: Application, private val initialTimestamp: Long
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(app, initialTimestamp) as T
        }
    }

    private val _currentDT = MutableLiveData<DateTimeTuple>()
    val currentDT: LiveData<DateTimeTuple>
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

    fun setDateFromPickerResult(y: Int, m: Int, d: Int) {
        _currentDT.value?.run {
            val c = this.clone() as Calendar // force copy
            c[Calendar.YEAR] = y
            c[Calendar.MONTH] = m
            c[Calendar.DAY_OF_MONTH] = d
            _currentDT.value = c
        } ?: check(false)
    }

    fun setTimeFromPickerResult(h: Int, m: Int, s: Int) {
        _currentDT.value?.run {
            val c = this.clone() as Calendar // force copy
            c[Calendar.HOUR_OF_DAY] = h
            c[Calendar.MINUTE] = m
            c[Calendar.SECOND] = s
            _currentDT.value = c
        } ?: check(false)
    }

}
