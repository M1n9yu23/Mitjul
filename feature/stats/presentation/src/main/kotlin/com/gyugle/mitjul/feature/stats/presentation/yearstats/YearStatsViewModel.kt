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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyugle.mitjul.core.domain.onFailure
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.stats.domain.RecordRefresher
import com.gyugle.mitjul.feature.stats.domain.model.YearStats
import com.gyugle.mitjul.feature.stats.domain.repository.StatsRepository
import com.gyugle.mitjul.feature.stats.presentation.R
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

private const val DARKEST_COUNT = 3

class YearStatsViewModel(
    statsRepository: StatsRepository,
    recordRefresher: RecordRefresher,
) : ViewModel() {

    private val _state = MutableStateFlow(YearStatsState())
    val state: StateFlow<YearStatsState> = _state.asStateFlow()

    private val _events = Channel<YearStatsEvent>()
    val events = _events.receiveAsFlow()

    init {
        statsRepository.observeYearStats()
            .onEach { stats -> _state.update { it.withStats(stats) } }
            .catch { _state.update { it.copy(isLoaded = true, isFailed = true) } }
            .launchIn(viewModelScope)
        viewModelScope.launch {
            recordRefresher.refresh().onFailure {
                _events.send(YearStatsEvent.ShowSnackbar(UiText.StringResource(R.string.stats_refresh_failed)))
            }
        }
    }

    private fun YearStatsState.withStats(stats: YearStats) = copy(
        year = stats.year,
        bookCount = stats.bookCount,
        pageCount = stats.pageCount,
        booksPerMonth = stats.booksPerMonth,
        dayShades = stats.recordsPerDay.map { count -> count.coerceAtMost(DARKEST_COUNT) / DARKEST_COUNT.toFloat() },
        firstWeekday = stats.firstWeekday,
        recordDayCount = stats.recordsPerDay.count { count -> count > 0 },
        isLoaded = true,
    )
}
