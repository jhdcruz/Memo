package io.github.jhdcruz.memo.domain.auth

import android.content.Context
import io.github.jhdcruz.memo.data.response.AuthResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

class AuthViewModelPreview : AuthViewModel() {
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

    override suspend fun onSignIn(context: Context) {
        flow {
            emit(true)
        }
    }

    override suspend fun onGoogleSignIn(context: Context) {
        flow {
            emit(true)
        }
    }

    override suspend fun onSignUp(context: Context) {
        flow {
            emit(true)
        }
    }
}
