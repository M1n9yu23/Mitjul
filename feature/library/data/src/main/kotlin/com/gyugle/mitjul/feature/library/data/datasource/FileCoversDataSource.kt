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

package com.gyugle.mitjul.feature.library.data.datasource

import android.content.Context
import android.graphics.Bitmap
import co.touchlab.kermit.Logger
import coil3.ImageLoader
import coil3.request.CachePolicy
import coil3.request.ImageRequest
import coil3.request.SuccessResult
import coil3.request.allowHardware
import coil3.size.Precision
import coil3.toBitmap
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext

private const val COVER_FOLDER = "covers"
private const val COVER_MAX_EDGE = 1600
private const val JPEG_QUALITY = 85

class FileCoversDataSource(private val context: Context) : CoversLocalDataSource {

    private val imageLoader = ImageLoader(context)

    override suspend fun prepare(uri: String): Result<String, DataError.Local> {
        val request = ImageRequest.Builder(context)
            .data(uri)
            .size(COVER_MAX_EDGE)
            .precision(Precision.INEXACT)
            .memoryCachePolicy(CachePolicy.DISABLED)
            .allowHardware(false)
            .build()
        val result = imageLoader.execute(request)
        if (result !is SuccessResult) {
            Logger.w { "표지로 쓸 사진을 읽지 못했다" }
            return Result.Error(DataError.Local.NOT_FOUND)
        }
        val bytes = withContext(Dispatchers.Default) {
            val out = ByteArrayOutputStream()
            result.image.toBitmap().compress(Bitmap.CompressFormat.JPEG, JPEG_QUALITY, out)
            out.toByteArray()
        }
        return write(bytes)
    }

    override suspend fun read(path: String): Result<ByteArray, DataError.Local> = try {
        Result.Success(withContext(Dispatchers.IO) { File(path).readBytes() })
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Logger.e { "표지 사진 파일을 읽지 못했다: ${e::class.simpleName}" }
        Result.Error(DataError.Local.NOT_FOUND)
    }

    private suspend fun write(bytes: ByteArray): Result<String, DataError.Local> = try {
        withContext(Dispatchers.IO) {
            val folder = File(context.cacheDir, COVER_FOLDER).apply { mkdirs() }
            val file = File(folder, "${UUID.randomUUID()}.jpg")
            file.writeBytes(bytes)
            folder.listFiles()?.filter { it != file }?.forEach { it.delete() }
            Result.Success(file.path)
        }
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Logger.e { "표지 사진 파일을 쓰지 못했다: ${e::class.simpleName}" }
        Result.Error(DataError.Local.UNKNOWN)
    }
}
