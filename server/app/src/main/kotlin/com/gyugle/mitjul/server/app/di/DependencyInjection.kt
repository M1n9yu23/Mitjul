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

package com.gyugle.mitjul.server.app.di

import com.gyugle.mitjul.server.app.ServerConfig
import com.gyugle.mitjul.server.core.database.Db
import com.gyugle.mitjul.server.core.security.di.securityModule
import com.gyugle.mitjul.server.feature.auth.di.authModule
import com.gyugle.mitjul.server.feature.book.di.bookModule
import com.gyugle.mitjul.server.feature.media.di.mediaModule
import com.gyugle.mitjul.server.feature.quote.di.quoteModule
import io.ktor.server.application.Application
import io.ktor.server.application.install
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureDependencyInjection(config: ServerConfig, db: Db) {
    install(Koin) {
        slf4jLogger()
        modules(
            module { single { db } },
            securityModule(config.signingKey),
            authModule,
            bookModule,
            mediaModule(config.uploadDir),
            quoteModule,
        )
    }
}
