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

package com.gyugle.mitjul.di

import co.touchlab.kermit.Logger
import coil3.SingletonImageLoader
import com.gyugle.mitjul.CacheCleaner
import com.gyugle.mitjul.MainViewModel
import com.gyugle.mitjul.RepositoryCopyCleaner
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.auth.domain.LocalCopyCleaner
import com.gyugle.mitjul.feature.library.domain.repository.BookRepository
import com.gyugle.mitjul.feature.quote.domain.repository.QuoteRepository
import com.gyugle.mitjul.feature.stats.domain.RecordRefresher
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::MainViewModel)
    singleOf(::RepositoryCopyCleaner) { bind<LocalCopyCleaner>() }
    single<RecordRefresher> {
        val books = get<BookRepository>()
        val quotes = get<QuoteRepository>()
        RecordRefresher {
            when (val booksResult = books.refresh()) {
                is Result.Success -> quotes.refresh()
                is Result.Error -> booksResult
            }
        }
    }
    single<CacheCleaner> {
        val context = androidContext()
        CacheCleaner {
            withContext(Dispatchers.IO) {
                val imageLoader = SingletonImageLoader.get(context)
                imageLoader.memoryCache?.clear()
                val imageFolder = imageLoader.diskCache?.directory?.toFile()
                try {
                    imageLoader.diskCache?.clear()
                } catch (e: IOException) {
                    Logger.w { "표지 캐시를 비우지 못했다: ${e::class.simpleName}" }
                }
                context.cacheDir.listFiles()?.filter { it != imageFolder }?.forEach { it.deleteRecursively() }
            }
        }
    }
}
