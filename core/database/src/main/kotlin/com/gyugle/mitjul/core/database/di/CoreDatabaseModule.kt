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

package com.gyugle.mitjul.core.database.di

import androidx.room3.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.gyugle.mitjul.core.database.MitjulDatabase
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val coreDatabaseModule = module {
    single {
        Room.databaseBuilder<MitjulDatabase>(androidContext(), "mitjul.db")
            .setDriver(AndroidSQLiteDriver())
            .build()
    }
    single { get<MitjulDatabase>().bookDao() }
    single { get<MitjulDatabase>().quoteDao() }
}
