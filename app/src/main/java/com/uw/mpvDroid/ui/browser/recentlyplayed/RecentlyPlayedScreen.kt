package com.uw.mpvDroid.ui.browser.recentlyplayed

import android.content.Intent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uw.mpvDroid.database.repository.PlaylistRepository
import com.uw.mpvDroid.domain.media.model.Video
import com.uw.mpvDroid.domain.media.model.VideoFolder
import com.uw.mpvDroid.preferences.BrowserPreferences
import com.uw.mpvDroid.preferences.GesturePreferences
import com.uw.mpvDroid.preferences.PlayerPreferences
import com.uw.mpvDroid.preferences.preference.collectAsState
import com.uw.mpvDroid.presentation.Screen
import com.uw.mpvDroid.ui.browser.cards.FolderCard
import com.uw.mpvDroid.ui.browser.cards.VideoCard
import com.uw.mpvDroid.ui.browser.components.BrowserTopBar
import com.uw.mpvDroid.ui.browser.dialogs.DeleteConfirmationDialog
import com.uw.mpvDroid.ui.browser.playlist.PlaylistDetailScreen
import com.uw.mpvDroid.ui.browser.states.EmptyState
import com.uw.mpvDroid.ui.player.PlayerActivity
import com.uw.mpvDroid.ui.utils.LocalBackStack
import com.uw.mpvDroid.utils.media.MediaUtils
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import my.nanihadesuka.compose.LazyColumnScrollbar
import my.nanihadesuka.compose.ScrollbarSettings
import org.koin.compose.koinInject
import androidx.compose.material.icons.filled.PlaylistPlay

@Serializable
object RecentlyPlayedScreen : Screen {
  
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val backStack = LocalBackStack.current
    val playlistRepository = koinInject<PlaylistRepository>()
    val viewModel: RecentlyPlayedViewModel =
      viewModel(factory = RecentlyPlayedViewModel.factory(context.applicationContext as android.app.Application))

    val recentItems by viewModel.recentItems.collectAsState()
    val recentVideos by viewModel.recentVideos.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val deleteDialogOpen = rememberSaveable { mutableStateOf(false) }
    val playerPreferences = koinInject<PlayerPreferences>()
    val playlistMode by playerPreferences.playlistMode.collectAsState()
    val recentlyPlayedRepository = koinInject<com.uw.mpvDroid.domain.recentlyplayed.repository.RecentlyPlayedRepository>()

    // Selection manager for recently played items
    val selectionManager = com.uw.mpvDroid.ui.browser.selection.rememberSelectionManager(
      items = recentItems,
      getId = { item ->
        when (item) {
          is RecentlyPlayedItem.VideoItem -> "video_${item.video.id}_${item.timestamp}"
          is RecentlyPlayedItem.PlaylistItem -> "playlist_${item.playlist.id}_${item.timestamp}"
          is RecentlyPlayedItem.LinkItem -> "link_${item.url.hashCode()}_${item.timestamp}"
        }
      },
      onDeleteItems = { items ->
        var deleted = 0
        items.forEach { item ->
          when (item) {
            is RecentlyPlayedItem.VideoItem -> {
              recentlyPlayedRepository.deleteByFilePath(item.video.path)
              deleted++
            }
            is RecentlyPlayedItem.PlaylistItem -> {
              // Delete all entries for this playlist
              deleted++
            }
            is RecentlyPlayedItem.LinkItem -> {
              recentlyPlayedRepository.deleteByFilePath(item.url)
              deleted++
            }
          }
        }
        Pair(deleted, 0)
      },
      onOperationComplete = {
        // Refresh will happen automatically via flow
      },
    )

    // Handle back button when in selection mode
    androidx.activity.compose.BackHandler(enabled = selectionManager.isInSelectionMode) {
      selectionManager.clear()
    }

    Scaffold(
      topBar = {
        BrowserTopBar(
          title = "Recently Played",
          isInSelectionMode = selectionManager.isInSelectionMode,
          selectedCount = selectionManager.selectedCount,
          totalCount = recentItems.size,
          onBackClick = if (selectionManager.isInSelectionMode) {
            { selectionManager.clear() }
          } else null,
          onCancelSelection = { selectionManager.clear() },
          onSortClick = null,
          onSearchClick = null,
          onDeleteClick = if (selectionManager.isInSelectionMode) {
            { deleteDialogOpen.value = true }
          } else null,
          onRenameClick = null,
          isSingleSelection = selectionManager.isSingleSelection,
          onInfoClick = null,
          onShareClick = null,
          onPlayClick = null,
          onSelectAll = if (selectionManager.isInSelectionMode) {
            { selectionManager.selectAll() }
          } else null,
          onInvertSelection = if (selectionManager.isInSelectionMode) {
            { selectionManager.invertSelection() }
          } else null,
          onDeselectAll = if (selectionManager.isInSelectionMode) {
            { selectionManager.clear() }
          } else null,
          additionalActions = {
            if (!selectionManager.isInSelectionMode && recentItems.isNotEmpty()) {
              IconButton(
                onClick = { deleteDialogOpen.value = true },
              ) {
                Icon(
                  imageVector = Icons.Filled.Delete,
                  contentDescription = "Clear all",
                )
              }
            }
          },
        )
      },
    ) { padding ->
      when {
        isLoading -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(padding),
            contentAlignment = Alignment.Center,
          ) {
            CircularProgressIndicator(
              modifier = Modifier.size(48.dp),
              color = MaterialTheme.colorScheme.primary,
            )
          }
        }

