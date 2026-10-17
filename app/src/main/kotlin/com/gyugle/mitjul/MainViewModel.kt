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

package com.gyugle.mitjul

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyugle.mitjul.core.domain.onFailure
import com.gyugle.mitjul.core.presentation.toUiText
import com.gyugle.mitjul.feature.auth.domain.repository.AuthRepository
import com.gyugle.mitjul.feature.quote.domain.QuoteSyncScheduler
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(
    private val authRepository: AuthRepository,
    private val syncScheduler: QuoteSyncScheduler,
) : ViewModel() {

    val state: StateFlow<MainState> = authRepository.observeIsLoggedIn()
        .onEach { isLoggedIn -> if (isLoggedIn) syncScheduler.scheduleSync() }
        .map { MainState(isCheckingSession = false, isLoggedIn = it) }
        .catch { emit(MainState(isCheckingSession = false)) }
        .stateIn(viewModelScope, SharingStarted.Eagerly, MainState())

    private val _events = Channel<MainEvent>()
    val events = _events.receiveAsFlow()

    fun logOut() {
        viewModelScope.launch {
            authRepository.logOut().onFailure { _events.send(MainEvent.ShowSnackbar(it.toUiText())) }
        }
    }
}
