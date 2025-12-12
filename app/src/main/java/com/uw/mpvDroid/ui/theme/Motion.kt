package com.uw.mpvDroid.ui.theme

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween

/**
 * Material 3 Expressive Motion System
 * 
 * This file defines the motion specifications and timing values for animations
 * across the app, following Material Design 3 Expressive guidelines.
 */
object MotionTokens {
    
    // ===== Duration Constants =====
    
    /** Extra short duration for micro-interactions */
    const val DurationExtraShort = 50
    
    /** Short duration for simple transitions */
    const val DurationShort1 = 100
    const val DurationShort2 = 150
    const val DurationShort3 = 200
    const val DurationShort4 = 250
    
    /** Medium duration for most UI transitions */
    const val DurationMedium1 = 300
    const val DurationMedium2 = 350
    const val DurationMedium3 = 400
    const val DurationMedium4 = 450
    
    /** Long duration for complex transitions */
    const val DurationLong1 = 500
    const val DurationLong2 = 600
    const val DurationLong3 = 700
    const val DurationLong4 = 800
    
    /** Extra long duration for elaborate animations */
    const val DurationExtraLong1 = 900
    const val DurationExtraLong2 = 1000
    
    // ===== Easing Curves =====
    
    /**
     * Emphasized Decelerate - For elements entering the screen or opening
     * Quickly reaches its peak and then slowly settles into place
     */
    val EmphasizedDecelerate = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    
    /**
     * Emphasized Accelerate - For elements exiting the screen or closing
     * Slowly starts moving and then quickly accelerates off screen
     */
    val EmphasizedAccelerate = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    
    /**
     * Emphasized - Balanced emphasized motion for persistent elements
     */
    val Emphasized = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    
    /**
     * Standard - Default easing for most transitions
     */
    val Standard = CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f)
    
    /**
     * Standard Accelerate - For exiting elements
     */
    val StandardAccelerate = CubicBezierEasing(0.3f, 0.0f, 1.0f, 1.0f)
    
    /**
     * Standard Decelerate - For entering elements
     */
    val StandardDecelerate = CubicBezierEasing(0.0f, 0.0f, 0.0f, 1.0f)
    
    /**
     * Linear - For continuous animations
     */
    val Linear = LinearEasing
    
    /**
     * Legacy - Fast out slow in for compatibility
     */
    val Legacy = FastOutSlowInEasing
}

/**
 * Pre-configured animation specs for common use cases
 */
object MotionSpec {
    
    // ===== Tween Animations =====
    
    /** Fast fade animation for simple visibility changes */
    fun <T> fastFade() = tween<T>(
        durationMillis = MotionTokens.DurationShort2,
        easing = MotionTokens.Linear
    )
    
    /** Standard fade with emphasized easing */
    fun <T> fade() = tween<T>(
        durationMillis = MotionTokens.DurationMedium1,
        easing = MotionTokens.Emphasized
    )
    
    /** Slow fade for emphasis */
    fun <T> slowFade() = tween<T>(
        durationMillis = MotionTokens.DurationMedium4,
        easing = MotionTokens.Emphasized
    )
    
    /** Enter animation - elements coming into view */
    fun <T> enter() = tween<T>(
        durationMillis = MotionTokens.DurationMedium2,
        easing = MotionTokens.EmphasizedDecelerate
    )
    
    /** Exit animation - elements leaving view */
    fun <T> exit() = tween<T>(
        durationMillis = MotionTokens.DurationShort4,
        easing = MotionTokens.EmphasizedAccelerate
    )
    
    /** Quick transition for micro-interactions */
    fun <T> quick() = tween<T>(
        durationMillis = MotionTokens.DurationShort1,
        easing = MotionTokens.Standard
    )
    
    /** Standard transition for most UI changes */
    fun <T> standard() = tween<T>(
        durationMillis = MotionTokens.DurationMedium1,
        easing = MotionTokens.Standard
    )
    
    /** Slow transition for complex changes */
    fun <T> slow() = tween<T>(
        durationMillis = MotionTokens.DurationLong1,
        easing = MotionTokens.Emphasized
    )
    
    /** Elaborate animation for special moments */
    fun <T> elaborate() = tween<T>(
        durationMillis = MotionTokens.DurationExtraLong1,
        easing = MotionTokens.Emphasized
    )
    
    // ===== Spring Animations =====
    
    /**
     * Bouncy spring - playful, expressive motion
     * High damping ratio for pronounced bounce
     */
    fun <T> springBouncy() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * Low bouncy spring - subtle bounce effect
     */
    fun <T> springLowBouncy() = spring<T>(
        dampingRatio = Spring.DampingRatioLowBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * Smooth spring - no bounce, smooth settling
     */
    fun <T> springSmooth() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessMedium
    )
    
    /**
     * Snappy spring - quick, responsive feel
     */
    fun <T> springSnappy() = spring<T>(
        dampingRatio = Spring.DampingRatioMediumBouncy,
        stiffness = Spring.StiffnessHigh
    )
    
    /**
     * Gentle spring - slow, gentle motion
     */
    fun <T> springGentle() = spring<T>(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    )
    
    /**
     * High bounce spring - very expressive motion
     */
    fun <T> springHighBouncy() = spring<T>(
        dampingRatio = 0.5f,
        stiffness = Spring.StiffnessLow
    )
}

/**
 * Delay values for staggered animations
 */
object MotionDelay {
    /** Minimal delay between items */
    const val Minimal = 25
    
    /** Short delay for tight sequences */
    const val Short = 50
    
    /** Standard delay for list items */
    const val Standard = 75
    
    /** Medium delay for emphasis */
    const val Medium = 100
    
    /** Long delay for dramatic effect */
    const val Long = 150
}

/**
 * Scale values for press/hover animations
 */
object MotionScale {
    /** Subtle scale reduction on press */
    const val PressSubtle = 0.98f
    
    /** Standard scale reduction on press */
    const val PressStandard = 0.95f
    
    /** Pronounced scale reduction on press */
    const val PressPronounced = 0.92f
    
    /** Subtle scale increase on hover */
    const val HoverSubtle = 1.02f
    
    /** Standard scale increase on hover */
    const val HoverStandard = 1.05f
    
    /** Pronounced scale increase on hover */
    const val HoverPronounced = 1.08f
    
    /** Maximum scale for emphasis */
    const val Maximum = 1.2f
}

/**
 * Rotation values for expressive animations
 */
object MotionRotation {
    /** Subtle rotation for micro-interactions */
    const val Subtle = 5f
    
    /** Standard rotation for feedback */
    const val Standard = 15f
    
    /** Quarter turn */
    const val QuarterTurn = 90f
    
    /** Half turn */
    const val HalfTurn = 180f
    
    /** Full turn */
    const val FullTurn = 360f
}

/**
 * Elevation values for animated shadow changes
 */
object MotionElevation {
    /** Resting elevation */
    const val Resting = 0f
    
    /** Subtle lift on interaction */
    const val Subtle = 2f
    
    /** Standard hover/focus elevation */
    const val Standard = 4f
    
    /** Pronounced elevation for emphasis */
    const val Pronounced = 8f
    
    /** Maximum elevation for modals */
    const val Maximum = 16f
}
