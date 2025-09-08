package org.friesoft.porturl.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.friesoft.porturl.data.model.Category
import org.friesoft.porturl.data.repository.CategoryRepository
import javax.inject.Inject

/**
 * ViewModel for the CategoryDetailScreen. Handles loading a category for editing,
 * creating a new blank category, and saving changes.
 */
@HiltViewModel
class CategoryDetailViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Success(val category: Category) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()
    val finishScreen = MutableSharedFlow<Boolean>()
    val errorMessage = MutableSharedFlow<String>()

    /**
     * Loads a category for editing or prepares a new, blank category for creation.
     * @param id The ID of the category to load, or -1L to create a new one.
     */
    fun loadCategory(id: Long) {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val category = if (id == -1L) {
                    // ** THE FIX IS HERE (Part 1) **
                    // When creating a new category, provide a blank template
                    // and immediately move to the Success state.
                    Category(id = -1, name = "", sortOrder = 0, applicationSortMode = "CUSTOM", icon = null, description = null, enabled = true)
                } else {
                    categoryRepository.getCategoryById(id)
                }
                _uiState.value = UiState.Success(category)
            } catch (e: Exception) {
                errorMessage.emit("Failed to load category.")
                finishScreen.emit(true)
            }
        }
    }

    /**
     * Saves a category. It correctly determines whether to call the create or update
     * endpoint based on the category's ID.
     * @param category The category object to save.
     */
    fun saveCategory(category: Category) {
        viewModelScope.launch {
            try {
                // ** THE FIX IS HERE (Part 2) **
                // Correctly call create or update based on the ID.
                if (category.id == -1L) {
                    categoryRepository.createCategory(category)
                } else {
                    categoryRepository.updateCategory(category)
                }
                finishScreen.emit(true)
            } catch (e: Exception) {
                errorMessage.emit("Failed to save category.")
            }
        }
    }
}

