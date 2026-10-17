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

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.layer.drawLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyugle.mitjul.core.designsystem.component.EmptyState
import com.gyugle.mitjul.core.designsystem.component.LoadingState
import com.gyugle.mitjul.core.designsystem.component.PrimaryActionButton
import com.gyugle.mitjul.core.designsystem.theme.AppTheme
import com.gyugle.mitjul.core.presentation.ObserveAsEvents
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.quote.presentation.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun QuoteCardRoot(
    quoteId: String,
    bookTitle: String,
    onBack: () -> Unit,
    viewModel: QuoteCardViewModel = koinViewModel { parametersOf(quoteId, bookTitle) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val card = rememberGraphicsLayer()
    val chooserTitle = stringResource(R.string.quote_card_share_title)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            QuoteCardEvent.ShareCard -> {
                val isShared = shareQuoteCard(context, card, chooserTitle)
                viewModel.onAction(QuoteCardAction.OnShareFinished(isShared))
            }
            is QuoteCardEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        QuoteCardScreen(
            state = state,
            onAction = { viewModel.onAction(it) },
            onBackClick = onBack,
            card = card,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun QuoteCardScreen(
    state: QuoteCardState,
    onAction: (QuoteCardAction) -> Unit,
    onBackClick: () -> Unit,
    card: GraphicsLayer,
    modifier: Modifier = Modifier,
) {
    when {
        !state.isLoaded -> LoadingState(label = stringResource(R.string.quote_list_loading), modifier = modifier)
        state.isMissing -> EmptyState(
            title = stringResource(R.string.quote_card_missing_title),
            description = stringResource(R.string.quote_card_missing_description),
            actionText = stringResource(R.string.quote_card_back),
            onAction = onBackClick,
            modifier = modifier,
        )
        else -> Column(
            modifier = modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(AppTheme.spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.itemGap),
        ) {
            QuoteCard(
                text = state.text,
                source = state.source?.asString(),
                modifier = Modifier.fillMaxWidth().drawWithContent {
                    card.record { this@drawWithContent.drawContent() }
                    drawLayer(card)
                },
            )
            PrimaryActionButton(
                text = stringResource(if (state.isSharing) R.string.quote_card_sharing else R.string.quote_card_share),
                onClick = { onAction(QuoteCardAction.OnShareClick) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isSharing,
            )
        }
    }
}

@Composable
private fun QuoteCard(text: String, source: String?, modifier: Modifier = Modifier) {
    val underline = MaterialTheme.colorScheme.primaryContainer
    var layout by remember { mutableStateOf<TextLayoutResult?>(null) }
    Surface(modifier = modifier) {
        Column(
            modifier = Modifier.padding(AppTheme.spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.itemGap),
        ) {
            Text(
                text = text,
                modifier = Modifier.drawBehind {
                    val lines = layout ?: return@drawBehind
                    for (line in 0 until lines.lineCount) {
                        val left = lines.getLineLeft(line)
                        val bottom = lines.getLineBottom(line)
                        val height = (bottom - lines.getLineTop(line)) / 3
                        drawRect(
                            color = underline,
                            topLeft = Offset(left, bottom - height),
                            size = Size(lines.getLineRight(line) - left, height),
                        )
                    }
                },
                style = MaterialTheme.typography.titleLarge,
                onTextLayout = { layout = it },
            )
            source?.let {
                Text(it, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                text = stringResource(R.string.quote_card_brand),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Preview(name = "카드가 있을 때")
@Composable
private fun QuoteCardScreenPreview() {
    AppTheme {
        Surface {
            QuoteCardScreen(
                state = QuoteCardState(
                    text = "우리는 별의 먼지로 만들어졌다. 우리 몸의 원자는 오래전 별 속에서 만들어졌다.",
                    source = UiText.StringResource(R.string.quote_card_source, listOf("코스모스", 72)),
                    isLoaded = true,
                ),
                onAction = {},
                onBackClick = {},
                card = rememberGraphicsLayer(),
            )
        }
    }
}

@Preview(name = "찾을 수 없을 때, 어두운 테마")
@Composable
private fun QuoteCardScreenMissingDarkPreview() {
    AppTheme(darkTheme = true) {
        Surface {
            QuoteCardScreen(
                state = QuoteCardState(isLoaded = true, isMissing = true),
                onAction = {},
                onBackClick = {},
                card = rememberGraphicsLayer(),
            )
        }
    }
}
