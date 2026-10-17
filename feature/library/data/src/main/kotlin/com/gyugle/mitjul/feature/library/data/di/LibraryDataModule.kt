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

package com.gyugle.mitjul.feature.library.data.di

import com.gyugle.mitjul.core.network.di.ApiBaseUrl
import com.gyugle.mitjul.feature.library.data.datasource.BooksLocalDataSource
import com.gyugle.mitjul.feature.library.data.datasource.BooksRemoteDataSource
import com.gyugle.mitjul.feature.library.data.datasource.CoversLocalDataSource
import com.gyugle.mitjul.feature.library.data.datasource.FileCoversDataSource
import com.gyugle.mitjul.feature.library.data.datasource.KtorBooksDataSource
import com.gyugle.mitjul.feature.library.data.datasource.RoomBooksDataSource
import com.gyugle.mitjul.feature.library.data.repository.OfflineFirstBookRepository
import com.gyugle.mitjul.feature.library.domain.repository.BookRepository
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val libraryDataModule = module {
    single<BooksRemoteDataSource> { KtorBooksDataSource(get(), get(ApiBaseUrl)) }
    singleOf(::RoomBooksDataSource) { bind<BooksLocalDataSource>() }
    singleOf(::FileCoversDataSource) { bind<CoversLocalDataSource>() }
    singleOf(::OfflineFirstBookRepository) { bind<BookRepository>() }
}
