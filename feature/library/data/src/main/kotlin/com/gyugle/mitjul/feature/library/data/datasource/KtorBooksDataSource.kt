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

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.network.getResult
import com.gyugle.mitjul.core.network.postResult
import com.gyugle.mitjul.core.network.putResult
import com.gyugle.mitjul.feature.library.data.api.BookEndpoint
import com.gyugle.mitjul.feature.library.data.api.ImageEndpoint
import com.gyugle.mitjul.feature.library.data.dto.BookDto
import com.gyugle.mitjul.feature.library.data.dto.BookRequestDto
import com.gyugle.mitjul.feature.library.data.dto.ImageDto
import io.ktor.client.HttpClient
import io.ktor.client.plugins.onUpload
import io.ktor.client.plugins.resources.href
import io.ktor.client.plugins.timeout
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.http.ContentType
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders

private const val UPLOAD_TIMEOUT_MILLIS = 120_000L

class KtorBooksDataSource(
    private val client: HttpClient,
    private val baseUrl: String,
) : BooksRemoteDataSource {

    override suspend fun getBooks(after: String?, limit: Int): Result<List<BookDto>, DataError.Network> =
        client.getResult(BookEndpoint(after = after, limit = limit))

    override suspend fun postBook(request: BookRequestDto): Result<BookDto, DataError.Network> =
        client.postResult(BookEndpoint(), request)

    override suspend fun putBook(id: String, request: BookRequestDto): Result<BookDto, DataError.Network> =
        client.putResult(BookEndpoint.ById(id = id), request)

    override suspend fun postImage(bytes: ByteArray, onProgress: (Float) -> Unit): Result<ImageDto, DataError.Network> =
        client.postResult(ImageEndpoint(), coverForm(bytes)) {
            timeout { requestTimeoutMillis = UPLOAD_TIMEOUT_MILLIS }
            onUpload { sent, total -> if (total != null && total > 0) onProgress(sent.toFloat() / total) }
        }

    override fun coverUrl(imageId: String): String =
        baseUrl.trimEnd('/') + client.href(ImageEndpoint.ById(id = imageId))
}

private fun coverForm(bytes: ByteArray) = MultiPartFormDataContent(
    formData {
        append(
            key = "file",
            value = bytes,
            headers = Headers.build {
                append(HttpHeaders.ContentType, ContentType.Image.JPEG.toString())
                append(HttpHeaders.ContentDisposition, "filename=\"cover.jpg\"")
            },
        )
    },
)
