package com.moshy.pickersdemo

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations

// This block of code is re-used extensively in other ViewModel's, but because it is UI-bound,
// it does not belong in DateTimeUtils.kt
internal fun dtStringView(dt: LiveData<DateTimeTuple>, context: Context, @StringRes resId: Int) =
    Transformations.map(dt) {
        it?.run {
            context.getString(resId, timestampToDateTimeString(pack()))
        }
    }