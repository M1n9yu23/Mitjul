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

package com.gyugle.mitjul.feature.library.domain

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.gyugle.mitjul.feature.library.domain.model.Book
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import kotlin.time.Instant
import org.junit.jupiter.api.Test

class BookWithProgressTest {

    private val firstFinish = Instant.parse("2026-09-01T09:00:00Z")
    private val now = Instant.parse("2026-09-14T09:00:00Z")

    private fun book(status: ReadingStatus, currentPage: Int, finishedAt: Instant? = null) = Book(
        id = "b1", title = "책", author = "저자", status = status, totalPages = 300, currentPage = currentPage,
        rating = null, review = null, coverImageId = null, createdAt = firstFinish, finishedAt = finishedAt,
    )

    @Test
    fun withProgress_lastPage_isDoneAndKeepsTheFirstFinish() {
        val done = book(ReadingStatus.READING, 120).withProgress(300, rating = 5, review = null, now = now)
        assertThat(done.status).isEqualTo(ReadingStatus.DONE)
        assertThat(done.finishedAt).isEqualTo(now)

        val again = book(ReadingStatus.DONE, 300, firstFinish).withProgress(300, rating = 4, review = null, now = now)
        assertThat(again.finishedAt).isEqualTo(firstFinish)
    }

    @Test
    fun withProgress_belowLastPage_reopensDoneAndStartsOnlyAfterPageOne() {
        val reopened = book(ReadingStatus.DONE, 300, firstFinish).withProgress(0, rating = null, review = null, now = now)
        assertThat(reopened.status).isEqualTo(ReadingStatus.READING)
        assertThat(reopened.finishedAt).isNull()

        val want = book(ReadingStatus.WANT, 0)
        assertThat(want.withProgress(0, rating = null, review = null, now = now).status).isEqualTo(ReadingStatus.WANT)
        assertThat(want.withProgress(1, rating = null, review = null, now = now).status).isEqualTo(ReadingStatus.READING)
    }
}
