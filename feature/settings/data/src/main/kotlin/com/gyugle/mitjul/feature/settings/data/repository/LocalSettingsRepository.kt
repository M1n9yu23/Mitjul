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

package com.gyugle.mitjul.feature.settings.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.gyugle.mitjul.core.database.dao.QuoteDao
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.EmptyResult
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.feature.settings.data.WorkManagerReminderScheduler
import com.gyugle.mitjul.feature.settings.domain.model.Reminder
import com.gyugle.mitjul.feature.settings.domain.repository.SettingsRepository
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalTime

private val REMINDER_ON = booleanPreferencesKey("reminder_on")
private val REMINDER_MINUTE = intPreferencesKey("reminder_minute_of_day")
private const val DEFAULT_MINUTE = 21 * 60

class LocalSettingsRepository(
    private val quoteDao: QuoteDao,
    private val preferences: DataStore<Preferences>,
    private val reminderScheduler: WorkManagerReminderScheduler,
) : SettingsRepository {

    override fun observePendingQuoteCount(): Flow<Int> = quoteDao.observePendingCount()

    override fun observeReminder(): Flow<Reminder> = preferences.data
        .catch { if (it is IOException) emit(emptyPreferences()) else throw it }
        .map { stored ->
            val minute = stored[REMINDER_MINUTE] ?: DEFAULT_MINUTE
            Reminder(isOn = stored[REMINDER_ON] ?: false, time = LocalTime(minute / 60, minute % 60))
        }

    override suspend fun setReminder(reminder: Reminder): EmptyResult<DataError.Local> {
        try {
            preferences.edit {
                it[REMINDER_ON] = reminder.isOn
                it[REMINDER_MINUTE] = reminder.time.hour * 60 + reminder.time.minute
            }
        } catch (_: IOException) {
            return Result.Error(DataError.Local.UNKNOWN)
        }
        if (reminder.isOn) reminderScheduler.schedule(reminder.time) else reminderScheduler.cancel()
        return Result.Success(Unit)
    }
}
