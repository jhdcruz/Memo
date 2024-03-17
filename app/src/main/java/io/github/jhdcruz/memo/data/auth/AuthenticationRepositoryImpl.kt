package io.github.jhdcruz.memo.data.auth

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.credentials.CreatePasswordRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.PasswordCredential
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.NoCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import io.github.jhdcruz.memo.BuildConfig
import io.github.jhdcruz.memo.data.User
import io.github.jhdcruz.memo.data.response.AuthResponse
import io.github.jhdcruz.memo.data.response.FirestoreResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import javax.inject.Inject

class AuthenticationRepositoryImpl @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val crashlytics: FirebaseCrashlytics,
) : AuthenticationRepository {

    /**
     * Manual sign-in option using input fields,
     */
    override suspend fun passwordSignIn(email: String, password: String): AuthResponse {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()

            AuthResponse.Success(result.user!!)
        } catch (e: FirebaseAuthInvalidUserException) {
            Log.i("Authentication", "User not found", e)
            AuthResponse.NotFound("User not found")
        } catch (e: FirebaseAuthInvalidCredentialsException) {
            Log.i("Authentication", "Invalid email or password", e)
            AuthResponse.Invalid("Invalid email or password")
        } catch (e: FirebaseAuthException) {
            Log.e("Authentication", "Failed to sign in with email and password", e)
            AuthResponse.Failure(e)
        }
    }

    override suspend fun googleSignIn(context: Context): AuthResponse {
        val credentialManager = CredentialManager.create(context)

        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(true)
            .setNonce(generateNonce())
            .setServerClientId(BuildConfig.GCP_WEB_CLIENT)
            .build()

        val getCredRequest = GetCredentialRequest(
            listOf(googleIdOption)
        )

        return withContext(Dispatchers.IO) {
            try {
                // get saved credentials from the user's device
                val result = credentialManager.getCredential(
                    context = context,
                    request = getCredRequest
                )

                handleSignIn(result.credential)
            } catch (e: GetCredentialCancellationException) {
                Log.i("Authentication", "User cancelled sign-in", e)
                AuthResponse.Cancelled("User cancelled sign-in")
            } catch (e: NoCredentialException) {
                Log.i("Authentication", "No saved credentials found", e)
                AuthResponse.NotFound("No saved credentials found")
            } catch (e: GetCredentialException) {
                Log.e("Authentication", "Failed to get credential", e)
                AuthResponse.Error(e)
            } catch (e: FirebaseAuthException) {
                Log.e("Authentication", "Failed to sign in with credential", e)
                AuthResponse.Failure(e)
            }
        }
    }

    /**
     * Uses the CredentialManager to sign-in using saved credentials.
     *
     * Also handles Google sign-in/register
     */
    override suspend fun signIn(context: Context): AuthResponse {
        val credentialManager = CredentialManager.create(context)

        // Options for signing-in
        val passwordOption = GetPasswordOption()
        val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setAutoSelectEnabled(true)
            .setNonce(generateNonce())
            .setServerClientId(BuildConfig.GCP_WEB_CLIENT)
            .build()

        val getCredRequest = GetCredentialRequest(
            listOf(passwordOption, googleIdOption)
        )

        return withContext(Dispatchers.IO) {
            try {
                // get saved credentials from the user's device
                val result = credentialManager.getCredential(
                    context = context,
                    request = getCredRequest
                )

                handleSignIn(result.credential)
                AuthResponse.Success(auth.currentUser!!)
            } catch (e: GetCredentialCancellationException) {
                Log.i("Authentication", "User cancelled sign-in", e)
                AuthResponse.Cancelled("User cancelled sign-in")
            } catch (e: NoCredentialException) {
                Log.i("Authentication", "No saved credentials found", e)
                AuthResponse.NotFound("No saved credentials found")
            } catch (e: GetCredentialException) {
                Log.e("Authentication", "Failed to start credential manager", e)
                AuthResponse.Error(e)
            } catch (e: FirebaseAuthException) {
                Log.e("Authentication", "Failed to sign in with credential", e)
                AuthResponse.Failure(e)
            }
        }
    }

    override suspend fun signUp(context: Context, email: String, password: String): AuthResponse {
        val credentialManager = CredentialManager.create(context)

        return try {
            // If the new account was created, the user is also signed in.
            val result = auth.createUserWithEmailAndPassword(email, password).await()

            credentialManager.createCredential(
                context = context,
                request = CreatePasswordRequest(email, password),
            )

            saveUser(result.user!!)

            AuthResponse.Success(result.user)
        } catch (e: FirebaseAuthException) {
            Log.e("Authentication", "Failed to sign up", e)
            AuthResponse.Failure(e)
        } catch (e: CreateCredentialException) {
            Log.e("Authentication", "Failed to save new credential", e)
            AuthResponse.Error(e)
        }
    }

    /**
     * Save new user data to database
     *
     * This should be migrated to 'Functions' instead.
     * https://extensions.dev/extensions/rowy/firestore-user-document
     */
    override suspend fun saveUser(user: FirebaseUser): FirestoreResponse {
        val data = User(
            uid = user.uid,
            email = user.email,
            name = user.displayName,
            photoUrl = user.photoUrl.toString(),
        )

        return try {
            firestore.collection("users")
                .document(data.uid)
                .set(data)
                .await()

            FirestoreResponse.Success()
        } catch (e: FirebaseFirestoreException) {
            Log.e("Firestore", "Failed to save new user data", e)
            FirestoreResponse.Failure(e)
        }
    }

    private suspend fun handleSignIn(credential: Credential): AuthResponse {
        // handle authentication based on selected credential
        return when (credential) {
            is PasswordCredential -> {
                val email = credential.id
                val password = credential.password

                try {
                    val result = auth.signInWithEmailAndPassword(email, password).await()
                    if (result.user != null) {
                        AuthResponse.Success(result.user!!)
                    } else {
                        AuthResponse.Invalid("Invalid email or password")
                    }
                } catch (e: FirebaseAuthException) {
                    Log.e("Authentication", "Failed to sign in with saved password", e)
                    return AuthResponse.Failure(e)
                }
            }

            is CustomCredential -> customSignIn(credential)

            else -> {
                // unrecognized credential type.
                Log.e("Authentication", "Unexpected type of credential used.")
                crashlytics.log("Unexpected type of credential used.")

                return AuthResponse.Invalid("Unexpected type of credential used.")
            }
        }
    }

    /**
     * Handles third-part login services
     */
    private suspend fun customSignIn(credential: Credential): AuthResponse {
        when (credential.type) {
            // Handles Google sign-in
            GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                return try {
                    // get google token credential
                    val googleIdTokenCredential = GoogleIdTokenCredential
                        .createFrom(credential.data).toString()

                    val firebaseCredential =
                        GoogleAuthProvider.getCredential(googleIdTokenCredential, null)

                    val result = auth.signInWithCredential(firebaseCredential).await()
                    AuthResponse.Success(result.user!!)
                } catch (e: FirebaseAuthException) {
                    Log.e("Authentication", "Failed to sign in with Google credential", e)
                    AuthResponse.Failure(e)
                }
            }

            else -> {
                // unrecognized credential type.
                Log.e("Authentication", "Unexpected type of custom credential used.")
                crashlytics.log("Unexpected type of custom credential used.")

                return AuthResponse.Invalid("Unexpected type of custom credential used.")
            }
        }

    }

    private fun generateNonce(): String {
        val nonce = ByteArray(32)
        SecureRandom().nextBytes(nonce)

        return Base64.encodeToString(nonce, Base64.DEFAULT)
    }
}
