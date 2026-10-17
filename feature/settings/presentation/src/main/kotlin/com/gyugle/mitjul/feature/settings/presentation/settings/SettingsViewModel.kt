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

package com.gyugle.mitjul.feature.settings.presentation.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.onFailure
import com.gyugle.mitjul.core.presentation.permissions.PermissionState
import com.gyugle.mitjul.core.presentation.toUiText
import com.gyugle.mitjul.feature.settings.domain.model.Reminder
import com.gyugle.mitjul.feature.settings.domain.repository.SettingsRepository
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
import kotlinx.datetime.LocalTime

class SettingsViewModel(private val settingsRepository: SettingsRepository) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _events = Channel<SettingsEvent>()
    val events = _events.receiveAsFlow()

    init {
        settingsRepository.observePendingQuoteCount()
            .onEach { count -> _state.update { it.copy(pendingQuoteCount = count) } }
            .catch { _state.update { it.copy(pendingQuoteCount = null) } }
            .launchIn(viewModelScope)
        settingsRepository.observeReminder()
            .onEach { reminder -> _state.update { it.copy(isReminderOn = reminder.isOn, reminderTime = reminder.time) } }
            .catch { _events.send(SettingsEvent.ShowSnackbar(DataError.Local.UNKNOWN.toUiText())) }
            .launchIn(viewModelScope)
    }

    fun onAction(action: SettingsAction) {
        when (action) {
            SettingsAction.OnLogOutClick -> _state.update { it.copy(isLogOutDialogOpen = true) }
            SettingsAction.OnLogOutConfirm,
            SettingsAction.OnLogOutDismiss -> _state.update { it.copy(isLogOutDialogOpen = false) }
            is SettingsAction.OnReminderToggle -> toggleReminder(action.isOn)
            is SettingsAction.OnPermissionResult -> onPermissionResult(action.state)
            is SettingsAction.OnPermissionChecked -> _state.update { it.copy(notificationPermission = action.state) }
            SettingsAction.OnReminderTimeClick -> _state.update { it.copy(isTimePickerOpen = true) }
            is SettingsAction.OnReminderTimePicked -> {
                _state.update { it.copy(isTimePickerOpen = false) }
                saveReminder(state.value.isReminderOn, action.time)
            }
            SettingsAction.OnReminderTimeDismiss -> _state.update { it.copy(isTimePickerOpen = false) }
        }
    }

    private fun toggleReminder(isOn: Boolean) {
        val current = state.value
        when {
            !isOn || current.notificationPermission == PermissionState.GRANTED -> saveReminder(isOn, current.reminderTime)
            current.isRequestingPermission -> Unit
            else -> {
                _state.update { it.copy(isRequestingPermission = true) }
                viewModelScope.launch { _events.send(SettingsEvent.RequestNotificationPermission) }
            }
        }
    }

    private fun onPermissionResult(permission: PermissionState) {
        _state.update { it.copy(notificationPermission = permission, isRequestingPermission = false) }
        if (permission == PermissionState.GRANTED) {
            saveReminder(isOn = true, time = state.value.reminderTime)
        } else {
            viewModelScope.launch { _events.send(SettingsEvent.NotificationsBlocked) }
        }
    }

    private fun saveReminder(isOn: Boolean, time: LocalTime) {
        viewModelScope.launch {
            settingsRepository.setReminder(Reminder(isOn, time))
                .onFailure { _events.send(SettingsEvent.ShowSnackbar(it.toUiText())) }
        }
    }
}
