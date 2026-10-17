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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.core.presentation.toUiText
import com.gyugle.mitjul.feature.library.domain.model.Book
import com.gyugle.mitjul.feature.library.domain.repository.BookRepository
import com.gyugle.mitjul.feature.library.presentation.R
import com.gyugle.mitjul.feature.library.presentation.toUiText
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BookListViewModel(
    private val bookRepository: BookRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(BookListState())
    val state: StateFlow<BookListState> = _state.asStateFlow()

    private val _events = Channel<BookListEvent>()
    val events = _events.receiveAsFlow()

    init {
        observeCopy()
        refresh()
    }

    fun onAction(action: BookListAction) {
        when (action) {
            is BookListAction.OnStatusSelect -> _state.update { it.copy(status = action.status) }
            is BookListAction.OnSortSelect -> _state.update { it.copy(sort = action.sort) }
            is BookListAction.OnQueryChange -> _state.update { it.copy(query = action.query) }
            BookListAction.OnRefresh -> refresh()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun observeCopy() {
        _state.map { Triple(it.status, it.sort, it.query.trim()) }
            .distinctUntilChanged()
            .flatMapLatest { (status, sort, query) ->
                bookRepository.observeBooks(status, sort).map { books -> books.filter { it.matches(query) } }
            }
            .onEach { books ->
                _state.update { current -> current.copy(books = books.map { it.toBookUi() }, isCopyLoaded = true) }
            }
            .catch { _state.update { it.copy(isCopyLoaded = true, error = DataError.Local.UNKNOWN.toUiText()) } }
            .launchIn(viewModelScope)
    }

    private fun refresh() {
        if (_state.value.isRefreshing) return
        _state.update { it.copy(isRefreshing = true, error = null) }
        viewModelScope.launch {
            val result = bookRepository.refresh()
            val loaded = _state.first { it.isCopyLoaded }
            _state.update { it.copy(isRefreshing = false) }
            if (result !is Result.Error) return@launch
            val message = result.error.toUiText()
            if (loaded.books.isEmpty()) {
                _state.update { it.copy(error = message) }
            } else {
                _events.send(BookListEvent.ShowSnackbar(message))
            }
        }
    }

    private fun Book.matches(query: String): Boolean =
        query.isEmpty() || title.contains(query, ignoreCase = true) || author.contains(query, ignoreCase = true)

    private fun Book.toBookUi() = BookUi(
        id = id,
        title = title,
        author = author,
        status = status.toUiText(),
        pages = UiText.StringResource(R.string.book_list_pages, listOf(currentPage, totalPages)),
        coverUrl = coverImageId?.let { bookRepository.coverUrl(it) },
    )
}
