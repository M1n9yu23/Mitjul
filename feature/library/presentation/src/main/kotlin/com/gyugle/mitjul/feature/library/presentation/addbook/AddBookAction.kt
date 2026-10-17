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

import com.gyugle.mitjul.core.presentation.permissions.PermissionState
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus

sealed interface AddBookAction {
    data class OnTitleChange(val title: String) : AddBookAction
    data class OnAuthorChange(val author: String) : AddBookAction
    data class OnStatusSelect(val status: ReadingStatus) : AddBookAction
    data class OnTotalPagesChange(val totalPages: String) : AddBookAction
    data class OnCoverPick(val uri: String) : AddBookAction
    data object OnCoverRemove : AddBookAction
    data class OnCameraPermissionResult(val state: PermissionState) : AddBookAction
    data object OnCameraUnavailable : AddBookAction
    data object OnSaveClick : AddBookAction
}
