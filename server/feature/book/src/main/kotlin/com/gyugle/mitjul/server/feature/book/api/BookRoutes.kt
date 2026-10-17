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

package com.gyugle.mitjul.server.feature.book.api

import com.gyugle.mitjul.server.core.domain.model.BookSort
import com.gyugle.mitjul.server.core.domain.model.ReadingStatus
import com.gyugle.mitjul.server.feature.book.dto.BookRequest
import com.gyugle.mitjul.server.feature.book.service.BookService
import com.gyugle.mitjul.server.feature.book.service.DEFAULT_LIMIT
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

@Resource("/books")
class BooksResource(
    val status: ReadingStatus? = null,
    val q: String? = null,
    val sort: BookSort? = null,
    val after: String? = null,
    val limit: Int? = null,
) {
    @Resource("/{id}")
    class ById(val parent: BooksResource = BooksResource(), val id: String)
}

@OptIn(ExperimentalKtorApi::class)
fun Route.bookRoutes(service: BookService) {
    post<BooksResource> {
        val created = service.add(call.userId(), call.receive<BookRequest>())
        call.respond(HttpStatusCode.Created, created)
    }.describe {
        summary = "서재에 책 한 권을 추가한다."
    }
    get<BooksResource> { route ->
        call.respond(
            service.list(
                owner = call.userId(),
                status = route.status,
                keyword = route.q,
                sort = route.sort,
                after = route.after,
                limit = route.limit ?: DEFAULT_LIMIT,
            ),
        )
    }.describe {
        summary = "내 서재를 필터링해 한 페이지씩 돌려준다."
    }
    get<BooksResource.ById> { route ->
        call.respond(service.get(call.userId(), route.id))
    }.describe {
        summary = "내 책 한 권을 돌려준다."
    }
    put<BooksResource.ById> { route ->
        call.respond(service.replace(call.userId(), route.id, call.receive<BookRequest>()))
    }.describe {
        summary = "내 책 한 권을 통째로 교체한다."
    }
    delete<BooksResource.ById> { route ->
        service.remove(call.userId(), route.id)
        call.respond(HttpStatusCode.NoContent)
    }.describe {
        summary = "내 책 한 권을 지운다."
    }
}

private fun ApplicationCall.userId(): UUID =
    UUID.fromString(checkNotNull(principal<JWTPrincipal>()?.subject))
