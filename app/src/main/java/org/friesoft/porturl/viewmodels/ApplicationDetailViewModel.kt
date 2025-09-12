package org.friesoft.porturl.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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

    sealed class UiState {
        object Loading : UiState()
        data class Success(
            val application: Application,
            val allCategories: List<Category> = emptyList(),
            val selectedImageUri: Uri? = null
        ) : UiState()
    }

    private val _applicationState = MutableStateFlow<UiState>(UiState.Loading)
    val applicationState: StateFlow<UiState> = _applicationState

    private var originalApplication: Application? = null
    val finishScreen = MutableSharedFlow<Boolean>()
    val errorMessage = MutableSharedFlow<String>()

    /**
     * Called by the UI when the user selects an image from their device.
     */
    fun onImageSelected(uri: Uri?) {
        val currentState = _applicationState.value
        if (currentState is UiState.Success) {
            _applicationState.value = currentState.copy(selectedImageUri = uri)
        }
    }

    fun loadApplication(id: Long) {
        viewModelScope.launch {
            _applicationState.value = UiState.Loading
            try {
                val allCategories = categoryRepository.getAllCategories()
                val app = if (id == -1L) {
                    // Create a blank template for a new application
                    Application(
                        null, "", "", emptyList(), null, null, null,
                        iconUrlLarge = null,
                        iconUrlMedium = null,
                        iconUrlThumbnail = null,
                    )
                } else {
                    applicationRepository.getApplicationById(id)
                }
                originalApplication = app
                _applicationState.value = UiState.Success(app, allCategories)
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
    fun saveApplication(name: String, url: String, selectedCategoryIds: Set<Long>) {
        val currentState = _applicationState.value
        if (currentState !is UiState.Success) return

        if (name.isBlank() || url.isBlank() || selectedCategoryIds.isEmpty()) {
            viewModelScope.launch { errorMessage.emit("Name, URL, and at least one category are required.") }
            return
        }

        viewModelScope.launch {
            try {
                var newIconFilename: String? = null
                // Step 1: Upload the image if a new one has been selected
                if (currentState.selectedImageUri != null) {
                    newIconFilename = applicationRepository.uploadImage(currentState.selectedImageUri)
                    if (newIconFilename == null) {
                        errorMessage.emit("Failed to upload image.")
                        return@launch
                    }
                }

                // Step 2: Construct the final Application object to save
                val newAppCategories = selectedCategoryIds.map { catId ->
                    val existingLink = originalApplication?.applicationCategories?.find { it.category.id == catId }
                    val category = currentState.allCategories.find { it.id == catId }!!
                    ApplicationCategory(category, existingLink?.sortOrder ?: 0)
                }

                val appToSave = originalApplication!!.copy(
                    name = name,
                    url = url,
                    applicationCategories = newAppCategories,
                    // Use the new filename if one was uploaded, otherwise keep the original.
                    // For simplicity, we're only handling the thumbnail here.
                    iconThumbnail = newIconFilename ?: originalApplication?.iconUrlThumbnail
                )

                // Step 3: Save the application data
                if (appToSave.id == null) {
                    applicationRepository.createApplication(appToSave)
                } else {
                    applicationRepository.updateApplication(appToSave.id!!, appToSave)
                }
                finishScreen.emit(true)
            } catch (e: Exception) {
                errorMessage.emit("Failed to save application.")
            }
        }
    }
}

