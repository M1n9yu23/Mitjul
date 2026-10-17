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

package com.gyugle.mitjul.core.session

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import co.touchlab.kermit.Logger
import java.io.IOException
import java.security.GeneralSecurityException
import kotlin.io.encoding.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class KeystoreTokenStore(private val dataStore: DataStore<Preferences>) : TokenStore {

    private val key = stringPreferencesKey("auth_tokens")
    private val accountKey = stringPreferencesKey("account_id")
    private val cipher = KeystoreCipher()

    override suspend fun tokens(): AuthTokens? {
        val stored = try {
            dataStore.data.first()[key]
        } catch (_: IOException) {
            null
        } ?: return null
        val tokens = withContext(Dispatchers.IO) { decode(stored) }
        if (tokens == null) clear()
        return tokens
    }

    override fun observeHasTokens(): Flow<Boolean> = dataStore.data
        .map { key in it }
        .catch { if (it is IOException) emit(false) else throw it }
        .distinctUntilChanged()

    override suspend fun save(tokens: AuthTokens) {
        val plain = Json.encodeToString(tokens).encodeToByteArray()
        val blob = withContext(Dispatchers.IO) { cipher.encrypt(plain) }
        val account = tokens.accountId()
        dataStore.edit {
            it[key] = Base64.encode(blob)
            if (account != null) it[accountKey] = account
        }
    }

    override suspend fun clear() {
        dataStore.edit { it.remove(key) }
    }

    override suspend fun lastAccountId(): String? = try {
        dataStore.data.first()[accountKey]
    } catch (_: IOException) {
        null
    }

    private fun decode(stored: String): AuthTokens? = try {
        Json.decodeFromString<AuthTokens>(cipher.decrypt(Base64.decode(stored)).decodeToString())
    } catch (e: GeneralSecurityException) {
        Logger.w { "저장된 토큰을 복호화하지 못했다: ${e::class.simpleName}" }
        null
    } catch (e: IllegalArgumentException) {
        Logger.w { "저장된 토큰의 형식이 잘못됐다: ${e::class.simpleName}" }
        null
    }
}
