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

import com.gyugle.mitjul.core.camera.CameraError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.presentation.permissions.PermissionState

sealed interface QuoteCaptureAction {
    data class OnPermissionChecked(val state: PermissionState) : QuoteCaptureAction
    data object OnRequestPermissionClick : QuoteCaptureAction
    data class OnPermissionResult(val state: PermissionState) : QuoteCaptureAction
    data object OnOpenSettingsClick : QuoteCaptureAction
    data class OnCameraBound(val result: EmptyResult<CameraError>) : QuoteCaptureAction
    data object OnShutterClick : QuoteCaptureAction
    data class OnPictureTaken(val path: String) : QuoteCaptureAction
    data object OnPictureFailed : QuoteCaptureAction
    data class OnBlockClick(val index: Int) : QuoteCaptureAction
    data object OnRetakeClick : QuoteCaptureAction
    data object OnInsertClick : QuoteCaptureAction
    data object OnCloseClick : QuoteCaptureAction
}
