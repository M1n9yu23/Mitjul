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

import com.gyugle.mitjul.server.core.database.Db
import com.gyugle.mitjul.server.feature.auth.table.createUsersTable
import com.gyugle.mitjul.server.feature.book.table.createBooksTable
import com.gyugle.mitjul.server.feature.quote.table.createQuotesTable
import io.ktor.server.application.Application
import io.ktor.server.application.log
import kotlinx.coroutines.runBlocking

fun Application.configureDatabase(db: Db) {
    val tables = runBlocking {
        db.createUsersTable()
        db.createBooksTable()
        db.createQuotesTable()
        db.tableNames()
    }
    log.info("데이터베이스 준비 완료: 테이블 ${tables.size}개")
}
