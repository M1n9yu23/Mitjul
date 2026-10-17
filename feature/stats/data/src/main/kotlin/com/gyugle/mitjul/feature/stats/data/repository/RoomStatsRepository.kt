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

package com.gyugle.mitjul.feature.stats.data.repository

import com.gyugle.mitjul.core.database.dao.BookDao
import com.gyugle.mitjul.core.database.dao.QuoteDao
import com.gyugle.mitjul.feature.stats.domain.model.FinishedBook
import com.gyugle.mitjul.feature.stats.domain.model.YearStats
import com.gyugle.mitjul.feature.stats.domain.repository.StatsRepository
import com.gyugle.mitjul.feature.stats.domain.yearStatsOf
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

class RoomStatsRepository(
    private val bookDao: BookDao,
    private val quoteDao: QuoteDao,
) : StatsRepository {

    override fun observeYearStats(): Flow<YearStats> {
        val timeZone = TimeZone.currentSystemDefault()
        val year = Clock.System.now().toLocalDateTime(timeZone).year
        return combine(bookDao.observeFinished(), quoteDao.observeCreatedTimes()) { books, createdTimes ->
            yearStatsOf(
                year = year,
                timeZone = timeZone,
                finishedBooks = books.mapNotNull { book ->
                    book.finishedAt?.let { FinishedBook(Instant.fromEpochMilliseconds(it), book.totalPages) }
                },
                recordedAt = createdTimes.map { Instant.fromEpochMilliseconds(it) },
            )
        }
    }
}
