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

package com.gyugle.mitjul.server.app

import com.gyugle.mitjul.server.core.domain.error.ApiFailure
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.application.log
import io.ktor.server.plugins.BadRequestException
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.response.respond

fun Application.configureErrorHandling() {
    install(StatusPages) {
        status(HttpStatusCode.NotFound) { call, status ->
            call.respond(status, ErrorResponse("not_found", "요청한 경로를 찾을 수 없습니다."))
        }
        status(HttpStatusCode.TooManyRequests) { call, status ->
            call.respond(status, ErrorResponse("too_many_requests", "요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."))
        }
        exception<ApiFailure> { call, cause ->
            call.respondFailure(cause)
        }
        exception<BadRequestException> { call, _ ->
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse("bad_request", "요청 형식이 올바르지 않습니다."),
            )
        }
        exception<Throwable> { call, cause ->
            call.application.log.error("처리하지 못한 예외", cause)
            call.respond(
                HttpStatusCode.InternalServerError,
                ErrorResponse("internal_error", "서버 내부 오류가 발생했습니다."),
            )
        }
    }
}

private suspend fun ApplicationCall.respondFailure(failure: ApiFailure) = when (failure) {
    is ApiFailure.Invalid -> respond(
        HttpStatusCode.BadRequest,
        ErrorResponse(failure.code, "입력값이 올바르지 않습니다.", failure.fields),
    )
    is ApiFailure.Duplicate -> respond(
        HttpStatusCode.Conflict,
        ErrorResponse(failure.code, "이미 사용 중인 값입니다.", mapOf(failure.field to "taken")),
    )
    is ApiFailure.Unauthorized -> respond(
        HttpStatusCode.Unauthorized,
        ErrorResponse(failure.code, "인증에 실패했습니다."),
    )
    is ApiFailure.NotFound -> respond(
        HttpStatusCode.NotFound,
        ErrorResponse(failure.code, "요청한 항목을 찾을 수 없습니다."),
    )
}
