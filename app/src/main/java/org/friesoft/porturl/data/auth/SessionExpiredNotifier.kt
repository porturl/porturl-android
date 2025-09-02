package org.friesoft.porturl.data.auth

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A singleton event bus for broadcasting that the user's session has expired.
 * This allows background services (like the AuthInterceptor) to communicate
 * this critical event to the UI layer.
 */
@Singleton
class SessionExpiredNotifier @Inject constructor() {
    private val _sessionExpiredEvents = MutableSharedFlow<Unit>()
    val sessionExpiredEvents = _sessionExpiredEvents.asSharedFlow()

    suspend fun notifySessionExpired() {
        // Emits an event to any collectors
        _sessionExpiredEvents.emit(Unit)
    }
}
