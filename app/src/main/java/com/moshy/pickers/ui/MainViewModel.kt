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
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import java.time.LocalDateTime
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

    // Ideally, this is in the Application and `ldtFormatter` is updated on 24h preference change.
    // Meaning that `locale` is an implementation detail that's only here for simplicity.
    @Suppress("DEPRECATION")
    private val locale = app.resources.configuration.locale
    private val ldtFormatter = MutableLiveData<DateTimeFormatter>()
            .also { it.value = DateTimeFormatter(locale, initialIs24HourView) }

    var is24Hour: Boolean = initialIs24HourView
        set(value) {
            field = value
            ldtFormatter.value = DateTimeFormatter(locale, value)
        }

    private val _currentDT = MutableLiveData<LocalDateTime>()
            .also { it.value = timestampToLocalDT(initialTimestamp) }
    val currentDT: LiveData<LocalDateTime>
        get() = _currentDT
    val dtString = dtStringView24(currentDT, ldtFormatter, app, R.string.datetime_message)

    fun setDateTimeFromPickerResult(dt: LocalDateTime) {
        _currentDT.value = dt
    }

}
