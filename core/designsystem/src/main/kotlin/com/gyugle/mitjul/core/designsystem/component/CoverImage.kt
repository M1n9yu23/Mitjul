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

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.gyugle.mitjul.core.designsystem.icon.AppIcons
import com.gyugle.mitjul.core.designsystem.theme.AppTheme

private const val COVER_ASPECT_RATIO = 2f / 3f

object CoverImageDefaults {
    val ThumbnailWidth = 48.dp
}

@Composable
fun CoverImage(
    model: Any?,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(COVER_ASPECT_RATIO)
            .clip(AppTheme.shapes.small)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center,
    ) {
        Icon(AppIcons.BookClosed, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = contentDescription,
                modifier = Modifier.matchParentSize(),
                contentScale = ContentScale.Crop,
            )
        }
    }
}

@Preview(name = "표지 없음", widthDp = 120)
@Composable
private fun CoverImageEmptyPreview() {
    AppTheme { CoverImage(model = null, contentDescription = null) }
}

@Preview(name = "표지 없음 어두운 테마", widthDp = 120)
@Composable
private fun CoverImageEmptyDarkPreview() {
    AppTheme(darkTheme = true) { CoverImage(model = null, contentDescription = null) }
}
