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

package com.gyugle.mitjul.feature.library.presentation.booklist

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isNull
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.presentation.toUiText
import com.gyugle.mitjul.feature.library.domain.model.Book
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import com.gyugle.mitjul.feature.library.domain.repository.FakeBookRepository
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BookListViewModelTest {

    @BeforeEach
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    private val offline = Result.Error(DataError.Network.NO_INTERNET)

    private fun book(id: String, status: ReadingStatus, title: String = "책", author: String = "저자") = Book(
        id = id, title = title, author = author, status = status, totalPages = 300, currentPage = 0, rating = null,
        review = null, coverImageId = null, createdAt = Instant.fromEpochMilliseconds(0), finishedAt = null,
    )

    @Test
    fun refresh_failsWithEmptyCopy_showsErrorState() {
        val viewModel = BookListViewModel(FakeBookRepository(refreshResult = offline))

        assertThat(viewModel.state.value.error).isEqualTo(DataError.Network.NO_INTERNET.toUiText())
    }

    @Test
    fun refresh_failsWithBooksInCopy_emitsSnackbarInstead() = runTest {
        val viewModel = BookListViewModel(FakeBookRepository(listOf(book("b1", ReadingStatus.READING)), offline))

        val expected = BookListEvent.ShowSnackbar(DataError.Network.NO_INTERNET.toUiText())
        assertThat(viewModel.events.first()).isEqualTo(expected)
        assertThat(viewModel.state.value.error).isNull()
    }

    @Test
    fun onStatusSelect_done_showsOnlyDoneBooks() {
        val books = listOf(book("b1", ReadingStatus.READING), book("b2", ReadingStatus.DONE))
        val viewModel = BookListViewModel(FakeBookRepository(books))

        viewModel.onAction(BookListAction.OnStatusSelect(ReadingStatus.DONE))

        assertThat(viewModel.state.value.books.map { it.id }).containsExactly("b2")
    }

    @Test
    fun onQueryChange_keepsBooksWhoseTitleOrAuthorMatches() {
        val books = listOf(
            book("b1", ReadingStatus.READING, title = "Cosmos", author = "Carl Sagan"),
            book("b2", ReadingStatus.DONE, title = "데미안", author = "헤르만 헤세"),
        )
        val viewModel = BookListViewModel(FakeBookRepository(books))

        viewModel.onAction(BookListAction.OnQueryChange(" cosmos "))
        assertThat(viewModel.state.value.books.map { it.id }).containsExactly("b1")

        viewModel.onAction(BookListAction.OnQueryChange("헤세"))
        assertThat(viewModel.state.value.books.map { it.id }).containsExactly("b2")
    }
}
