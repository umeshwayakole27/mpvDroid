package live.uwapps.mpvkt.ui.library.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFramePercent
import live.uwapps.mpvkt.domain.models.Video
import live.uwapps.mpvkt.ui.theme.AnimationDurations
import live.uwapps.mpvkt.ui.theme.ExpressiveEasing

/**
 * Optimized video grid item with Material 3 Expressive design
 * Features: smooth animations, better performance, expressive motion
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoGridItemOptimized(
  video: Video,
  isSelected: Boolean,
  onVideoClick: (Video) -> Unit,
  onVideoLongClick: (Video) -> Unit,
  onToggleFavorite: (Video) -> Unit,
  modifier: Modifier = Modifier
) {
  // Stable callbacks to prevent unnecessary recomposition
  val onClickAction = remember(video.path) { { onVideoClick(video) } }
  val onLongClickAction = remember(video.path) { { onVideoLongClick(video) } }
  val onToggleFavoriteAction = remember(video.path) { { onToggleFavorite(video) } }

  // Animated scale for tactile feedback - Material 3 Expressive
  val scale by animateFloatAsState(
    targetValue = if (isSelected) 0.95f else 1f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "selectionScale"
  )

  // Animated elevation for depth perception
  val elevation by animateFloatAsState(
    targetValue = if (isSelected) 8f else 2f,
    animationSpec = tween(
      durationMillis = AnimationDurations.Fast,
      easing = ExpressiveEasing
    ),
    label = "elevation"
  )

  Card(
    modifier = modifier
      .width(160.dp)
      .scale(scale)
      .combinedClickable(
        onClick = onClickAction,
        onLongClick = onLongClickAction
      )
      .then(
        if (isSelected) Modifier.border(
          width = 3.dp,
          color = MaterialTheme.colorScheme.primary,
          shape = MaterialTheme.shapes.medium
        ) else Modifier
      )
      .animateContentSize(
        animationSpec = tween(
          durationMillis = AnimationDurations.Medium,
          easing = ExpressiveEasing
        )
      ),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
    shape = MaterialTheme.shapes.medium
  ) {
    val context = LocalContext.current

    // Optimize image loading with proper caching
    val imageModel = remember(video.path) {
      ImageRequest.Builder(context)
        .data(video.path)
        .videoFramePercent(0.1)
        .crossfade(AnimationDurations.Fast)
        .memoryCacheKey(video.path)
        .diskCacheKey(video.path)
        .build()
    }

    val animatedProgress by animateFloatAsState(
      targetValue = video.watchProgress,
      animationSpec = tween(
        durationMillis = AnimationDurations.Medium,
        easing = ExpressiveEasing
      ),
      label = "watchProgress"
    )

    val favoriteTint by animateColorAsState(
      targetValue = if (video.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
      animationSpec = tween(
        durationMillis = AnimationDurations.Fast,
        easing = ExpressiveEasing
      ),
      label = "favTint"
    )

    // Pre-compute boolean values to avoid repeated calculations
    val hasDuration = remember(video.duration) { video.duration > 0 }
    val hasProgress = remember(animatedProgress) { animatedProgress > 0f }

    Column {
      // Thumbnail container
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(90.dp)
          .background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        AsyncImage(
          model = imageModel,
          contentDescription = "Video thumbnail",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        // Selection indicator with smooth transition
        if (isSelected) {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.3f))
          )
          Icon(
            Icons.Default.CheckCircle,
            contentDescription = "Selected",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
              .align(Alignment.TopStart)
              .padding(4.dp)
              .size(24.dp)
          )
        }

        // Favorite toggle button
        if (!isSelected) {
          IconButton(
            onClick = onToggleFavoriteAction,
            modifier = Modifier
              .align(Alignment.TopEnd)
              .padding(2.dp)
              .size(28.dp)
          ) {
            Icon(
              if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = if (video.isFavorite) "Remove from favorites" else "Add to favorites",
              tint = favoriteTint,
              modifier = Modifier.size(18.dp)
            )
          }
        }

        // Duration overlay
        if (hasDuration) {
          Surface(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(4.dp),
            shape = MaterialTheme.shapes.extraSmall,
            color = Color.Black.copy(alpha = 0.7f)
          ) {
            Text(
              text = video.formattedDuration,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
              style = MaterialTheme.typography.labelSmall,
              color = Color.White
            )
          }
        }

        // Watch progress indicator
        if (hasProgress) {
          LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(3.dp)
              .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      // Video info
      Column(
        modifier = Modifier.padding(8.dp)
      ) {
        Text(
          text = video.displayName,
          style = MaterialTheme.typography.bodyMedium,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = video.formattedSize,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

/**
 * Optimized video list item with Material 3 Expressive design
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoListItemOptimized(
  video: Video,
  isSelected: Boolean,
  onVideoClick: (Video) -> Unit,
  onVideoLongClick: (Video) -> Unit,
  onToggleFavorite: (Video) -> Unit,
  modifier: Modifier = Modifier
) {
  // Stable callbacks
  val onClickAction = remember(video.path) { { onVideoClick(video) } }
  val onLongClickAction = remember(video.path) { { onVideoLongClick(video) } }
  val onToggleFavoriteAction = remember(video.path) { { onToggleFavorite(video) } }

  // Animated elevation
  val elevation by animateFloatAsState(
    targetValue = if (isSelected) 8f else 1f,
    animationSpec = tween(
      durationMillis = AnimationDurations.Fast,
      easing = ExpressiveEasing
    ),
    label = "elevation"
  )

  Card(
    modifier = modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = onClickAction,
        onLongClick = onLongClickAction
      )
      .then(
        if (isSelected) Modifier.border(
          width = 3.dp,
          color = MaterialTheme.colorScheme.primary,
          shape = MaterialTheme.shapes.medium
        ) else Modifier
      )
      .animateContentSize(
        animationSpec = tween(
          durationMillis = AnimationDurations.Medium,
          easing = ExpressiveEasing
        )
      ),
    elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
      else
        MaterialTheme.colorScheme.surface
    )
  ) {
    val context = LocalContext.current

    val imageModel = remember(video.path) {
      ImageRequest.Builder(context)
        .data(video.path)
        .videoFramePercent(0.1)
        .crossfade(AnimationDurations.Fast)
        .memoryCacheKey(video.path)
        .diskCacheKey(video.path)
        .build()
    }

    val animatedProgress by animateFloatAsState(
      targetValue = video.watchProgress,
      animationSpec = tween(
        durationMillis = AnimationDurations.Medium,
        easing = ExpressiveEasing
      ),
      label = "watchProgress"
    )

    val favoriteTint by animateColorAsState(
      targetValue = if (video.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
      animationSpec = tween(
        durationMillis = AnimationDurations.Fast,
        easing = ExpressiveEasing
      ),
      label = "favTint"
    )

    val hasDuration = remember(video.duration) { video.duration > 0 }
    val hasProgress = remember(animatedProgress) { animatedProgress > 0f }

    Row(
      modifier = Modifier.padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Selection indicator
      if (isSelected) {
        Icon(
          Icons.Default.CheckCircle,
          contentDescription = "Selected",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
      }

      // Thumbnail
      Box(
        modifier = Modifier
          .size(80.dp, 60.dp)
          .clip(MaterialTheme.shapes.small)
          .background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        AsyncImage(
          model = imageModel,
          contentDescription = "Video thumbnail",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        if (hasDuration) {
          Surface(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(4.dp),
            shape = MaterialTheme.shapes.extraSmall,
            color = Color.Black.copy(alpha = 0.7f)
          ) {
            Text(
              text = video.formattedDuration,
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
              style = MaterialTheme.typography.labelSmall,
              color = Color.White
            )
          }
        }

        if (hasProgress) {
          LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier
              .fillMaxWidth()
              .height(2.dp)
              .align(Alignment.BottomCenter),
            color = MaterialTheme.colorScheme.primary
          )
        }
      }

      Spacer(modifier = Modifier.width(16.dp))

      // Video information
      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = video.displayName,
          style = MaterialTheme.typography.titleMedium,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
          text = video.folderName,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(2.dp))
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = video.formattedSize,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          if (video.resolution != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "• ${video.resolution}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          if (video.isWatched) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              Icons.Default.CheckCircle,
              contentDescription = "Watched",
              modifier = Modifier.size(12.dp),
              tint = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      // Action button
      if (!isSelected) {
        IconButton(
          onClick = onToggleFavoriteAction
        ) {
          Icon(
            if (video.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
            contentDescription = if (video.isFavorite) "Remove from favorites" else "Add to favorites",
            tint = favoriteTint
          )
        }
      }
    }
  }
}

