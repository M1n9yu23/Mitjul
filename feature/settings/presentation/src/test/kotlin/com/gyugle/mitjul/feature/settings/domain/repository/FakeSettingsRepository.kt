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

package com.gyugle.mitjul.feature.settings.domain.repository

import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.settings.domain.model.Reminder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class FakeSettingsRepository : SettingsRepository {

    val saved = mutableListOf<Reminder>()

    override fun observePendingQuoteCount(): Flow<Int> = emptyFlow()

    override fun observeReminder(): Flow<Reminder> = emptyFlow()

    override suspend fun setReminder(reminder: Reminder): EmptyResult<DataError.Local> {
        saved += reminder
        return Result.Success(Unit)
    }
}
