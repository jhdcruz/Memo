package io.github.jhdcruz.memo.ui.login

import android.content.Context
import io.github.jhdcruz.memo.domain.response.AuthResponseUseCase
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class LoginViewModelPreview : LoginViewModel() {
    private val _email = MutableStateFlow("")
    override val email: Flow<String> = _email

    private val _password = MutableStateFlow("")
    override val password: Flow<String> = _password

    private val _status = MutableStateFlow("")
    override val status: Flow<String> = _status

    override fun onEmailChange(email: String) {
        _email.value = email
    }

    override fun onPasswordChange(password: String) {
        _password.value = password
    }

    override suspend fun onSignIn(context: Context): AuthResponseUseCase {
        return AuthResponseUseCase.Invalid("View model preview is used")
    }

    override suspend fun onGoogleSignIn(context: Context): AuthResponseUseCase {
        return AuthResponseUseCase.Invalid("View model preview is used")
    }

    override suspend fun onSignUp(context: Context): AuthResponseUseCase {
        return AuthResponseUseCase.Invalid("View model preview is used")
    }
}
