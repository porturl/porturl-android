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
import org.friesoft.porturl.client.model.Application
import org.friesoft.porturl.client.model.ApplicationCreateRequest
import org.friesoft.porturl.client.model.ApplicationUpdateRequest
import org.friesoft.porturl.client.model.Category
import org.friesoft.porturl.data.auth.AuthService
import org.friesoft.porturl.data.repository.ApplicationRepository
import org.friesoft.porturl.data.repository.CategoryRepository
import org.friesoft.porturl.data.repository.ImageRepository
import javax.inject.Inject

@HiltViewModel
class ApplicationDetailViewModel @Inject constructor(
    private val applicationRepository: ApplicationRepository,
    private val categoryRepository: CategoryRepository,
    private val imageRepository: ImageRepository,
    private val authService: AuthService
) : ViewModel() {

    data class UiState(
        val application: Application? = null,
        val allCategories: List<Category> = emptyList(),
        val selectedImageUri: Uri? = null,
        val isLoading: Boolean = true,
        val isSaving: Boolean = false,
        val roles: List<String> = emptyList()
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val finishScreen = MutableSharedFlow<Boolean>()
    val errorMessage = MutableSharedFlow<String>()

    fun onImageSelected(uri: Uri?) {
        _uiState.update { it.copy(selectedImageUri = uri) }
    }

    fun loadApplication(id: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val allCategories = categoryRepository.getAllCategories()
                val app = if (id == -1L) {
                    Application(
                        id = null, name = "", url = "", categories = emptyList()
                    )
                } else {
                    applicationRepository.getApplicationById(id)
                }

                val roles = if (app.id != null) {
                    applicationRepository.getApplicationRoles(app.id)
                } else {
                    emptyList()
                }

                _uiState.value = UiState(
                    application = app,
                    allCategories = allCategories,
                    isLoading = false,
                    roles = roles
                )
            } catch (e: Exception) {
                errorMessage.emit("Failed to load application data.")
                finishScreen.emit(true)
            }
        }
    }

    fun saveApplication(name: String, url: String, selectedCategoryIds: Set<Long>, availableRoles: List<String>) {
        val originalApplication = _uiState.value.application ?: return
        if (_uiState.value.isSaving) return

        if (name.isBlank() || url.isBlank() || selectedCategoryIds.isEmpty()) {
            viewModelScope.launch { errorMessage.emit("Name, URL, and at least one category are required.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val iconFilename = handleImageUpload()
                val selectedCategories = _uiState.value.allCategories.filter { it.id in selectedCategoryIds }

                if (originalApplication.id == null) {
                    val appToSave = ApplicationCreateRequest(
                        name = name,
                        url = url,
                        icon = iconFilename,
                        categories = selectedCategories,
                        roles = availableRoles
                    )
                    applicationRepository.createApplication(appToSave)
                } else {
                    val appUpdateRequest = ApplicationUpdateRequest(
                        name = name,
                        url = url,
                        icon = iconFilename ?: originalApplication.icon,
                        categories = selectedCategories,
                        availableRoles = availableRoles
                    )
                    applicationRepository.updateApplication(originalApplication.id, appUpdateRequest)
                }
                authService.forceTokenRefresh()
                finishScreen.emit(true)
            } catch (e: Exception) {
                errorMessage.emit("Failed to save application: ${e.message}")
            } finally {
                _uiState.update { it.copy(isSaving = false) }
            }
        }
    }

    private suspend fun handleImageUpload(): String? {
        val selectedUri = _uiState.value.selectedImageUri
        if (selectedUri != null) {
            val newIconFilename = imageRepository.uploadImage(selectedUri)
                ?: throw Exception("Failed to upload image.")
            return newIconFilename
        }
        return null
    }
}