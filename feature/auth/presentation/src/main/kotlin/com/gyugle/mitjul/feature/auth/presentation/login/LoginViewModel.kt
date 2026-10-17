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

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state.asStateFlow()

    private val _events = Channel<LoginEvent>()
    val events = _events.receiveAsFlow()

    fun onAction(action: LoginAction) {
        when (action) {
            is LoginAction.OnEmailChange ->
                _state.update { it.copy(email = action.email, emailError = null) }
            is LoginAction.OnPasswordChange ->
                _state.update { it.copy(password = action.password, passwordError = null) }
            LoginAction.OnLoginClick -> logIn()
        }
    }

    private fun logIn() {
        val current = _state.value
        if (current.isLoggingIn) return

        var emailError: UiText? = null
        var passwordError: UiText? = null
        CredentialValidator.validateEmail(current.email).onFailure { emailError = it.toUiText() }
        CredentialValidator.validatePassword(current.password).onFailure { passwordError = it.toUiText() }
        if (emailError != null || passwordError != null) {
            _state.update { it.copy(emailError = emailError, passwordError = passwordError) }
            return
        }

        _state.update { it.copy(isLoggingIn = true) }
        viewModelScope.launch {
            val result = authRepository.logIn(current.email, current.password)
            _state.update { it.copy(isLoggingIn = false) }
            when (result) {
                is Result.Success -> _events.send(LoginEvent.LoginSucceeded)
                is Result.Error -> _events.send(LoginEvent.ShowSnackbar(result.error.toLoginMessage()))
            }
        }
    }

    private fun DataError.toLoginMessage(): UiText =
        if (this == DataError.Network.UNAUTHORIZED) {
            UiText.StringResource(R.string.login_error_wrong_credentials)
        } else {
            toUiText()
        }
}
