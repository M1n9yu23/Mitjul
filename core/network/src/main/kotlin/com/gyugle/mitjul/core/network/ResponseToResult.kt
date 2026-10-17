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

import co.touchlab.kermit.Logger
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive

suspend inline fun <reified T> responseToResult(
    response: HttpResponse,
): Result<T, DataError.Network> = when (val code = response.status.value) {
    in 200..299 -> try {
        Result.Success(response.body<T>())
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Logger.e { "응답 본문을 읽지 못했다: ${e::class.simpleName}" }
        Result.Error(DataError.Network.SERIALIZATION)
    }
    400 -> Result.Error(DataError.Network.BAD_REQUEST)
    401 -> Result.Error(DataError.Network.UNAUTHORIZED)
    403 -> Result.Error(DataError.Network.FORBIDDEN)
    404 -> Result.Error(DataError.Network.NOT_FOUND)
    408 -> Result.Error(DataError.Network.REQUEST_TIMEOUT)
    409 -> Result.Error(DataError.Network.CONFLICT)
    413 -> Result.Error(DataError.Network.PAYLOAD_TOO_LARGE)
    429 -> Result.Error(DataError.Network.TOO_MANY_REQUESTS)
    503 -> Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
    in 500..599 -> Result.Error(DataError.Network.SERVER_ERROR)
    else -> {
        Logger.w { "처리하지 않는 상태 코드: $code" }
        Result.Error(DataError.Network.UNKNOWN)
    }
}
