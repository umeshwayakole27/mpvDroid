package live.uwapps.mpvkt.ui.library.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import coil.request.videoFramePercent
import live.uwapps.mpvkt.domain.models.Video

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoGridItem(
  video: Video,
  isSelected: Boolean,
  isDragSelecting: Boolean,
  onVideoClick: (Video) -> Unit,
  onVideoLongClick: (Video) -> Unit,
  onVideoHover: (Video) -> Unit,
  onToggleFavorite: (Video) -> Unit,
  modifier: Modifier = Modifier
) {
  // Stable callbacks to prevent recomposition
  val onClickAction = remember(video) { { onVideoClick(video) } }
  val onLongClickAction = remember(video) { { onVideoLongClick(video) } }
  val onToggleFavoriteAction = remember(video) { { onToggleFavorite(video) } }

  Card(
    modifier = modifier
      .width(160.dp)
      .combinedClickable(
        onClick = onClickAction,
        onLongClick = onLongClickAction
      )
      .pointerInput(isDragSelecting, video.path) {
        if (isDragSelecting) {
          awaitPointerEventScope {
            while (true) {
              val event = awaitPointerEvent()
              if (event.type == PointerEventType.Move || event.type == PointerEventType.Enter) {
                onVideoHover(video)
              }
            }
          }
        }
      }
      .then(
        if (isSelected) Modifier.border(
          width = 3.dp,
          color = MaterialTheme.colorScheme.primary,
          shape = RoundedCornerShape(12.dp)
        ) else Modifier
      )
      .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow)),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    val context = LocalContext.current
    val imageModel = remember(context, video.path) {
      ImageRequest.Builder(context)
        .data(video.path)
        .videoFramePercent(0.1)
        .crossfade(true)
        .build()
    }
    val animatedProgress by animateFloatAsState(
      targetValue = video.watchProgress,
      animationSpec = tween(300, easing = FastOutSlowInEasing),
      label = "watchProgress"
    )
    val favoriteTint by animateColorAsState(
      targetValue = if (video.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
      animationSpec = tween(200),
      label = "favTint"
    )

    // Pre-compute boolean values
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

        // Selection indicator
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
            shape = RoundedCornerShape(4.dp),
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

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoListItem(
  video: Video,
  isSelected: Boolean,
  isDragSelecting: Boolean,
  onVideoClick: (Video) -> Unit,
  onVideoLongClick: (Video) -> Unit,
  onVideoHover: (Video) -> Unit,
  onToggleFavorite: (Video) -> Unit,
  modifier: Modifier = Modifier
) {
  // Stable callbacks to prevent recomposition
  val onClickAction = remember(video) { { onVideoClick(video) } }
  val onLongClickAction = remember(video) { { onVideoLongClick(video) } }
  val onToggleFavoriteAction = remember(video) { { onToggleFavorite(video) } }

  Card(
    modifier = modifier
      .fillMaxWidth()
      .combinedClickable(
        onClick = onClickAction,
        onLongClick = onLongClickAction
      )
      .pointerInput(isDragSelecting, video.path) {
        if (isDragSelecting) {
          awaitPointerEventScope {
            while (true) {
              val event = awaitPointerEvent()
              if (event.type == PointerEventType.Move || event.type == PointerEventType.Enter) {
                onVideoHover(video)
              }
            }
          }
        }
      }
      .then(
        if (isSelected) Modifier.border(
          width = 3.dp,
          color = MaterialTheme.colorScheme.primary,
          shape = RoundedCornerShape(12.dp)
        ) else Modifier
      )
      .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessLow)),
    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
      else
        MaterialTheme.colorScheme.surface
    )
  ) {
    val context = LocalContext.current
    val imageModel = remember(context, video.path) {
      ImageRequest.Builder(context)
        .data(video.path)
        .videoFramePercent(0.1)
        .crossfade(true)
        .build()
    }
    val animatedProgress by animateFloatAsState(
      targetValue = video.watchProgress,
      animationSpec = tween(300, easing = FastOutSlowInEasing),
      label = "watchProgress"
    )
    val favoriteTint by animateColorAsState(
      targetValue = if (video.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurfaceVariant,
      animationSpec = tween(200),
      label = "favTint"
    )

    // Pre-compute boolean values
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
          .clip(RoundedCornerShape(8.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
      ) {
        AsyncImage(
          model = imageModel,
          contentDescription = "Video thumbnail",
          modifier = Modifier.fillMaxSize(),
          contentScale = ContentScale.Crop
        )

        // Duration overlay
        if (hasDuration) {
          Surface(
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(4.dp),
            shape = RoundedCornerShape(4.dp),
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

        // Watch progress indicator
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
