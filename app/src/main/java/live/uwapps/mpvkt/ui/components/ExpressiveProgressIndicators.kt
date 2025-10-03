package live.uwapps.mpvkt.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.progressSemantics
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import live.uwapps.mpvkt.ui.theme.waveAnimation
import kotlin.math.PI
import kotlin.math.sin

/**
 * Material 3 Expressive Wavy Linear Progress Indicator
 * Features animated wave effect for delightful loading experience
 */
@Composable
fun ExpressiveWavyLinearProgressIndicator(
    progress: Float,
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeCap: StrokeCap = StrokeCap.Round,
    waveAmplitude: Float = 4f,
    waveFrequency: Float = 2f
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "progress"
    )

    val wavePhase by rememberInfiniteTransition(label = "wave").animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = waveAnimation(1500),
        label = "wavePhase"
    )

    Canvas(
        modifier = modifier
            .progressSemantics(animatedProgress)
            .height(8.dp)
            .fillMaxWidth()
    ) {
        val strokeWidth = size.height
        val width = size.width
        val progressWidth = width * animatedProgress.coerceIn(0f, 1f)

        // Draw track with wave
        drawWavyLine(
            color = trackColor,
            start = Offset(0f, size.height / 2),
            end = Offset(width, size.height / 2),
            strokeWidth = strokeWidth,
            amplitude = waveAmplitude,
            frequency = waveFrequency,
            phase = wavePhase,
            strokeCap = strokeCap
        )

        // Draw progress with wave
        if (progressWidth > 0) {
            drawWavyLine(
                color = color,
                start = Offset(0f, size.height / 2),
                end = Offset(progressWidth, size.height / 2),
                strokeWidth = strokeWidth,
                amplitude = waveAmplitude,
                frequency = waveFrequency,
                phase = wavePhase,
                strokeCap = strokeCap
            )
        }
    }
}

/**
 * Indeterminate wavy progress indicator with flowing animation
 */
@Composable
fun ExpressiveWavyLinearProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    trackColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    strokeCap: StrokeCap = StrokeCap.Round
) {
    val infiniteTransition = rememberInfiniteTransition(label = "progress")

    val position by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "position"
    )

    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = waveAnimation(1200),
        label = "wave"
    )

    Canvas(
        modifier = modifier
            .progressSemantics()
            .height(8.dp)
            .fillMaxWidth()
    ) {
        val strokeWidth = size.height
        val width = size.width
        val segmentWidth = width * 0.3f
        val startX = (width + segmentWidth) * position - segmentWidth

        // Draw track
        drawWavyLine(
            color = trackColor,
            start = Offset(0f, size.height / 2),
            end = Offset(width, size.height / 2),
            strokeWidth = strokeWidth,
            amplitude = 4f,
            frequency = 2f,
            phase = wavePhase,
            strokeCap = strokeCap
        )

        // Draw moving segment
        if (startX < width) {
            val segmentStart = startX.coerceAtLeast(0f)
            val segmentEnd = (startX + segmentWidth).coerceAtMost(width)

            if (segmentEnd > segmentStart) {
                drawWavyLine(
                    color = color,
                    start = Offset(segmentStart, size.height / 2),
                    end = Offset(segmentEnd, size.height / 2),
                    strokeWidth = strokeWidth,
                    amplitude = 6f,
                    frequency = 3f,
                    phase = wavePhase,
                    strokeCap = strokeCap
                )
            }
        }
    }
}

/**
 * Expressive Circular Progress Indicator with bouncy animation
 */
@Composable
fun ExpressiveCircularProgressIndicator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = ProgressIndicatorDefaults.CircularStrokeWidth,
    trackColor: Color = Color.Transparent,
    strokeCap: StrokeCap = StrokeCap.Round
) {
    val infiniteTransition = rememberInfiniteTransition(label = "rotation")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val sweep by infiniteTransition.animateFloat(
        initialValue = 30f,
        targetValue = 300f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "sweep"
    )

    Canvas(
        modifier = modifier
            .progressSemantics()
            .size(48.dp)
    ) {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            cap = strokeCap
        )

        // Draw track if needed
        if (trackColor != Color.Transparent) {
            drawArc(
                color = trackColor,
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = stroke
            )
        }

        // Draw animated arc
        drawArc(
            color = color,
            startAngle = rotation - 90f,
            sweepAngle = sweep,
            useCenter = false,
            style = stroke
        )
    }
}

// Helper function to draw wavy lines
private fun DrawScope.drawWavyLine(
    color: Color,
    start: Offset,
    end: Offset,
    strokeWidth: Float,
    amplitude: Float,
    frequency: Float,
    phase: Float,
    strokeCap: StrokeCap
) {
    val segments = 100
    val dx = (end.x - start.x) / segments

    for (i in 0 until segments) {
        val x1 = start.x + i * dx
        val x2 = start.x + (i + 1) * dx

        val wave1 = amplitude * sin(frequency * x1 / size.width * 2 * PI.toFloat() + phase)
        val wave2 = amplitude * sin(frequency * x2 / size.width * 2 * PI.toFloat() + phase)

        drawLine(
            color = color,
            start = Offset(x1, start.y + wave1),
            end = Offset(x2, start.y + wave2),
            strokeWidth = strokeWidth,
            cap = strokeCap
        )
    }
}
