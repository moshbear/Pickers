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

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.moshy.pickersdemo.databinding.DatetimeWidgetBinding
import com.moshy.pickersdemo.databinding.MainActivityBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: MainActivityBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nowSecs = System.currentTimeMillis() / 1000
        binding = DataBindingUtil.setContentView<MainActivityBinding>(this, R.layout.main_activity)
        viewModel = ViewModelProvider(this, MainViewModel.Factory(nowSecs)).get(MainViewModel::class.java)

        binding.vm = viewModel
        binding.lifecycleOwner = this
        binding.trigger.setOnClickListener { viewModel.onClickButton() }

        initPickers(binding.picker)
    }

    private fun initPickers(pickerWidget: DatetimeWidgetBinding)
    {
        pickerWidget.datetimeLayout.visibility = View.GONE
        pickerWidget.pickDate.setOnClickListener {
            DatePickerFragment(this)
            { _, y, m, d -> viewModel.setDateFromPickerResult(y, m, d) }
            .show(supportFragmentManager, "datePicker/0")
        }
        pickerWidget.pickTime.setOnClickListener {
            TimePickerFragment(this)
            { _, h, m, s -> viewModel.setTimeFromPickerResult(h, m, s) }
            .show(supportFragmentManager, "timePicker/0")
        }
    }
}
