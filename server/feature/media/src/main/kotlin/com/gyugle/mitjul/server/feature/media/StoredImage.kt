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

package com.gyugle.mitjul.server.feature.media

private val JPEG_MAGIC = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
private val PNG_MAGIC = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)

class StoredImage private constructor(val mime: String, val bytes: ByteArray) {

    companion object {
        fun of(bytes: ByteArray): StoredImage? = when {
            bytes.startsWith(JPEG_MAGIC) -> StoredImage("image/jpeg", bytes)
            bytes.startsWith(PNG_MAGIC) -> StoredImage("image/png", bytes)
            else -> null
        }
    }
}

private fun ByteArray.startsWith(magic: ByteArray): Boolean =
    size >= magic.size && magic.indices.all { this[it] == magic[it] }
