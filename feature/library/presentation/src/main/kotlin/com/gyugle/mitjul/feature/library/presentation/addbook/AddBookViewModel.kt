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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.domain.onFailure
import com.gyugle.mitjul.core.domain.onSuccess
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.core.presentation.permissions.PermissionState
import com.gyugle.mitjul.core.presentation.toUiText
import com.gyugle.mitjul.feature.library.domain.BookValidator
import com.gyugle.mitjul.feature.library.domain.model.NewBook
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import com.gyugle.mitjul.feature.library.domain.repository.BookRepository
import com.gyugle.mitjul.feature.library.presentation.R
import com.gyugle.mitjul.feature.library.presentation.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val KEY_TITLE = "title"
private const val KEY_AUTHOR = "author"
private const val KEY_STATUS = "status"
private const val KEY_TOTAL_PAGES = "totalPages"
private const val KEY_COVER_PATH = "coverPath"
private const val KEY_COVER_IMAGE_ID = "coverImageId"

class AddBookViewModel(
    private val savedStateHandle: SavedStateHandle,
    private val bookRepository: BookRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(
        AddBookState(
            title = savedStateHandle[KEY_TITLE] ?: "",
            author = savedStateHandle[KEY_AUTHOR] ?: "",
            status = savedStateHandle[KEY_STATUS] ?: ReadingStatus.WANT,
            totalPages = savedStateHandle[KEY_TOTAL_PAGES] ?: "",
            coverPath = savedStateHandle[KEY_COVER_PATH],
        ),
    )
    val state: StateFlow<AddBookState> = _state.asStateFlow()

    private val _events = Channel<AddBookEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: AddBookAction) {
        when (action) {
            is AddBookAction.OnTitleChange -> {
                savedStateHandle[KEY_TITLE] = action.title
                _state.update { it.copy(title = action.title, titleError = null) }
            }
            is AddBookAction.OnAuthorChange -> {
                savedStateHandle[KEY_AUTHOR] = action.author
                _state.update { it.copy(author = action.author, authorError = null) }
            }
            is AddBookAction.OnStatusSelect -> {
                savedStateHandle[KEY_STATUS] = action.status
                _state.update { it.copy(status = action.status) }
            }
            is AddBookAction.OnTotalPagesChange -> {
                savedStateHandle[KEY_TOTAL_PAGES] = action.totalPages
                _state.update { it.copy(totalPages = action.totalPages, totalPagesError = null) }
            }
            is AddBookAction.OnCoverPick -> prepareCover(action.uri)
            AddBookAction.OnCoverRemove -> changeCover(null)
            is AddBookAction.OnCameraPermissionResult -> viewModelScope.launch {
                when (action.state) {
                    PermissionState.GRANTED -> _events.send(AddBookEvent.TakePhoto)
                    PermissionState.PERMANENTLY_DENIED -> _events.send(AddBookEvent.CameraBlocked)
                    else -> _events.send(AddBookEvent.ShowSnackbar(UiText.StringResource(R.string.add_book_error_camera_denied)))
                }
            }
            AddBookAction.OnCameraUnavailable -> viewModelScope.launch {
                _events.send(AddBookEvent.ShowSnackbar(UiText.StringResource(R.string.add_book_error_no_camera)))
            }
            AddBookAction.OnSaveClick -> save()
        }
    }

    private fun prepareCover(uri: String) {
        _state.update { it.copy(isPreparingCover = true) }
        viewModelScope.launch {
            val result = bookRepository.prepareCover(uri)
            _state.update { it.copy(isPreparingCover = false) }
            when (result) {
                is Result.Success -> changeCover(result.data)
                is Result.Error -> _events.send(AddBookEvent.ShowSnackbar(result.error.toCoverMessage()))
            }
        }
    }

    private fun changeCover(path: String?) {
        savedStateHandle[KEY_COVER_PATH] = path
        savedStateHandle.remove<String>(KEY_COVER_IMAGE_ID)
        _state.update { it.copy(coverPath = path) }
    }

    private fun save() {
        val current = _state.value
        if (current.isSaving || current.isPreparingCover) return

        var titleError: UiText? = null
        var authorError: UiText? = null
        var totalPagesError: UiText? = null
        BookValidator.validateTitle(current.title).onFailure { titleError = it.toUiText() }
        BookValidator.validateAuthor(current.author).onFailure { authorError = it.toUiText() }
        BookValidator.validateTotalPages(current.totalPages).onFailure { totalPagesError = it.toUiText() }
        if (titleError != null || authorError != null || totalPagesError != null) {
            _state.update {
                it.copy(titleError = titleError, authorError = authorError, totalPagesError = totalPagesError)
            }
            return
        }

        _state.update { it.copy(isSaving = true) }
        val book = NewBook(
            title = current.title.trim(),
            author = current.author.trim(),
            status = current.status,
            totalPages = current.totalPages.trim().toInt(),
        )
        viewModelScope.launch {
            val coverImageId = when (val cover = uploadCover(current.coverPath)) {
                is Result.Success -> cover.data
                is Result.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(AddBookEvent.ShowSnackbar(cover.error.toCoverMessage()))
                    return@launch
                }
            }
            when (val result = bookRepository.addBook(book.copy(coverImageId = coverImageId))) {
                is Result.Success -> _events.send(AddBookEvent.BookAdded)
                is Result.Error -> {
                    _state.update { it.copy(isSaving = false) }
                    _events.send(AddBookEvent.ShowSnackbar(result.error.toAddBookMessage()))
                }
            }
        }
    }

    private suspend fun uploadCover(path: String?): Result<String?, DataError> {
        val uploaded: String? = savedStateHandle[KEY_COVER_IMAGE_ID]
        if (path == null || uploaded != null) return Result.Success(uploaded)
        val result = bookRepository.uploadCover(path) { progress ->
            _state.update { it.copy(uploadProgress = progress) }
        }
        _state.update { it.copy(uploadProgress = null) }
        return result.onSuccess { savedStateHandle[KEY_COVER_IMAGE_ID] = it }
    }

    private fun DataError.Network.toAddBookMessage(): UiText =
        if (this == DataError.Network.REQUEST_TIMEOUT) {
            UiText.StringResource(R.string.add_book_error_unconfirmed)
        } else {
            toUiText()
        }

    private fun DataError.toCoverMessage(): UiText =
        if (this is DataError.Local) {
            UiText.StringResource(R.string.add_book_error_cover_unreadable)
        } else {
            toUiText()
        }
}
