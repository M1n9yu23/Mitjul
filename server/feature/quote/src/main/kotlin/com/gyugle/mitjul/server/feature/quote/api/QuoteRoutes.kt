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

package com.gyugle.mitjul.server.feature.quote.api

import com.gyugle.mitjul.server.feature.quote.dto.QuoteRequest
import com.gyugle.mitjul.server.feature.quote.service.DEFAULT_LIMIT
import com.gyugle.mitjul.server.feature.quote.service.QuoteService
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.resources.delete
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.resources.put
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import java.util.UUID

@Resource("/quotes")
class QuotesResource(
    val bookId: String? = null,
    val after: String? = null,
    val limit: Int? = null,
) {
    @Resource("/{id}")
    class ById(val parent: QuotesResource = QuotesResource(), val id: String)
}

@OptIn(ExperimentalKtorApi::class)
fun Route.quoteRoutes(service: QuoteService) {
    post<QuotesResource> {
        val created = service.add(call.userId(), call.receive<QuoteRequest>())
        call.respond(HttpStatusCode.Created, created)
    }.describe {
        summary = "책에 문장 하나를 남긴다."
    }
    get<QuotesResource> { route ->
        call.respond(
            service.list(
                owner = call.userId(),
                book = route.bookId,
                after = route.after,
                limit = route.limit ?: DEFAULT_LIMIT,
            ),
        )
    }.describe {
        summary = "내 문장을 한 페이지씩 돌려준다."
    }
    put<QuotesResource.ById> { route ->
        call.respond(service.replace(call.userId(), route.id, call.receive<QuoteRequest>()))
    }.describe {
        summary = "내 문장 하나를 통째로 교체한다."
    }
    delete<QuotesResource.ById> { route ->
        service.remove(call.userId(), route.id)
        call.respond(HttpStatusCode.NoContent)
    }.describe {
        summary = "내 문장 하나를 지운다."
    }
}

private fun ApplicationCall.userId(): UUID =
    UUID.fromString(checkNotNull(principal<JWTPrincipal>()?.subject))
