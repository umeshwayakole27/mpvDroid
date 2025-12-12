package com.uw.mpvDroid.ui.components.navigation

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Navigation Bar with enhanced animations
 * 
 * Features:
 * - Smooth floating indicator with morphing animation
 * - Icon animations on selection with spring physics
 * - Label expansion with smooth transitions
 * - Expressive color transitions
 */
@Composable
fun ExpressiveNavigationBar(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 3.dp,
    windowInsets: WindowInsets = NavigationBarDefaults.windowInsets,
    content: @Composable RowScope.() -> Unit
) {
    Surface(
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        modifier = modifier
    ) {
        NavigationBar(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup(),
            containerColor = Color.Transparent,
            contentColor = contentColor,
            tonalElevation = 0.dp,
            windowInsets = windowInsets,
            content = content
        )
    }
}

/**
 * Material 3 Expressive Navigation Bar Item with enhanced animations
 * 
 * Expressive features:
 * - Icon scale animation (bouncy spring)
 * - Icon rotation on selection (subtle)
 * - Smooth color transitions
 */
@Composable
fun RowScope.ExpressiveNavigationBarItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    // Expressive scale animation with bouncy spring
    val iconScale by animateFloatAsState(
        targetValue = if (selected) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "iconScale"
    )
    
    // Subtle rotation animation
    val iconRotation by animateFloatAsState(
        targetValue = if (selected) 0f else -5f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "iconRotation"
    )
    
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Box(
                modifier = Modifier
                    .graphicsLayer {
                        scaleX = iconScale
                        scaleY = iconScale
                        rotationZ = iconRotation
                    }
            ) {
                icon()
            }
        },
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = NavigationBarItemDefaults.colors(
            selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
            selectedTextColor = MaterialTheme.colorScheme.onSurface,
            indicatorColor = MaterialTheme.colorScheme.secondaryContainer,
            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
        ),
        interactionSource = interactionSource
    )
}

object NavigationBarDefaults {
    val windowInsets: WindowInsets
        @Composable
        get() = WindowInsets.navigationBars
}
