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

package com.gyugle.mitjul.core.database

import androidx.room3.Room
import androidx.room3.testing.MigrationTestHelper
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.sqlite.execSQL
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.containsExactly
import assertk.assertions.isEmpty
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MitjulDatabaseMigrationTest {

    private val instrumentation = InstrumentationRegistry.getInstrumentation()
    private val context = instrumentation.targetContext
    private val file = context.getDatabasePath("migration-test.db")

    @get:Rule
    val helper = MigrationTestHelper(instrumentation, file, AndroidSQLiteDriver(), MitjulDatabase::class)

    @Before
    fun deleteLeftover() {
        context.deleteDatabase(file.name)
    }

    @Test
    fun open_fileFromVersion1_keepsBooks() = runTest {
        helper.createDatabase(1).use { connection ->
            connection.execSQL(
                "INSERT INTO books (id, title, author, status, totalPages, currentPage, createdAt) " +
                    "VALUES ('book-1', '채식주의자', '한강', 'READING', 247, 30, 1)",
            )
        }

        val database = openCurrent()

        assertThat(database.bookDao().observeRecent(null).first().map { it.id }).containsExactly("book-1")
    }

    @Test
    fun open_fileFromVersion2_keepsQuotesAsSent() = runTest {
        helper.createDatabase(2).use { connection ->
            connection.execSQL(
                "INSERT INTO quotes (id, bookId, text, page, memo, createdAt) " +
                    "VALUES ('quote-1', 'book-1', '밑줄 그은 문장', 12, NULL, 1)",
            )
        }

        val database = openCurrent()

        assertThat(database.quoteDao().observeByBook("book-1").first().map { it.id }).containsExactly("quote-1")
        assertThat(database.quoteDao().pending()).isEmpty()
    }

    private fun openCurrent(): MitjulDatabase =
        Room.databaseBuilder<MitjulDatabase>(context, file.path)
            .setDriver(AndroidSQLiteDriver())
            .build()
            .also { helper.closeWhenFinished(it) }
}
