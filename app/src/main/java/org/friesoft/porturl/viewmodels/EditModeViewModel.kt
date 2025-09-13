package org.friesoft.porturl.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A simple ViewModel to share the edit mode state across different screens.
 */
class EditModeViewModel : ViewModel() {
    private val _isEditing = MutableStateFlow(false)
    val isEditing = _isEditing.asStateFlow()

    fun setEditMode(isEditing: Boolean) {
        _isEditing.value = isEditing
    }
}
