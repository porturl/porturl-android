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

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _activeAppDetailId = MutableStateFlow<Long?>(null)
    val activeAppDetailId = _activeAppDetailId.asStateFlow()

    private val _activeCategoryDetailId = MutableStateFlow<Long?>(null)
    val activeCategoryDetailId = _activeCategoryDetailId.asStateFlow()

    fun setEditMode(isEditing: Boolean) {
        _isEditing.value = isEditing
    }

    fun triggerRefreshAppList() {
        _shouldRefreshAppList.value = true
    }

    fun onAppListRefreshed() {
        _shouldRefreshAppList.value = false
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun openAppDetail(id: Long) {
        _activeAppDetailId.value = id
    }

    fun closeAppDetail() {
        _activeAppDetailId.value = null
    }

    fun openCategoryDetail(id: Long) {
        _activeCategoryDetailId.value = id
    }

    fun closeCategoryDetail() {
        _activeCategoryDetailId.value = null
    }
}
