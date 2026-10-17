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

package com.gyugle.mitjul.feature.library.presentation.addbook

import android.content.ActivityNotFoundException
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyugle.mitjul.core.designsystem.component.ChoiceChip
import com.gyugle.mitjul.core.designsystem.component.CoverImage
import com.gyugle.mitjul.core.designsystem.component.PrimaryActionButton
import com.gyugle.mitjul.core.designsystem.component.SecondaryActionButton
import com.gyugle.mitjul.core.designsystem.theme.AppTheme
import com.gyugle.mitjul.core.presentation.ObserveAsEvents
import com.gyugle.mitjul.core.presentation.permissions.Permission
import com.gyugle.mitjul.core.presentation.permissions.rememberPermissionController
import com.gyugle.mitjul.feature.library.domain.error.BookInputError
import com.gyugle.mitjul.feature.library.domain.model.ReadingStatus
import com.gyugle.mitjul.feature.library.presentation.R
import com.gyugle.mitjul.feature.library.presentation.toUiText
import java.io.File
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel

private const val CAPTURE_FILE = "camera/cover.jpg"
private const val CAPTURE_AUTHORITY_SUFFIX = ".covers"

@Composable
fun AddBookRoot(
    onBack: () -> Unit,
    viewModel: AddBookViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val pickPhoto = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { viewModel.onAction(AddBookAction.OnCoverPick(it.toString())) }
    }
    val takePhoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { saved ->
        if (saved) viewModel.onAction(AddBookAction.OnCoverPick(context.captureUri().toString()))
    }
    val permissionController = rememberPermissionController()
    val scope = rememberCoroutineScope()
    val resources = LocalResources.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            AddBookEvent.BookAdded -> onBack()
            AddBookEvent.TakePhoto -> try {
                takePhoto.launch(context.captureUri())
            } catch (_: ActivityNotFoundException) {
                viewModel.onAction(AddBookAction.OnCameraUnavailable)
            }
            AddBookEvent.CameraBlocked -> {
                val result = snackbarHostState.showSnackbar(
                    message = resources.getString(R.string.add_book_error_camera_blocked),
                    actionLabel = resources.getString(R.string.add_book_open_settings),
                    duration = SnackbarDuration.Long,
                )
                if (result == SnackbarResult.ActionPerformed) permissionController.openAppSettings()
            }
            is AddBookEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        AddBookScreen(
            state = state,
            onAction = { viewModel.onAction(it) },
            onPickPhotoClick = { pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
            onTakePhotoClick = {
                scope.launch {
                    val result = permissionController.requestPermission(Permission.CAMERA)
                    viewModel.onAction(AddBookAction.OnCameraPermissionResult(result))
                }
            },
            onCancelClick = onBack,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun AddBookScreen(
    state: AddBookState,
    onAction: (AddBookAction) -> Unit,
    onPickPhotoClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
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
        Text(stringResource(R.string.add_book_title), style = MaterialTheme.typography.headlineMedium)
        CoverChoice(
            coverPath = state.coverPath,
            enabled = !state.isSaving && !state.isPreparingCover,
            onPickPhotoClick = onPickPhotoClick,
            onTakePhotoClick = onTakePhotoClick,
            onRemoveClick = { onAction(AddBookAction.OnCoverRemove) },
        )
        OutlinedTextField(
            value = state.title,
            onValueChange = { onAction(AddBookAction.OnTitleChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
            label = { Text(stringResource(R.string.add_book_title_label)) },
            supportingText = { state.titleError?.let { Text(it.asString()) } },
            isError = state.titleError != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.author,
            onValueChange = { onAction(AddBookAction.OnAuthorChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
            label = { Text(stringResource(R.string.add_book_author_label)) },
            supportingText = { state.authorError?.let { Text(it.asString()) } },
            isError = state.authorError != null,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
            singleLine = true,
        )
        StatusChoice(status = state.status, onStatusSelect = { onAction(AddBookAction.OnStatusSelect(it)) })
        OutlinedTextField(
            value = state.totalPages,
            onValueChange = { onAction(AddBookAction.OnTotalPagesChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
            label = { Text(stringResource(R.string.add_book_total_pages_label)) },
            supportingText = { state.totalPagesError?.let { Text(it.asString()) } },
            isError = state.totalPagesError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAction(AddBookAction.OnSaveClick) }),
            singleLine = true,
        )
        state.uploadProgress?.let { progress ->
            LinearProgressIndicator(progress = { progress }, modifier = Modifier.fillMaxWidth())
        }
        PrimaryActionButton(
            text = stringResource(if (state.isSaving) R.string.add_book_saving else R.string.add_book_save),
            onClick = { onAction(AddBookAction.OnSaveClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving && !state.isPreparingCover,
        )
        SecondaryActionButton(
            text = stringResource(R.string.add_book_cancel),
            onClick = onCancelClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSaving,
        )
    }
}

@Composable
private fun CoverChoice(
    coverPath: String?,
    enabled: Boolean,
    onPickPhotoClick: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onRemoveClick: () -> Unit,
) {
    Text(stringResource(R.string.add_book_cover_label), style = MaterialTheme.typography.titleSmall)
    Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.itemGap)) {
        CoverImage(
            model = coverPath?.let { File(it) },
            contentDescription = null,
            modifier = Modifier.weight(1f),
        )
        Column(modifier = Modifier.weight(2f)) {
            SecondaryActionButton(text = stringResource(R.string.add_book_cover_pick), onClick = onPickPhotoClick, enabled = enabled)
            SecondaryActionButton(text = stringResource(R.string.add_book_cover_take), onClick = onTakePhotoClick, enabled = enabled)
            if (coverPath != null) {
                SecondaryActionButton(text = stringResource(R.string.add_book_cover_remove), onClick = onRemoveClick, enabled = enabled)
            }
        }
    }
}

@Composable
private fun StatusChoice(status: ReadingStatus, onStatusSelect: (ReadingStatus) -> Unit) {
    Text(stringResource(R.string.add_book_status_label), style = MaterialTheme.typography.titleSmall)
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.itemGap),
    ) {
        ReadingStatus.entries.forEach { entry ->
            ChoiceChip(entry.toUiText().asString(), selected = status == entry, onClick = { onStatusSelect(entry) })
        }
    }
}

private fun Context.captureUri(): Uri {
    val file = File(cacheDir, CAPTURE_FILE).apply { parentFile?.mkdirs() }
    return FileProvider.getUriForFile(this, packageName + CAPTURE_AUTHORITY_SUFFIX, file)
}

@Preview(name = "입력란 오류, 어두운 테마")
@Composable
private fun AddBookScreenErrorDarkPreview() {
    AppTheme(darkTheme = true) {
        Surface {
            AddBookScreen(
                state = AddBookState(
                    author = "칼 세이건",
                    totalPages = "0",
                    titleError = BookInputError.Title.BLANK.toUiText(),
                    totalPagesError = BookInputError.TotalPages.OUT_OF_RANGE.toUiText(),
                ),
                onAction = {},
                onPickPhotoClick = {},
                onTakePhotoClick = {},
                onCancelClick = {},
            )
        }
    }
}

@Preview(name = "등록하는 중")
@Composable
private fun AddBookScreenSavingPreview() {
    AppTheme {
        Surface {
            AddBookScreen(
                state = AddBookState(
                    title = "코스모스",
                    author = "칼 세이건",
                    status = ReadingStatus.READING,
                    totalPages = "720",
                    isSaving = true,
                    uploadProgress = 0.4f,
                ),
                onAction = {},
                onPickPhotoClick = {},
                onTakePhotoClick = {},
                onCancelClick = {},
            )
        }
    }
}
