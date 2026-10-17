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

package com.gyugle.mitjul.feature.auth.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.gyugle.mitjul.core.designsystem.component.PrimaryActionButton
import com.gyugle.mitjul.core.designsystem.component.SecondaryActionButton
import com.gyugle.mitjul.core.designsystem.theme.AppTheme
import com.gyugle.mitjul.core.presentation.ObserveAsEvents
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.auth.presentation.R
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginRoot(
    onLoginSuccess: () -> Unit,
    onSignupClick: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            LoginEvent.LoginSucceeded -> onLoginSuccess()
            is LoginEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        LoginScreen(
            state = state,
            onAction = { viewModel.onAction(it) },
            onSignupClick = onSignupClick,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun LoginScreen(
    state: LoginState,
    onAction: (LoginAction) -> Unit,
    onSignupClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(AppTheme.spacing.screenMargin),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.itemGap),
    ) {
        Text(stringResource(R.string.login_title), style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = state.email,
            onValueChange = { onAction(LoginAction.OnEmailChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoggingIn,
            label = { Text(stringResource(R.string.login_email_label)) },
            supportingText = { state.emailError?.let { Text(it.asString()) } },
            isError = state.emailError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = { onAction(LoginAction.OnPasswordChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoggingIn,
            label = { Text(stringResource(R.string.login_password_label)) },
            supportingText = { state.passwordError?.let { Text(it.asString()) } },
            isError = state.passwordError != null,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAction(LoginAction.OnLoginClick) }),
            singleLine = true,
        )
        PrimaryActionButton(
            text = stringResource(if (state.isLoggingIn) R.string.login_button_progress else R.string.login_button),
            onClick = { onAction(LoginAction.OnLoginClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoggingIn,
        )
        SecondaryActionButton(
            text = stringResource(R.string.login_go_to_signup),
            onClick = onSignupClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isLoggingIn,
        )
    }
}

@Preview(name = "입력 오류")
@Composable
private fun LoginScreenErrorPreview() {
    AppTheme {
        Surface {
            LoginScreen(
                state = LoginState(email = "reader", emailError = UiText.StringResource(R.string.error_email_malformed)),
                onAction = {},
                onSignupClick = {},
            )
        }
    }
}

@Preview(name = "로그인하는 중, 어두운 테마")
@Composable
private fun LoginScreenLoggingInDarkPreview() {
    AppTheme(darkTheme = true) {
        Surface {
            LoginScreen(
                state = LoginState(email = "reader@test.invalid", isLoggingIn = true),
                onAction = {},
                onSignupClick = {},
            )
        }
    }
}
