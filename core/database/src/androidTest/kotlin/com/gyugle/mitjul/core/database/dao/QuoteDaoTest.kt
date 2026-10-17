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

package com.gyugle.mitjul.core.database.dao

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import assertk.assertThat
import assertk.assertions.containsExactly
import com.gyugle.mitjul.core.database.MitjulDatabase
import com.gyugle.mitjul.core.database.entity.QuoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuoteDaoTest {

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var database: MitjulDatabase

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder<MitjulDatabase>(context)
            .setDriver(AndroidSQLiteDriver())
            .build()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun replaceSent_pendingQuoteMissingFromServer_keepsIt() = runTest {
        val dao = database.quoteDao()
        dao.upsertAll(listOf(quote("deleted", createdAt = 1, isPending = false), quote("offline", createdAt = 2, isPending = true)))

        dao.replaceSent(listOf(quote("fresh", createdAt = 3, isPending = false)))

        assertThat(dao.observeByBook("book-1").first().map { it.id }).containsExactly("fresh", "offline")
    }

    private fun quote(id: String, createdAt: Long, isPending: Boolean) =
        QuoteEntity(id, "book-1", "밑줄 그은 문장", page = 12, memo = null, createdAt = createdAt, isPending = isPending)
}
