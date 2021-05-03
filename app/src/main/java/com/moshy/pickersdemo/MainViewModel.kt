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

import android.view.View
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

internal class MainViewModel(
    initialTimestamp: Long
) : ViewModel() {

    class Factory(private val initialTimestamp: Long
    ): ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return MainViewModel(initialTimestamp) as T
        }
    }
    private val currentDT = MutableLiveData<DateTimeTuple>()
    val getDTString get() = Transformations.map(currentDT)
    { if (it != null) timestampToDateTimeString(it.pack()) else null }

    enum class OpState {
        INACTIVE, ACTIVE,
        ;
    }
    private val state = MutableLiveData<OpState>()
    val pickerVisibility get() = Transformations.map(state) {
        when (state.value) {
            OpState.ACTIVE -> View.VISIBLE
            else -> View.GONE
        }
    }
    val detailsVisibility get() = Transformations.map(state) {
        when (state.value) {
            OpState.INACTIVE -> View.VISIBLE
            else -> View.GONE
        }
    }

    init {
        currentDT.value = DateTimeTuple.unpack(initialTimestamp)
        state.value = OpState.INACTIVE
    }

    fun onClickButton() {
        when (state.value) {
            OpState.INACTIVE -> state.value = OpState.ACTIVE
            OpState.ACTIVE -> state.value = OpState.INACTIVE
            else -> require(false) { "Unexpected state transition" }
        }
    }

    enum class WhichOf {
        DATE, TIME,
        ;
    }

    private fun setFromPickerResult(dt: WhichOf, v0: Int, v1: Int, v2: Int)
    {
        currentDT.value?.run {
            val copyOther =
                when (dt) {
                    WhichOf.DATE -> this::copyTime
                    WhichOf.TIME -> this::copyDate
                }
            currentDT.value = copyOther(v0, v1, v2)
        } ?: check(false)
    }

    fun setDateFromPickerResult(y: Int, m: Int, d: Int) = setFromPickerResult(WhichOf.DATE, y, m, d)

    fun setTimeFromPickerResult(h: Int, m: Int, s: Int) = setFromPickerResult(WhichOf.TIME, h, m, s)

}
