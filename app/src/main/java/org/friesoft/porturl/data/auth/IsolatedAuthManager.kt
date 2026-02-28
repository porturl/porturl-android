package org.friesoft.porturl.data.auth

import android.content.ComponentName
import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.browser.customtabs.CustomTabsSession
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.friesoft.porturl.client.api.UserApi
import org.friesoft.porturl.data.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class IsolatedAuthManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val userApi: UserApi,
    private val settingsRepository: SettingsRepository,
    private val authStateManager: AuthStateManager,
    private val authService: AuthService
) {
    private val TAG = "IsolatedAuthManager"
    private var client: CustomTabsClient? = null
    private var session: CustomTabsSession? = null

    private val connection = object : CustomTabsServiceConnection() {
        override fun onCustomTabsServiceConnected(name: ComponentName, customTabsClient: CustomTabsClient) {
            client = customTabsClient
            client?.warmup(0L)
            session = client?.newSession(null)
        }

        override fun onServiceDisconnected(name: ComponentName) {
            client = null
            session = null
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        val packageName = CustomTabsClient.getPackageName(context, emptyList<String>())
        if (packageName != null) {
            CustomTabsClient.bindCustomTabsService(context, packageName, connection)
        }
    }

    suspend fun openIsolatedLink(targetUrl: String) {
        Log.d(TAG, "Opening isolated link: $targetUrl")
        val ticket = withContext(Dispatchers.IO) {
            ensureFreshTokens()
            Log.d(TAG, "Tokens fresh, calling createAuthTicket")
            val response = userApi.createAuthTicket()
            if (!response.isSuccessful) {
                Log.e(TAG, "Failed to create auth ticket: ${response.code()}")
                throw RuntimeException("Failed to create auth ticket: ${response.code()}")
            }
            val ticket = response.body()?.ticket
            Log.d(TAG, "Auth ticket received: $ticket")
            ticket ?: throw RuntimeException("Ticket missing in response")
        }

        val backendUrl = settingsRepository.getBackendUrlBlocking()
        val bridgeUrl = Uri.parse(backendUrl).buildUpon()
            .path("/auth/bridge")
            .appendQueryParameter("ticket", ticket.toString())
            .appendQueryParameter("next", targetUrl)
            .build()

        Log.d(TAG, "Launching Custom Tab with bridge URL: $bridgeUrl")
        launchCustomTab(bridgeUrl)
    }

    /**
     * Pre-warms the browser with the bridge URL if the target is known.
     */
    fun preWarm(targetUrl: String) {
        session?.let {
            // We can't know the ticket yet, but we can warm up the backend domain
            it.mayLaunchUrl(Uri.parse(targetUrl), null, null)
        }
    }

    private suspend fun ensureFreshTokens() {
        val state = authStateManager.current
        if (state.needsTokenRefresh) {
            authService.forceTokenRefresh()
        }
    }

    private fun launchCustomTab(uri: Uri) {
        val builder = CustomTabsIntent.Builder(session)
        
        // Ephemeral browsing is disabled to ensure SSO cookies from the system browser 
        // and AppAuth session are available to the bridge session.
        // This is necessary for the "Source Realm" SSO session (e.g., 6 months)
        // to silently authorize target apps.
        // if (isEphemeralBrowsingSupported(context)) {
        //     builder.setEphemeralBrowsingEnabled(true)
        // }

        val customTabsIntent = builder.build()
        customTabsIntent.intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK)
        customTabsIntent.launchUrl(context, uri)
    }

    private fun isEphemeralBrowsingSupported(context: Context): Boolean {
        val packageName = CustomTabsClient.getPackageName(context, emptyList<String>())
        return packageName != null
    }
}
