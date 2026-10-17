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

package com.gyugle.mitjul.feature.auth.presentation.signup

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
fun SignupRoot(
    onSignupSuccess: () -> Unit,
    onLoginClick: () -> Unit,
    viewModel: SignupViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            SignupEvent.SignupSucceeded -> onSignupSuccess()
            is SignupEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message.resolve(context))
        }
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { innerPadding ->
        SignupScreen(
            state = state,
            onAction = { viewModel.onAction(it) },
            onLoginClick = onLoginClick,
            modifier = Modifier.padding(innerPadding),
        )
    }
}

@Composable
fun SignupScreen(
    state: SignupState,
    onAction: (SignupAction) -> Unit,
    onLoginClick: () -> Unit,
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
        Text(stringResource(R.string.signup_title), style = MaterialTheme.typography.headlineMedium)
        OutlinedTextField(
            value = state.email,
            onValueChange = { onAction(SignupAction.OnEmailChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSigningUp,
            label = { Text(stringResource(R.string.login_email_label)) },
            supportingText = { state.emailError?.let { Text(it.asString()) } },
            isError = state.emailError != null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.password,
            onValueChange = { onAction(SignupAction.OnPasswordChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSigningUp,
            label = { Text(stringResource(R.string.login_password_label)) },
            supportingText = { state.passwordError?.let { Text(it.asString()) } },
            isError = state.passwordError != null,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            singleLine = true,
        )
        OutlinedTextField(
            value = state.passwordConfirm,
            onValueChange = { onAction(SignupAction.OnPasswordConfirmChange(it)) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSigningUp,
            label = { Text(stringResource(R.string.signup_password_confirm_label)) },
            supportingText = { state.passwordConfirmError?.let { Text(it.asString()) } },
            isError = state.passwordConfirmError != null,
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { onAction(SignupAction.OnSignupClick) }),
            singleLine = true,
        )
        PrimaryActionButton(
            text = stringResource(if (state.isSigningUp) R.string.signup_button_progress else R.string.signup_button),
            onClick = { onAction(SignupAction.OnSignupClick) },
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSigningUp,
        )
        SecondaryActionButton(
            text = stringResource(R.string.signup_go_to_login),
            onClick = onLoginClick,
            modifier = Modifier.fillMaxWidth(),
            enabled = !state.isSigningUp,
        )
    }
}

@Preview(name = "비밀번호 확인 오류, 어두운 테마")
@Composable
private fun SignupScreenConfirmErrorDarkPreview() {
    AppTheme(darkTheme = true) {
        Surface {
            SignupScreen(
                state = SignupState(
                    email = "reader@test.invalid",
                    passwordConfirmError = UiText.StringResource(R.string.signup_error_password_mismatch),
                ),
                onAction = {},
                onLoginClick = {},
            )
        }
    }
}

@Preview(name = "가입하는 중")
@Composable
private fun SignupScreenSigningUpPreview() {
    AppTheme {
        Surface {
            SignupScreen(state = SignupState(isSigningUp = true), onAction = {}, onLoginClick = {})
        }
    }
}
