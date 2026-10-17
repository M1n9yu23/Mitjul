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

package com.gyugle.mitjul.feature.library.presentation.bookdetail

import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isNotNull
import assertk.assertions.isNull
import com.gyugle.mitjul.feature.library.domain.model.Book
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import com.gyugle.mitjul.feature.library.domain.repository.FakeBookRepository
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookDetailViewModelTest {

    @BeforeEach
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun onSaveClick_pageOverTotalThenLastPage_sendsOnlyTheFixedBook() {
        val momo = Book(
            id = "b1", title = "Momo", author = "Michael Ende", status = ReadingStatus.READING, totalPages = 304,
            currentPage = 120, rating = 4, review = null, coverImageId = null,
            createdAt = Instant.fromEpochMilliseconds(0), finishedAt = null,
        )
        val repository = FakeBookRepository(books = listOf(momo))
        val viewModel = BookDetailViewModel("b1", SavedStateHandle(), repository)
        assertThat(viewModel.state.value.currentPage).isEqualTo("120")

        viewModel.onAction(BookDetailAction.OnCurrentPageChange("400"))
        viewModel.onAction(BookDetailAction.OnSaveClick)
        assertThat(viewModel.state.value.currentPageError).isNotNull()
        assertThat(repository.updatedBooks).isEmpty()

        viewModel.onAction(BookDetailAction.OnCurrentPageChange("304"))
        viewModel.onAction(BookDetailAction.OnRatingSelect(4))
        viewModel.onAction(BookDetailAction.OnSaveClick)
        val saved = repository.updatedBooks.single()
        assertThat(saved.status).isEqualTo(ReadingStatus.DONE)
        assertThat(saved.rating).isNull()
    }
}
