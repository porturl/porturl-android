package org.friesoft.porturl.viewmodels

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
            val allCategories: List<Category> = emptyList()
        ) : UiState()
    }

    private val _applicationState = MutableStateFlow<UiState>(UiState.Loading)
    val applicationState: StateFlow<UiState> = _applicationState

    private var originalApplication: Application? = null
    val finishScreen = MutableSharedFlow<Boolean>()
    val errorMessage = MutableSharedFlow<String>()

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
                        iconUrlThumbnail = null
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

    fun saveApplication(
        name: String,
        url: String,
        selectedCategoryIds: Set<Long>,
        iconLarge: String?,
        iconMedium: String?,
        iconThumbnail: String?
    ) {
        if (name.isBlank() || url.isBlank()) {
            viewModelScope.launch { errorMessage.emit("Name and URL cannot be empty.") }
            return
        }
        if (selectedCategoryIds.isEmpty()) {
            viewModelScope.launch { errorMessage.emit("An application must belong to at least one category.") }
            return
        }

        val currentState = _applicationState.value
        if (currentState !is UiState.Success) return

        // Construct the list of ApplicationCategory relationships for the save operation.
        // For new relationships, we assign a default sortOrder of 0.
        // For existing relationships, we preserve the old sortOrder.
        val newAppCategories = selectedCategoryIds.map { catId ->
            val existingLink = originalApplication?.applicationCategories?.find { it.category.id == catId }
            val category = currentState.allCategories.find { it.id == catId }!!
            ApplicationCategory(category, existingLink?.sortOrder ?: 0)
        }

        val appToSave = originalApplication!!.copy(
            name = name,
            url = url,
            applicationCategories = newAppCategories,
            iconUrlLarge = iconLarge?.takeIf { it.isNotBlank() },
            iconUrlMedium = iconMedium?.takeIf { it.isNotBlank() },
            iconUrlThumbnail = iconThumbnail?.takeIf { it.isNotBlank() }
        )

        viewModelScope.launch {
            try {
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

