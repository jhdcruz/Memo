package io.github.jhdcruz.memo.domain.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import io.github.jhdcruz.memo.data.response.AuthResponse
import kotlinx.coroutines.flow.Flow

abstract class AuthViewModel : ViewModel() {
    abstract val email: Flow<String>
    abstract val password: Flow<String>
    abstract val status: Flow<String>

    abstract fun onEmailChange(email: String)
    abstract fun onPasswordChange(password: String)

    abstract suspend fun onSignIn(context: Context): AuthResponse
    abstract suspend fun onGoogleSignIn(context: Context): AuthResponse
    abstract suspend fun onSignUp(context: Context): AuthResponse
}
