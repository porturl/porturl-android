package org.friesoft.porturl.data.repository

import org.friesoft.porturl.data.model.Category
import org.friesoft.porturl.data.remote.ApiService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(private val apiService: ApiService) {

    suspend fun getAllCategories(): List<Category> {
        return apiService.getAllCategories()
    }

    public suspend fun getCategoryById(id: Long): Category {
        return apiService.getCategoryById(id);
    }

    suspend fun createCategory(category: Category): Category = apiService.createCategory(category)

    suspend fun updateCategory(category: Category): Category {
        return apiService.updateCategory(category.id, category)
    }

    /**
     * Deletes a category by its ID.
     */
    suspend fun deleteCategory(id: Long) {
        apiService.deleteCategory(id)
    }
}
