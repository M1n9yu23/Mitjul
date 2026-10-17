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

import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ListItem
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyugle.mitjul.core.designsystem.component.EmptyState
import com.gyugle.mitjul.core.designsystem.component.ErrorState
import com.gyugle.mitjul.core.designsystem.component.LoadingState
import com.gyugle.mitjul.core.designsystem.component.PressableSurface
import com.gyugle.mitjul.core.designsystem.component.PrimaryActionButton
import com.gyugle.mitjul.core.designsystem.component.SecondaryActionButton
import com.gyugle.mitjul.core.designsystem.theme.AppTheme
import com.gyugle.mitjul.core.presentation.ObserveAsEvents
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.quote.domain.QuoteValidator
import com.gyugle.mitjul.feature.quote.domain.error.QuoteInputError
import com.gyugle.mitjul.feature.quote.presentation.R
import com.gyugle.mitjul.feature.quote.presentation.quotecapture.QuoteCaptureRoot
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun QuoteListRoot(
    bookId: String,
    bookTitle: String,
    onQuoteClick: (String) -> Unit,
    viewModel: QuoteListViewModel = koinViewModel { parametersOf(bookId) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is QuoteListEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
        }
    }

    if (state.isCaptureOpen) {
        QuoteCaptureRoot(
            onTextPicked = { viewModel.onAction(QuoteListAction.OnTextCaptured(it)) },
            onClose = { viewModel.onAction(QuoteListAction.OnCaptureClose) },
        )
        return
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        QuoteListScreen(
            bookTitle = bookTitle,
            state = state,
            onAction = { viewModel.onAction(it) },
            onQuoteClick = onQuoteClick,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun QuoteListScreen(
    bookTitle: String,
    state: QuoteListState,
    onAction: (QuoteListAction) -> Unit,
    onQuoteClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val spacing = AppTheme.spacing
    val focusRequester = remember { FocusRequester() }
    Column(modifier = modifier.fillMaxSize().imePadding()) {
        QuoteForm(
            bookTitle = bookTitle,
            state = state,
            onAction = onAction,
            focusRequester = focusRequester,
            modifier = Modifier.padding(start = spacing.screenMargin, top = spacing.screenMargin, end = spacing.screenMargin),
        )
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(spacing.itemGap),
        ) {
            when {
                state.quotes.isNotEmpty() -> items(items = state.quotes, key = { it.id }) { quote ->
                    QuoteItem(
                        quote = quote,
                        onClick = { onQuoteClick(quote.id) },
                        modifier = Modifier.animateItem(fadeInSpec = tween(AppTheme.motion.fadeIn)),
                    )
                }
                state.error != null -> item {
                    ErrorState(
                        title = stringResource(R.string.quote_list_error_title),
                        description = state.error.asString(),
                        retryText = stringResource(R.string.quote_list_refresh),
                        onRetry = { onAction(QuoteListAction.OnRefresh) },
                    )
                }
                !state.isCopyLoaded || state.isRefreshing -> item {
                    LoadingState(label = stringResource(R.string.quote_list_loading))
                }
                else -> item {
                    EmptyState(
                        title = stringResource(R.string.quote_list_empty_title),
                        description = stringResource(R.string.quote_list_empty_description),
                        actionText = stringResource(R.string.quote_list_empty_action),
                        onAction = { focusRequester.requestFocus() },
                    )
                }
            }
        }
    }
}

@Composable
private fun QuoteForm(
    bookTitle: String,
    state: QuoteListState,
    onAction: (QuoteListAction) -> Unit,
    focusRequester: FocusRequester,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.itemGap)) {
        Text(bookTitle, style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = state.text,
            onValueChange = { onAction(QuoteListAction.OnTextChange(it)) },
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            enabled = !state.isSaving,
            label = { Text(stringResource(R.string.quote_list_text_label)) },
            supportingText = {
                val count = stringResource(R.string.quote_list_text_count, state.text.length, QuoteValidator.TEXT_MAX_LENGTH)
                Text(state.textError?.asString() ?: count)
            },
            isError = state.textError != null,
            minLines = 2,
        )
        SecondaryActionButton(
            text = stringResource(R.string.quote_list_capture),
            onClick = { onAction(QuoteListAction.OnCaptureClick) },
            enabled = !state.isSaving,
        )
        OutlinedTextField(
            value = state.page,
            onValueChange = { onAction(QuoteListAction.OnPageChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
            label = { Text(stringResource(R.string.quote_list_page_label)) },
            supportingText = { state.pageError?.let { Text(it.asString()) } },
            isError = state.pageError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.memo,
            onValueChange = { onAction(QuoteListAction.OnMemoChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
            label = { Text(stringResource(R.string.quote_list_memo_label)) },
        )
        PrimaryActionButton(
            text = stringResource(if (state.isSaving) R.string.quote_list_saving else R.string.quote_list_save),
            onClick = { onAction(QuoteListAction.OnSaveClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        )
    }
}

@Composable
private fun QuoteItem(quote: QuoteUi, onClick: () -> Unit, modifier: Modifier = Modifier) {
    PressableSurface(
        onClick = onClick,
        clickLabel = stringResource(R.string.quote_list_open_card),
        modifier = modifier,
    ) {
        ListItem(
            headlineContent = { Text(quote.text) },
            overlineContent = if (quote.page != null) {
                { Text(quote.page.asString()) }
            } else {
                null
            },
            supportingContent = if (quote.memo != null) {
                { Text(quote.memo) }
            } else {
                null
            },
            trailingContent = if (quote.isPending) {
                { Text(stringResource(R.string.quote_list_pending), style = MaterialTheme.typography.labelMedium) }
            } else {
                null
            },
        )
    }
}

@Preview(name = "문장이 있을 때")
@Composable
private fun QuoteListScreenPreview() {
    val quotes = listOf(
        QuoteUi("q1", "우리는 별의 먼지로 만들어졌다.", UiText.StringResource(R.string.quote_list_page, listOf(72)), "첫 장부터 좋다", isPending = true),
        QuoteUi("q2", "광활한 우주에서 지구는 작은 점 하나다.", null, null, isPending = false),
    )
    AppTheme {
        Surface {
            QuoteListScreen(
                bookTitle = "코스모스",
                state = QuoteListState(quotes = quotes, isCopyLoaded = true),
                onAction = {},
                onQuoteClick = {},
            )
        }
    }
}

@Preview(name = "비어 있고 입력 오류, 어두운 테마")
@Composable
private fun QuoteListScreenEmptyDarkPreview() {
    AppTheme(darkTheme = true) {
        Surface {
            QuoteListScreen(
                bookTitle = "코스모스",
                state = QuoteListState(
                    page = "0",
                    textError = QuoteInputError.Text.BLANK.toUiText(),
                    pageError = QuoteInputError.Page.OUT_OF_RANGE.toUiText(),
                    isCopyLoaded = true,
                ),
                onAction = {},
                onQuoteClick = {},
            )
        }
    }
}
