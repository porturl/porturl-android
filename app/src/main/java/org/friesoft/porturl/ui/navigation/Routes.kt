package org.friesoft.porturl.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

object Routes {
    @Serializable
    data object AuthCheck : NavKey

    @Serializable
    data object Login : NavKey

    @Serializable
    data object Settings : NavKey

    @Serializable
    data object AppList : NavKey

    @Serializable
    data class AppDetail(val appId: Long) : NavKey

    @Serializable
    data class CategoryDetail(val categoryId: Long) : NavKey

    @Serializable
    data object UserList : NavKey

    @Serializable
    data class UserDetail(val userId: String) : NavKey
}
