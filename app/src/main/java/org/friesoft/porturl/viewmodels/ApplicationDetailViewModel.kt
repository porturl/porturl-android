package org.friesoft.porturl.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.friesoft.porturl.data.model.Application
import org.friesoft.porturl.data.model.ApplicationCategory
import org.friesoft.porturl.data.model.Category
import org.friesoft.porturl.data.repository.ApplicationRepository
import org.friesoft.porturl.data.repository.CategoryRepository
import javax.inject.Inject

@HiltViewModel
class ApplicationDetailViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    data class UiState(
        val application: Application? = null,
        val allCategories: List<Category> = emptyList(),
        val selectedImageUri: Uri? = null,
        val isLoading: Boolean = true,
        val isSaving: Boolean = false // To show a progress indicator on save
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val finishScreen = MutableSharedFlow<Boolean>()
    val errorMessage = MutableSharedFlow<String>()

    /**
     * Called by the UI when the user selects an image from their device.
     */
    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun loadApplication(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allCategories = categoryRepository.getAllCategories()
                val app = if (id == -1L) {
                    // Create a blank template for a new application
                    Application(
                        id = null, name = "", url = "", applicationCategories = emptyList(),
                        iconLarge = null, iconMedium = null, iconThumbnail = null,
                        iconUrlLarge = null, iconUrlMedium = null, iconUrlThumbnail = null, description = null,
                        roles = emptyList()
                    )
                } else {
                    applicationRepository.getApplicationById(id)
                }
                _uiState.value = UiState(
                    application = app,
                    allCategories = allCategories,
                    isLoading = false
                )
            } catch (e: Exception) {
                errorMessage.emit("Failed to load application data.")
                finishScreen.emit(true)
            }
        }
    }

    /**
     * Orchestrates the save process. If a new image was selected, it uploads it first,
     * then saves the application with the new icon filename.
     */
    fun saveApplication(name: String, url: String, selectedCategoryIds: Set<Long>, roles: List<String>) {
        val originalApplication = _uiState.value.application ?: return
        if (_uiState.value.isSaving) return // Prevent duplicate saves

        if (name.isBlank() || url.isBlank() || selectedCategoryIds.isEmpty()) {
            viewModelScope.launch { errorMessage.emit("Name, URL, and at least one category are required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val iconFilename = handleImageUpload()

                val appToSave = originalApplication.copy(
                    name = name,
                    url = url,
                    applicationCategories = updateApplicationCategories(originalApplication, selectedCategoryIds),
                    iconThumbnail = iconFilename ?: originalApplication.iconThumbnail,
                    roles = roles
                )

                if (appToSave.id == null) {
                    applicationRepository.createApplication(appToSave)
                } else {
                    applicationRepository.updateApplication(appToSave.id!!, appToSave)
                }
                finishScreen.emit(true)
            } catch (e: Exception) {
                errorMessage.emit("Failed to save application: ${e.message}")
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    /**
     * Handles the image upload process if a new image URI is present.
     * @return The new filename if an image was uploaded, otherwise null.
     */
    private suspend fun handleImageUpload(): String? {
        val selectedUri = _uiState.value.selectedImageUri
        if (selectedUri != null) {
            val newIconFilename = applicationRepository.uploadImage(selectedUri)
            if (newIconFilename == null) {
                throw Exception("Failed to upload image.")
            }
            return newIconFilename
        }
        return null
    }

    /**
     * Constructs the new list of ApplicationCategory objects, preserving existing sort orders.
     */
    private fun updateApplicationCategories(
        originalApp: Application,
        selectedCategoryIds: Set<Long>
    ): List<ApplicationCategory> {
        val allCategoriesMap = _uiState.value.allCategories.associateBy { it.id }
        // Use mapNotNull to safely find categories and create links, preventing crashes.
        return selectedCategoryIds.mapNotNull { catId ->
            allCategoriesMap[catId]?.let { category ->
                val existingLink = originalApp.applicationCategories.find { it.category?.id == catId }
                ApplicationCategory(category, existingLink?.sortOrder ?: 0)
            }
        }
    }
}

