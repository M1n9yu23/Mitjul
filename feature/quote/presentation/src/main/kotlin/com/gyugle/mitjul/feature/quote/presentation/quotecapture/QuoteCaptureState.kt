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

import androidx.compose.runtime.Immutable
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.core.presentation.permissions.PermissionState

@Immutable
data class QuoteCaptureState(
    val permission: PermissionState = PermissionState.NOT_DETERMINED,
    val isRequestingPermission: Boolean = false,
    val cameraError: UiText? = null,
    val isReading: Boolean = false,
    val blocks: List<String> = emptyList(),
    val selected: Set<Int> = emptySet(),
)
