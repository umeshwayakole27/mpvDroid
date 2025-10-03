package live.uwapps.mpvkt.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import live.uwapps.mpvkt.ui.theme.ExpressiveSpring

/**
 * Expressive Button with bouncy press animation and shape morphing
 */
@Composable
fun ExpressiveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable RowScope.() -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = ExpressiveSpring.Bouncy,
        label = "scale"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 12.dp else 20.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cornerRadius"
    )

    Button(
        onClick = onClick,
        modifier = modifier.scale(scale),
        enabled = enabled,
        shape = RoundedCornerShape(cornerRadius),
        colors = colors,
        contentPadding = contentPadding,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Expressive FAB with unfurling animation
 */
@Composable
fun ExpressiveFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    icon: @Composable () -> Unit,
    text: @Composable (() -> Unit)? = null,
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() }
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = ExpressiveSpring.ExtraBouncy,
        label = "scale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isPressed) 2.dp else 6.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "elevation"
    )

    if (expanded && text != null) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            icon = icon,
            text = text,
            containerColor = containerColor,
            contentColor = contentColor,
            interactionSource = interactionSource
        )
    } else {
        FloatingActionButton(
            onClick = onClick,
            modifier = modifier.scale(scale),
            containerColor = containerColor,
            contentColor = contentColor,
            interactionSource = interactionSource,
            content = icon
        )
    }
}

/**
 * Animated Button Group with shape morphing for toggle/selection
 */
@Composable
fun ExpressiveButtonGroup(
    selectedIndex: Int,
    onSelectionChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    buttons: List<@Composable () -> Unit>
) {
    val animatedOffset by animateDpAsState(
        targetValue = (selectedIndex * 100).dp, // Adjust based on button width
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "offset"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        buttons.forEachIndexed { index, button ->
            val isSelected = index == selectedIndex

            val backgroundColor by animateColorAsState(
                targetValue = if (isSelected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    Color.Transparent
                },
                animationSpec = tween(200),
                label = "backgroundColor"
            )

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1f,
                animationSpec = ExpressiveSpring.Bouncy,
                label = "scale"
            )

            Box(
                modifier = Modifier
                    .weight(1f)
                    .scale(scale)
                    .clip(RoundedCornerShape(12.dp))
                    .background(backgroundColor)
                    .clickable { onSelectionChange(index) }
                    .padding(12.dp),
                contentAlignment = Alignment.Center
            ) {
                button()
            }
        }
    }
}

/**
 * Expressive Icon Button with bounce effect
 */
@Composable
fun ExpressiveIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: IconButtonColors = IconButtonDefaults.iconButtonColors(),
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    content: @Composable () -> Unit
) {
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = ExpressiveSpring.VeryBouncy,
        label = "scale"
    )

    val rotation by animateFloatAsState(
        targetValue = if (isPressed) -5f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "rotation"
    )

    IconButton(
        onClick = onClick,
        modifier = modifier
            .scale(scale),
        enabled = enabled,
        colors = colors,
        interactionSource = interactionSource,
        content = content
    )
}

/**
 * Stretchable Banner component that can stretch and change shape
 */
@Composable
fun StretchableBanner(
    text: String,
    modifier: Modifier = Modifier,
    icon: @Composable (() -> Unit)? = null,
    expanded: Boolean = false,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    val height by animateDpAsState(
        targetValue = if (expanded) 80.dp else 56.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "height"
    )

    val cornerRadius by animateDpAsState(
        targetValue = if (expanded) 24.dp else 16.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cornerRadius"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(height),
        shape = RoundedCornerShape(cornerRadius),
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier.size(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    icon()
                }
            }

            Text(
                text = text,
                style = if (expanded) {
                    MaterialTheme.typography.headlineSmall
                } else {
                    MaterialTheme.typography.bodyLarge
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

/**
 * Edge-hugging container that sits at screen edge
 */
@Composable
fun EdgeHuggingContainer(
    edge: Edge,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.surface,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    visible: Boolean = true,
    content: @Composable () -> Unit
) {
    val offset by animateDpAsState(
        targetValue = if (visible) 0.dp else when (edge) {
            Edge.Top -> (-100).dp
            Edge.Bottom -> 100.dp
            Edge.Start -> (-100).dp
            Edge.End -> 100.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "offset"
    )

    val shape = when (edge) {
        Edge.Top -> RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
        Edge.Bottom -> RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        Edge.Start -> RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)
        Edge.End -> RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)
    }

    Surface(
        modifier = modifier.offset(
            x = if (edge == Edge.Start || edge == Edge.End) offset else 0.dp,
            y = if (edge == Edge.Top || edge == Edge.Bottom) offset else 0.dp
        ),
        shape = shape,
        color = containerColor,
        contentColor = contentColor,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            content()
        }
    }
}

enum class Edge {
    Top, Bottom, Start, End
}