        recentItems.isEmpty() -> {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(padding)
              .padding(bottom = 8.dp),
            contentAlignment = Alignment.Center,
          ) {
            EmptyState(
              icon = Icons.Filled.History,
              title = "No recently played videos",
              message = "Videos you play will appear here",
            )
          }
        }

        else -> {
          RecentItemsContent(
            recentItems = recentItems,
            playlistMode = playlistMode,
            playlistRepository = playlistRepository,
            selectionManager = selectionManager,
            onVideoClick = { video ->
              // If playlist mode is enabled, play all videos starting from the clicked one
              if (playlistMode) {
                val startIndex = recentVideos.indexOfFirst { it.id == video.id }
                if (startIndex >= 0) {
                  if (recentVideos.size == 1) {
                    // Single video - play normally
                    MediaUtils.playFile(video, context, "recently_played")
                  } else {
                    // Multiple videos - play as playlist starting from clicked video
                    val intent = Intent(Intent.ACTION_VIEW, recentVideos[startIndex].uri)
                    intent.setClass(context, PlayerActivity::class.java)
                    intent.putExtra("internal_launch", true)
                    intent.putParcelableArrayListExtra("playlist", ArrayList(recentVideos.map { it.uri }))
                    intent.putExtra("playlist_index", startIndex)
                    intent.putExtra("launch_source", "recently_played_list")
                    context.startActivity(intent)
                  }
                } else {
                  MediaUtils.playFile(video, context, "recently_played")
                }
              } else {
                MediaUtils.playFile(video, context, "recently_played")
              }
            },
            onPlaylistClick = { playlistItem ->
              // Navigate to playlist detail screen
              backStack.add(PlaylistDetailScreen(playlistItem.playlist.id))
            },
            modifier = Modifier.padding(padding),
          )
        }
      }

      // Delete confirmation dialog
      DeleteConfirmationDialog(
        isOpen = deleteDialogOpen.value,
        onDismiss = { deleteDialogOpen.value = false },
        onConfirm = {
          coroutineScope.launch {
            if (selectionManager.isInSelectionMode) {
              selectionManager.deleteSelected()
            } else {
              viewModel.clearAllRecentlyPlayed()
            }
            deleteDialogOpen.value = false
          }
        },
        itemType = if (selectionManager.isInSelectionMode) "item(s)" else "recently played history",
        itemCount = if (selectionManager.isInSelectionMode) selectionManager.selectedCount else recentItems.size,
      )
    }
  }
}

