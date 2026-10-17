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

package com.gyugle.mitjul.core.session.di

import com.gyugle.mitjul.core.datastore.PreferencesStoreFactory
import com.gyugle.mitjul.core.network.AuthProvider
import com.gyugle.mitjul.core.network.di.PlainHttpClient
import com.gyugle.mitjul.core.session.KeystoreTokenStore
import com.gyugle.mitjul.core.session.SessionAuthProvider
import com.gyugle.mitjul.core.session.TokenStore
import org.koin.dsl.module

val coreSessionModule = module {
    single<TokenStore> { KeystoreTokenStore(get<PreferencesStoreFactory>().create("session")) }
    single<AuthProvider> { SessionAuthProvider(get(PlainHttpClient), get()) }
}
