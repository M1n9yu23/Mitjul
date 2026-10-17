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

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gyugle.mitjul.core.domain.DataError
import com.gyugle.mitjul.core.domain.Result
import com.gyugle.mitjul.core.domain.onFailure
import com.gyugle.mitjul.core.presentation.UiText
import com.gyugle.mitjul.core.presentation.toUiText
import com.gyugle.mitjul.feature.auth.domain.CredentialValidator
import com.gyugle.mitjul.feature.auth.domain.repository.AuthRepository
import com.gyugle.mitjul.feature.auth.presentation.R
import com.gyugle.mitjul.feature.auth.presentation.toUiText
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SignupViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(SignupState())
    val state: StateFlow<SignupState> = _state.asStateFlow()

    private val _events = Channel<SignupEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: SignupAction) {
        when (action) {
            is SignupAction.OnEmailChange ->
                _state.update { it.copy(email = action.email, emailError = null) }
            is SignupAction.OnPasswordChange ->
                _state.update { it.copy(password = action.password, passwordError = null) }
            is SignupAction.OnPasswordConfirmChange ->
                _state.update { it.copy(passwordConfirm = action.passwordConfirm, passwordConfirmError = null) }
            SignupAction.OnSignupClick -> signUp()
        }
    }

    private fun signUp() {
        val current = _state.value
        if (current.isSigningUp) return

        var emailError: UiText? = null
        var passwordError: UiText? = null
        CredentialValidator.validateEmail(current.email).onFailure { emailError = it.toUiText() }
        CredentialValidator.validatePassword(current.password).onFailure { passwordError = it.toUiText() }
        val passwordConfirmError = UiText.StringResource(R.string.signup_error_password_mismatch)
            .takeIf { current.passwordConfirm != current.password }
        if (emailError != null || passwordError != null || passwordConfirmError != null) {
            _state.update {
                it.copy(emailError = emailError, passwordError = passwordError, passwordConfirmError = passwordConfirmError)
            }
            return
        }

        _state.update { it.copy(isSigningUp = true) }
        viewModelScope.launch {
            val result = when (val created = authRepository.signUp(current.email, current.password)) {
                is Result.Success -> authRepository.logIn(current.email, current.password)
                is Result.Error -> created
            }
            _state.update { it.copy(isSigningUp = false) }
            when (result) {
                is Result.Success -> _events.send(SignupEvent.SignupSucceeded)
                is Result.Error -> _events.send(SignupEvent.ShowSnackbar(result.error.toSignupMessage()))
            }
        }
    }

    private fun DataError.toSignupMessage(): UiText =
        if (this == DataError.Network.CONFLICT) {
            UiText.StringResource(R.string.signup_error_email_taken)
        } else {
            toUiText()
        }
}
