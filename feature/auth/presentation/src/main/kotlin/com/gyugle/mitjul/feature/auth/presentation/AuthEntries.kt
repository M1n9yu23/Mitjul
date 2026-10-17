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

package com.gyugle.mitjul.feature.auth.presentation

import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavKey
import com.gyugle.mitjul.core.navigation.Navigator
import com.gyugle.mitjul.feature.auth.navigation.LoginRoute
import com.gyugle.mitjul.feature.auth.navigation.SignupRoute
import com.gyugle.mitjul.feature.auth.presentation.login.LoginRoot
import com.gyugle.mitjul.feature.auth.presentation.signup.SignupRoot

fun EntryProviderScope<NavKey>.authEntries(navigator: Navigator, onLoggedIn: () -> Unit) {
    entry<LoginRoute> {
        LoginRoot(
            onLoginSuccess = onLoggedIn,
            onSignupClick = { navigator.goTo(SignupRoute) },
        )
    }
    entry<SignupRoute> {
        SignupRoot(
            onSignupSuccess = onLoggedIn,
            onLoginClick = { navigator.goBack() },
        )
    }
}
