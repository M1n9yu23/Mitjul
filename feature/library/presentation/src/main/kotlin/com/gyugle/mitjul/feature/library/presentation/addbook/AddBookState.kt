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

import androidx.compose.runtime.Immutable
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus

@Immutable
data class AddBookState(
    val title: String = "",
    val author: String = "",
    val status: ReadingStatus = ReadingStatus.WANT,
    val totalPages: String = "",
    val coverPath: String? = null,
    val titleError: UiText? = null,
    val authorError: UiText? = null,
    val totalPagesError: UiText? = null,
    val isSaving: Boolean = false,
    val isPreparingCover: Boolean = false,
    val uploadProgress: Float? = null,
)
