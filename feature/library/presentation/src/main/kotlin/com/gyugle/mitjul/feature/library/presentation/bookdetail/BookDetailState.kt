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

import androidx.compose.runtime.Immutable
import com.gyugle.mitjul.core.presentation.UiText

@Immutable
data class BookDetailState(
    val title: String = "",
    val author: String = "",
    val status: UiText? = null,
    val pages: UiText? = null,
    val progress: Float = 0f,
    val coverUrl: String? = null,
    val currentPage: String = "",
    val rating: Int? = null,
    val review: String = "",
    val currentPageError: UiText? = null,
    val isLoaded: Boolean = false,
    val isMissing: Boolean = false,
    val isSaving: Boolean = false,
)
