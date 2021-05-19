package com.moshy.pickers.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.moshy.pickers.databinding.ThirdActivityBinding
import com.moshy.pickers.R

class ThirdActivity : AppCompatActivity() {

    private lateinit var binding: ThirdActivityBinding
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val nowSecs = System.currentTimeMillis() / 1000
        binding = DataBindingUtil.setContentView(this, R.layout.third_activity)
        viewModel =
            ViewModelProvider(this, MainViewModel.Factory(application, nowSecs))
                .get(MainViewModel::class.java)
        binding.vm = viewModel
        binding.lifecycleOwner = this
        viewModel.is24Hour.observe(this) {
            it?.apply {
                binding.picker.is24HourView = this
            }
        }
        binding.trigger.setOnClickListener {
            viewModel.onToggle24Hour()
        }
        binding.picker.setOnDateTimeChangedListener {
                _, c -> viewModel.setDateTimeFromPickerResult(c)
        }
    }
}