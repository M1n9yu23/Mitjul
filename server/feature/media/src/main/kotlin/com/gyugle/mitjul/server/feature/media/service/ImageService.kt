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

package com.gyugle.mitjul.server.feature.media.service

import com.gyugle.mitjul.server.core.domain.error.ApiFailure
import com.gyugle.mitjul.server.feature.media.ImageStore
import com.gyugle.mitjul.server.feature.media.StoredImage
import com.gyugle.mitjul.server.feature.media.dto.ImageResponse
import java.util.UUID

const val MAX_IMAGE_BYTES = 2 * 1024 * 1024

class ImageService(private val store: ImageStore) {

    suspend fun save(owner: UUID, bytes: ByteArray?): ImageResponse {
        if (bytes == null) throw ApiFailure.Invalid(mapOf("file" to "blank"))
        if (bytes.size > MAX_IMAGE_BYTES) throw ApiFailure.Invalid(mapOf("file" to "too_large"))
        val image = StoredImage.of(bytes) ?: throw ApiFailure.Invalid(mapOf("file" to "malformed"))
        val id = UUID.randomUUID()
        store.write(owner, id, image.bytes)
        return ImageResponse(id.toString(), image.bytes.size)
    }

    suspend fun open(owner: UUID, id: String): StoredImage {
        val bytes = store.read(owner, id.asId("id")) ?: throw ApiFailure.NotFound()
        return StoredImage.of(bytes) ?: throw ApiFailure.NotFound()
    }
}

private fun String.asId(field: String): UUID =
    runCatching { UUID.fromString(this) }.getOrNull()
        ?: throw ApiFailure.Invalid(mapOf(field to "malformed"))
