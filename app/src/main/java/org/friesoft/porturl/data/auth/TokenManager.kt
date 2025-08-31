package org.friesoft.porturl.data.auth

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import net.openid.appauth.AuthState
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Manages the secure persistence of the authentication state.
 *
 * This class uses SharedPreferences to store the AuthState object as a JSON string.
 * This allows the user's session (including access and refresh tokens) to be
 * maintained even after the app is closed.
 *
 * @property context The application context, provided by Hilt.
 */
@Singleton
class TokenManager @Inject constructor(@ApplicationContext context: Context) {

    // A private instance of SharedPreferences, specifically for auth data.
    private val prefs = context.getSharedPreferences("auth_state", Context.MODE_PRIVATE)

    companion object {
        // The key used to store the AuthState JSON string in SharedPreferences.
        private const val KEY_AUTH_STATE = "auth_state_json"
    }

    /**
     * Saves the provided AuthState to SharedPreferences.
     *
     * The AuthState object is first serialized into a JSON string.
     * @param authState The AuthState to persist.
     */
    fun saveAuthState(authState: AuthState) {
        prefs.edit { putString(KEY_AUTH_STATE, authState.jsonSerializeString()) }
    }

    /**
     * Retrieves the AuthState from SharedPreferences.
     *
     * It reads the stored JSON string and deserializes it back into an AuthState object.
     * If no state is found or if there's a parsing error, it returns a fresh,
     * unauthenticated AuthState.
     *
     * @return The deserialized AuthState, or a new AuthState if none was saved.
     */
    fun getAuthState(): AuthState {
        val stateJson = prefs.getString(KEY_AUTH_STATE, null)
        return try {
            if (stateJson != null) {
                AuthState.jsonDeserialize(stateJson)
            } else {
                AuthState()
            }
        } catch (e: org.json.JSONException) {
            // If the stored JSON is malformed, return a fresh state.
            AuthState()
        }
    }

    /**
     * Clears the saved authentication state from SharedPreferences.
     * This is used during the logout process.
     */
    fun clearAuthState() {
        prefs.edit { remove(KEY_AUTH_STATE) }
    }
}

