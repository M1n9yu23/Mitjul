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

package com.gyugle.mitjul.server.core.database

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.jetbrains.exposed.v1.jdbc.Database
import org.jetbrains.exposed.v1.jdbc.SchemaUtils
import org.jetbrains.exposed.v1.jdbc.transactions.transaction

class Db private constructor(
    private val database: Database,
    private val ioDispatcher: CoroutineDispatcher,
) {
    suspend fun <T> query(block: () -> T): T =
        withContext(ioDispatcher) { transaction(database) { block() } }

    suspend fun tableNames(): List<String> = query { SchemaUtils.listTables() }

    companion object {
        fun connect(
            url: String,
            ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
        ): Db = Db(Database.connect(url), ioDispatcher)
    }
}
