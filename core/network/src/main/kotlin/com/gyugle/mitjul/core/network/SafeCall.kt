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
import io.ktor.client.network.sockets.ConnectTimeoutException
import io.ktor.client.network.sockets.SocketTimeoutException
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.statement.HttpResponse
import io.ktor.resources.ResourceSerializationException
import io.ktor.util.network.UnresolvedAddressException
import java.net.ConnectException
import java.net.UnknownHostException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.SerializationException

suspend inline fun <reified T> safeCall(execute: () -> HttpResponse): Result<T, DataError.Network> {
    val response = try {
        execute()
    } catch (e: ResourceSerializationException) {
        throw IllegalStateException("리소스 클래스가 자기 경로와 맞지 않는다", e)
    } catch (_: UnresolvedAddressException) {
        Logger.w { "인터넷에 연결하지 못했다" }
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (_: UnknownHostException) {
        Logger.w { "호스트 이름을 IP 주소로 바꾸지 못했다" }
        return Result.Error(DataError.Network.NO_INTERNET)
    } catch (_: ConnectTimeoutException) {
        Logger.w { "서버 연결 시간이 초과됐다" }
        return Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (_: SocketTimeoutException) {
        Logger.w { "데이터를 주고받다가 시간이 초과됐다" }
        return Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (_: ConnectException) {
        Logger.w { "서버가 연결을 거부했다" }
        return Result.Error(DataError.Network.SERVICE_UNAVAILABLE)
    } catch (_: HttpRequestTimeoutException) {
        Logger.w { "요청 시간이 초과됐다" }
        return Result.Error(DataError.Network.REQUEST_TIMEOUT)
    } catch (_: SerializationException) {
        Logger.e { "요청 본문을 JSON으로 직렬화하지 못했다" }
        return Result.Error(DataError.Network.SERIALIZATION)
    } catch (e: Exception) {
        currentCoroutineContext().ensureActive()
        Logger.e { "요청이 실패했다: ${e::class.simpleName}" }
        return Result.Error(DataError.Network.UNKNOWN)
    }
    return responseToResult(response)
}
