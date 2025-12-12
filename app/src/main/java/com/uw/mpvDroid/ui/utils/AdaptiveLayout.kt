package com.uw.mpvDroid.ui.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

/**
 * Navigation type based on screen size and form factor
 */
enum class NavigationType {
    BOTTOM_NAVIGATION,
    NAVIGATION_RAIL,
    PERMANENT_NAVIGATION_DRAWER
}

/**
 * Content display type based on screen size
 */
enum class ContentType {
    SINGLE_PANE,
    DUAL_PANE
}

/**
 * Navigation content positioning for vertical layouts
 */
enum class NavigationContentPosition {
    TOP,
    CENTER
}

/**
 * Get the appropriate navigation type based on window size
 */
@Composable
fun getNavigationType(): NavigationType {
    val configuration = LocalConfiguration.current
    
    return when {
        // Compact screens (phones) use bottom navigation
        configuration.screenWidthDp < 600 -> NavigationType.BOTTOM_NAVIGATION
        
        // Very wide screens (>= 1200dp) use permanent drawer
        configuration.screenWidthDp >= 1200 -> NavigationType.PERMANENT_NAVIGATION_DRAWER
        
        // Medium screens (tablets) use navigation rail
        else -> NavigationType.NAVIGATION_RAIL
    }
}

/**
 * Get the appropriate content type based on window size
 */
@Composable
fun getContentType(): ContentType {
    val configuration = LocalConfiguration.current
    
    return when {
        configuration.screenWidthDp < 600 -> ContentType.SINGLE_PANE
        else -> ContentType.DUAL_PANE
    }
}

/**
 * Get navigation content position based on window height
 */
@Composable
fun getNavigationContentPosition(): NavigationContentPosition {
    val configuration = LocalConfiguration.current
    return when {
        configuration.screenHeightDp < 480 -> NavigationContentPosition.TOP
        else -> NavigationContentPosition.CENTER
    }
}
