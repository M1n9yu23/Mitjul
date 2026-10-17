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

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyugle.mitjul.core.designsystem.component.ChoiceChip
import com.gyugle.mitjul.core.designsystem.component.CoverImage
import com.gyugle.mitjul.core.designsystem.component.EmptyState
import com.gyugle.mitjul.core.designsystem.component.LoadingState
import com.gyugle.mitjul.core.designsystem.component.PrimaryActionButton
import com.gyugle.mitjul.core.designsystem.component.SecondaryActionButton
import com.gyugle.mitjul.core.designsystem.theme.AppTheme
import com.gyugle.mitjul.core.navigation.sharedNavElement
import com.gyugle.mitjul.core.presentation.ObserveAsEvents
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.library.domain.BookValidator
import com.gyugle.mitjul.feature.library.presentation.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun BookDetailRoot(
    bookId: String,
    onBack: () -> Unit,
    onQuotesClick: (String) -> Unit,
    viewModel: BookDetailViewModel = koinViewModel { parametersOf(bookId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            BookDetailEvent.BookSaved -> onBack()
            is BookDetailEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        BookDetailScreen(
            state = state,
            onAction = { viewModel.onAction(it) },
            onBackClick = onBack,
            onQuotesClick = onQuotesClick,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun BookDetailScreen(
    state: BookDetailState,
    onAction: (BookDetailAction) -> Unit,
    onBackClick: () -> Unit,
    onQuotesClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        !state.isLoaded -> LoadingState(label = stringResource(R.string.book_detail_loading), modifier = modifier)
        state.isMissing -> EmptyState(
            title = stringResource(R.string.book_detail_missing_title),
            description = stringResource(R.string.book_detail_missing_description),
            actionText = stringResource(R.string.book_detail_back),
            onAction = onBackClick,
            modifier = modifier,
        )
        else -> BookDetailForm(state = state, onAction = onAction, onQuotesClick = onQuotesClick, modifier = modifier)
    }
}

@Composable
private fun BookDetailForm(
    state: BookDetailState,
    onAction: (BookDetailAction) -> Unit,
    onQuotesClick: (String) -> Unit,
    modifier: Modifier,
) {
    val spacing = AppTheme.spacing
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(spacing.screenMargin),
        verticalArrangement = Arrangement.spacedBy(spacing.itemGap),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.itemGap)) {
            CoverImage(
                model = state.coverUrl,
                contentDescription = null,
                modifier = Modifier.weight(1f).sharedNavElement(state.coverUrl),
            )
            Column(modifier = Modifier.weight(2f)) {
                state.status?.let { Text(it.asString(), style = MaterialTheme.typography.labelLarge) }
                Text(state.title, style = MaterialTheme.typography.headlineSmall)
                Text(state.author, style = MaterialTheme.typography.bodyLarge)
            }
        }
        LinearProgressIndicator(progress = { state.progress }, modifier = Modifier.fillMaxWidth())
        state.pages?.let { Text(it.asString(), style = MaterialTheme.typography.bodyMedium) }
        SecondaryActionButton(
            text = stringResource(R.string.book_detail_quotes),
            onClick = { onQuotesClick(state.title) },
        )
        OutlinedTextField(
            value = state.currentPage,
            onValueChange = { onAction(BookDetailAction.OnCurrentPageChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
            label = { Text(stringResource(R.string.book_detail_current_page_label)) },
            supportingText = { state.currentPageError?.let { Text(it.asString()) } },
            isError = state.currentPageError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        RatingChoice(rating = state.rating, onRatingSelect = { onAction(BookDetailAction.OnRatingSelect(it)) })
        OutlinedTextField(
            value = state.review,
            onValueChange = { onAction(BookDetailAction.OnReviewChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
            label = { Text(stringResource(R.string.book_detail_review_label)) },
            supportingText = {
                Text(stringResource(R.string.book_detail_review_count, state.review.length, BookValidator.REVIEW_MAX_LENGTH))
            },
            minLines = 3,
        )
        PrimaryActionButton(
            text = stringResource(if (state.isSaving) R.string.book_detail_saving else R.string.book_detail_save),
            onClick = { onAction(BookDetailAction.OnSaveClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        )
    }
}

@Composable
private fun RatingChoice(rating: Int?, onRatingSelect: (Int) -> Unit) {
    Text(stringResource(R.string.book_detail_rating_label), style = MaterialTheme.typography.titleSmall)
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.itemGap),
    ) {
        (1..BookValidator.RATING_MAX).forEach { value ->
            val text = stringResource(R.string.book_detail_rating_value, value)
            ChoiceChip(text, selected = rating == value, onClick = { onRatingSelect(value) })
        }
    }
}

@Preview(name = "책이 있을 때")
@Composable
private fun BookDetailScreenPreview() {
    AppTheme {
        Surface {
            BookDetailScreen(
                state = BookDetailState(
                    title = "코스모스",
                    author = "칼 세이건",
                    status = UiText.StringResource(R.string.reading_status_reading),
                    pages = UiText.StringResource(R.string.book_list_pages, listOf(120, 720)),
                    progress = 120f / 720,
                    currentPage = "120",
                    rating = 4,
                    review = "우주 앞에서 작아지는 기분",
                    isLoaded = true,
                ),
                onAction = {},
                onBackClick = {},
                onQuotesClick = {},
            )
        }
    }
}

@Preview(name = "찾을 수 없을 때, 어두운 테마")
@Composable
private fun BookDetailScreenMissingDarkPreview() {
    AppTheme(darkTheme = true) {
        Surface {
            BookDetailScreen(
                state = BookDetailState(isLoaded = true, isMissing = true),
                onAction = {},
                onBackClick = {},
                onQuotesClick = {},
            )
        }
    }
}
