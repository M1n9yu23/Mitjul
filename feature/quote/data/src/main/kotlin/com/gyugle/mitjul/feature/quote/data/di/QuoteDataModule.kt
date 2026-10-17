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

package com.gyugle.mitjul.feature.quote.data.di

import com.gyugle.mitjul.feature.quote.data.QuoteSyncWorker
import com.gyugle.mitjul.feature.quote.data.WorkManagerQuoteSyncScheduler
import com.gyugle.mitjul.feature.quote.data.datasource.KtorQuotesDataSource
import com.gyugle.mitjul.feature.quote.data.datasource.QuotesLocalDataSource
import com.gyugle.mitjul.feature.quote.data.datasource.QuotesRemoteDataSource
import com.gyugle.mitjul.feature.quote.data.datasource.RoomQuotesDataSource
import com.gyugle.mitjul.feature.quote.data.repository.OfflineFirstQuoteRepository
import com.gyugle.mitjul.feature.quote.domain.QuoteSyncScheduler
import com.gyugle.mitjul.feature.quote.domain.repository.QuoteRepository
import org.koin.androidx.workmanager.dsl.workerOf
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val quoteDataModule = module {
    singleOf(::KtorQuotesDataSource) { bind<QuotesRemoteDataSource>() }
    singleOf(::RoomQuotesDataSource) { bind<QuotesLocalDataSource>() }
    singleOf(::OfflineFirstQuoteRepository) { bind<QuoteRepository>() }
    singleOf(::WorkManagerQuoteSyncScheduler) { bind<QuoteSyncScheduler>() }
    workerOf(::QuoteSyncWorker)
}
