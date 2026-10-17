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

package com.gyugle.mitjul.server.core.domain.model

import java.time.Instant
import java.util.UUID

data class Book(
    val id: UUID,
    val title: String,
    val author: String,
    val status: ReadingStatus,
    val totalPages: Int,
    val currentPage: Int,
    val rating: Int?,
    val review: String?,
    val coverImageId: UUID?,
    val createdAt: Instant,
    val finishedAt: Instant?,
)
