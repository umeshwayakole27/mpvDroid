package live.uwapps.mpvkt.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

/**
 * Material 3 Expressive Shape System
 * Provides more varied and dynamic shapes for visual interest
 */
val ExpressiveShapes = Shapes(
    // Extra Small - Subtle rounding for tight spaces
    extraSmall = RoundedCornerShape(8.dp),
    
    // Small - Cards, chips, small buttons
    small = RoundedCornerShape(12.dp),
    
    // Medium - Standard buttons, input fields
    medium = RoundedCornerShape(16.dp),
    
    // Large - Prominent cards, dialogs
    large = RoundedCornerShape(24.dp),
    
    // Extra Large - Hero elements, bottom sheets
    extraLarge = RoundedCornerShape(32.dp)
)

/**
 * Asymmetric shapes for expressive design
 */
object ExpressiveAsymmetricShapes {
    val topRounded = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 28.dp,
        bottomStart = 4.dp,
        bottomEnd = 4.dp
    )
    
    val bottomRounded = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 4.dp,
        bottomStart = 28.dp,
        bottomEnd = 28.dp
    )
    
    val leftRounded = RoundedCornerShape(
        topStart = 28.dp,
        topEnd = 4.dp,
        bottomStart = 28.dp,
        bottomEnd = 4.dp
    )
    
    val rightRounded = RoundedCornerShape(
        topStart = 4.dp,
        topEnd = 28.dp,
        bottomStart = 4.dp,
        bottomEnd = 28.dp
    )
    
    val diagonalCut = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 4.dp,
        bottomStart = 4.dp,
        bottomEnd = 24.dp
    )
}

/**
 * Edge-hugging container shapes
 */
object EdgeHuggingShapes {
    val topEdge = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 0.dp,
        bottomStart = 24.dp,
        bottomEnd = 24.dp
    )
    
    val bottomEdge = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 0.dp
    )
    
    val startEdge = RoundedCornerShape(
        topStart = 0.dp,
        topEnd = 24.dp,
        bottomStart = 0.dp,
        bottomEnd = 24.dp
    )
    
    val endEdge = RoundedCornerShape(
        topStart = 24.dp,
        topEnd = 0.dp,
        bottomStart = 24.dp,
        bottomEnd = 0.dp
    )
}

