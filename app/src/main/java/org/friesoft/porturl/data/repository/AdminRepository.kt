package org.friesoft.porturl.data.repository

import org.friesoft.porturl.client.api.AdminApi
import org.friesoft.porturl.client.model.ExportData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(private val adminApi: AdminApi) {

    suspend fun exportData(): ExportData {
        val response = adminApi.exportData()
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Empty response body")
        } else {
            throw Exception("Export failed: ${response.code()}")
        }
    }

    suspend fun importData(data: org.friesoft.porturl.client.model.ExportData) {
        val response = adminApi.importData(data)
        if (!response.isSuccessful) {
            throw Exception("Import failed: ${response.code()}")
        }
    }
}
