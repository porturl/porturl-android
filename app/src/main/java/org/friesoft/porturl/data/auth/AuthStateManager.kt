package org.friesoft.porturl.data.auth

import android.content.Context
import androidx.annotation.AnyThread
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import com.google.crypto.tink.RegistryConfiguration
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import net.openid.appauth.AuthState
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicReference

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth_state_secure")

class AuthStateManager private constructor(private val context: Context) {
    private val currentAuthState: AtomicReference<AuthState> = AtomicReference()
    private val aead: Aead

    init {
        AeadConfig.register()
        aead = AndroidKeysetManager.Builder()
            .withSharedPref(context, "tink_keyset", "tink_key")
            .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
            .withMasterKeyUri("android-keystore://tink_master_key")
            .build()
            .keysetHandle
            .getPrimitive(RegistryConfiguration.get(), Aead::class.java)
    }

    @get:AnyThread
    val current: AuthState
        get() {
            val cached = currentAuthState.get()
            if (cached != null) {
                return cached
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
        runBlocking {
            context.dataStore.edit {
                it.remove(KEY_STATE_ENCRYPTED)
            }
        }
        currentAuthState.set(clearedState)
    }

    @AnyThread
    private fun readState(): AuthState {
        return try {
            val encryptedBase64 = runBlocking {
                context.dataStore.data.map { it[KEY_STATE_ENCRYPTED] }.first()
            }
            if (encryptedBase64 == null) {
                AuthState()
            } else {
                val encryptedBytes = android.util.Base64.decode(encryptedBase64, android.util.Base64.DEFAULT)
                val decryptedBytes = aead.decrypt(encryptedBytes, null)
                val stateJson = String(decryptedBytes, Charsets.UTF_8)
                AuthState.jsonDeserialize(stateJson)
            }
        } catch (ex: Exception) {
            AuthState()
        }
    }

    @AnyThread
    private fun writeState(state: AuthState?) {
        runBlocking {
            context.dataStore.edit { prefs ->
                if (state == null) {
                    prefs.remove(KEY_STATE_ENCRYPTED)
                } else {
                    val stateJson = state.jsonSerializeString()
                    val encryptedBytes = aead.encrypt(stateJson.toByteArray(Charsets.UTF_8), null)
                    val encryptedBase64 = android.util.Base64.encodeToString(encryptedBytes, android.util.Base64.DEFAULT)
                    prefs[KEY_STATE_ENCRYPTED] = encryptedBase64
                }
            }
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

        private val KEY_STATE_ENCRYPTED = stringPreferencesKey("state_encrypted")
    }
}
