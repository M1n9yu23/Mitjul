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

package com.gyugle.mitjul.feature.stats.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.gyugle.mitjul.feature.stats.domain.model.FinishedBook
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import org.junit.jupiter.api.Test

class YearStatsOfTest {

    private val seoul = TimeZone.of("Asia/Seoul")

    @Test
    fun yearStatsOf_aroundNewYearInSeoul_countsByTheLocalDate() {
        val firstMorning = Instant.parse("2025-12-31T15:30:00Z")
        val nextYearMorning = Instant.parse("2026-12-31T15:30:00Z")
        val lastNight = Instant.parse("2026-12-31T14:30:00Z")

        val stats = yearStatsOf(
            year = 2026,
            timeZone = seoul,
            finishedBooks = listOf(FinishedBook(firstMorning, 300), FinishedBook(nextYearMorning, 500)),
            recordedAt = listOf(firstMorning, lastNight),
        )

        assertThat(stats.bookCount).isEqualTo(1)
        assertThat(stats.pageCount).isEqualTo(300)
        assertThat(stats.booksPerMonth.first()).isEqualTo(1)
        assertThat(stats.recordsPerDay.first()).isEqualTo(2)
        assertThat(stats.recordsPerDay.last()).isEqualTo(1)
        assertThat(stats.firstWeekday).isEqualTo(3)
    }

    @Test
    fun yearStatsOf_leapYear_hasADay366() {
        val stats = yearStatsOf(2028, seoul, emptyList(), listOf(Instant.parse("2028-12-31T00:00:00Z")))

        assertThat(stats.recordsPerDay.size).isEqualTo(366)
        assertThat(stats.recordsPerDay.last()).isEqualTo(1)
    }
}
