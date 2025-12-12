package com.uw.mpvDroid.ui.browser.cards

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import kotlinx.coroutines.launch
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.uw.mpvDroid.ui.theme.MotionSpec
import com.uw.mpvDroid.domain.media.model.VideoFolder
import com.uw.mpvDroid.preferences.AppearancePreferences
import com.uw.mpvDroid.preferences.BrowserPreferences
import com.uw.mpvDroid.preferences.preference.collectAsState
import com.uw.mpvDroid.ui.utils.debouncedCombinedClickable
import org.koin.compose.koinInject
import kotlin.math.pow

@Composable
fun FolderCard(
  folder: VideoFolder,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  isRecentlyPlayed: Boolean = false,
  onLongClick: (() -> Unit)? = null,
  isSelected: Boolean = false,
  onThumbClick: () -> Unit = {},
  showDateModified: Boolean = false,
  customIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
) {
  val appearancePreferences = koinInject<AppearancePreferences>()
  val browserPreferences = koinInject<BrowserPreferences>()
  val unlimitedNameLines by appearancePreferences.unlimitedNameLines.collectAsState()
  val showTotalVideosChip by browserPreferences.showTotalVideosChip.collectAsState()
  val showTotalDurationChip by browserPreferences.showTotalDurationChip.collectAsState()
  val showTotalSizeChip by browserPreferences.showTotalSizeChip.collectAsState()
  val showFolderPath by browserPreferences.showFolderPath.collectAsState()
  val maxLines = if (unlimitedNameLines) Int.MAX_VALUE else 2

  // Remove the redundant folder name from the path
  val parentPath = folder.path.substringBeforeLast("/", folder.path)
  
  // Expressive animation: scale on press with interaction source
  val interactionSource = remember { MutableInteractionSource() }
  var isPressed by remember { mutableStateOf(false) }
  
  // More pronounced scale animation
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.92f else 1f, // More visible scale down
    animationSpec = MotionSpec.springBouncy(),
    label = "folder_press_scale"
  )
  
  // Expressive animation: elevation on selection
  val cardElevation by animateDpAsState(
    targetValue = if (isSelected) 6.dp else 0.dp, // Only elevate when selected
    animationSpec = MotionSpec.standard(),
    label = "folder_elevation"
  )

  // Launch effect to handle press animation with longer duration
  LaunchedEffect(isPressed) {
    if (isPressed) {
      kotlinx.coroutines.delay(200) // Longer delay for more visible animation
      isPressed = false
    }
  }

  Card(
    modifier =
      modifier
        .fillMaxWidth()
        .scale(scale)
        .debouncedCombinedClickable(
          onClick = {
            isPressed = true
            kotlinx.coroutines.MainScope().launch {
              kotlinx.coroutines.delay(100) // Brief delay to show animation
              onClick()
            }
          },
          onLongClick = {
            isPressed = true
            onLongClick?.invoke()
          },
          interactionSource = interactionSource,
        ),
    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
    elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
    shape = RoundedCornerShape(0.dp), // Remove card corners
  ) {
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp)) // Add rounded corners to selection background
          .background(
            if (isSelected) MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f) else Color.Transparent,
          )
          .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Box(
        modifier =
          Modifier
            .size(64.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .debouncedCombinedClickable(
              onClick = onThumbClick,
              onLongClick = onLongClick,
            ),
        contentAlignment = Alignment.Center,
      ) {
        Icon(
          customIcon ?: Icons.Filled.Folder,
          contentDescription = "Folder",
          modifier = Modifier.size(48.dp),
          tint = MaterialTheme.colorScheme.secondary,
        )
      }
      Spacer(modifier = Modifier.width(16.dp))
      Column(
        modifier = Modifier.weight(1f),
      ) {
        Text(
          folder.name,
          style = MaterialTheme.typography.titleMedium,
          color = if (isRecentlyPlayed) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface,
          maxLines = maxLines,
          overflow = TextOverflow.Ellipsis,
        )
        if (showFolderPath && parentPath.isNotEmpty()) {
          Text(
            parentPath,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = maxLines,
            overflow = TextOverflow.Ellipsis,
          )
          Spacer(modifier = Modifier.height(4.dp))
        } else {
          Spacer(modifier = Modifier.height(4.dp))
        }
        Row {
          // Hide chips at storage root level (when videoCount is 0)
          var hasChip = false

          if (showTotalVideosChip && folder.videoCount > 0) {
            Text(
              if (folder.videoCount == 1) "1 Video" else "${folder.videoCount} Videos",
              style = MaterialTheme.typography.labelSmall,
              modifier =
                Modifier
                  .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(8.dp),
                  )
                  .padding(horizontal = 8.dp, vertical = 4.dp),
              color = MaterialTheme.colorScheme.onSurface,
            )
            hasChip = true
          }

          if (showTotalSizeChip && folder.totalSize > 0) {
            if (hasChip) {
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
              formatFileSize(folder.totalSize),
              style = MaterialTheme.typography.labelSmall,
              modifier =
                Modifier
                  .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(8.dp),
                  )
                  .padding(horizontal = 8.dp, vertical = 4.dp),
              color = MaterialTheme.colorScheme.onSurface,
            )
            hasChip = true
          }

          if (showTotalDurationChip && folder.totalDuration > 0) {
            if (hasChip) {
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
              formatDuration(folder.totalDuration),
              style = MaterialTheme.typography.labelSmall,
              modifier =
                Modifier
                  .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(8.dp),
                  )
                  .padding(horizontal = 8.dp, vertical = 4.dp),
              color = MaterialTheme.colorScheme.onSurface,
            )
            hasChip = true
          }

          if (showDateModified && folder.lastModified > 0) {
            if (hasChip) {
              Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
              formatDate(folder.lastModified),
              style = MaterialTheme.typography.labelSmall,
              modifier =
                Modifier
                  .background(
                    MaterialTheme.colorScheme.surfaceContainerHigh,
                    RoundedCornerShape(8.dp),
                  )
                  .padding(horizontal = 8.dp, vertical = 4.dp),
              color = MaterialTheme.colorScheme.onSurface,
            )
          }
        }
      }
    }
  }
}

private fun formatDuration(durationMs: Long): String {
  val seconds = durationMs / 1000
  val hours = seconds / 3600
  val minutes = (seconds % 3600) / 60
  val secs = seconds % 60

  return when {
    hours > 0 -> "${hours}h ${minutes}m"
    minutes > 0 -> "${minutes}m"
    else -> "${secs}s"
  }
}

private fun formatFileSize(bytes: Long): String {
  if (bytes <= 0) return "0 B"
  val units = arrayOf("B", "KB", "MB", "GB", "TB")
  val digitGroups = (kotlin.math.log10(bytes.toDouble()) / kotlin.math.log10(1024.0)).toInt()
  val value = bytes / 1024.0.pow(digitGroups.toDouble())
  return String.format(java.util.Locale.getDefault(), "%.1f %s", value, units[digitGroups])
}

private fun formatDate(timestampSeconds: Long): String {
  val sdf = java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
  return sdf.format(java.util.Date(timestampSeconds * 1000))
}
