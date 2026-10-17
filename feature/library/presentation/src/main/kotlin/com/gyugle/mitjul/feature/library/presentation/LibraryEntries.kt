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

package com.gyugle.mitjul.feature.library.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gyugle.mitjul.core.navigation.Navigator
import com.gyugle.mitjul.feature.library.navigation.AddBookRoute
import com.gyugle.mitjul.feature.library.navigation.BookDetailRoute
import com.gyugle.mitjul.feature.library.navigation.BookListRoute
import com.gyugle.mitjul.feature.library.presentation.addbook.AddBookRoot
import com.gyugle.mitjul.feature.library.presentation.bookdetail.BookDetailRoot
import com.gyugle.mitjul.feature.library.presentation.booklist.BookListRoot
import com.gyugle.mitjul.feature.quote.navigation.QuoteListRoute
import com.gyugle.mitjul.feature.settings.navigation.SettingsHomeRoute
import com.gyugle.mitjul.feature.stats.navigation.YearStatsRoute

fun EntryProviderScope<NavKey>.libraryEntries(navigator: Navigator) {
    entry<BookListRoute> {
        BookListRoot(
            onAddBookClick = { navigator.goTo(AddBookRoute) },
            onStatsClick = { navigator.goTo(YearStatsRoute) },
            onBookClick = { navigator.goTo(BookDetailRoute(bookId = it)) },
            onSettingsClick = { navigator.goTo(SettingsHomeRoute) },
        )
    }
    entry<AddBookRoute> {
        AddBookRoot(onBack = { navigator.goBack() })
    }
    entry<BookDetailRoute> { key ->
        BookDetailRoot(
            bookId = key.bookId,
            onBack = { navigator.goBack() },
            onQuotesClick = { title -> navigator.goTo(QuoteListRoute(bookId = key.bookId, bookTitle = title)) },
        )
    }
}
