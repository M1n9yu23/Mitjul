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

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Transaction
import androidx.room3.Upsert
import com.gyugle.mitjul.core.database.entity.QuoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface QuoteDao {

    @Query("SELECT * FROM quotes WHERE bookId = :bookId ORDER BY createdAt DESC, id DESC")
    fun observeByBook(bookId: String): Flow<List<QuoteEntity>>

    @Query("SELECT * FROM quotes WHERE id = :id")
    fun observeById(id: String): Flow<QuoteEntity?>

    @Query("SELECT createdAt FROM quotes")
    fun observeCreatedTimes(): Flow<List<Long>>

    @Query("SELECT * FROM quotes WHERE isPending = 1 ORDER BY createdAt, id")
    suspend fun pending(): List<QuoteEntity>

    @Query("SELECT COUNT(*) FROM quotes WHERE isPending = 1")
    fun observePendingCount(): Flow<Int>

    @Query("DELETE FROM quotes")
    suspend fun deleteAll()

    @Query("DELETE FROM quotes WHERE isPending = 0")
    suspend fun deleteSent()

    @Upsert
    suspend fun upsertAll(quotes: List<QuoteEntity>)

    @Transaction
    suspend fun replaceSent(quotes: List<QuoteEntity>) {
        deleteSent()
        upsertAll(quotes)
    }
}
