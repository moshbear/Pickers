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

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId

private fun getSystemZoneOffset() =
    Instant.now().run {
        val systemZone = ZoneId.systemDefault()
        systemZone.rules.getOffset(this)
    }


internal fun timestampToLocalDT(ts: Long): LocalDateTime =
    LocalDateTime.ofEpochSecond(ts, 0, getSystemZoneOffset())

internal fun localDtToTimestamp(ldt: LocalDateTime): Long =
    ldt.toEpochSecond(getSystemZoneOffset())