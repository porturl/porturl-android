package org.friesoft.porturl.ui.navigation

import androidx.navigation3.runtime.NavKey

import android.util.Log

/**
 * Handles navigation events (forward and back) by updating the navigation state.
 */
class Navigator(val state: NavigationState){
    val canGoBack: Boolean
        get() {
            val currentStack = state.backStacks[state.topLevelRoute] ?: return false
            // Can go back if there's more than one item in the current stack,
            // or if the current topLevelRoute is not the startRoute.
            return currentStack.size > 1 || state.topLevelRoute != state.startRoute
        }

    fun navigate(route: NavKey){
        Log.d("Navigator", "Navigating to $route")
        if (route in state.backStacks.keys){
            // This is a top level route, just switch to it.
            Log.d("Navigator", "Switching top level route to $route")
            state.topLevelRoute = route
        } else {
            Log.d("Navigator", "Adding $route to backstack of ${state.topLevelRoute}")
            state.backStacks[state.topLevelRoute]?.add(route)
        }
    }

    fun goBack(){
        Log.d("Navigator", "goBack called")
        val currentStack = state.backStacks[state.topLevelRoute] ?:
        error("Stack for ${state.topLevelRoute} not found")
        val currentRoute = currentStack.last()

        // If we're at the base of the current route, go back to the start route stack.
        if (currentRoute == state.topLevelRoute){
            Log.d("Navigator", "goBack: At base of ${state.topLevelRoute}, switching to ${state.startRoute}")
            state.topLevelRoute = state.startRoute
        } else {
            Log.d("Navigator", "goBack: Popping $currentRoute from ${state.topLevelRoute} stack")
            currentStack.removeLastOrNull()
        }
    }
}
