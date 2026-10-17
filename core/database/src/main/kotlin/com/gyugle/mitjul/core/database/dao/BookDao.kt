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
import com.gyugle.mitjul.core.database.entity.BookEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookDao {

    @Query(
        "SELECT * FROM books WHERE :status IS NULL OR status = :status " +
            "ORDER BY createdAt DESC, id DESC",
    )
    fun observeRecent(status: String?): Flow<List<BookEntity>>

    @Query(
        "SELECT * FROM books WHERE :status IS NULL OR status = :status " +
            "ORDER BY title ASC, id ASC",
    )
    fun observeByTitle(status: String?): Flow<List<BookEntity>>

    @Query("SELECT * FROM books WHERE id = :id")
    fun observeById(id: String): Flow<BookEntity?>

    @Query("SELECT * FROM books WHERE finishedAt IS NOT NULL")
    fun observeFinished(): Flow<List<BookEntity>>

    @Query("DELETE FROM books")
    suspend fun deleteAll()

    @Upsert
    suspend fun upsertAll(books: List<BookEntity>)

    @Transaction
    suspend fun replaceAll(books: List<BookEntity>) {
        deleteAll()
        upsertAll(books)
    }
}
