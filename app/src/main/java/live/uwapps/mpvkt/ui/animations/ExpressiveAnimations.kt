package live.uwapps.mpvkt.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import live.uwapps.mpvkt.ui.theme.ExpressiveDuration
import live.uwapps.mpvkt.ui.theme.ExpressiveSpring
import kotlin.math.PI
import kotlin.math.sin

/**
 * Shake animation modifier
 * For error states and attention-grabbing
 */
fun Modifier.shakeAnimation(
    enabled: Boolean,
    onAnimationComplete: () -> Unit = {}
): Modifier = composed {
    val offsetX by remember { mutableStateOf(0f) }
    var trigger by remember { mutableStateOf(0) }

    LaunchedEffect(enabled) {
        if (enabled) {
            trigger++
        }
    }

    val shake by animateFloatAsState(
        targetValue = if (trigger % 2 == 0) 0f else 1f,
        animationSpec = keyframes {
            durationMillis = 500
            0f at 0
            -10f at 50
            10f at 100
            -10f at 150
            10f at 200
            -5f at 250
            5f at 300
            0f at 350
        },
        label = "shake",
        finishedListener = { onAnimationComplete() }
    )

    graphicsLayer {
        translationX = if (enabled) shake * 10f else 0f
    }
}

/**
 * Pulse animation modifier
 * For highlighting new or important items
 */
fun Modifier.pulseAnimation(
    enabled: Boolean = true,
    minScale: Float = 0.95f,
    maxScale: Float = 1.05f,
    durationMillis: Int = 1000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    if (enabled) {
        graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    } else {
        this
    }
}

/**
 * Breathing animation modifier
 * Subtle scale animation for ambient elements
 */
fun Modifier.breathingAnimation(
    enabled: Boolean = true,
    minScale: Float = 0.98f,
    maxScale: Float = 1.02f,
    durationMillis: Int = 2000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "breathing")

    val scale by infiniteTransition.animateFloat(
        initialValue = minScale,
        targetValue = maxScale,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathingScale"
    )

    if (enabled) {
        graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
    } else {
        this
    }
}

/**
 * Shimmer effect modifier
 * For loading states and skeleton screens
 */
fun Modifier.shimmerEffect(
    enabled: Boolean = true,
    colors: List<Color> = listOf(
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.5f),
        Color.LightGray.copy(alpha = 0.3f)
    ),
    durationMillis: Int = 1500
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")

    val offset by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerOffset"
    )

    if (enabled) {
        drawBehind {
            val width = size.width
            val height = size.height
            
            drawRect(
                brush = Brush.linearGradient(
                    colors = colors,
                    start = Offset(width * offset, 0f),
                    end = Offset(width * (offset + 0.3f), height)
                )
            )
        }
    } else {
        this
    }
}

/**
 * Rotate animation modifier
 * For loading indicators and dynamic elements
 */
fun Modifier.rotateAnimation(
    enabled: Boolean = true,
    durationMillis: Int = 1000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "rotate")
    
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    
    if (enabled) {
        graphicsLayer {
            rotationZ = rotation
        }
    } else {
        this
    }
}

/**
 * Wave animation modifier
 * For playful, expressive elements
 */
fun Modifier.waveAnimation(
    enabled: Boolean = true,
    amplitude: Float = 10f,
    frequency: Float = 2f,
    durationMillis: Int = 2000
): Modifier = composed {
    val infiniteTransition = rememberInfiniteTransition(label = "wave")
    
    val phase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )
    
    if (enabled) {
        graphicsLayer {
            translationY = amplitude * sin(frequency * phase)
        }
    } else {
        this
    }
}

/**
 * Bounce animation modifier
 * For interactive feedback
 */
fun Modifier.bounceAnimation(
    trigger: Any,
    intensity: Float = 0.1f
): Modifier = composed {
    var bounceState by remember { mutableStateOf(0) }
    
    LaunchedEffect(trigger) {
        bounceState++
    }
    
    val bounce by animateFloatAsState(
        targetValue = if (bounceState % 2 == 0) 0f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "bounce"
    )
    
    graphicsLayer {
        val scale = 1f + (intensity * (1f - bounce))
        scaleX = scale
        scaleY = scale
    }
}

/**
 * Fade in animation with slide
 */
fun fadeInSlideIn() = fadeIn(
    animationSpec = tween(ExpressiveDuration.Medium)
) + slideInVertically(
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    ),
    initialOffsetY = { it / 4 }
)

/**
 * Fade out animation with slide
 */
fun fadeOutSlideOut() = fadeOut(
    animationSpec = tween(ExpressiveDuration.Fast)
) + slideOutVertically(
    animationSpec = tween(ExpressiveDuration.Fast),
    targetOffsetY = { -it / 4 }
)

/**
 * Scale in animation
 */
fun scaleIn() = scaleIn(
    initialScale = 0.8f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
) + fadeIn(
    animationSpec = tween(ExpressiveDuration.Medium)
)

/**
 * Scale out animation
 */
fun scaleOut() = scaleOut(
    targetScale = 0.8f,
    animationSpec = tween(ExpressiveDuration.Fast)
) + fadeOut(
    animationSpec = tween(ExpressiveDuration.Fast)
)

/**
 * Expressive expand vertically
 */
fun expressiveExpandVertically() = expandVertically(
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
) + fadeIn(
    animationSpec = tween(ExpressiveDuration.Medium)
)

/**
 * Expressive shrink vertically
 */
fun expressiveShrinkVertically() = shrinkVertically(
    animationSpec = tween(ExpressiveDuration.Fast)
) + fadeOut(
    animationSpec = tween(ExpressiveDuration.Fast)
)

/**
 * Expressive expand horizontally
 */
fun expressiveExpandHorizontally() = expandHorizontally(
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
) + fadeIn(
    animationSpec = tween(ExpressiveDuration.Medium)
)

/**
 * Expressive shrink horizontally
 */
fun expressiveShrinkHorizontally() = shrinkHorizontally(
    animationSpec = tween(ExpressiveDuration.Fast)
) + fadeOut(
    animationSpec = tween(ExpressiveDuration.Fast)
)

/**
 * Bounce in animation for dramatic entrance
 */
fun bounceIn() = scaleIn(
    initialScale = 0.3f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )
) + fadeIn(
    animationSpec = tween(ExpressiveDuration.Medium)
)

/**
 * Bounce out animation
 */
fun bounceOut() = scaleOut(
    targetScale = 0.3f,
    animationSpec = spring(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
) + fadeOut(
    animationSpec = tween(ExpressiveDuration.Fast)
)

