package org.friesoft.porturl.data.repository

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ConfigRepositoryTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `TelemetryInfo should parse even if healthy is missing`() {
        val jsonString = """{"enabled":false}"""
        val telemetryInfo = json.decodeFromString<TelemetryInfo>(jsonString)
        
        assertFalse(telemetryInfo.enabled)
        assertFalse(telemetryInfo.healthy)
    }

    @Test
    fun `TelemetryInfo should parse if all fields are present`() {
        val jsonString = """{"enabled":true, "healthy":true}"""
        val telemetryInfo = json.decodeFromString<TelemetryInfo>(jsonString)
        
        assertTrue(telemetryInfo.enabled)
        assertTrue(telemetryInfo.healthy)
    }

    @Test
    fun `AppConfig should parse if telemetry fields are missing`() {
        val jsonString = """
            {
                "auth": {
                    "issuer-uri": "https://example.com"
                },
                "telemetry": {
                    "enabled": false
                }
            }
        """.trimIndent()
        val appConfig = json.decodeFromString<AppConfig>(jsonString)
        
        assertEquals("https://example.com", appConfig.auth.issuerUri)
        val telemetry = appConfig.telemetry
        assertTrue(telemetry != null)
        assertFalse(telemetry!!.enabled)
        assertFalse(telemetry.healthy)
    }
}
