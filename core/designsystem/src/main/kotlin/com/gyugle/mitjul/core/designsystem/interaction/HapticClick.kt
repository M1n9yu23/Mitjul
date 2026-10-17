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

package com.gyugle.mitjul.core.designsystem.interaction

import androidx.compose.runtime.Composable
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback

internal enum class InteractionRole {
    PrimaryAction,
    DestructiveAction,
    SecondaryAction,
    Choice,
    PressableSurface,
}

@Composable
internal fun rememberHapticClick(role: InteractionRole, onClick: () -> Unit): () -> Unit {
    val haptics = LocalHapticFeedback.current
    return {
        val type = when (role) {
            InteractionRole.PrimaryAction -> HapticFeedbackType.Confirm
            InteractionRole.DestructiveAction -> HapticFeedbackType.Reject
            InteractionRole.SecondaryAction -> HapticFeedbackType.VirtualKey
            InteractionRole.Choice -> HapticFeedbackType.SegmentTick
            InteractionRole.PressableSurface -> HapticFeedbackType.ContextClick
        }
        haptics.performHapticFeedback(type)
        onClick()
    }
}
