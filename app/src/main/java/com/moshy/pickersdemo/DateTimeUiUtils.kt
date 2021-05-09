package com.moshy.pickersdemo

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import java.util.Calendar

// This block of code is re-used extensively in other ViewModel's, but because it is UI-bound,
// it does not belong in DateTimeUtils.kt
internal fun dtStringView(dt: LiveData<Calendar>, df: DateFormatConfiguration,
                          context: Context, @StringRes resId: Int) =
    Transformations.map(dt) {
        context.getString(resId, timestampToDateTimeString(df, it.timeInMillis / 1000))
    }