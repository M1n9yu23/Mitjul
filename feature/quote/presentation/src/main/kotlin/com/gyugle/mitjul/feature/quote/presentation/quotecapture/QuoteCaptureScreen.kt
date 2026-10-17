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

package com.gyugle.mitjul.feature.quote.presentation.quotecapture

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.gyugle.mitjul.core.camera.CameraPreview
import com.gyugle.mitjul.core.camera.PhotoCamera
import com.gyugle.mitjul.core.designsystem.component.EmptyState
import com.gyugle.mitjul.core.designsystem.component.ErrorState
import com.gyugle.mitjul.core.designsystem.component.LoadingState
import com.gyugle.mitjul.core.designsystem.component.PrimaryActionButton
import com.gyugle.mitjul.core.designsystem.component.SecondaryActionButton
import com.gyugle.mitjul.core.designsystem.theme.AppTheme
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.presentation.ObserveAsEvents
import com.gyugle.mitjul.core.presentation.permissions.Permission
import com.gyugle.mitjul.core.presentation.permissions.PermissionState
import com.gyugle.mitjul.core.presentation.permissions.rememberPermissionController
import com.gyugle.mitjul.feature.quote.presentation.R
import java.io.File
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val CAPTURE_FILE = "quote_capture.jpg"

@Composable
fun QuoteCaptureRoot(
    onTextPicked: (String) -> Unit,
    onClose: () -> Unit,
    viewModel: QuoteCaptureViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val permissionController = rememberPermissionController()
    val camera = remember { PhotoCamera() }

    LaunchedEffect(lifecycleOwner) {
        lifecycleOwner.repeatOnLifecycle(Lifecycle.State.RESUMED) {
            val current = permissionController.getPermissionState(Permission.CAMERA)
            viewModel.onAction(QuoteCaptureAction.OnPermissionChecked(current))
        }
    }

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            QuoteCaptureEvent.RequestPermission -> {
                val result = permissionController.requestPermission(Permission.CAMERA)
                viewModel.onAction(QuoteCaptureAction.OnPermissionResult(result))
            }
            QuoteCaptureEvent.TakePicture -> {
                val file = File(context.cacheDir, CAPTURE_FILE)
                when (camera.takePicture(file, view.display.rotation)) {
                    is Result.Success -> viewModel.onAction(QuoteCaptureAction.OnPictureTaken(file.path))
                    is Result.Error -> viewModel.onAction(QuoteCaptureAction.OnPictureFailed)
                }
            }
            is QuoteCaptureEvent.TextPicked -> onTextPicked(event.text)
            is QuoteCaptureEvent.ShowSnackbar -> scope.launch {
                snackbarHostState.showSnackbar(event.message.resolve(context))
            }
        }
    }

    val onAction: (QuoteCaptureAction) -> Unit = { action ->
        when (action) {
            QuoteCaptureAction.OnOpenSettingsClick -> permissionController.openAppSettings()
            QuoteCaptureAction.OnCloseClick -> onClose()
            else -> Unit
        }
        viewModel.onAction(action)
    }
    BackHandler { onAction(QuoteCaptureAction.OnCloseClick) }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        QuoteCaptureScreen(
            state = state,
            onAction = onAction,
            viewfinder = { modifier ->
                CameraPreview(
                    camera = camera,
                    onBindResult = { viewModel.onAction(QuoteCaptureAction.OnCameraBound(it)) },
                    modifier = modifier,
                )
            },
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun QuoteCaptureScreen(
    state: QuoteCaptureState,
    onAction: (QuoteCaptureAction) -> Unit,
    viewfinder: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.permission != PermissionState.GRANTED ->
            PermissionMessage(state.permission, state.isRequestingPermission, onAction, modifier)
        state.cameraError != null -> ErrorState(
            title = stringResource(R.string.quote_capture_error_title),
            description = state.cameraError.asString(),
            retryText = stringResource(R.string.quote_capture_close),
            onRetry = { onAction(QuoteCaptureAction.OnCloseClick) },
            modifier = modifier,
        )
        state.blocks.isNotEmpty() -> RecognizedBlocks(state, onAction, modifier)
        else -> Viewfinder(state.isReading, onAction, viewfinder, modifier)
    }
}

