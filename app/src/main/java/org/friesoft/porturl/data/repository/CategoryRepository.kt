package org.friesoft.porturl.data.repository

import org.friesoft.porturl.client.api.CategoryApi
import org.friesoft.porturl.client.model.Category
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(private val categoryApi: CategoryApi) {

    suspend fun getAllCategories(): List<Category> {
        return categoryApi.findAllCategories().body() ?: emptyList()
    }

    suspend fun getCategoryById(id: Long): Category {
        return categoryApi.findCategoryById(id).body() ?: throw Exception("Category not found")
    }

    suspend fun createCategory(category: Category): Category = categoryApi.addCategory(category).body() ?: throw Exception("Failed to create category")

    suspend fun updateCategory(category: Category): Category {
        return categoryApi.updateCategory(category.id ?: 0L, category).body() ?: throw Exception("Failed to update category")
    }

    /**
     * Deletes a category by its ID.
     */
    suspend fun deleteCategory(id: Long) {
        categoryApi.deleteCategory(id)
    }

    /**
     * Sends a list of updated categories to the backend's batch-update endpoint.
     */
    suspend fun reorderCategories(categories: List<Category>) {
        if (categories.isNotEmpty()) {
            categoryApi.reorderCategories(categories)
        }
    }
}
