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

package com.gyugle.mitjul.feature.quote.presentation.quotecapture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyugle.mitjul.core.camera.CameraError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.ml.TextRecognitionEngine
import com.gyugle.mitjul.core.ml.TextRecognitionError
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.core.presentation.permissions.PermissionState
import com.gyugle.mitjul.feature.quote.presentation.R
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuoteCaptureViewModel(private val engine: TextRecognitionEngine) : ViewModel() {

    private val _state = MutableStateFlow(QuoteCaptureState())
    val state: StateFlow<QuoteCaptureState> = _state.asStateFlow()

    private val _events = Channel<QuoteCaptureEvent>()
    val events = _events.receiveAsFlow()

    private var readJob: Job? = null

    fun onAction(action: QuoteCaptureAction) {
        when (action) {
            is QuoteCaptureAction.OnPermissionChecked -> _state.update { current ->
                val keepsSettingsRoute = current.permission == PermissionState.PERMANENTLY_DENIED &&
                    action.state == PermissionState.NOT_DETERMINED
                current.copy(
                    permission = if (keepsSettingsRoute) current.permission else action.state,
                    isRequestingPermission = false,
                )
            }
            QuoteCaptureAction.OnRequestPermissionClick -> requestPermission()
            is QuoteCaptureAction.OnPermissionResult -> _state.update {
                it.copy(permission = action.state, isRequestingPermission = false)
            }
            QuoteCaptureAction.OnOpenSettingsClick -> Unit
            is QuoteCaptureAction.OnCameraBound -> onCameraBound(action.result)
            QuoteCaptureAction.OnShutterClick -> takePicture()
            is QuoteCaptureAction.OnPictureTaken -> read(action.path)
            QuoteCaptureAction.OnPictureFailed -> {
                _state.update { it.copy(isReading = false) }
                showSnackbar(CameraError.CAPTURE_FAILED.toMessage())
            }
            is QuoteCaptureAction.OnBlockClick -> _state.update { current ->
                val index = action.index
                current.copy(selected = if (index in current.selected) current.selected - index else current.selected + index)
            }
            QuoteCaptureAction.OnRetakeClick -> _state.update { it.copy(blocks = emptyList(), selected = emptySet()) }
            QuoteCaptureAction.OnInsertClick -> insert()
            QuoteCaptureAction.OnCloseClick -> reset()
        }
    }

    private fun requestPermission() {
        if (_state.value.isRequestingPermission) return
        _state.update { it.copy(isRequestingPermission = true) }
        viewModelScope.launch { _events.send(QuoteCaptureEvent.RequestPermission) }
    }

    private fun onCameraBound(result: EmptyResult<CameraError>) {
        val error = (result as? Result.Error)?.error
        _state.update { current ->
            current.copy(
                cameraError = error?.toMessage(),
                isReading = current.isReading && readJob?.isActive == true,
            )
        }
    }

    private fun takePicture() {
        if (_state.value.isReading) return
        _state.update { it.copy(isReading = true) }
        viewModelScope.launch { _events.send(QuoteCaptureEvent.TakePicture) }
    }

    private fun read(path: String) {
        readJob = viewModelScope.launch {
            when (val result = engine.recognize(path)) {
                is Result.Success -> {
                    _state.update { it.copy(isReading = false, blocks = result.data, selected = emptySet()) }
                    if (result.data.isEmpty()) showSnackbar(UiText.StringResource(R.string.quote_capture_no_text))
                }
                is Result.Error -> {
                    _state.update { it.copy(isReading = false) }
                    showSnackbar(result.error.toMessage())
                }
            }
        }
    }

    private fun insert() {
        val current = _state.value
        if (current.selected.isEmpty()) return
        val text = current.blocks.filterIndexed { index, _ -> index in current.selected }.joinToString(" ")
        reset()
        viewModelScope.launch { _events.send(QuoteCaptureEvent.TextPicked(text)) }
    }

    private fun reset() {
        readJob?.cancel()
        _state.update { QuoteCaptureState(permission = it.permission) }
    }

    private fun showSnackbar(message: UiText) {
        viewModelScope.launch { _events.send(QuoteCaptureEvent.ShowSnackbar(message)) }
    }

    private fun CameraError.toMessage(): UiText = UiText.StringResource(
        when (this) {
            CameraError.NO_CAMERA -> R.string.quote_capture_error_no_camera
            CameraError.UNAVAILABLE -> R.string.quote_capture_error_unavailable
            CameraError.CAPTURE_FAILED -> R.string.quote_capture_error_capture
        },
    )

    private fun TextRecognitionError.toMessage(): UiText = UiText.StringResource(
        when (this) {
            TextRecognitionError.IMAGE_UNREADABLE -> R.string.quote_capture_error_unreadable
            TextRecognitionError.RECOGNITION_FAILED -> R.string.quote_capture_error_failed
        },
    )
}
