package com.uw.mpvDroid.ui.browser.cards

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.uw.mpvDroid.database.entities.PlaylistEntity
import com.uw.mpvDroid.domain.media.model.VideoFolder

@Composable
fun PlaylistCard(
  playlist: PlaylistEntity,
  itemCount: Int,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  onThumbClick: () -> Unit,
  modifier: Modifier = Modifier,
  isSelected: Boolean = false,
) {
  // Convert playlist to VideoFolder format for FolderCard
  val folderModel = VideoFolder(
    bucketId = playlist.id.toString(),
    name = playlist.name,
    path = "", // Not used for playlists
    videoCount = itemCount,
    totalSize = 0, // Not tracked for playlists
    totalDuration = 0, // Not tracked for playlists
    lastModified = playlist.updatedAt / 1000,
  )

  FolderCard(
    folder = folderModel,
    isSelected = isSelected,
    isRecentlyPlayed = false,
    onClick = onClick,
    onLongClick = onLongClick,
    onThumbClick = onThumbClick,
    showDateModified = true,
    customIcon = Icons.Filled.PlaylistPlay,
    modifier = modifier,
  )
}
