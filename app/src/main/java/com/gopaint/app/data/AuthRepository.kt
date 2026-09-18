package com.gopaint.app.data

import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.StateFlow

class AuthRepository {

    private val auth = SupabaseClientProvider.client.auth

    val sessionStatus: StateFlow<SessionStatus> = auth.sessionStatus

    suspend fun signUpWithEmail(email: String, password: String, username: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
            data = kotlinx.serialization.json.buildJsonObject {
                put("username", kotlinx.serialization.json.JsonPrimitive(username))
            }
        }
    }

    suspend fun signInWithEmail(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    /** Opens the system browser / custom tab for Google OAuth; result comes back via the
     *  gopaint://login-callback deep link declared in AndroidManifest.xml. */
    suspend fun signInWithGoogle() {
        auth.signInWith(Google)
    }

    suspend fun signOut() {
        auth.signOut()
    }

    fun isLoggedIn(): Boolean = auth.currentUserOrNull() != null
}
