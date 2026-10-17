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

package com.gyugle.mitjul.feature.quote.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gyugle.mitjul.core.navigation.Navigator
import com.gyugle.mitjul.feature.quote.navigation.QuoteCardRoute
import com.gyugle.mitjul.feature.quote.navigation.QuoteListRoute
import com.gyugle.mitjul.feature.quote.presentation.quotecard.QuoteCardRoot
import com.gyugle.mitjul.feature.quote.presentation.quotelist.QuoteListRoot

fun EntryProviderScope<NavKey>.quoteEntries(navigator: Navigator) {
    entry<QuoteListRoute> { key ->
        QuoteListRoot(
            bookId = key.bookId,
            bookTitle = key.bookTitle,
            onQuoteClick = { navigator.goTo(QuoteCardRoute(quoteId = it, bookTitle = key.bookTitle)) },
        )
    }
    entry<QuoteCardRoute> { key ->
        QuoteCardRoot(quoteId = key.quoteId, bookTitle = key.bookTitle, onBack = { navigator.goBack() })
    }
}
