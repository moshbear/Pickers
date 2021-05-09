package com.moshy.pickersdemo

import android.content.Context
import androidx.annotation.StringRes
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import java.util.Calendar

// This block of code is re-used extensively in other ViewModel's, but because it is UI-bound,
// it does not belong in DateTimeUtils.kt
@JvmName("dtStringViewDTT")
internal fun dtStringView(dt: LiveData<DateTimeTuple>, df: DateFormatConfiguration,
                          context: Context, @StringRes resId: Int) =
    Transformations.map(dt) {
        it?.run {
            context.getString(resId, timestampToDateTimeString(df, pack()))
        }
    }