@Composable
private fun RecentItemsContent(
  recentItems: List<RecentlyPlayedItem>,
  playlistMode: Boolean,
  playlistRepository: PlaylistRepository,
  selectionManager: com.uw.mpvDroid.ui.browser.selection.SelectionManager<RecentlyPlayedItem, String>,
  onVideoClick: (Video) -> Unit,
  onPlaylistClick: suspend (RecentlyPlayedItem.PlaylistItem) -> Unit,
  modifier: Modifier = Modifier,
) {
  val gesturePreferences = koinInject<GesturePreferences>()
  val tapThumbnailToSelect by gesturePreferences.tapThumbnailToSelect.collectAsState()
  val listState = rememberLazyListState()
  val coroutineScope = rememberCoroutineScope()

  // Check if at top of list to hide scrollbar
  val isAtTop by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
    }
  }

  val hasEnoughItems = recentItems.size > 20

  val scrollbarAlpha by androidx.compose.animation.core.animateFloatAsState(
    targetValue = if (isAtTop || !hasEnoughItems) 0f else 1f,
    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
    label = "scrollbarAlpha",
  )

  LazyColumnScrollbar(
    state = listState,
    settings = ScrollbarSettings(
      thumbUnselectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f * scrollbarAlpha),
      thumbSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = scrollbarAlpha),
    ),
  ) {
    LazyColumn(
      state = listState,
      modifier = modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
    ) {
      items(
        count = recentItems.size,
        key = { index ->
          when (val item = recentItems[index]) {
            is RecentlyPlayedItem.VideoItem -> "video_${item.video.id}_${item.timestamp}"
            is RecentlyPlayedItem.PlaylistItem -> "playlist_${item.playlist.id}_${item.timestamp}"
            is RecentlyPlayedItem.LinkItem -> "link_${item.url.hashCode()}_${item.timestamp}"
          }
        },
      ) { index ->
        when (val item = recentItems[index]) {
          is RecentlyPlayedItem.VideoItem -> {
            VideoCard(
              video = item.video,
              progressPercentage = null,
              isSelected = selectionManager.isSelected(item),
              onClick = {
                if (selectionManager.isInSelectionMode) {
                  selectionManager.toggle(item)
                } else {
                  onVideoClick(item.video)
                }
              },
              onLongClick = {
                selectionManager.toggle(item)
              },
              onThumbClick = if (tapThumbnailToSelect) {
                { selectionManager.toggle(item) }
              } else {
                { onVideoClick(item.video) }
              },
            )
          }

          is RecentlyPlayedItem.PlaylistItem -> {
            val folderModel = VideoFolder(
              bucketId = item.playlist.id.toString(),
              name = item.playlist.name,
              path = "",
              videoCount = item.videoCount,
              totalSize = 0,
              totalDuration = 0,
              lastModified = item.playlist.updatedAt / 1000,
            )
            FolderCard(
              folder = folderModel,
              isSelected = selectionManager.isSelected(item),
              isRecentlyPlayed = false,
              onClick = {
                if (selectionManager.isInSelectionMode) {
                  selectionManager.toggle(item)
                } else {
                  coroutineScope.launch {
                    onPlaylistClick(item)
                  }
                }
              },
              onLongClick = {
                selectionManager.toggle(item)
              },
              onThumbClick = {
                if (tapThumbnailToSelect && selectionManager.isInSelectionMode) {
                  selectionManager.toggle(item)
                } else {
                  coroutineScope.launch {
                    onPlaylistClick(item)
                  }
                }
              },
              customIcon = Icons.Filled.PlaylistPlay,
              showDateModified = true,
            )
          }

          is RecentlyPlayedItem.LinkItem -> {
            val context = androidx.compose.ui.platform.LocalContext.current
            com.uw.mpvDroid.ui.browser.cards.LinkCard(
              url = item.url,
              displayName = item.displayName,
              timestamp = item.timestamp,
              isSelected = selectionManager.isSelected(item),
              onClick = {
                if (selectionManager.isInSelectionMode) {
                  selectionManager.toggle(item)
                } else {
                  MediaUtils.playFile(item.url, context, "recently_played_link")
                }
              },
              onLongClick = {
                selectionManager.toggle(item)
              },
              onCopy = {
                val clipboardManager = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) 
                  as? android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Video URL", item.url)
                clipboardManager?.setPrimaryClip(clip)
                android.widget.Toast.makeText(context, "URL copied to clipboard", android.widget.Toast.LENGTH_SHORT).show()
              },
              onShare = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_TEXT, item.url)
                  putExtra(Intent.EXTRA_TITLE, item.displayName)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share video link"))
              },
            )
          }
        }
      }
    }
  }
}

@Composable
private fun RecentVideosContent(
  recentVideos: List<Video>,
  playlistMode: Boolean,
  onVideoClick: (Video) -> Unit,
  modifier: Modifier = Modifier,
) {
  val gesturePreferences = koinInject<GesturePreferences>()
  val tapThumbnailToSelect by gesturePreferences.tapThumbnailToSelect.collectAsState()
  val listState = rememberLazyListState()

  // Check if at top of list to hide scrollbar
  val isAtTop by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
    }
  }

  // Only show scrollbar if list has more than 20 items
  val hasEnoughItems = recentVideos.size > 20

  // Animate scrollbar alpha
  val scrollbarAlpha by androidx.compose.animation.core.animateFloatAsState(
    targetValue = if (isAtTop || !hasEnoughItems) 0f else 1f,
    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
    label = "scrollbarAlpha",
  )

  LazyColumnScrollbar(
    state = listState,
    settings = ScrollbarSettings(
      thumbUnselectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f * scrollbarAlpha),
      thumbSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = scrollbarAlpha),
    ),
  ) {
    LazyColumn(
      state = listState,
      modifier = modifier.fillMaxSize(),
      contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
    ) {
      items(
        count = recentVideos.size,
        key = { index -> "${recentVideos[index].id}_${recentVideos[index].path}_$index" },
      ) { index ->
        val video = recentVideos[index]
        VideoCard(
          video = video,
          progressPercentage = null,
          isSelected = false,
          onClick = { onVideoClick(video) },
          onLongClick = { },
          onThumbClick = if (tapThumbnailToSelect) {
            { }
          } else {
            { onVideoClick(video) }
          },
        )
      }
    }
  }
}
