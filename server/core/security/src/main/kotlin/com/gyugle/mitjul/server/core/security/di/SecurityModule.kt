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

package com.gyugle.mitjul.server.core.security.di

import com.gyugle.mitjul.server.core.security.PasswordHasher
import com.gyugle.mitjul.server.core.security.Secret
import com.gyugle.mitjul.server.core.security.TokenService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

fun securityModule(signingKey: Secret) = module {
    singleOf(::PasswordHasher)
    single { TokenService(signingKey) }
}
