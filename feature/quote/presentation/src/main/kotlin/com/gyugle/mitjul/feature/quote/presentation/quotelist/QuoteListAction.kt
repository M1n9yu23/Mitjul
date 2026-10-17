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

sealed interface QuoteListAction {
    data class OnTextChange(val text: String) : QuoteListAction
    data class OnPageChange(val page: String) : QuoteListAction
    data class OnMemoChange(val memo: String) : QuoteListAction
    data object OnSaveClick : QuoteListAction
    data object OnRefresh : QuoteListAction
    data object OnCaptureClick : QuoteListAction
    data object OnCaptureClose : QuoteListAction
    data class OnTextCaptured(val text: String) : QuoteListAction
}
