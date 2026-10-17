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

package com.gyugle.mitjul.feature.library.presentation.addbook

import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.library.domain.error.BookInputError
import com.gyugle.mitjul.feature.library.domain.model.NewBook
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import com.gyugle.mitjul.feature.library.domain.repository.FakeBookRepository
import com.gyugle.mitjul.feature.library.presentation.R
import com.gyugle.mitjul.feature.library.presentation.toUiText
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
class AddBookViewModelTest {

    @BeforeEach
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    private fun AddBookViewModel.save(title: String, totalPages: String) {
        onAction(AddBookAction.OnTitleChange(title))
        onAction(AddBookAction.OnAuthorChange("칼 세이건"))
        onAction(AddBookAction.OnTotalPagesChange(totalPages))
        onAction(AddBookAction.OnSaveClick)
    }

    @Test
    fun onSaveClick_blankTitleAndZeroPages_showsFieldErrorsWithoutRequest() {
        val repository = FakeBookRepository()
        val viewModel = AddBookViewModel(SavedStateHandle(), repository)

        viewModel.save(title = "   ", totalPages = "0")

        val state = viewModel.state.value
        assertThat(state.titleError).isEqualTo(BookInputError.Title.BLANK.toUiText())
        assertThat(state.totalPagesError).isEqualTo(BookInputError.TotalPages.OUT_OF_RANGE.toUiText())
        assertThat(repository.addedBooks).isEmpty()
    }

    @Test
    fun onSaveClick_timeout_keepsTheFormAndSaysItMayBeSaved() = runTest {
        val timeout = Result.Error(DataError.Network.REQUEST_TIMEOUT)
        val viewModel = AddBookViewModel(SavedStateHandle(), FakeBookRepository(addBookResult = timeout))

        viewModel.save(title = "코스모스", totalPages = "720")

        val expected = AddBookEvent.ShowSnackbar(UiText.StringResource(R.string.add_book_error_unconfirmed))
        assertThat(viewModel.events.first()).isEqualTo(expected)
        assertThat(viewModel.state.value.isSaving).isFalse()
        assertThat(viewModel.state.value.title).isEqualTo("코스모스")
    }

    @Test
    fun onSaveClick_acceptedThenClickedAgain_sendsOneTrimmedBook() = runTest {
        val repository = FakeBookRepository()
        val viewModel = AddBookViewModel(SavedStateHandle(), repository)

        viewModel.save(title = " 코스모스 ", totalPages = "720")
        viewModel.onAction(AddBookAction.OnSaveClick)

        assertThat(viewModel.events.first()).isEqualTo(AddBookEvent.BookAdded)
        assertThat(repository.addedBooks).containsExactly(NewBook("코스모스", "칼 세이건", ReadingStatus.WANT, 720))
    }

    @Test
    fun onSaveClick_bookRejectedAfterCoverUploaded_retryReusesTheCover() {
        val repository = FakeBookRepository(addBookResult = Result.Error(DataError.Network.SERVER_ERROR))
        val viewModel = AddBookViewModel(SavedStateHandle(), repository)

        viewModel.onAction(AddBookAction.OnCoverPick("content://photo"))
        viewModel.save(title = "코스모스", totalPages = "720")
        viewModel.onAction(AddBookAction.OnSaveClick)

        assertThat(repository.uploadedCovers).containsExactly("content://photo.jpg")
        assertThat(repository.addedBooks.map { it.coverImageId }).containsExactly("cover-1", "cover-1")
    }
}
