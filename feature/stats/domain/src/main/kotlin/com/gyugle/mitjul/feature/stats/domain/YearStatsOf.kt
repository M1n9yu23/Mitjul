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

import com.gyugle.mitjul.feature.stats.domain.model.FinishedBook
import com.gyugle.mitjul.feature.stats.domain.model.YearStats
import kotlin.time.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

fun yearStatsOf(
    year: Int,
    timeZone: TimeZone,
    finishedBooks: List<FinishedBook>,
    recordedAt: List<Instant>,
): YearStats {
    val firstDay = LocalDate(year, 1, 1)
    val booksPerMonth = MutableList(12) { 0 }
    val recordsPerDay = MutableList(firstDay.daysUntil(LocalDate(year + 1, 1, 1))) { 0 }
    var pageCount = 0
    for (book in finishedBooks) {
        val date = book.finishedAt.toLocalDateTime(timeZone).date
        if (date.year != year) continue
        booksPerMonth[date.month.number - 1]++
        recordsPerDay[date.dayOfYear - 1]++
        pageCount += book.totalPages
    }
    for (time in recordedAt) {
        val date = time.toLocalDateTime(timeZone).date
        if (date.year == year) recordsPerDay[date.dayOfYear - 1]++
    }
    return YearStats(
        year = year,
        bookCount = booksPerMonth.sum(),
        pageCount = pageCount,
        booksPerMonth = booksPerMonth,
        recordsPerDay = recordsPerDay,
        firstWeekday = firstDay.dayOfWeek.isoDayNumber - 1,
    )
}
