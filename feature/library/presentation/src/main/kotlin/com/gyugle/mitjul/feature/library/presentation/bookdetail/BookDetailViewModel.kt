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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.domain.onFailure
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.core.presentation.toUiText
import com.gyugle.mitjul.feature.library.domain.BookValidator
import com.gyugle.mitjul.feature.library.domain.model.Book
import com.gyugle.mitjul.feature.library.domain.repository.BookRepository
import com.gyugle.mitjul.feature.library.domain.withProgress
import com.gyugle.mitjul.feature.library.presentation.R
import com.gyugle.mitjul.feature.library.presentation.toUiText
import kotlin.time.Clock
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val KEY_CURRENT_PAGE = "currentPage"
private const val KEY_RATING = "rating"
private const val KEY_REVIEW = "review"

class BookDetailViewModel(
    bookId: String,
    private val savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        BookDetailState(
            currentPage = savedStateHandle[KEY_CURRENT_PAGE] ?: "",
            rating = savedStateHandle[KEY_RATING],
            review = savedStateHandle[KEY_REVIEW] ?: "",
        ),
    )
    val state: StateFlow<BookDetailState> = _state.asStateFlow()

    private val _events = Channel<BookDetailEvent>()
    val events = _events.receiveAsFlow()

    private var book: Book? = null

    init {
        bookRepository.observeBook(bookId)
            .onEach { found -> show(found) }
            .catch { _state.update { it.copy(isLoaded = true, isMissing = true) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: BookDetailAction) {
        when (action) {
            is BookDetailAction.OnCurrentPageChange -> {
                savedStateHandle[KEY_CURRENT_PAGE] = action.currentPage
                _state.update { it.copy(currentPage = action.currentPage, currentPageError = null) }
            }
            is BookDetailAction.OnRatingSelect -> {
                val rating = action.rating.takeIf { it != _state.value.rating }
                savedStateHandle[KEY_RATING] = rating
                _state.update { it.copy(rating = rating) }
            }
            is BookDetailAction.OnReviewChange -> {
                val review = action.review.take(BookValidator.REVIEW_MAX_LENGTH)
                savedStateHandle[KEY_REVIEW] = review
                _state.update { it.copy(review = review) }
            }
            BookDetailAction.OnSaveClick -> save()
        }
    }

    private fun show(found: Book?) {
        book = found
        if (found == null) {
            _state.update { it.copy(isLoaded = true, isMissing = true) }
            return
        }
        if (!savedStateHandle.contains(KEY_CURRENT_PAGE)) fillForm(found)
        _state.update {
            it.copy(
                title = found.title,
                author = found.author,
                status = found.status.toUiText(),
                pages = UiText.StringResource(R.string.book_list_pages, listOf(found.currentPage, found.totalPages)),
                progress = found.currentPage.toFloat() / found.totalPages,
                coverUrl = found.coverImageId?.let { id -> bookRepository.coverUrl(id) },
                isLoaded = true,
                isMissing = false,
            )
        }
    }

    private fun fillForm(found: Book) {
        val currentPage = found.currentPage.toString()
        val review = found.review.orEmpty()
        savedStateHandle[KEY_CURRENT_PAGE] = currentPage
        savedStateHandle[KEY_RATING] = found.rating
        savedStateHandle[KEY_REVIEW] = review
        _state.update { it.copy(currentPage = currentPage, rating = found.rating, review = review) }
    }

    private fun save() {
        val current = _state.value
        val saved = book
        if (saved == null || current.isSaving) return
        BookValidator.validateCurrentPage(current.currentPage, saved.totalPages).onFailure { error ->
            _state.update { it.copy(currentPageError = error.toUiText()) }
            return
        }

        _state.update { it.copy(isSaving = true) }
        val updated = saved.withProgress(
            currentPage = current.currentPage.trim().toInt(),
            rating = current.rating,
            review = current.review.trim().ifEmpty { null },
            now = Clock.System.now(),
        )
        viewModelScope.launch {
            when (val result = bookRepository.updateBook(updated)) {
                is Result.Success -> _events.send(BookDetailEvent.BookSaved)
                is Result.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(BookDetailEvent.ShowSnackbar(result.error.toUiText()))
                }
            }
        }
    }
}
