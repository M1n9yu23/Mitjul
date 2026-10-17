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

package com.gyugle.mitjul

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import com.gyugle.mitjul.core.navigation.Navigator
import com.gyugle.mitjul.core.navigation.SharedNavTransitions
import com.gyugle.mitjul.core.navigation.rememberNavigator
import com.gyugle.mitjul.core.presentation.ObserveAsEvents
import com.gyugle.mitjul.feature.auth.navigation.AuthRoute
import com.gyugle.mitjul.feature.auth.navigation.LoginRoute
import com.gyugle.mitjul.feature.auth.navigation.authNavSerializers
import com.gyugle.mitjul.feature.auth.presentation.authEntries
import com.gyugle.mitjul.feature.library.navigation.BookListRoute
import com.gyugle.mitjul.feature.library.navigation.libraryNavSerializers
import com.gyugle.mitjul.feature.library.presentation.libraryEntries
import com.gyugle.mitjul.feature.quote.navigation.quoteNavSerializers
import com.gyugle.mitjul.feature.quote.presentation.quoteEntries
import com.gyugle.mitjul.feature.settings.navigation.settingsNavSerializers
import com.gyugle.mitjul.feature.settings.presentation.settingsEntries
import com.gyugle.mitjul.feature.stats.navigation.statsNavSerializers
import com.gyugle.mitjul.feature.stats.presentation.statsEntries
import com.mikepenz.aboutlibraries.ui.compose.android.produceLibraries
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import kotlinx.serialization.modules.plus

private val navConfiguration = SavedStateConfiguration {
    serializersModule = appNavSerializers + authNavSerializers + libraryNavSerializers + quoteNavSerializers + settingsNavSerializers + statsNavSerializers
}

@Composable
fun MitjulApp(viewModel: MainViewModel) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    if (state.isCheckingSession) return

    val navigator = rememberNavigator(
        startKey = if (state.isLoggedIn) BookListRoute else LoginRoute,
        configuration = navConfiguration,
    )
    LaunchedEffect(state.isLoggedIn) {
        if (!state.isLoggedIn && navigator.backStack.last() !is AuthRoute) {
            navigator.replaceAll(LoginRoute)
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is MainEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SharedNavTransitions {
            AppNavDisplay(navigator = navigator, onLogOutClick = { viewModel.logOut() })
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter).navigationBarsPadding(),
        )
    }
}

@Composable
private fun AppNavDisplay(navigator: Navigator, onLogOutClick: () -> Unit) {
    NavDisplay(
        backStack = navigator.backStack,
        onBack = { navigator.goBack() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            authEntries(navigator, onLoggedIn = { navigator.replaceAll(BookListRoute) })
            libraryEntries(navigator)
            quoteEntries(navigator)
            settingsEntries(
                onLicensesClick = { navigator.goTo(LicensesRoute) },
                onLogOut = onLogOutClick,
            )
            statsEntries(navigator)
            entry<LicensesRoute> {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val libraries by produceLibraries(R.raw.aboutlibraries)
                    LibrariesContainer(libraries = libraries, modifier = Modifier.padding(innerPadding))
                }
            }
        },
    )
}
