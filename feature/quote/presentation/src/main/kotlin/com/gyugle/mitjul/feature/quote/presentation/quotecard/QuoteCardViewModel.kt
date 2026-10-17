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

package com.gyugle.mitjul.feature.quote.presentation.quotecard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.quote.domain.model.Quote
import com.gyugle.mitjul.feature.quote.domain.repository.QuoteRepository
import com.gyugle.mitjul.feature.quote.presentation.R
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

class QuoteCardViewModel(
    quoteId: String,
    private val bookTitle: String,
    quoteRepository: QuoteRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(QuoteCardState())
    val state: StateFlow<QuoteCardState> = _state.asStateFlow()

    private val _events = Channel<QuoteCardEvent>()
    val events = _events.receiveAsFlow()

    init {
        quoteRepository.observeQuote(quoteId)
            .onEach { quote ->
                _state.update {
                    it.copy(
                        text = quote?.text.orEmpty(),
                        source = quote?.toSource(),
                        isLoaded = true,
                        isMissing = quote == null,
                    )
                }
            }
            .catch { _state.update { it.copy(isLoaded = true, isMissing = true) } }
            .launchIn(viewModelScope)
    }

    fun onAction(action: QuoteCardAction) {
        when (action) {
            QuoteCardAction.OnShareClick -> share()
            is QuoteCardAction.OnShareFinished -> {
                _state.update { it.copy(isSharing = false) }
                if (action.isShared) return
                val message = UiText.StringResource(R.string.quote_card_share_failed)
                viewModelScope.launch { _events.send(QuoteCardEvent.ShowSnackbar(message)) }
            }
        }
    }

    private fun share() {
        val current = _state.value
        if (!current.isLoaded || current.isMissing || current.isSharing) return
        _state.update { it.copy(isSharing = true) }
        viewModelScope.launch { _events.send(QuoteCardEvent.ShareCard) }
    }

    private fun Quote.toSource(): UiText =
        page?.let { UiText.StringResource(R.string.quote_card_source, listOf(bookTitle, it)) }
            ?: UiText.DynamicString(bookTitle)
}
