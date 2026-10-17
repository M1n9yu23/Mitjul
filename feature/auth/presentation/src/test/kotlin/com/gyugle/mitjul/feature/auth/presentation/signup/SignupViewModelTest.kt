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

import assertk.assertThat
import assertk.assertions.isEqualTo
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.feature.auth.domain.repository.FakeAuthRepository
import com.gyugle.mitjul.feature.auth.presentation.R
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
class SignupViewModelTest {

    @BeforeEach
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @AfterEach
    fun tearDown() = Dispatchers.resetMain()

    private fun SignupViewModel.signUp(password: String, passwordConfirm: String) {
        onAction(SignupAction.OnEmailChange("reader@test.invalid"))
        onAction(SignupAction.OnPasswordChange(password))
        onAction(SignupAction.OnPasswordConfirmChange(passwordConfirm))
        onAction(SignupAction.OnSignupClick)
    }

    @Test
    fun onSignupClick_confirmationDiffers_showsErrorWithoutRequest() {
        val repository = FakeAuthRepository()
        val viewModel = SignupViewModel(repository)

        viewModel.signUp(password = "a".repeat(12), passwordConfirm = "b".repeat(12))

        val expected = UiText.StringResource(R.string.signup_error_password_mismatch)
        assertThat(viewModel.state.value.passwordConfirmError).isEqualTo(expected)
        assertThat(repository.signUpCalls).isEqualTo(0)
    }

    @Test
    fun onSignupClick_emailTaken_showsEmailTakenMessage() = runTest {
        val repository = FakeAuthRepository(signUpResult = Result.Error(DataError.Network.CONFLICT))
        val viewModel = SignupViewModel(repository)

        viewModel.signUp(password = "a".repeat(12), passwordConfirm = "a".repeat(12))

        val expected = SignupEvent.ShowSnackbar(UiText.StringResource(R.string.signup_error_email_taken))
        assertThat(viewModel.events.first()).isEqualTo(expected)
        assertThat(repository.logInCalls).isEqualTo(0)
    }

    @Test
    fun onSignupClick_created_logsInAndEmitsSignupSucceeded() = runTest {
        val repository = FakeAuthRepository()
        val viewModel = SignupViewModel(repository)

        viewModel.signUp(password = "a".repeat(12), passwordConfirm = "a".repeat(12))

        assertThat(viewModel.events.first()).isEqualTo(SignupEvent.SignupSucceeded)
        assertThat(repository.logInCalls).isEqualTo(1)
    }
}
