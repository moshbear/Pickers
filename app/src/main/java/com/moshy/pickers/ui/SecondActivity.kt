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

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.moshy.pickers.databinding.SecondActivityBinding
import com.moshy.pickers.R
import com.moshy.pickers.DateTimePickerFragment
import java.time.LocalDateTime
import java.util.Calendar

class SecondActivity : AppCompatActivity() {

    private lateinit var binding: SecondActivityBinding
    private lateinit var viewModel: MainViewModel

    private lateinit var currentDT: LocalDateTime

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nowSecs = System.currentTimeMillis() / 1000
        binding = DataBindingUtil.setContentView(this, R.layout.second_activity)
        viewModel =
            ViewModelProvider(this, MainViewModel.Factory(application, nowSecs))
                .get(MainViewModel::class.java)

        binding.vm = viewModel
        binding.lifecycleOwner = this
        binding.trigger.setOnClickListener {
            DateTimePickerFragment.newInstance(currentDT)
            { _, c -> viewModel.setDateTimeFromPickerResult(c) }
            .show(supportFragmentManager, "dt/0")
        }

        fun updateCurrentDT(dt: LocalDateTime) {
            currentDT = dt
        }

        updateCurrentDT(checkNotNull(viewModel.currentDT.value))

        viewModel.currentDT.observe(this) {
            updateCurrentDT(it)
        }
    }
}
