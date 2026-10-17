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

import app.cash.turbine.test
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.gyugle.mitjul.core.presentation.permissions.PermissionState
import com.gyugle.mitjul.feature.settings.domain.model.Reminder
import com.gyugle.mitjul.feature.settings.domain.repository.FakeSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalTime
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @BeforeEach
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun onReminderToggle_deniedThenGranted_savesOnlyAfterPermission() = runTest {
        val repository = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repository)

        viewModel.events.test {
            viewModel.onAction(SettingsAction.OnReminderToggle(isOn = true))
            assertThat(awaitItem()).isEqualTo(SettingsEvent.RequestNotificationPermission)
            viewModel.onAction(SettingsAction.OnPermissionResult(PermissionState.DENIED))
            assertThat(awaitItem()).isEqualTo(SettingsEvent.NotificationsBlocked)
            assertThat(repository.saved).isEmpty()
            assertThat(viewModel.state.value.isRequestingPermission).isFalse()

            viewModel.onAction(SettingsAction.OnReminderToggle(isOn = true))
            assertThat(awaitItem()).isEqualTo(SettingsEvent.RequestNotificationPermission)
            viewModel.onAction(SettingsAction.OnPermissionResult(PermissionState.GRANTED))
        }

        assertThat(repository.saved).containsExactly(Reminder(isOn = true, time = LocalTime(21, 0)))
    }
}