@Composable
private fun PermissionMessage(
    permission: PermissionState,
    isRequestingPermission: Boolean,
    onAction: (QuoteCaptureAction) -> Unit,
    modifier: Modifier,
) {
    val (title, description, actionText) = when (permission) {
        PermissionState.PERMANENTLY_DENIED -> Triple(
            R.string.quote_capture_blocked_title,
            R.string.quote_capture_blocked_description,
            R.string.quote_capture_open_settings,
        )
        PermissionState.DENIED -> Triple(
            R.string.quote_capture_denied_title,
            R.string.quote_capture_denied_description,
            R.string.quote_capture_denied_retry,
        )
        else -> Triple(
            R.string.quote_capture_permission_title,
            R.string.quote_capture_permission_description,
            R.string.quote_capture_permission_allow,
        )
    }
    val action = if (permission == PermissionState.PERMANENTLY_DENIED) {
        QuoteCaptureAction.OnOpenSettingsClick
    } else {
        QuoteCaptureAction.OnRequestPermissionClick
    }
    Column(modifier = modifier.fillMaxSize()) {
        EmptyState(
            title = stringResource(title),
            description = stringResource(description),
            actionText = stringResource(actionText),
            onAction = { onAction(action) },
            enabled = !isRequestingPermission,
        )
        SecondaryActionButton(
            text = stringResource(R.string.quote_capture_close),
            onClick = { onAction(QuoteCaptureAction.OnCloseClick) },
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun Viewfinder(
    isReading: Boolean,
    onAction: (QuoteCaptureAction) -> Unit,
    viewfinder: @Composable (Modifier) -> Unit,
    modifier: Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        viewfinder(Modifier.weight(1f).fillMaxWidth())
        Column(
            modifier = Modifier.padding(AppTheme.spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.itemGap),
        ) {
            if (isReading) LoadingState(label = stringResource(R.string.quote_capture_reading))
            PrimaryActionButton(
                text = stringResource(R.string.quote_capture_shutter),
                onClick = { onAction(QuoteCaptureAction.OnShutterClick) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isReading,
            )
            SecondaryActionButton(
                text = stringResource(R.string.quote_capture_close),
                onClick = { onAction(QuoteCaptureAction.OnCloseClick) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RecognizedBlocks(
    state: QuoteCaptureState,
    onAction: (QuoteCaptureAction) -> Unit,
    modifier: Modifier,
) {
    val spacing = AppTheme.spacing
    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.quote_capture_pick_hint),
            modifier = Modifier.padding(start = spacing.screenMargin, top = spacing.screenMargin, end = spacing.screenMargin),
            style = MaterialTheme.typography.titleMedium,
        )
        LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(spacing.screenMargin)) {
            itemsIndexed(state.blocks) { index, block ->
                val checked = index in state.selected
                ListItem(
                    headlineContent = { Text(block) },
                    modifier = Modifier.toggleable(
                        value = checked,
                        role = Role.Checkbox,
                        onValueChange = { onAction(QuoteCaptureAction.OnBlockClick(index)) },
                    ),
                    leadingContent = { Checkbox(checked = checked, onCheckedChange = null) },
                )
            }
        }
        Column(
            modifier = Modifier.padding(spacing.screenMargin),
            verticalArrangement = Arrangement.spacedBy(spacing.itemGap),
        ) {
            PrimaryActionButton(
                text = stringResource(R.string.quote_capture_insert),
                onClick = { onAction(QuoteCaptureAction.OnInsertClick) },
                modifier = Modifier.fillMaxWidth(),
                enabled = state.selected.isNotEmpty(),
            )
            SecondaryActionButton(
                text = stringResource(R.string.quote_capture_retake),
                onClick = { onAction(QuoteCaptureAction.OnRetakeClick) },
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Preview(name = "읽은 문단을 고를 때")
@Composable
private fun QuoteCaptureScreenBlocksPreview() {
    AppTheme {
        Surface {
            QuoteCaptureScreen(
                state = QuoteCaptureState(
                    permission = PermissionState.GRANTED,
                    blocks = listOf("우리는 별의 먼지로 만들어졌다.", "광활한 우주에서 지구는 작은 점 하나다."),
                    selected = setOf(0),
                ),
                onAction = {},
                viewfinder = {},
            )
        }
    }
}

@Preview(name = "권한이 꺼져 있을 때, 어두운 테마")
@Composable
private fun QuoteCaptureScreenBlockedDarkPreview() {
    AppTheme(darkTheme = true) {
        Surface {
            QuoteCaptureScreen(
                state = QuoteCaptureState(permission = PermissionState.PERMANENTLY_DENIED),
                onAction = {},
                viewfinder = {},
            )
        }
    }
}
