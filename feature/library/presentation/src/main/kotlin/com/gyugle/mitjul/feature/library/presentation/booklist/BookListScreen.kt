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

package com.gyugle.mitjul.feature.library.presentation.booklist

import androidx.compose.animation.core.tween
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyugle.mitjul.core.designsystem.component.ChoiceChip
import com.gyugle.mitjul.core.designsystem.component.CoverImage
import com.gyugle.mitjul.core.designsystem.component.CoverImageDefaults
import com.gyugle.mitjul.core.designsystem.component.EmptyState
import com.gyugle.mitjul.core.designsystem.component.ErrorState
import com.gyugle.mitjul.core.designsystem.component.LoadingState
import com.gyugle.mitjul.core.designsystem.component.PressableSurface
import com.gyugle.mitjul.core.designsystem.component.SecondaryActionButton
import com.gyugle.mitjul.core.designsystem.theme.AppTheme
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.navigation.sharedNavElement
import com.gyugle.mitjul.core.presentation.ObserveAsEvents
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.core.presentation.toUiText
import com.gyugle.mitjul.feature.library.domain.model.BookSort
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import com.gyugle.mitjul.feature.library.presentation.R
import com.gyugle.mitjul.feature.library.presentation.toUiText
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun BookListRoot(
    onAddBookClick: () -> Unit,
    onStatsClick: () -> Unit,
    onBookClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: BookListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is BookListEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        BookListScreen(
            state = state,
            onAction = { viewModel.onAction(it) },
            onAddBookClick = onAddBookClick,
            onStatsClick = onStatsClick,
            onBookClick = onBookClick,
            onSettingsClick = onSettingsClick,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun BookListScreen(
    state: BookListState,
    onAction: (BookListAction) -> Unit,
    onAddBookClick: () -> Unit,
    onStatsClick: () -> Unit,
    onBookClick: (String) -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(start = AppTheme.spacing.screenMargin),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.book_list_title),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.headlineMedium,
            )
            SecondaryActionButton(text = stringResource(R.string.book_list_add), onClick = onAddBookClick)
            SecondaryActionButton(text = stringResource(R.string.book_list_stats), onClick = onStatsClick)
            SecondaryActionButton(text = stringResource(R.string.book_list_settings), onClick = onSettingsClick)
        }
        OutlinedTextField(
            value = state.query,
            onValueChange = { onAction(BookListAction.OnQueryChange(it)) },
            modifier = Modifier.fillMaxWidth().padding(horizontal = AppTheme.spacing.screenMargin),
            label = { Text(stringResource(R.string.book_list_search)) },
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            singleLine = true,
        )
        FilterBar(
            status = state.status,
            sort = state.sort,
            onStatusSelect = { onAction(BookListAction.OnStatusSelect(it)) },
            onSortSelect = { onAction(BookListAction.OnSortSelect(it)) },
        )
        when {
            state.books.isNotEmpty() -> PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = { onAction(BookListAction.OnRefresh) },
            ) {
                BookList(books = state.books, onBookClick = onBookClick)
            }
            state.error != null -> ErrorState(
                title = stringResource(R.string.book_list_error_title),
                description = state.error.asString(),
                retryText = stringResource(R.string.book_list_refresh),
                onRetry = { onAction(BookListAction.OnRefresh) },
            )
            !state.isCopyLoaded || state.isRefreshing -> LoadingState(label = stringResource(R.string.book_list_loading))
            state.query.isNotBlank() -> EmptyState(
                title = stringResource(R.string.book_list_no_match_title),
                description = stringResource(R.string.book_list_no_match_description),
                actionText = stringResource(R.string.book_list_clear_search),
                onAction = { onAction(BookListAction.OnQueryChange("")) },
            )
            else -> EmptyState(
                title = stringResource(R.string.book_list_empty_title),
                description = stringResource(R.string.book_list_empty_description),
                actionText = stringResource(R.string.book_list_add),
                onAction = onAddBookClick,
            )
        }
    }
}

@Composable
private fun FilterBar(
    status: ReadingStatus?,
    sort: BookSort,
    onStatusSelect: (ReadingStatus?) -> Unit,
    onSortSelect: (BookSort) -> Unit,
) {
    val spacing = AppTheme.spacing
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = spacing.screenMargin),
        horizontalArrangement = Arrangement.spacedBy(spacing.itemGap),
    ) {
        ChoiceChip(stringResource(R.string.book_list_filter_all), selected = status == null, onClick = { onStatusSelect(null) })
        ReadingStatus.entries.forEach { entry ->
            ChoiceChip(entry.toUiText().asString(), selected = status == entry, onClick = { onStatusSelect(entry) })
        }
    }
    Row(
        modifier = Modifier.padding(horizontal = spacing.screenMargin),
        horizontalArrangement = Arrangement.spacedBy(spacing.itemGap),
    ) {
        ChoiceChip(stringResource(R.string.book_list_sort_recent), sort == BookSort.RECENT, { onSortSelect(BookSort.RECENT) })
        ChoiceChip(stringResource(R.string.book_list_sort_title), sort == BookSort.TITLE, { onSortSelect(BookSort.TITLE) })
    }
}

@Composable
private fun BookList(books: List<BookUi>, onBookClick: (String) -> Unit) {
    val motion = AppTheme.motion
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(items = books, key = { it.id }) { book ->
            PressableSurface(
                onClick = { onBookClick(book.id) },
                clickLabel = stringResource(R.string.book_list_open),
                modifier = Modifier.animateItem(fadeInSpec = tween(motion.fadeIn), fadeOutSpec = tween(motion.fadeIn)),
            ) {
                ListItem(
                    headlineContent = { Text(book.title) },
                    overlineContent = { Text(book.status.asString()) },
                    supportingContent = { Text(book.author) },
                    leadingContent = {
                        CoverImage(
                            model = book.coverUrl,
                            contentDescription = null,
                            modifier = Modifier.width(CoverImageDefaults.ThumbnailWidth).sharedNavElement(book.coverUrl),
                        )
                    },
                    trailingContent = { Text(book.pages.asString()) },
                )
            }
        }
    }
}

@Preview(name = "책이 있을 때")
@Composable
private fun BookListScreenBooksPreview() {
    val reading = UiText.StringResource(R.string.reading_status_reading)
    val books = listOf(
        BookUi("b1", "코스모스", "칼 세이건", reading, UiText.StringResource(R.string.book_list_pages, listOf(120, 720)), null),
        BookUi("b2", "사피엔스", "유발 하라리", reading, UiText.StringResource(R.string.book_list_pages, listOf(40, 443)), null),
    )
    AppTheme {
        Surface {
            BookListScreen(
                state = BookListState(books = books, isCopyLoaded = true),
                onAction = {},
                onAddBookClick = {},
                onStatsClick = {},
                onBookClick = {},
                onSettingsClick = {},
            )
        }
    }
}

@Preview(name = "불러오지 못했을 때, 어두운 테마")
@Composable
private fun BookListScreenErrorDarkPreview() {
    AppTheme(darkTheme = true) {
        Surface {
            BookListScreen(
                state = BookListState(isCopyLoaded = true, error = DataError.Network.NO_INTERNET.toUiText()),
                onAction = {},
                onAddBookClick = {},
                onStatsClick = {},
                onBookClick = {},
                onSettingsClick = {},
            )
        }
    }
}
