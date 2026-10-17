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

package com.gyugle.mitjul.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.gyugle.mitjul.core.designsystem.icon.AppIcons
import com.gyugle.mitjul.core.designsystem.theme.AppTheme

@Composable
private fun StateMessage(
    icon: Painter,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: @Composable () -> Unit,
) {
    val subtle = MaterialTheme.colorScheme.onSurfaceVariant
    Column(
        modifier = modifier.fillMaxWidth().padding(AppTheme.spacing.screenMargin),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.itemGap),
    ) {
        Icon(painter = icon, contentDescription = null, tint = subtle)
        Text(title, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        Text(description, style = MaterialTheme.typography.bodyMedium, color = subtle, textAlign = TextAlign.Center)
        action()
    }
}

@Composable
fun EmptyState(
    title: String,
    description: String,
    actionText: String,
    onAction: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    StateMessage(AppIcons.BookClosed, title, description, modifier) {
        PrimaryActionButton(text = actionText, onClick = onAction, enabled = enabled)
    }
}

@Preview(name = "빈 상태 기본")
@Composable
private fun EmptyStatePreview() {
    AppTheme { EmptyState("아직 옮겨 적은 문장이 없다", "책을 펴고 마음에 남은 문장을 한 줄 적어 보자", "문장 적기", {}) }
}

@Preview(name = "빈 상태 좁은 화면", widthDp = 240)
@Composable
private fun EmptyStateNarrowPreview() {
    AppTheme { EmptyState("아직 옮겨 적은 문장이 없다", "책을 펴고 마음에 남은 문장을 한 줄 적어 보자", "문장 적기", {}) }
}

@Preview(name = "빈 상태 어두운 테마")
@Composable
private fun EmptyStateDarkPreview() {
    AppTheme(darkTheme = true) { EmptyState("아직 옮겨 적은 문장이 없다", "책을 펴고 마음에 남은 문장을 한 줄 적어 보자", "문장 적기", {}) }
}

@Composable
fun ErrorState(
    title: String,
    description: String,
    retryText: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StateMessage(AppIcons.AlertTriangle, title, description, modifier) {
        PrimaryActionButton(text = retryText, onClick = onRetry)
    }
}

@Preview(name = "실패 상태 기본")
@Composable
private fun ErrorStatePreview() {
    AppTheme { ErrorState("문장을 불러오지 못했다", "인터넷 연결을 확인하고 다시 시도해 보자", "다시 시도", {}) }
}

@Preview(name = "실패 상태 어두운 테마")
@Composable
private fun ErrorStateDarkPreview() {
    AppTheme(darkTheme = true) { ErrorState("문장을 불러오지 못했다", "인터넷 연결을 확인하고 다시 시도해 보자", "다시 시도", {}) }
}
