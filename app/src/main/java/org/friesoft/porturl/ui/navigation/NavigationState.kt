package org.friesoft.porturl.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberDecoratedNavEntries
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.runtime.serialization.NavKeySerializer
import androidx.savedstate.compose.serialization.serializers.MutableStateSerializer
import android.util.Log
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator

/**
 * Create a navigation state that persists config changes and process death.
 */
@Composable
fun rememberNavigationState(
    startRoute: NavKey,
    topLevelRoutes: Set<NavKey>
): NavigationState {

    val topLevelRoute = rememberSerializable(
        startRoute, topLevelRoutes,
        serializer = MutableStateSerializer(NavKeySerializer())
    ) {
        mutableStateOf(startRoute)
    }

    val backStacks = topLevelRoutes.associateWith { key -> rememberNavBackStack(key) }

    return remember(startRoute, topLevelRoutes) {
        NavigationState(
            startRoute = startRoute,
            topLevelRoute = topLevelRoute,
            backStacks = backStacks
        )
    }
}

/**
 * State holder for navigation state.
 *
 * @param startRoute - the start route. The user will exit the app through this route.
 * @param topLevelRoute - the current top level route
 * @param backStacks - the back stacks for each top level route
 */
class NavigationState(
    val startRoute: NavKey,
    topLevelRoute: MutableState<NavKey>,
    val backStacks: Map<NavKey, NavBackStack<NavKey>>
) {
    var topLevelRoute: NavKey by topLevelRoute

    val stacksInUse: List<NavKey>
        get() = if (topLevelRoute == Routes.Login) {
            listOf(Routes.Login)
        } else if (topLevelRoute == startRoute) {
            listOf(startRoute)
        } else {
            listOf(startRoute, topLevelRoute)
        }

    fun clearAllBackStacks() {
        backStacks.forEach { (key, stack) ->
            // NavBackStack in navigation3 is a SnapshotStateList/MutableList of NavKey
            // We want to remove all items.
            try {
                // Try to clear it. If it's a SnapshotStateList it has clear()
                val list = stack as? MutableList<NavKey>
                list?.clear()
                // Navigation3 might expect at least one item (the base) in the stack
                // so we add the key itself back.
                list?.add(key)
                Log.d("NavigationState", "Cleared and reset stack for $key")
            } catch (e: Exception) {
                Log.e("NavigationState", "Failed to clear stack for $key", e)
            }
        }
    }
}

/**
 * Convert NavigationState into NavEntries.
 */
@Composable
fun NavigationState.toEntries(
    entryProvider: (NavKey) -> NavEntry<NavKey>
): SnapshotStateList<NavEntry<NavKey>> {

    val result = stacksInUse.flatMap { stackKey ->
        val stack = backStacks[stackKey] ?: return@flatMap emptyList()
        // Use key(stackKey) to ensure that the decorated entries are uniquely identified
        // and correctly disposed when the stack is no longer in stacksInUse.
        key(stackKey) {
            val decorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator<NavKey>(),
            )

            // Force state read to ensure recomposition when stack contents change
            val trigger = (stack as? Collection<*>)?.size
            Log.d("NavigationState", "toEntries: stack $stackKey size: $trigger")

            rememberDecoratedNavEntries(
                backStack = stack,
                entryDecorators = decorators,
                entryProvider = entryProvider
            )
        }
    }

    val finalResult = result.toMutableStateList()
    Log.d("NavigationState", "toEntries: result size: ${finalResult.size}")
    return finalResult
}
