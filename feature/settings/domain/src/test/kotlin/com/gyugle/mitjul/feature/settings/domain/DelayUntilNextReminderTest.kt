/*
 * Copyright 2026 MinGyu
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

package com.gyugle.mitjul.feature.settings.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Test

class DelayUntilNextReminderTest {

    private val seoul = TimeZone.of("Asia/Seoul")
    private val nine = LocalTime(21, 0)

    @Test
    fun delayUntilNextReminder_beforeTheTimeToday_waitsForToday() {
        val evening = Instant.parse("2026-09-16T11:30:00Z")

        assertThat(delayUntilNextReminder(evening, nine, seoul)).isEqualTo(30.minutes)
    }

    @Test
    fun delayUntilNextReminder_atOrAfterTheTime_waitsForTomorrow() {
        val atNine = Instant.parse("2026-09-16T12:00:00Z")
        val nearMidnight = Instant.parse("2026-09-16T14:50:00Z")

        assertThat(delayUntilNextReminder(atNine, nine, seoul)).isEqualTo(24.hours)
        assertThat(delayUntilNextReminder(nearMidnight, LocalTime(0, 30), seoul)).isEqualTo(40.minutes)
    }
}
