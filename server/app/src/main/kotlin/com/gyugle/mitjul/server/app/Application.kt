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

import com.gyugle.mitjul.server.app.di.configureDependencyInjection
import com.gyugle.mitjul.server.core.database.Db
import io.ktor.server.application.Application
import io.ktor.server.application.log
import io.ktor.server.engine.addShutdownHook
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty

fun main() {
    val config = ServerConfig.fromEnvironment()
    val db = Db.connect(config.databaseUrl)
    val server = embeddedServer(Netty, port = config.port, host = config.host) {
        module(config, db)
    }
    server.addShutdownHook {
        server.stop(gracePeriodMillis = 3_000, timeoutMillis = 10_000)
    }
    server.start(wait = true)
}

fun Application.module(config: ServerConfig, db: Db) {
    log.info("설정: $config")
    configureDependencyInjection(config, db)
    configureDatabase(db)
    configureMonitoring()
    configureHardening()
    configureSerialization()
    configureErrorHandling()
    configureSecurity()
    configureRouting()
}
