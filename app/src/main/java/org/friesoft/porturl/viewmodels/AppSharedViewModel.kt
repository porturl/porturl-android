package org.friesoft.porturl.viewmodels

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * A ViewModel to share state and events across different screens (e.g. List and Detail).
 */
class AppSharedViewModel : ViewModel() {
    private val _isEditing = MutableStateFlow(false)
    val isEditing = _isEditing.asStateFlow()

    private val _shouldRefreshAppList = MutableStateFlow(false)
    val shouldRefreshAppList = _shouldRefreshAppList.asStateFlow()

    fun setEditMode(isEditing: Boolean) {
        _isEditing.value = isEditing
    }

    fun triggerRefreshAppList() {
        _shouldRefreshAppList.value = true
    }

    fun onAppListRefreshed() {
        _shouldRefreshAppList.value = false
    }
}
