package live.uwapps.mpvkt.ui.theme

import androidx.compose.animation.core.*

/**
 * Material 3 Expressive Motion System
 * Provides bouncy, shape-shifting, and expressive animation specs
 */

// Animation durations
object ExpressiveDuration {
    const val Fast = 150
    const val Medium = 250
    const val Slow = 350
    const val VerySlow = 500
    const val ExtraSlow = 700
}

// Bouncy spring specs
object ExpressiveSpring {
    val Bouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    val ExtraBouncy = spring<Float>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    val Smooth = spring<Float>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMediumLow
    )
    
    val VeryBouncy = spring<Float>(
        dampingRatio = 0.5f,
        stiffness = Spring.StiffnessMedium
    )
}

// Wave animation for progress indicators
fun waveAnimation(
    durationMillis: Int = ExpressiveDuration.ExtraSlow
): InfiniteRepeatableSpec<Float> = infiniteRepeatable(
    animation = tween(
        durationMillis = durationMillis,
        easing = LinearEasing
    ),
    repeatMode = RepeatMode.Reverse
)
