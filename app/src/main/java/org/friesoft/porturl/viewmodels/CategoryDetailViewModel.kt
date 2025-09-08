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
    fun loadCategory(id: Long) {
        if (id == -1L) { /* Create new category logic could go here */ return }
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val category = categoryRepository.getCategoryById(id) // Assumes getCategoryById exists
                _uiState.value = UiState.Success(category)
            } catch (e: Exception) {
                errorMessage.emit("Failed to load category.")
                finishScreen.emit(true)
            }
        }
    }
    fun saveCategory(category: Category) {
        viewModelScope.launch {
            try {
                categoryRepository.updateCategory(category)
                finishScreen.emit(true)
            } catch (e: Exception) {
                errorMessage.emit("Failed to save category.")
            }
        }
    }
}
