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

package com.gyugle.mitjul.feature.quote.data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.onFailure
import com.gyugle.mitjul.feature.quote.domain.repository.QuoteRepository

private const val MAX_ATTEMPTS = 5

private val RETRYABLE = setOf(
    DataError.Network.NO_INTERNET,
    DataError.Network.REQUEST_TIMEOUT,
    DataError.Network.TOO_MANY_REQUESTS,
    DataError.Network.SERVER_ERROR,
    DataError.Network.SERVICE_UNAVAILABLE,
)

class QuoteSyncWorker(
    context: Context,
    params: WorkerParameters,
    private val quoteRepository: QuoteRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): WorkerResult {
        if (runAttemptCount >= MAX_ATTEMPTS) return WorkerResult.failure()
        quoteRepository.sendPending().onFailure { error ->
            return if (error in RETRYABLE) WorkerResult.retry() else WorkerResult.failure()
        }
        return WorkerResult.success()
    }
}
