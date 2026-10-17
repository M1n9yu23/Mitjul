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

package com.gyugle.mitjul.feature.quote.presentation.quotelist

import androidx.lifecycle.SavedStateHandle
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.presentation.toUiText
import com.gyugle.mitjul.feature.quote.domain.error.QuoteInputError
import com.gyugle.mitjul.feature.quote.domain.model.NewQuote
import com.gyugle.mitjul.feature.quote.domain.repository.FakeQuoteRepository
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
class QuoteListViewModelTest {

    @BeforeEach
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun onSaveClick_blankTextThenQuote_sendsOneTrimmedQuoteAndClearsTheForm() {
        val repository = FakeQuoteRepository()
        val viewModel = QuoteListViewModel("b1", SavedStateHandle(), repository)

        viewModel.onAction(QuoteListAction.OnTextChange("   "))
        viewModel.onAction(QuoteListAction.OnPageChange("0"))
        viewModel.onAction(QuoteListAction.OnSaveClick)
        assertThat(viewModel.state.value.textError).isEqualTo(QuoteInputError.Text.BLANK.toUiText())
        assertThat(viewModel.state.value.pageError).isEqualTo(QuoteInputError.Page.OUT_OF_RANGE.toUiText())
        assertThat(repository.addedQuotes).isEmpty()

        viewModel.onAction(QuoteListAction.OnTextChange(" 별의 먼지 "))
        viewModel.onAction(QuoteListAction.OnPageChange(""))
        viewModel.onAction(QuoteListAction.OnSaveClick)
        assertThat(repository.addedQuotes).containsExactly(NewQuote("b1", "별의 먼지", page = null, memo = null))
        assertThat(viewModel.state.value.text).isEmpty()
    }

    @Test
    fun onSaveClick_diskFull_keepsTheFormAndSaysWhy() = runTest {
        val repository = FakeQuoteRepository(addQuoteResult = Result.Error(DataError.Local.DISK_FULL))
        val viewModel = QuoteListViewModel("b1", SavedStateHandle(), repository)

        viewModel.onAction(QuoteListAction.OnTextChange("별의 먼지"))
        viewModel.onAction(QuoteListAction.OnSaveClick)

        val expected = QuoteListEvent.ShowSnackbar(DataError.Local.DISK_FULL.toUiText())
        assertThat(viewModel.events.first()).isEqualTo(expected)
        assertThat(viewModel.state.value.text).isEqualTo("별의 먼지")
        assertThat(viewModel.state.value.isSaving).isFalse()
    }
}
