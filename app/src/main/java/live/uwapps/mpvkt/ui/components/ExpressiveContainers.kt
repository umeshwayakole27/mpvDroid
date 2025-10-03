package live.uwapps.mpvkt.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import live.uwapps.mpvkt.ui.theme.ExpressiveDuration
import live.uwapps.mpvkt.ui.theme.ExpressiveSpring

/**
 * Expressive Bottom Sheet with drag-to-dismiss
 * Implements Material 3 Expressive bottom sheet behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(),
    shape: Shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    tonalElevation: Dp = 1.dp,
    scrimColor: Color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.32f),
    dragHandle: @Composable (() -> Unit)? = { ExpressiveBottomSheetDragHandle() },
    content: @Composable ColumnScope.() -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        modifier = modifier,
        sheetState = sheetState,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        tonalElevation = tonalElevation,
        scrimColor = scrimColor,
        dragHandle = dragHandle,
        content = content
    )
}

/**
 * Expressive drag handle with breathing animation
 */
@Composable
fun ExpressiveBottomSheetDragHandle(
    modifier: Modifier = Modifier,
    width: Dp = 32.dp,
    height: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(2.dp),
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
) {
    // Subtle breathing animation
    val scale by rememberInfiniteTransition(label = "handleBreathing").animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "handleScale"
    )
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = modifier
                .width(width)
                .height(height)
                .scale(scaleX = scale, scaleY = 1f),
            shape = shape,
            color = color
        ) {}
    }
}

/**
 * Expressive Dialog with entrance animation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    properties: DialogProperties = DialogProperties(),
    content: @Composable () -> Unit
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        isVisible = true
    }
    
    BasicAlertDialog(
        onDismissRequest = {
            isVisible = false
            onDismissRequest()
        },
        modifier = modifier,
        properties = properties
    ) {
        AnimatedVisibility(
            visible = isVisible,
            enter = fadeIn(
                animationSpec = tween(ExpressiveDuration.Medium)
            ) + expandVertically(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ),
            exit = fadeOut(
                animationSpec = tween(ExpressiveDuration.Fast)
            ) + shrinkVertically(
                animationSpec = tween(ExpressiveDuration.Fast)
            )
        ) {
            content()
        }
    }
}

/**
 * Expressive Snackbar with slide-in animation
 */
@Composable
fun ExpressiveSnackbar(
    snackbarData: SnackbarData,
    modifier: Modifier = Modifier,
    actionOnNewLine: Boolean = false,
    shape: Shape = MaterialTheme.shapes.small,
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    actionColor: Color = MaterialTheme.colorScheme.inversePrimary,
    dismissActionContentColor: Color = MaterialTheme.colorScheme.inverseOnSurface
) {
    val scale by animateFloatAsState(
        targetValue = 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "snackbarScale"
    )
    
    Snackbar(
        snackbarData = snackbarData,
        modifier = modifier
            .padding(12.dp)
            .scale(scale),
        actionOnNewLine = actionOnNewLine,
        shape = shape,
        containerColor = containerColor,
        contentColor = contentColor,
        actionColor = actionColor,
        dismissActionContentColor = dismissActionContentColor
    )
}

/**
 * Expressive Pull-to-Refresh indicator
 */
@Composable
fun ExpressivePullRefreshIndicator(
    refreshing: Boolean,
    state: Float,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.primary
) {
    // Scale based on pull distance
    val scale by animateFloatAsState(
        targetValue = if (refreshing) 1f else (state * 0.5f).coerceIn(0f, 1f),
        animationSpec = ExpressiveSpring.Bouncy,
        label = "refreshScale"
    )
    
    // Rotate when refreshing
    val rotation by animateFloatAsState(
        targetValue = if (refreshing) 360f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "refreshRotation"
    )
    
    Surface(
        modifier = modifier
            .size(40.dp)
            .scale(scale)
            .rotate(if (refreshing) rotation else 0f),
        shape = RoundedCornerShape(20.dp),
        color = backgroundColor,
        tonalElevation = 6.dp,
        shadowElevation = 6.dp
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (refreshing) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = contentColor,
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

/**
 * Expressive Tooltip with pop animation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpressiveTooltip(
    tooltip: @Composable TooltipScope.() -> Unit,
    modifier: Modifier = Modifier,
    tooltipState: TooltipState = rememberTooltipState(),
    shape: Shape = MaterialTheme.shapes.extraSmall,
    containerColor: Color = MaterialTheme.colorScheme.inverseSurface,
    contentColor: Color = MaterialTheme.colorScheme.inverseOnSurface,
    content: @Composable () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (tooltipState.isVisible) 1f else 0.8f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "tooltipScale"
    )
    
    TooltipBox(
        positionProvider = TooltipDefaults.rememberTooltipPositionProvider(),
        tooltip = {
            Box(modifier = Modifier.scale(scale)) {
                PlainTooltip(
                    shape = shape,
                    containerColor = containerColor,
                    contentColor = contentColor
                ) {
                    tooltip()
                }
            }
        },
        state = tooltipState,
        modifier = modifier
    ) {
        content()
    }
}

/**
 * Expressive Badge with pulse animation
 */
@Composable
fun ExpressiveBadge(
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.error,
    contentColor: Color = MaterialTheme.colorScheme.onError,
    content: (@Composable RowScope.() -> Unit)? = null
) {
    val scale by rememberInfiniteTransition(label = "badgePulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "badgeScale"
    )
    
    Badge(
        modifier = modifier.scale(scale),
        containerColor = containerColor,
        contentColor = contentColor,
        content = content
    )
}

/**
 * Expressive Navigation Rail Item with morphing background
 */
@Composable
fun ExpressiveNavigationRailItem(
    selected: Boolean,
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    label: @Composable (() -> Unit)? = null,
    alwaysShowLabel: Boolean = true,
    colors: NavigationRailItemColors = NavigationRailItemDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.05f else 1f,
        animationSpec = ExpressiveSpring.Bouncy,
        label = "navRailScale"
    )
    
    NavigationRailItem(
        selected = selected,
        onClick = onClick,
        icon = icon,
        modifier = modifier.scale(scale),
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = colors,
        interactionSource = interactionSource
    )
}

/**
 * Expressive Navigation Bar Item with bounce
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
    colors: NavigationBarItemColors = NavigationBarItemDefaults.colors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "navBarScale"
    )
    
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Box(modifier = Modifier.scale(scale)) {
                icon()
            }
        },
        modifier = modifier,
        enabled = enabled,
        label = label,
        alwaysShowLabel = alwaysShowLabel,
        colors = colors,
        interactionSource = interactionSource
    )
}
