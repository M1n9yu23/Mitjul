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

import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import assertk.assertions.isTrue
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.quote.domain.model.Quote
import com.gyugle.mitjul.feature.quote.domain.repository.FakeQuoteRepository
import com.gyugle.mitjul.feature.quote.presentation.R
import kotlin.time.Instant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class QuoteCardViewModelTest {

    @BeforeEach
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun onShareClick_twiceThenFailed_sharesOnceAndReleasesTheButton() = runTest {
        val quote = Quote("q1", "b1", "우리는 별의 먼지로 만들어졌다.", 72, null, Instant.parse("2026-09-01T09:00:00Z"), isPending = false)
        val viewModel = QuoteCardViewModel("q1", "코스모스", FakeQuoteRepository(quote = quote))
        val events = mutableListOf<QuoteCardEvent>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.toList(events) }

        viewModel.onAction(QuoteCardAction.OnShareClick)
        viewModel.onAction(QuoteCardAction.OnShareClick)
        viewModel.onAction(QuoteCardAction.OnShareFinished(isShared = false))

        assertThat(events).containsExactly(
            QuoteCardEvent.ShareCard,
            QuoteCardEvent.ShowSnackbar(UiText.StringResource(R.string.quote_card_share_failed)),
        )
        assertThat(viewModel.state.value.isSharing).isFalse()
        assertThat(viewModel.state.value.source).isEqualTo(UiText.StringResource(R.string.quote_card_source, listOf("코스모스", 72)))
    }

    @Test
    fun init_noPageOrNoQuote_showsTitleOnlyOrMissing() {
        val quote = Quote("q2", "b1", "광활한 우주에서 지구는 작은 점 하나다.", null, null, Instant.parse("2026-09-01T09:00:00Z"), isPending = false)

        val found = QuoteCardViewModel("q2", "코스모스", FakeQuoteRepository(quote = quote)).state.value
        val missing = QuoteCardViewModel("q9", "코스모스", FakeQuoteRepository(quote = quote)).state.value

        assertThat(found.source).isEqualTo(UiText.DynamicString("코스모스"))
        assertThat(missing.isMissing).isTrue()
    }
}
