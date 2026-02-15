package org.friesoft.porturl.data.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.annotation.AnyThread
import androidx.core.content.edit
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import net.openid.appauth.AuthState
import org.json.JSONException
import java.io.File
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference
import java.util.concurrent.locks.ReentrantLock

class AuthStateManager private constructor(context: Context) {
    private val prefs: SharedPreferences

    init {
        // Delete old insecure preferences if they exist
        val oldPrefsFile = File(context.filesDir.parent, "shared_prefs/$STORE_NAME.xml")
        if (oldPrefsFile.exists()) {
            context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE).edit().clear().apply()
            oldPrefsFile.delete()
        }

        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            SECURE_STORE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private val prefsLock: ReentrantLock = ReentrantLock()
    private val currentAuthState: AtomicReference<AuthState> = AtomicReference()

    @get:AnyThread
    val current: AuthState
        get() {
            if (currentAuthState.get() != null) {
                return currentAuthState.get()
            }
            val state = readState()
            return if (currentAuthState.compareAndSet(null, state)) {
                state
            } else {
                currentAuthState.get()
            }
        }

    @AnyThread
    fun replace(state: AuthState): AuthState {
        writeState(state)
        currentAuthState.set(state)
        return state
    }

    @AnyThread
    fun update(state: AuthState): AuthState {
        return replace(state)
    }

    @AnyThread
    fun clearAuthState() {
        val clearedState = AuthState()
        // perform read and write in critical section to avoid race conditions
        prefsLock.lock()
        try {
            prefs.edit {
                remove(KEY_STATE)
            }
        } finally {
            prefsLock.unlock()
        }
        currentAuthState.set(clearedState)
    }

    @AnyThread
    private fun readState(): AuthState {
        prefsLock.lock()
        return try {
            val currentState = prefs.getString(KEY_STATE, null)
            if (currentState == null) {
                AuthState()
            } else {
                try {
                    AuthState.jsonDeserialize(currentState)
                } catch (ex: JSONException) {
                    AuthState()
                }
            }
        } finally {
            prefsLock.unlock()
        }
    }

    @AnyThread
    private fun writeState(state: AuthState?) {
        prefsLock.lock()
        try {
            val editor = prefs.edit()
            if (state == null) {
                editor.remove(KEY_STATE)
            } else {
                editor.putString(KEY_STATE, state.jsonSerializeString())
            }

            check(editor.commit()) { "Failed to write state to shared prefs" }
        } finally {
            prefsLock.unlock()
        }
    }

    companion object {
        private val INSTANCE_REF = AtomicReference(WeakReference<AuthStateManager>(null))

        @AnyThread
        fun getInstance(context: Context): AuthStateManager {
            var instance = INSTANCE_REF.get().get()
            if (instance == null) {
                instance = AuthStateManager(context.applicationContext)
                INSTANCE_REF.set(WeakReference(instance))
            }
            return instance
        }

        private const val STORE_NAME = "AuthState"
        private const val SECURE_STORE_NAME = "AuthStateSecure"
        private const val KEY_STATE = "state"
    }
}