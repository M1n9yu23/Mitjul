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

package com.gyugle.mitjul.server.feature.media.api

import com.gyugle.mitjul.server.feature.media.firstFile
import com.gyugle.mitjul.server.feature.media.service.ImageService
import com.gyugle.mitjul.server.feature.media.service.MAX_IMAGE_BYTES
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.resources.Resource
import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.plugins.ratelimit.RateLimitName
import io.ktor.server.plugins.ratelimit.rateLimit
import io.ktor.server.resources.get
import io.ktor.server.resources.post
import io.ktor.server.response.respond
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.Route
import io.ktor.server.routing.openapi.describe
import io.ktor.utils.io.ExperimentalKtorApi
import java.util.UUID

const val UPLOAD_LIMIT = "upload"

@Resource("/images")
class ImagesResource {
    @Resource("/{id}")
    class ById(val parent: ImagesResource = ImagesResource(), val id: String)
}

@OptIn(ExperimentalKtorApi::class)
fun Route.imageRoutes(service: ImageService) {
    rateLimit(RateLimitName(UPLOAD_LIMIT)) {
        post<ImagesResource> {
            val saved = service.save(call.userId(), call.firstFile(MAX_IMAGE_BYTES))
            call.respond(HttpStatusCode.Created, saved)
        }.describe {
            summary = "사진 한 장을 업로드한다."
        }
    }
    get<ImagesResource.ById> { route ->
        val image = service.open(call.userId(), route.id)
        call.respondBytes(image.bytes, ContentType.parse(image.mime))
    }.describe {
        summary = "내가 업로드한 사진 한 장을 다운로드한다."
    }
}

private fun ApplicationCall.userId(): UUID =
    UUID.fromString(checkNotNull(principal<JWTPrincipal>()?.subject))
