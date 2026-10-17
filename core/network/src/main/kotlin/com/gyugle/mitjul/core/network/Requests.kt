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

package com.gyugle.mitjul.core.network

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.domain.asEmptyResult
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.plugins.resources.delete as deleteResource
import io.ktor.client.plugins.resources.get as getResource
import io.ktor.client.plugins.resources.post as postResource
import io.ktor.client.plugins.resources.put as putResource

suspend inline fun <reified R : Any, reified T> HttpClient.getResult(
    route: R,
    noinline configure: HttpRequestBuilder.() -> Unit = {},
): Result<T, DataError.Network> = safeCall { getResource(route, configure) }

suspend inline fun <reified R : Any, reified B, reified T> HttpClient.postResult(
    route: R,
    body: B,
    noinline configure: HttpRequestBuilder.() -> Unit = {},
): Result<T, DataError.Network> = safeCall {
    postResource(route) {
        contentType(ContentType.Application.Json)
        setBody(body)
        configure()
    }
}

suspend inline fun <reified R : Any, reified B, reified T> HttpClient.putResult(
    route: R,
    body: B,
    noinline configure: HttpRequestBuilder.() -> Unit = {},
): Result<T, DataError.Network> = safeCall {
    putResource(route) {
        contentType(ContentType.Application.Json)
        setBody(body)
        configure()
    }
}

suspend inline fun <reified R : Any> HttpClient.deleteResult(
    route: R,
    noinline configure: HttpRequestBuilder.() -> Unit = {},
): EmptyResult<DataError.Network> =
    safeCall<Unit> { deleteResource(route, configure) }.asEmptyResult()
