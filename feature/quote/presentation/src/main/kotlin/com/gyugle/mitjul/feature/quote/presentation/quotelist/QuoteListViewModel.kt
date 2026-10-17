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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.domain.onFailure
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.core.presentation.toUiText
import com.gyugle.mitjul.feature.quote.domain.QuoteValidator
import com.gyugle.mitjul.feature.quote.domain.model.NewQuote
import com.gyugle.mitjul.feature.quote.domain.model.Quote
import com.gyugle.mitjul.feature.quote.domain.repository.QuoteRepository
import com.gyugle.mitjul.feature.quote.presentation.R
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val KEY_TEXT = "text"
private const val KEY_PAGE = "page"
private const val KEY_MEMO = "memo"
private const val KEY_CAPTURE_OPEN = "captureOpen"

class QuoteListViewModel(
    private val bookId: String,
    private val savedStateHandle: SavedStateHandle,
    private val quoteRepository: QuoteRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        QuoteListState(
            text = savedStateHandle[KEY_TEXT] ?: "",
            page = savedStateHandle[KEY_PAGE] ?: "",
            memo = savedStateHandle[KEY_MEMO] ?: "",
            isCaptureOpen = savedStateHandle[KEY_CAPTURE_OPEN] ?: false,
        ),
    )
    val state: StateFlow<QuoteListState> = _state.asStateFlow()

    private val _events = Channel<QuoteListEvent>()
    val events = _events.receiveAsFlow()

    init {
        quoteRepository.observeQuotes(bookId)
            .onEach { quotes ->
                _state.update { current -> current.copy(quotes = quotes.map { it.toQuoteUi() }, isCopyLoaded = true) }
            }
            .catch { _state.update { it.copy(isCopyLoaded = true, error = DataError.Local.UNKNOWN.toUiText()) } }
            .launchIn(viewModelScope)
        refresh()
    }

    fun onAction(action: QuoteListAction) {
        when (action) {
            is QuoteListAction.OnTextChange -> {
                val text = action.text.take(QuoteValidator.TEXT_MAX_LENGTH)
                savedStateHandle[KEY_TEXT] = text
                _state.update { it.copy(text = text, textError = null) }
            }
            is QuoteListAction.OnPageChange -> {
                savedStateHandle[KEY_PAGE] = action.page
                _state.update { it.copy(page = action.page, pageError = null) }
            }
            is QuoteListAction.OnMemoChange -> {
                val memo = action.memo.take(QuoteValidator.MEMO_MAX_LENGTH)
                savedStateHandle[KEY_MEMO] = memo
                _state.update { it.copy(memo = memo) }
            }
            QuoteListAction.OnSaveClick -> save()
            QuoteListAction.OnRefresh -> refresh()
            QuoteListAction.OnCaptureClick -> setCaptureOpen(true)
            QuoteListAction.OnCaptureClose -> setCaptureOpen(false)
            is QuoteListAction.OnTextCaptured -> {
                val text = listOf(_state.value.text.trim(), action.text).filter { it.isNotEmpty() }.joinToString(" ")
                onAction(QuoteListAction.OnTextChange(text))
                setCaptureOpen(false)
            }
        }
    }

    private fun setCaptureOpen(isOpen: Boolean) {
        savedStateHandle[KEY_CAPTURE_OPEN] = isOpen
        _state.update { it.copy(isCaptureOpen = isOpen) }
    }

    private fun refresh() {
        if (_state.value.isRefreshing) return
        _state.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            val result = quoteRepository.refresh()
            val loaded = _state.first { it.isCopyLoaded }
            _state.update { it.copy(isRefreshing = false) }
            if (result !is Result.Error) return@launch
            val message = result.error.toUiText()
            if (loaded.quotes.isEmpty()) {
                _state.update { it.copy(error = message) }
            } else {
                _events.send(QuoteListEvent.ShowSnackbar(message))
            }
        }
    }

    private fun save() {
        val current = _state.value
        if (current.isSaving) return
        var textError: UiText? = null
        var pageError: UiText? = null
        QuoteValidator.validateText(current.text).onFailure { textError = it.toUiText() }
        QuoteValidator.validatePage(current.page).onFailure { pageError = it.toUiText() }
        if (textError != null || pageError != null) {
            _state.update { it.copy(textError = textError, pageError = pageError) }
            return
        }

        _state.update { it.copy(isSaving = true) }
        val quote = NewQuote(
            bookId = bookId,
            text = current.text.trim(),
            page = current.page.trim().toIntOrNull(),
            memo = current.memo.trim().ifEmpty { null },
        )
        viewModelScope.launch {
            when (val result = quoteRepository.addQuote(quote)) {
                is Result.Success -> {
                    listOf(KEY_TEXT, KEY_PAGE, KEY_MEMO).forEach { savedStateHandle.remove<String>(it) }
                    _state.update { it.copy(text = "", page = "", memo = "", isSaving = false) }
                    _events.send(QuoteListEvent.ShowSnackbar(UiText.StringResource(R.string.quote_list_saved)))
                }
                is Result.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(QuoteListEvent.ShowSnackbar(result.error.toUiText()))
                }
            }
        }
    }

    private fun Quote.toQuoteUi() = QuoteUi(
        id = id,
        text = text,
        page = page?.let { UiText.StringResource(R.string.quote_list_page, listOf(it)) },
        memo = memo,
        isPending = isPending,
    )
}
