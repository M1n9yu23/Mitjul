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

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TimePickerDialog
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.gyugle.mitjul.core.designsystem.component.DestructiveActionButton
import com.gyugle.mitjul.core.designsystem.component.PressableSurface
import com.gyugle.mitjul.core.designsystem.component.PrimaryActionButton
import com.gyugle.mitjul.core.designsystem.component.SecondaryActionButton
import com.gyugle.mitjul.core.designsystem.theme.AppTheme
import com.gyugle.mitjul.core.presentation.ObserveAsEvents
import com.gyugle.mitjul.core.presentation.permissions.Permission
import com.gyugle.mitjul.core.presentation.permissions.PermissionState
import com.gyugle.mitjul.core.presentation.permissions.rememberPermissionController
import com.gyugle.mitjul.feature.settings.presentation.R
import kotlinx.datetime.LocalTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun SettingsRoot(
    onLicensesClick: () -> Unit,
    onLogOut: () -> Unit,
    viewModel: SettingsViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val resources = LocalResources.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionController = rememberPermissionController()

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val current = permissionController.getPermissionState(Permission.NOTIFICATIONS)
            viewModel.onAction(SettingsAction.OnPermissionChecked(current))
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SettingsEvent.RequestNotificationPermission -> {
                val result = permissionController.requestPermission(Permission.NOTIFICATIONS)
                viewModel.onAction(SettingsAction.OnPermissionResult(result))
            }
            SettingsEvent.NotificationsBlocked -> {
                val result = snackbarHostState.showSnackbar(
                    message = resources.getString(R.string.settings_reminder_needs_permission),
                    actionLabel = resources.getString(R.string.settings_open_system_settings),
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) permissionController.openAppSettings()
            }
            is SettingsEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        SettingsScreen(
            state = state,
            onAction = { action ->
                if (action == SettingsAction.OnLogOutConfirm) onLogOut()
                viewModel.onAction(action)
            },
            onLicensesClick = onLicensesClick,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun SettingsScreen(
    state: SettingsState,
    onAction: (SettingsAction) -> Unit,
    onLicensesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.settings_title),
            modifier = Modifier.padding(horizontal = AppTheme.spacing.screenMargin),
            style = MaterialTheme.typography.headlineMedium,
        )
        ReminderRow(
            isOn = state.isReminderOn,
            enabled = !state.isRequestingPermission,
            isBlocked = state.isReminderOn && state.notificationPermission.let { it != null && it != PermissionState.GRANTED },
            onToggle = { onAction(SettingsAction.OnReminderToggle(it)) },
        )
        SettingsRow(
            text = stringResource(R.string.settings_reminder_time, state.reminderTime.hour, state.reminderTime.minute),
            clickLabel = stringResource(R.string.settings_reminder_change_time),
            onClick = { onAction(SettingsAction.OnReminderTimeClick) },
        )
        SettingsRow(
            text = stringResource(R.string.settings_licenses),
            clickLabel = stringResource(R.string.settings_open),
            onClick = onLicensesClick,
        )
        SettingsRow(
            text = stringResource(R.string.settings_log_out),
            clickLabel = stringResource(R.string.settings_log_out),
            onClick = { onAction(SettingsAction.OnLogOutClick) },
        )
    }
    if (state.isLogOutDialogOpen) {
        LogOutDialog(
            pendingQuoteCount = state.pendingQuoteCount,
            onConfirm = { onAction(SettingsAction.OnLogOutConfirm) },
            onDismiss = { onAction(SettingsAction.OnLogOutDismiss) },
        )
    }
    if (state.isTimePickerOpen) {
        ReminderTimeDialog(
            time = state.reminderTime,
            onPick = { onAction(SettingsAction.OnReminderTimePicked(it)) },
            onDismiss = { onAction(SettingsAction.OnReminderTimeDismiss) },
        )
    }
}

@Composable
private fun ReminderRow(isOn: Boolean, enabled: Boolean, isBlocked: Boolean, onToggle: (Boolean) -> Unit) {
    ListItem(
        headlineContent = { Text(stringResource(R.string.settings_reminder)) },
        modifier = Modifier.toggleable(value = isOn, enabled = enabled, role = Role.Switch, onValueChange = onToggle),
        supportingContent = if (isBlocked) {
            { Text(stringResource(R.string.settings_reminder_blocked), color = MaterialTheme.colorScheme.error) }
        } else {
            null
        },
        trailingContent = { Switch(checked = isOn, onCheckedChange = null, enabled = enabled) },
    )
}

@Composable
private fun SettingsRow(text: String, clickLabel: String, onClick: () -> Unit) {
    PressableSurface(onClick = onClick, clickLabel = clickLabel) {
        ListItem(headlineContent = { Text(text) })
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReminderTimeDialog(time: LocalTime, onPick: (LocalTime) -> Unit, onDismiss: () -> Unit) {
    val pickerState = rememberTimePickerState(initialHour = time.hour, initialMinute = time.minute, is24Hour = true)
    TimePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            PrimaryActionButton(
                text = stringResource(R.string.settings_confirm),
                onClick = { onPick(LocalTime(pickerState.hour, pickerState.minute)) },
            )
        },
        dismissButton = { SecondaryActionButton(text = stringResource(R.string.settings_cancel), onClick = onDismiss) },
        title = { Text(stringResource(R.string.settings_reminder)) },
    ) {
        TimePicker(state = pickerState)
    }
}

@Composable
private fun LogOutDialog(pendingQuoteCount: Int?, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    val message = when {
        pendingQuoteCount == null -> stringResource(R.string.settings_log_out_unknown)
        pendingQuoteCount > 0 -> stringResource(R.string.settings_log_out_pending, pendingQuoteCount)
        else -> stringResource(R.string.settings_log_out_clean)
    }
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = { DestructiveActionButton(text = stringResource(R.string.settings_log_out), onClick = onConfirm) },
        dismissButton = { SecondaryActionButton(text = stringResource(R.string.settings_cancel), onClick = onDismiss) },
        title = { Text(stringResource(R.string.settings_log_out_title)) },
        text = { Text(message) },
    )
}

@Preview(name = "설정")
@Composable
private fun SettingsScreenPreview() {
    AppTheme { Surface { SettingsScreen(state = SettingsState(pendingQuoteCount = 0), onAction = {}, onLicensesClick = {}) } }
}

@Preview(name = "보내기 전 문장이 있을 때 로그아웃, 어두운 테마")
@Composable
private fun SettingsScreenLogOutDarkPreview() {
    val state = SettingsState(pendingQuoteCount = 2, isLogOutDialogOpen = true)
    AppTheme(darkTheme = true) { Surface { SettingsScreen(state = state, onAction = {}, onLicensesClick = {}) } }
}

@Preview(name = "알림이 꺼진 채 켠 리마인더")
@Composable
private fun SettingsScreenReminderBlockedPreview() {
    val state = SettingsState(isReminderOn = true, notificationPermission = PermissionState.DENIED)
    AppTheme { Surface { SettingsScreen(state = state, onAction = {}, onLicensesClick = {}) } }
}
