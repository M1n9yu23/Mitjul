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

import com.gyugle.mitjul.feature.library.domain.model.Book
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import kotlin.time.Instant

fun Book.withProgress(currentPage: Int, rating: Int?, review: String?, now: Instant): Book {
    val newStatus = when {
        currentPage == totalPages -> ReadingStatus.DONE
        currentPage > 0 || status == ReadingStatus.DONE -> ReadingStatus.READING
        else -> status
    }
    return copy(
        status = newStatus,
        currentPage = currentPage,
        rating = rating,
        review = review,
        finishedAt = if (newStatus == ReadingStatus.DONE) finishedAt ?: now else null,
    )
}
