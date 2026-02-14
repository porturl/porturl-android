package org.friesoft.porturl

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import io.opentelemetry.android.OpenTelemetryRum
import io.opentelemetry.android.agent.OpenTelemetryRumInitializer
import io.opentelemetry.api.common.AttributeKey.stringKey
import io.opentelemetry.api.common.Attributes
import org.friesoft.porturl.data.repository.SettingsRepository
import javax.inject.Inject
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

@HiltAndroidApp
class PortUrlApplication : Application() {
    lateinit var openTelemetryRum: OpenTelemetryRum

    @Inject
    lateinit var settingsRepository: SettingsRepository

    override fun onCreate() {
        super.onCreate()
        
        val isTelemetryEnabled = settingsRepository.isTelemetryEnabledBlocking()

        if (isTelemetryEnabled) {
            openTelemetryRum = initializeOTel(settingsRepository)
        }
    }

    private fun initializeOTel(settingsRepository: SettingsRepository): OpenTelemetryRum {
        val backendUrl = settingsRepository.getBackendUrlBlocking()
        val otlpEndpoint = "$backendUrl/otlp"

        return OpenTelemetryRumInitializer.initialize(
            context = this,
            configuration = {
                httpExport {
                    baseUrl = otlpEndpoint
                }
                session {
                    backgroundInactivityTimeout = 15.minutes
                    maxLifetime = 4.days
                }
                globalAttributes {
                    Attributes.builder()
                        .put(stringKey("service.name"), "porturl-android")
                        .put(stringKey("service.version"), BuildConfig.VERSION_NAME)
                        .put(stringKey("deployment.environment"), if (BuildConfig.DEBUG) "development" else "production")
                        .build()
                }
            }
        )
    }
}
