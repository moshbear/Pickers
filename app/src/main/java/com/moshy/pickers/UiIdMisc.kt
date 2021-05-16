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

import android.content.res.Resources
import android.view.View
import androidx.annotation.IdRes

private val systemResources by lazy { requireNotNull(Resources.getSystem()) }


internal fun getAndroidResourceByString(type: String, name: String): Int? =
    (systemResources.getIdentifier(name, type, "android"))
        .run { if (this <= 0) null else this }

internal fun requireAndroidResource(type: String, name: String): Int =
    requireNotNull(getAndroidResourceByString(type, name))
    { "Could not find resource id for android:$type/$name" }

internal fun <T: View> View.requireViewById1(@IdRes id: Int): T =
    requireNotNull(findViewById(id))

