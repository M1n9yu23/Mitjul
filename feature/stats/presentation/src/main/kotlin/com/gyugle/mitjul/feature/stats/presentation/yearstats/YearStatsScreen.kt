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

package com.gyugle.mitjul.feature.stats.presentation.yearstats

import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyugle.mitjul.core.designsystem.component.EmptyState
import com.gyugle.mitjul.core.designsystem.component.ErrorState
import com.gyugle.mitjul.core.designsystem.component.LoadingState
import com.gyugle.mitjul.core.designsystem.theme.AppTheme
import com.gyugle.mitjul.core.presentation.ObserveAsEvents
import com.gyugle.mitjul.feature.stats.presentation.R
import kotlin.math.roundToInt
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun YearStatsRoot(
    onBack: () -> Unit,
    viewModel: YearStatsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is YearStatsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        YearStatsScreen(state = state, onBackClick = onBack, modifier = Modifier.padding(innerPadding))
    }
}

@Composable
fun YearStatsScreen(state: YearStatsState, onBackClick: () -> Unit, modifier: Modifier = Modifier) {
    when {
        !state.isLoaded -> LoadingState(label = stringResource(R.string.stats_loading), modifier = modifier)
        state.isFailed -> ErrorState(
            title = stringResource(R.string.stats_error_title),
            description = stringResource(R.string.stats_error_description),
            retryText = stringResource(R.string.stats_back),
            onRetry = onBackClick,
            modifier = modifier,
        )
        state.recordDayCount == 0 -> EmptyState(
            title = stringResource(R.string.stats_empty_title),
            description = stringResource(R.string.stats_empty_description),
            actionText = stringResource(R.string.stats_back),
            onAction = onBackClick,
            modifier = modifier,
        )
        else -> YearStatsContent(state = state, modifier = modifier)
    }
}

@Composable
private fun YearStatsContent(state: YearStatsState, modifier: Modifier = Modifier) {
    val spacing = AppTheme.spacing
    val isPreview = LocalInspectionMode.current
    var started by remember { mutableStateOf(isPreview) }
    LaunchedEffect(Unit) { started = true }
    val fill = animateFloatAsState(if (started) 1f else 0f, tween(AppTheme.motion.valueFill), label = "statsFill")
    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(spacing.screenMargin),
        verticalArrangement = Arrangement.spacedBy(spacing.itemGap),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(spacing.itemGap), verticalAlignment = Alignment.Bottom) {
            Text(stringResource(R.string.stats_title, state.year), style = MaterialTheme.typography.headlineMedium)
            FilledCount(R.string.stats_books, state.bookCount) { fill.value }
            FilledCount(R.string.stats_pages, state.pageCount) { fill.value }
        }
        Text(stringResource(R.string.stats_record_days), style = MaterialTheme.typography.titleSmall)
        RecordHeatmap(
            dayShades = state.dayShades,
            firstWeekday = state.firstWeekday,
            description = stringResource(R.string.stats_record_days_description, state.year, state.recordDayCount),
        )
        MonthRange()
        Text(stringResource(R.string.stats_monthly_books), style = MaterialTheme.typography.titleSmall)
        MonthBars(booksPerMonth = state.booksPerMonth) { fill.value }
        MonthRange()
    }
}

@Composable
private fun FilledCount(@StringRes format: Int, count: Int, fill: () -> Float) {
    Text(stringResource(format, (count * fill()).roundToInt()), style = MaterialTheme.typography.titleLarge)
}

@Composable
private fun RecordHeatmap(dayShades: List<Float>, firstWeekday: Int, description: String) {
    val blank = MaterialTheme.colorScheme.surfaceVariant
    val ink = MaterialTheme.colorScheme.primary
    val weekCount = (firstWeekday + dayShades.size + 6) / 7
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(weekCount / 7f)
            .semantics { contentDescription = description },
    ) {
        val cell = size.width / weekCount
        dayShades.forEachIndexed { day, shade ->
            val slot = firstWeekday + day
            drawRect(
                color = if (shade == 0f) blank else ink.copy(alpha = shade),
                topLeft = Offset(slot / 7 * cell, slot % 7 * cell),
                size = Size(cell * 0.8f, cell * 0.8f),
            )
        }
    }
}

@Composable
private fun MonthBars(booksPerMonth: List<Int>, fill: () -> Float) {
    val ink = MaterialTheme.colorScheme.primary
    val most = (booksPerMonth.maxOrNull() ?: 0).coerceAtLeast(1)
    val description = booksPerMonth.mapIndexed { index, count ->
        stringResource(R.string.stats_month_books, index + 1, count)
    }.joinToString()
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(3f)
            .semantics { contentDescription = description },
    ) {
        val slot = size.width / booksPerMonth.size
        booksPerMonth.forEachIndexed { month, count ->
            val height = size.height * count / most * fill()
            drawRect(
                color = ink,
                topLeft = Offset(month * slot + slot * 0.2f, size.height - height),
                size = Size(slot * 0.6f, height),
            )
        }
    }
}

@Composable
private fun MonthRange() {
    val style = MaterialTheme.typography.labelSmall
    val color = MaterialTheme.colorScheme.onSurfaceVariant
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(stringResource(R.string.stats_first_month), style = style, color = color)
        Text(stringResource(R.string.stats_last_month), style = style, color = color)
    }
}

@Preview(name = "기록이 있을 때")
@Composable
private fun YearStatsScreenPreview() {
    val shades = listOf(0f, 1f / 3, 0f, 2f / 3, 0f, 1f)
    val state = YearStatsState(
        year = 2026,
        bookCount = 14,
        pageCount = 3180,
        booksPerMonth = listOf(3, 1, 6, 4, 0, 0, 0, 0, 0, 0, 0, 0),
        dayShades = List(365) { day -> shades[day % shades.size] },
        firstWeekday = 3,
        recordDayCount = 182,
        isLoaded = true,
    )
    AppTheme { Surface { YearStatsScreen(state = state, onBackClick = {}) } }
}

@Preview(name = "기록이 없을 때, 어두운 테마")
@Composable
private fun YearStatsScreenEmptyDarkPreview() {
    AppTheme(darkTheme = true) {
        Surface { YearStatsScreen(state = YearStatsState(year = 2026, isLoaded = true), onBackClick = {}) }
    }
}
