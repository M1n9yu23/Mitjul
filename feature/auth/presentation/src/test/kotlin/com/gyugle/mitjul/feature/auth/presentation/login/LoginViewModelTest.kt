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

import assertk.assertThat
import assertk.assertions.isEqualTo
import assertk.assertions.isFalse
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.auth.domain.error.CredentialError
import com.gyugle.mitjul.feature.auth.domain.repository.FakeAuthRepository
import com.gyugle.mitjul.feature.auth.presentation.R
import com.gyugle.mitjul.feature.auth.presentation.toUiText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class LoginViewModelTest {

    @BeforeEach
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    private fun LoginViewModel.logIn(email: String, password: String) {
        onAction(LoginAction.OnEmailChange(email))
        onAction(LoginAction.OnPasswordChange(password))
        onAction(LoginAction.OnLoginClick)
    }

    @Test
    fun onLoginClick_invalidInput_showsFieldErrorsWithoutRequest() {
        val repository = FakeAuthRepository()
        val viewModel = LoginViewModel(repository)

        viewModel.logIn(email = " ", password = "short")

        assertThat(viewModel.state.value.emailError).isEqualTo(CredentialError.Email.BLANK.toUiText())
        assertThat(viewModel.state.value.passwordError).isEqualTo(CredentialError.Password.TOO_SHORT.toUiText())
        assertThat(repository.logInCalls).isEqualTo(0)
    }

    @Test
    fun onLoginClick_unauthorized_showsWrongCredentialsMessage() = runTest {
        val viewModel = LoginViewModel(FakeAuthRepository(Result.Error(DataError.Network.UNAUTHORIZED)))

        viewModel.logIn(email = "reader@test.invalid", password = "a".repeat(12))

        val expected = LoginEvent.ShowSnackbar(UiText.StringResource(R.string.login_error_wrong_credentials))
        assertThat(viewModel.events.first()).isEqualTo(expected)
        assertThat(viewModel.state.value.isLoggingIn).isFalse()
    }

    @Test
    fun onLoginClick_accepted_emitsLoginSucceeded() = runTest {
        val viewModel = LoginViewModel(FakeAuthRepository())

        viewModel.logIn(email = "reader@test.invalid", password = "a".repeat(12))

        assertThat(viewModel.events.first()).isEqualTo(LoginEvent.LoginSucceeded)
    }
}
