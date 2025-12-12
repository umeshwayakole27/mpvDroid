package com.uw.mpvDroid.ui.browser.folderlist

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.core.tween
import com.uw.mpvDroid.ui.theme.MotionTokens
import my.nanihadesuka.compose.LazyColumnScrollbar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uw.mpvDroid.domain.media.model.Video
import com.uw.mpvDroid.domain.media.model.VideoFolder
import com.uw.mpvDroid.preferences.AppearancePreferences
import com.uw.mpvDroid.preferences.BrowserPreferences
import com.uw.mpvDroid.preferences.FolderSortType
import com.uw.mpvDroid.preferences.FolderViewMode
import com.uw.mpvDroid.preferences.GesturePreferences
import com.uw.mpvDroid.preferences.SortOrder
import com.uw.mpvDroid.preferences.VideoSortType
import com.uw.mpvDroid.preferences.preference.collectAsState
import com.uw.mpvDroid.presentation.Screen
import com.uw.mpvDroid.presentation.components.pullrefresh.PullRefreshBox
import com.uw.mpvDroid.repository.VideoRepository
import com.uw.mpvDroid.ui.browser.cards.FolderCard
import com.uw.mpvDroid.ui.browser.cards.VideoCard
import com.uw.mpvDroid.ui.browser.components.BrowserTopBar
import com.uw.mpvDroid.ui.browser.dialogs.DeleteConfirmationDialog
import com.uw.mpvDroid.ui.browser.dialogs.SortDialog
import com.uw.mpvDroid.ui.browser.dialogs.ViewModeSelector
import com.uw.mpvDroid.ui.browser.dialogs.VisibilityToggle
import com.uw.mpvDroid.ui.browser.fab.MediaActionFab
import com.uw.mpvDroid.ui.browser.selection.rememberSelectionManager
import com.uw.mpvDroid.ui.browser.sheets.PlayLinkSheet
import com.uw.mpvDroid.ui.browser.states.EmptyState
import com.uw.mpvDroid.ui.browser.states.PermissionDeniedState
import com.uw.mpvDroid.ui.browser.videolist.VideoListScreen
import com.uw.mpvDroid.ui.utils.LocalBackStack
import com.uw.mpvDroid.utils.media.CopyPasteOps
import com.uw.mpvDroid.utils.media.MediaInfoOps
import com.uw.mpvDroid.utils.media.MediaUtils
import com.uw.mpvDroid.utils.permission.PermissionUtils
import com.uw.mpvDroid.utils.sort.SortUtils
import com.google.accompanist.permissions.PermissionStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import my.nanihadesuka.compose.ScrollbarSettings
import org.koin.compose.koinInject
import java.io.File

@Serializable
object FolderListScreen : Screen {
  
  @Composable
  override fun Content() {
    val browserPreferences = koinInject<BrowserPreferences>()
    val folderViewMode by browserPreferences.folderViewMode.collectAsState()
    
    // Shared dialog state that persists across view mode changes
    val sortDialogOpen = rememberSaveable { mutableStateOf(false) }

    // Switch between different view modes
    when (folderViewMode) {
      FolderViewMode.AllVideos -> AllVideosContent(sortDialogOpen)
      FolderViewMode.MediaStore -> MediaStoreFolderListContent(sortDialogOpen)
    }
  }

  @Composable
  private fun MediaStoreFolderListContent(sortDialogOpen: MutableState<Boolean>) {
    val context = LocalContext.current
    val viewModel: FolderListViewModel =
      viewModel(factory = FolderListViewModel.factory(context.applicationContext as android.app.Application))
    val videoFolders by viewModel.videoFolders.collectAsState()
    val recentlyPlayedFilePath by viewModel.recentlyPlayedFilePath.collectAsState()
    val backstack = LocalBackStack.current
    val coroutineScope = rememberCoroutineScope()
    val browserPreferences = koinInject<BrowserPreferences>()
    val videoRepository = koinInject<VideoRepository>()
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // UI State
    val listState = rememberLazyListState()
    val isRefreshing = remember { mutableStateOf(false) }
    val showLinkDialog = remember { mutableStateOf(false) }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var hasRecentlyPlayed by remember { mutableStateOf(false) }
    val deleteDialogOpen = rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var isSearching by rememberSaveable { mutableStateOf(false) }
    var allVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var videosLoaded by remember { mutableStateOf(false) }

    // Sorting
    val folderSortType by browserPreferences.folderSortType.collectAsState()
    val folderSortOrder by browserPreferences.folderSortOrder.collectAsState()

    // View mode
    val sortedFolders =
      remember(videoFolders, folderSortType, folderSortOrder) {
        SortUtils.sortFolders(videoFolders, folderSortType, folderSortOrder)
      }
    val filteredFolders = if (isSearching && searchQuery.isNotBlank()) {
      sortedFolders.filter { folder ->
        folder.name.contains(searchQuery, ignoreCase = true) ||
          folder.path.contains(searchQuery, ignoreCase = true)
      }
    } else {
      sortedFolders
    }

    // Selection manager (folders handle deletion through videos)
    val selectionManager =
      rememberSelectionManager(
        items = sortedFolders,
        getId = { it.bucketId },
        onDeleteItems = { folders ->
          // Delete all videos in selected folders via ViewModel
          val ids = folders.map { it.bucketId }.toSet()
          val videos = videoRepository.getVideosForBuckets(context, ids)
          viewModel.deleteVideos(videos)
          Pair(videos.size, 0) // Return (successCount, failureCount)
        },
        onOperationComplete = { viewModel.refresh() },
      )

    // Permissions
    val permissionState =
      PermissionUtils.handleStoragePermission(
        onPermissionGranted = { viewModel.refresh() },
      )

    // File picker
    val filePicker =
      rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
      ) { uri ->
        uri?.let {
          runCatching {
            context.contentResolver.takePersistableUriPermission(
              it,
              Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
          }
          MediaUtils.playFile(it.toString(), context, "open_file")
        }
      }

    // Effects
    LaunchedEffect(Unit) {
      hasRecentlyPlayed =
        com.uw.mpvDroid.utils.history.RecentlyPlayedOps
          .hasRecentlyPlayed()
    }

    LaunchedEffect(fabMenuExpanded) {
      if (fabMenuExpanded) {
        hasRecentlyPlayed =
          com.uw.mpvDroid.utils.history.RecentlyPlayedOps
            .hasRecentlyPlayed()
      }
    }

    LaunchedEffect(isSearching) {
      if (isSearching && !videosLoaded) {
        // Load all videos across all folders using all bucketIds
        val bucketIds = videoFolders.map { it.bucketId }.toSet()
        allVideos = videoRepository.getVideosForBuckets(context, bucketIds)
        videosLoaded = true
      }
      if (!isSearching) {
        videosLoaded = false
        allVideos = emptyList()
      }
      if (isSearching) {
        focusRequester.requestFocus()
        keyboardController?.show()
      }
    }

    val filteredVideos = if (isSearching && searchQuery.isNotBlank() && videosLoaded) {
      allVideos.filter { video ->
        video.title.contains(searchQuery, ignoreCase = true) ||
          video.displayName.contains(searchQuery, ignoreCase = true) ||
          video.path.contains(searchQuery, ignoreCase = true)
      }
    } else emptyList()

    // Predictive back: Only intercept when in selection mode OR search mode
    androidx.activity.compose.BackHandler(enabled = selectionManager.isInSelectionMode || isSearching) {
      when {
        selectionManager.isInSelectionMode -> selectionManager.clear()
        isSearching -> {
          isSearching = false
          searchQuery = ""
        }
      }
    }

    Scaffold(
      topBar = {
        if (isSearching) {
          // Search mode - show search bar instead of top bar
          SearchBar(
            inputField = {
              SearchBarDefaults.InputField(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { },
                expanded = false,
                onExpandedChange = { },
                placeholder = { Text("Search videos...") },
                leadingIcon = {
                  Icon(
                    imageVector = Icons.Filled.Search,
                    contentDescription = "Search",
                  )
                },
                trailingIcon = {
                  IconButton(
                    onClick = {
                      isSearching = false
                      searchQuery = ""
                    },
                  ) {
                    Icon(
                      imageVector = Icons.Filled.Close,
                      contentDescription = "Cancel",
                    )
                  }
                },
                modifier = Modifier.focusRequester(focusRequester),
              )
            },
            expanded = false,
            onExpandedChange = { },
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 8.dp),
            shape = RoundedCornerShape(28.dp),
            tonalElevation = 6.dp,
          ) {
            // Empty content for SearchBar
          }
        } else {
          BrowserTopBar(
            title = stringResource(com.uw.mpvDroid.R.string.app_name),
            isInSelectionMode = selectionManager.isInSelectionMode,
            selectedCount = selectionManager.selectedCount,
            totalCount = videoFolders.size,
            onBackClick = null, // No back button for folder list (root screen)
            onCancelSelection = { selectionManager.clear() },
            onSortClick = { sortDialogOpen.value = true },
            onSearchClick = { isSearching = !isSearching },
            onDeleteClick = { deleteDialogOpen.value = true },
            onRenameClick = null,
            isSingleSelection = selectionManager.isSingleSelection,
            onInfoClick = null,
            onShareClick = {
              // Share all videos across selected folders with a single chooser
              coroutineScope.launch {
                val selectedIds = selectionManager.getSelectedItems().map { it.bucketId }.toSet()
                val allVideos = videoRepository.getVideosForBuckets(context, selectedIds)
                if (allVideos.isNotEmpty()) {
                  MediaUtils.shareVideos(context, allVideos)
                }
              }
            },
            onPlayClick = {
              // Play all videos from selected folders as a playlist
              coroutineScope.launch {
                val selectedIds = selectionManager.getSelectedItems().map { it.bucketId }.toSet()
                val allVideos = videoRepository.getVideosForBuckets(context, selectedIds)
                if (allVideos.isNotEmpty()) {
                  if (allVideos.size == 1) {
                    // Single video - play normally
                    MediaUtils.playFile(allVideos.first(), context)
                  } else {
                    // Multiple videos - play as playlist
                    val intent = Intent(Intent.ACTION_VIEW, allVideos.first().uri)
                    intent.setClass(context, com.uw.mpvDroid.ui.player.PlayerActivity::class.java)
                    intent.putExtra("internal_launch", true)
                    intent.putParcelableArrayListExtra("playlist", ArrayList(allVideos.map { it.uri }))
                    intent.putExtra("playlist_index", 0)
                    intent.putExtra("launch_source", "playlist")
                    context.startActivity(intent)
                  }
                  // Clear selection after starting playback
                  selectionManager.clear()
                }
              }
            },
            onSelectAll = { selectionManager.selectAll() },
            onInvertSelection = { selectionManager.invertSelection() },
            onDeselectAll = { selectionManager.clear() },
          )
        }
      },
      floatingActionButton = {
        if (videoFolders.isNotEmpty()) {
          MediaActionFab(
              listState = listState,
              hasRecentlyPlayed = hasRecentlyPlayed,
              onOpenFile = { filePicker.launch(arrayOf("video/*")) },
              onPlayRecentlyPlayed = {
                coroutineScope.launch {
                val lastPlayedEntity = com.uw.mpvDroid.utils.history.RecentlyPlayedOps
                  .getLastPlayedEntity()

                if (lastPlayedEntity != null) {
                  // Check if this was played from a playlist
                  if (lastPlayedEntity.playlistId != null) {
                    // Load the full playlist and play from the most recently played video
                    val playlistRepository =
                      org.koin.java.KoinJavaComponent.get<com.uw.mpvDroid.database.repository.PlaylistRepository>(
                        com.uw.mpvDroid.database.repository.PlaylistRepository::class.java,
                      )
                    val videoRepository =
                      org.koin.java.KoinJavaComponent.get<com.uw.mpvDroid.repository.VideoRepository>(
                        com.uw.mpvDroid.repository.VideoRepository::class.java,
                      )
                    val playlistItems = playlistRepository.getPlaylistItems(lastPlayedEntity.playlistId)

                    if (playlistItems.isNotEmpty()) {
                      // Get unique folders (bucketIds) from playlist items
                      // For each video, extract its parent folder and query that bucket
                      val pathToBucketMap = mutableMapOf<String, String>()
                      val bucketIds = mutableSetOf<String>()

                      playlistItems.forEach { item ->
                        val file = java.io.File(item.filePath)
                        val parentPath = file.parent
                        if (parentPath != null) {
                          val normalizedPath = parentPath.replace("\\", "/")
                          pathToBucketMap[item.filePath] = normalizedPath
                          bucketIds.add(normalizedPath)
                        }
                      }

                      // Get all videos from those buckets
                      val allVideos = videoRepository.getVideosForBuckets(context, bucketIds)

                      // Match videos by path, maintaining playlist order
                      val videos = playlistItems.mapNotNull { item ->
                        allVideos.find { video -> video.path == item.filePath }
                      }

                      if (videos.isNotEmpty()) {
                        // Find the most recently played video in this playlist
                        val mostRecentItem = playlistItems
                          .filter { it.lastPlayedAt > 0 }
                          .maxByOrNull { it.lastPlayedAt }

                        val startIndex = if (mostRecentItem != null) {
                          videos.indexOfFirst { it.path == mostRecentItem.filePath }
                        } else {
                          0
                        }

                        val validStartIndex = if (startIndex >= 0) startIndex else 0
                        val uris = videos.map { it.uri }

                        val intent = Intent(
                          context,
                          com.uw.mpvDroid.ui.player.PlayerActivity::class.java,
                        ).apply {
                          action = Intent.ACTION_VIEW
                          data = uris[validStartIndex]
                          putParcelableArrayListExtra("playlist", ArrayList(uris))
                          putExtra("playlist_index", validStartIndex)
                          putExtra("launch_source", "playlist")
                          putExtra("playlist_id", lastPlayedEntity.playlistId)
                        }
                        context.startActivity(intent)
                      }
                    }
                  } else {
                    // Just play the single video
                    MediaUtils.playFile(lastPlayedEntity.filePath, context, "recently_played_button")
                  }
                }
                }
              },
              expanded = fabMenuExpanded,
              onExpandedChange = { fabMenuExpanded = it },
            )
        }
      },
    ) { padding ->
      when (permissionState.status) {
        PermissionStatus.Granted -> {
          if (isSearching) {
            // Search results
            if (searchQuery.isNotBlank() && videosLoaded) {
              if (filteredVideos.isEmpty()) {
                Box(
                  modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                  contentAlignment = Alignment.Center,
                ) {
                  EmptyState(
                    icon = Icons.Filled.Folder,
                    title = "No videos found",
                    message = "Try a different search term.",
                  )
                }
              } else {
                val searchListState = rememberLazyListState()

                // Check if at top of list to hide scrollbar
                val isAtTop by remember {
                  derivedStateOf {
                    searchListState.firstVisibleItemIndex == 0 && searchListState.firstVisibleItemScrollOffset == 0
                  }
                }

                // Only show scrollbar if list has more than 20 items
                val hasEnoughItems = filteredVideos.size > 20

                // Animate scrollbar alpha
                val scrollbarAlpha by androidx.compose.animation.core.animateFloatAsState(
                  targetValue = if (isAtTop || !hasEnoughItems) 0f else 1f,
                  animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
                  label = "scrollbarAlpha",
                )

                LazyColumnScrollbar(
                  state = searchListState,
                  settings = ScrollbarSettings(
                    thumbUnselectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f * scrollbarAlpha),
                    thumbSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = scrollbarAlpha),
                  ),
                ) {
                  LazyColumn(
                    state = searchListState,
                    modifier = Modifier
                      .fillMaxSize()
                      .padding(padding),
                    contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
                  ) {
                    items(filteredVideos) { video ->
                      VideoCard(
                        video = video,
                        progressPercentage = null,
                        isRecentlyPlayed = false,
                        isSelected = false,
                        onClick = { MediaUtils.playFile(video, context, "search") },
                        onLongClick = {},
                        onThumbClick = {},
                      )
                    }
                  }
                }
              }
            }
          } else {
            // Normal mode - show folder list
            FolderListContent(
              folders = filteredFolders,
              listState = listState,
              isRefreshing = isRefreshing,
              recentlyPlayedFilePath = recentlyPlayedFilePath,
              onRefresh = { viewModel.refresh() },
              selectionManager = selectionManager,
              onFolderClick = { folder ->
                if (selectionManager.isInSelectionMode) {
                  selectionManager.toggle(folder)
                } else {
                  fabMenuExpanded = false
                  backstack.add(VideoListScreen(folder.bucketId, folder.name))
                }
              },
              onFolderLongClick = { folder -> selectionManager.toggle(folder) },
              modifier = Modifier.padding(padding),
            )
          }
        }

        is PermissionStatus.Denied -> {
          PermissionDeniedState(
            onRequestPermission = { permissionState.launchPermissionRequest() },
            modifier = Modifier.padding(padding),
          )
        }
      }

      // Dialogs
      PlayLinkSheet(
        isOpen = showLinkDialog.value,
        onDismiss = { showLinkDialog.value = false },
        onPlayLink = { url -> MediaUtils.playFile(url, context, "play_link") },
      )

      FolderSortDialog(
        isOpen = sortDialogOpen.value,
        onDismiss = { sortDialogOpen.value = false },
        sortType = folderSortType,
        sortOrder = folderSortOrder,
        onSortTypeChange = { browserPreferences.folderSortType.set(it) },
        onSortOrderChange = { browserPreferences.folderSortOrder.set(it) },
      )

      DeleteConfirmationDialog(
        isOpen = deleteDialogOpen.value,
        onDismiss = { deleteDialogOpen.value = false },
        onConfirm = { selectionManager.deleteSelected() },
        itemType = "folder",
        itemCount = selectionManager.selectedCount,
      )
    }
  }


  @Composable
  private fun AllVideosContent(sortDialogOpen: MutableState<Boolean>) {
    val context = LocalContext.current
    val viewModel: FolderListViewModel =
      viewModel(factory = FolderListViewModel.factory(context.applicationContext as android.app.Application))
    val videoFolders by viewModel.videoFolders.collectAsState()
    val videoRepository = koinInject<VideoRepository>()
    val browserPreferences = koinInject<BrowserPreferences>()
    val appearancePreferences = koinInject<AppearancePreferences>()
    val backstack = LocalBackStack.current
    val coroutineScope = rememberCoroutineScope()

    // UI State
    val listState = rememberLazyListState()
    val isRefreshing = remember { mutableStateOf(false) }
    val showLinkDialog = remember { mutableStateOf(false) }
    var fabMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var hasRecentlyPlayed by remember { mutableStateOf(false) }
    var allVideos by remember { mutableStateOf<List<Video>>(emptyList()) }
    var allVideosLoaded by remember { mutableStateOf(false) }

    val videoSortType by browserPreferences.videoSortType.collectAsState()
    val videoSortOrder by browserPreferences.videoSortOrder.collectAsState()

    // Permissions
    val permissionState =
      PermissionUtils.handleStoragePermission(
        onPermissionGranted = {},
      )

    // File picker
    val filePicker =
      rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
      ) { uri ->
        uri?.let {
          runCatching {
            context.contentResolver.takePersistableUriPermission(
              it,
              Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
          }
          MediaUtils.playFile(it.toString(), context, "open_file")
        }
      }

    // Load all videos - now watching videoFolders state properly
    LaunchedEffect(videoFolders) {
      if (videoFolders.isNotEmpty()) {
        launch(Dispatchers.IO) {
          val bucketIds = videoFolders.map { it.bucketId }.toSet()
          allVideos = videoRepository.getVideosForBuckets(context, bucketIds)
          allVideosLoaded = true
        }
      } else {
        // Mark as loaded if no folders (show empty state)
        allVideosLoaded = true
        allVideos = emptyList()
      }
    }

    LaunchedEffect(fabMenuExpanded) {
      if (fabMenuExpanded) {
        hasRecentlyPlayed =
          com.uw.mpvDroid.utils.history.RecentlyPlayedOps
            .hasRecentlyPlayed()
      }
    }

    val sortedVideos =
      remember(allVideos, videoSortType, videoSortOrder) {
        com.uw.mpvDroid.utils.sort.SortUtils.sortVideos(allVideos, videoSortType, videoSortOrder)
      }

    // Selection manager
    val selectionManager =
      rememberSelectionManager(
        items = sortedVideos,
        getId = { it.id },
        onDeleteItems = { viewModel.deleteVideos(it) },
        onRenameItem = { video, newName -> viewModel.renameVideo(video, newName) },
        onOperationComplete = { viewModel.refresh() },
      )

    // Additional UI State for selection mode
    val deleteDialogOpen = rememberSaveable { mutableStateOf(false) }
    val renameDialogOpen = rememberSaveable { mutableStateOf(false) }
    val mediaInfoDialogOpen = rememberSaveable { mutableStateOf(false) }
    val addToPlaylistDialogOpen = rememberSaveable { mutableStateOf(false) }
    val selectedVideo = remember { mutableStateOf<Video?>(null) }
    val mediaInfoData = remember { mutableStateOf<MediaInfoOps.MediaInfoData?>(null) }
    val mediaInfoLoading = remember { mutableStateOf(false) }
    val mediaInfoError = remember { mutableStateOf<String?>(null) }

    // Copy/Move state
    val folderPickerOpen = rememberSaveable { mutableStateOf(false) }
    val operationType = remember { mutableStateOf<CopyPasteOps.OperationType?>(null) }
    val progressDialogOpen = rememberSaveable { mutableStateOf(false) }
    val operationProgress by CopyPasteOps.operationProgress.collectAsState()

    // Predictive back: Only intercept when in selection mode
    androidx.activity.compose.BackHandler(enabled = selectionManager.isInSelectionMode) {
      selectionManager.clear()
    }

    // Control selection bottom bar through parent
    val setSelectionBar = com.uw.mpvDroid.ui.utils.LocalSelectionBottomBar.current
    val setShowBottomNav = com.uw.mpvDroid.ui.utils.LocalShowBottomNav.current
    
    // Detect scroll direction and hide/show bottom bar
    androidx.compose.runtime.LaunchedEffect(listState) {
      var previousIndex = listState.firstVisibleItemIndex
      var previousScrollOffset = listState.firstVisibleItemScrollOffset
      
      snapshotFlow {
        listState.firstVisibleItemIndex to listState.firstVisibleItemScrollOffset
      }.collect { (currentIndex, currentOffset) ->
        val isScrollingDown = when {
          currentIndex > previousIndex -> true
          currentIndex < previousIndex -> false
          else -> currentOffset > previousScrollOffset
        }
        
        // Hide on scroll down, show on scroll up (but not in selection mode)
        if (!selectionManager.isInSelectionMode) {
          if (isScrollingDown && currentOffset > 50) {
            setShowBottomNav(false)
          } else if (!isScrollingDown) {
            setShowBottomNav(true)
          }
        }
        
        previousIndex = currentIndex
        previousScrollOffset = currentOffset
      }
    }
    
    androidx.compose.runtime.LaunchedEffect(selectionManager.isInSelectionMode) {
      // Always show bottom bar in selection mode
      if (selectionManager.isInSelectionMode) {
        setShowBottomNav(true)
        setSelectionBar(
          com.uw.mpvDroid.ui.utils.SelectionBottomBarState(
            isVisible = true,
            onCopyClick = {
              operationType.value = CopyPasteOps.OperationType.Copy
              folderPickerOpen.value = true
            },
            onMoveClick = {
              operationType.value = CopyPasteOps.OperationType.Move
              folderPickerOpen.value = true
            },
            onRenameClick = { renameDialogOpen.value = true },
            onDeleteClick = { deleteDialogOpen.value = true },
            onAddToPlaylistClick = { addToPlaylistDialogOpen.value = true },
          )
        )
      } else {
        setSelectionBar(com.uw.mpvDroid.ui.utils.SelectionBottomBarState(isVisible = false))
      }
    }

    Scaffold(
      topBar = {
        BrowserTopBar(
          title = "All Videos",
          isInSelectionMode = selectionManager.isInSelectionMode,
          selectedCount = selectionManager.selectedCount,
          totalCount = sortedVideos.size,
          onCancelSelection = { selectionManager.clear() },
          onSortClick = { sortDialogOpen.value = true },
          onDeleteClick = null, // Hide from top bar, it's in bottom bar
          onRenameClick = null, // Hide from top bar, it's in bottom bar
          isSingleSelection = selectionManager.isSingleSelection,
          onInfoClick = if (selectionManager.isSingleSelection) {
            {
              val video = selectionManager.getSelectedItems().firstOrNull()
              if (video != null) {
                selectedVideo.value = video
                mediaInfoDialogOpen.value = true
                mediaInfoLoading.value = true
                mediaInfoError.value = null
                mediaInfoData.value = null

                coroutineScope.launch {
                  MediaInfoOps
                    .getMediaInfo(context, video.uri, video.displayName)
                    .onSuccess { info ->
                      mediaInfoData.value = info
                      mediaInfoLoading.value = false
                    }.onFailure { error ->
                      mediaInfoError.value = error.message ?: "Unknown error"
                      mediaInfoLoading.value = false
                    }
                }
              }
            }
          } else null,
          onShareClick = if (selectionManager.isInSelectionMode) {
            { selectionManager.shareSelected() }
          } else null,
          onPlayClick = if (selectionManager.isInSelectionMode) {
            { selectionManager.playSelected() }
          } else null,
          onSelectAll = { selectionManager.selectAll() },
          onInvertSelection = { selectionManager.invertSelection() },
          onDeselectAll = { selectionManager.clear() },
        )
      },
      floatingActionButton = {
        androidx.compose.animation.AnimatedVisibility(
          visible = !selectionManager.isInSelectionMode,
          enter = androidx.compose.animation.scaleIn(
            animationSpec = androidx.compose.animation.core.tween(
              durationMillis = 200,
              easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
          ) + androidx.compose.animation.fadeIn(
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 200)
          ),
          exit = androidx.compose.animation.scaleOut(
            animationSpec = androidx.compose.animation.core.tween(
              durationMillis = 200,
              easing = androidx.compose.animation.core.FastOutSlowInEasing
            )
          ) + androidx.compose.animation.fadeOut(
            animationSpec = androidx.compose.animation.core.tween(durationMillis = 200)
          )
        ) {
          MediaActionFab(
            listState = listState,
            hasRecentlyPlayed = hasRecentlyPlayed,
            onOpenFile = { filePicker.launch(arrayOf("video/*")) },
            onPlayRecentlyPlayed = {
              coroutineScope.launch {
                val lastPlayedPath = com.uw.mpvDroid.utils.history.RecentlyPlayedOps.getLastPlayed()
                if (lastPlayedPath != null) {
                  MediaUtils.playFile(lastPlayedPath, context, "recently_played")
                }
              }
            },
            expanded = fabMenuExpanded,
            onExpandedChange = { fabMenuExpanded = it },
          )
        }
      },
    ) { padding ->
      Box(modifier = Modifier.fillMaxSize()) {
        when {
          permissionState.status !is PermissionStatus.Granted -> {
            Box(
              modifier = Modifier.fillMaxSize().padding(padding),
              contentAlignment = Alignment.Center,
            ) {
              PermissionDeniedState(
                onRequestPermission = { permissionState.launchPermissionRequest() },
              )
            }
          }

          !allVideosLoaded -> {
            Box(
              modifier = Modifier.fillMaxSize().padding(padding),
              contentAlignment = Alignment.Center,
            ) {
              androidx.compose.material3.CircularProgressIndicator()
            }
          }

          sortedVideos.isEmpty() -> {
            Box(
              modifier = Modifier.fillMaxSize().padding(padding),
              contentAlignment = Alignment.Center,
            ) {
              EmptyState(
                icon = Icons.Filled.VideoLibrary,
                title = "No videos found",
                message = "Add some video files to your device to see them here",
              )
            }
          }

          else -> {
            val gesturePreferences = koinInject<GesturePreferences>()
            val tapThumbnailToSelect by gesturePreferences.tapThumbnailToSelect.collectAsState()

            PullRefreshBox(
              isRefreshing = isRefreshing,
              onRefresh = {
                viewModel.refresh()
                val folders = viewModel.videoFolders.value
                if (folders.isNotEmpty()) {
                  val bucketIds = folders.map { it.bucketId }.toSet()
                  allVideos = videoRepository.getVideosForBuckets(context, bucketIds)
                }
              },
              listState = listState,
              modifier = Modifier.fillMaxSize().padding(padding),
            ) {
              LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(8.dp),
              ) {
                items(
                  count = sortedVideos.size,
                  key = { index -> sortedVideos[index].id },
                ) { index ->
                  val video = sortedVideos[index]
                  VideoCard(
                    video = video,
                    isSelected = selectionManager.isSelected(video),
                    onClick = {
                      if (selectionManager.isInSelectionMode) {
                        selectionManager.toggle(video)
                      } else {
                        MediaUtils.playFile(video, context, "all_videos")
                      }
                    },
                    onLongClick = { selectionManager.toggle(video) },
                    onThumbClick = if (tapThumbnailToSelect) {
                      { selectionManager.toggle(video) }
                    } else {
                      {
                        if (selectionManager.isInSelectionMode) {
                          selectionManager.toggle(video)
                        } else {
                          MediaUtils.playFile(video, context, "all_videos")
                        }
                      }
                    },
                  )
                }
              }
            }
          }
        }
      }

      // Sort Dialog
      VideoSortDialog(
        isOpen = sortDialogOpen.value,
        onDismiss = { sortDialogOpen.value = false },
        videoSortType = videoSortType,
        videoSortOrder = videoSortOrder,
        onVideoSortTypeChange = { browserPreferences.videoSortType.set(it) },
        onVideoSortOrderChange = { browserPreferences.videoSortOrder.set(it) },
      )

      // Link dialog
      PlayLinkSheet(
        isOpen = showLinkDialog.value,
        onDismiss = { showLinkDialog.value = false },
        onPlayLink = { link ->
          MediaUtils.playFile(link, context, "link")
          showLinkDialog.value = false
        },
      )

      // Delete Dialog
      com.uw.mpvDroid.ui.browser.dialogs.DeleteConfirmationDialog(
        isOpen = deleteDialogOpen.value,
        onDismiss = { deleteDialogOpen.value = false },
        onConfirm = { selectionManager.deleteSelected() },
        itemType = "video",
        itemCount = selectionManager.selectedCount,
      )

      // Rename Dialog
      if (renameDialogOpen.value && selectionManager.isSingleSelection) {
        val video = selectionManager.getSelectedItems().firstOrNull()
        if (video != null) {
          val baseName = video.displayName.substringBeforeLast('.')
          val extension = "." + video.displayName.substringAfterLast('.', "")
          com.uw.mpvDroid.ui.browser.dialogs.RenameDialog(
            isOpen = true,
            onDismiss = { renameDialogOpen.value = false },
            onConfirm = { newName -> selectionManager.renameSelected(newName) },
            currentName = baseName,
            itemType = "file",
            extension = if (extension != ".") extension else null,
          )
        }
      }

      // Media Info Dialog
      com.uw.mpvDroid.ui.browser.dialogs.MediaInfoDialog(
        isOpen = mediaInfoDialogOpen.value,
        onDismiss = {
          mediaInfoDialogOpen.value = false
          selectedVideo.value = null
          mediaInfoData.value = null
          mediaInfoError.value = null
        },
        fileName = selectedVideo.value?.displayName ?: "",
        mediaInfo = mediaInfoData.value,
        isLoading = mediaInfoLoading.value,
        error = mediaInfoError.value,
        videoForShare = selectedVideo.value,
      )

      // Folder Picker Dialog
      com.uw.mpvDroid.ui.browser.dialogs.FolderPickerDialog(
        isOpen = folderPickerOpen.value,
        currentPath = "",
        onDismiss = { folderPickerOpen.value = false },
        onFolderSelected = { destinationPath ->
          folderPickerOpen.value = false
          val selectedVideos = selectionManager.getSelectedItems()
          if (selectedVideos.isNotEmpty() && operationType.value != null) {
            progressDialogOpen.value = true
            coroutineScope.launch {
              when (operationType.value) {
                is CopyPasteOps.OperationType.Copy -> {
                  CopyPasteOps.copyFiles(context, selectedVideos, destinationPath)
                }
                is CopyPasteOps.OperationType.Move -> {
                  CopyPasteOps.moveFiles(context, selectedVideos, destinationPath)
                }
                else -> {}
              }
            }
          }
        },
      )

      // File Operation Progress Dialog
      if (operationType.value != null) {
        com.uw.mpvDroid.ui.browser.dialogs.FileOperationProgressDialog(
          isOpen = progressDialogOpen.value,
          operationType = operationType.value!!,
          progress = operationProgress,
          onCancel = {
            CopyPasteOps.cancelOperation()
          },
          onDismiss = {
            progressDialogOpen.value = false
            operationType.value = null
            selectionManager.clear()
            viewModel.refresh()
          },
        )
      }

      // Add to Playlist Dialog
      com.uw.mpvDroid.ui.browser.dialogs.AddToPlaylistDialog(
        isOpen = addToPlaylistDialogOpen.value,
        videos = selectionManager.getSelectedItems(),
        onDismiss = { addToPlaylistDialogOpen.value = false },
        onSuccess = {
          selectionManager.clear()
          viewModel.refresh()
        },
      )
    }
  }
}

@Composable
private fun FolderListContent(
  folders: List<VideoFolder>,
  listState: LazyListState,
  isRefreshing: MutableState<Boolean>,
  recentlyPlayedFilePath: String?,
  onRefresh: suspend () -> Unit,
  selectionManager: com.uw.mpvDroid.ui.browser.selection.SelectionManager<VideoFolder, String>,
  onFolderClick: (VideoFolder) -> Unit,
  onFolderLongClick: (VideoFolder) -> Unit,
  modifier: Modifier = Modifier,
) {
  val gesturePreferences = koinInject<GesturePreferences>()
  val tapThumbnailToSelect by gesturePreferences.tapThumbnailToSelect.collectAsState()

  // Avoid brief empty-state flicker by delaying its appearance slightly
  val showEmpty =
    remember(folders) {
      mutableStateOf(false)
    }
  LaunchedEffect(folders) {
    if (folders.isEmpty()) {
      kotlinx.coroutines.delay(250)
      showEmpty.value = folders.isEmpty()
    } else {
      showEmpty.value = false
    }
  }

  // Check if at top of list to hide scrollbar during pull-to-refresh
  val isAtTop by remember {
    derivedStateOf {
      listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
    }
  }

  // Only show scrollbar if list has more than 20 items
  val hasEnoughItems = folders.size > 20

  // Animate scrollbar alpha
  val scrollbarAlpha by androidx.compose.animation.core.animateFloatAsState(
    targetValue = if (isAtTop || !hasEnoughItems) 0f else 1f,
    animationSpec = androidx.compose.animation.core.tween(durationMillis = 200),
    label = "scrollbarAlpha",
  )

  PullRefreshBox(
    isRefreshing = isRefreshing,
    onRefresh = onRefresh,
    listState = listState,
    modifier = modifier.fillMaxSize(),
  ) {
    LazyColumnScrollbar(
      state = listState,
      settings = ScrollbarSettings(
        thumbUnselectedColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f * scrollbarAlpha),
        thumbSelectedColor = MaterialTheme.colorScheme.primary.copy(alpha = scrollbarAlpha),
      ),
    ) {
      LazyColumn(
        state = listState,
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp),
      ) {
        // Regular folders
        items(folders, key = { it.bucketId }) { folder ->
          val isRecentlyPlayed =
            recentlyPlayedFilePath?.let { filePath ->
              val file = File(filePath)
              file.parent == folder.path
            } ?: false

          FolderCard(
            folder = folder,
            isSelected = selectionManager.isSelected(folder),
            isRecentlyPlayed = isRecentlyPlayed,
            onClick = { onFolderClick(folder) },
            onLongClick = { onFolderLongClick(folder) },
            onThumbClick = if (tapThumbnailToSelect) {
              { onFolderLongClick(folder) }
            } else {
              { onFolderClick(folder) }
            },
            modifier = Modifier.animateItem(
              fadeInSpec = tween(durationMillis = MotionTokens.DurationMedium1),
              fadeOutSpec = tween(durationMillis = MotionTokens.DurationShort2),
              placementSpec = tween(durationMillis = MotionTokens.DurationMedium2)
            )
          )
        }

        if (showEmpty.value) {
          item {
            EmptyState(
              icon = Icons.Filled.Folder,
              title = "No video folders found",
              message = "Add some video files to your device to see them here",
            )
          }
        }
      }
    }
  }
}

@Composable
private fun FolderSortDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  sortType: FolderSortType,
  sortOrder: SortOrder,
  onSortTypeChange: (FolderSortType) -> Unit,
  onSortOrderChange: (SortOrder) -> Unit,
) {
  val browserPreferences = koinInject<BrowserPreferences>()
  val appearancePreferences = koinInject<AppearancePreferences>()
  val showTotalVideosChip by browserPreferences.showTotalVideosChip.collectAsState()
  val showTotalDurationChip by browserPreferences.showTotalDurationChip.collectAsState()
  val showTotalSizeChip by browserPreferences.showTotalSizeChip.collectAsState()
  val showFolderPath by browserPreferences.showFolderPath.collectAsState()
  val showSizeChip by browserPreferences.showSizeChip.collectAsState()
  val showResolutionChip by browserPreferences.showResolutionChip.collectAsState()
  val showFramerateInResolution by browserPreferences.showFramerateInResolution.collectAsState()
  val showProgressBar by browserPreferences.showProgressBar.collectAsState()
  val unlimitedNameLines by appearancePreferences.unlimitedNameLines.collectAsState()
  val folderViewMode by browserPreferences.folderViewMode.collectAsState()

  // Dynamic dialog based on view mode
  val isInFolderView = folderViewMode == FolderViewMode.MediaStore

  SortDialog(
    isOpen = isOpen,
    onDismiss = onDismiss,
    title = "Sort & View Options",
    sortType = sortType.displayName,
    onSortTypeChange = { typeName ->
      FolderSortType.entries.find { it.displayName == typeName }?.let(onSortTypeChange)
    },
    sortOrderAsc = sortOrder.isAscending,
    onSortOrderChange = { isAsc ->
      onSortOrderChange(if (isAsc) SortOrder.Ascending else SortOrder.Descending)
    },
    types =
      listOf(
        FolderSortType.Title.displayName,
        FolderSortType.Date.displayName,
        FolderSortType.Size.displayName,
      ),
    icons =
      listOf(
        Icons.Filled.Title,
        Icons.Filled.CalendarToday,
        Icons.Filled.SwapVert,
      ),
    getLabelForType = { type, _ ->
      when (type) {
        FolderSortType.Title.displayName -> Pair("A-Z", "Z-A")
        FolderSortType.Date.displayName -> Pair("Oldest", "Newest")
        FolderSortType.Size.displayName -> Pair("Smallest", "Largest")
        else -> Pair("Asc", "Desc")
      }
    },
    showSortOptions = isInFolderView,
    viewModeSelector =
      ViewModeSelector(
        label = "View Mode",
        firstOptionLabel = "Folder View",
        secondOptionLabel = "All Videos",
        firstOptionIcon = Icons.Filled.ViewModule,
        secondOptionIcon = Icons.Filled.VideoLibrary,
        isFirstOptionSelected = folderViewMode == FolderViewMode.MediaStore,
        onViewModeChange = { isFirstOption ->
          browserPreferences.folderViewMode.set(
            if (isFirstOption) FolderViewMode.MediaStore else FolderViewMode.AllVideos,
          )
        },
      ),
    visibilityToggles =
      buildList {
        add(
          VisibilityToggle(
            label = "Full Name",
            checked = unlimitedNameLines,
            onCheckedChange = { appearancePreferences.unlimitedNameLines.set(it) },
          )
        )
        if (isInFolderView) {
          add(
            VisibilityToggle(
              label = "Path",
              checked = showFolderPath,
              onCheckedChange = { browserPreferences.showFolderPath.set(it) },
            )
          )
          add(
            VisibilityToggle(
              label = "Total Videos",
              checked = showTotalVideosChip,
              onCheckedChange = { browserPreferences.showTotalVideosChip.set(it) },
            )
          )
          add(
            VisibilityToggle(
              label = "Total Duration",
              checked = showTotalDurationChip,
              onCheckedChange = { browserPreferences.showTotalDurationChip.set(it) },
            )
          )
          add(
            VisibilityToggle(
              label = "Folder Size",
              checked = showTotalSizeChip,
              onCheckedChange = { browserPreferences.showTotalSizeChip.set(it) },
            )
          )
        }
        add(
          VisibilityToggle(
            label = "Size",
            checked = showSizeChip,
            onCheckedChange = { browserPreferences.showSizeChip.set(it) },
          )
        )
        add(
          VisibilityToggle(
            label = "Resolution",
            checked = showResolutionChip,
            onCheckedChange = { browserPreferences.showResolutionChip.set(it) },
          )
        )
        add(
          VisibilityToggle(
            label = "Framerate",
            checked = showFramerateInResolution,
            onCheckedChange = { browserPreferences.showFramerateInResolution.set(it) },
          )
        )
        add(
          VisibilityToggle(
            label = "Progress Bar",
            checked = showProgressBar,
            onCheckedChange = { browserPreferences.showProgressBar.set(it) },
          )
        )
      },
  )
}

@Composable
private fun VideoSortDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  videoSortType: VideoSortType,
  videoSortOrder: SortOrder,
  onVideoSortTypeChange: (VideoSortType) -> Unit,
  onVideoSortOrderChange: (SortOrder) -> Unit,
) {
  val browserPreferences = koinInject<BrowserPreferences>()
  val appearancePreferences = koinInject<AppearancePreferences>()
  val showTotalVideosChip by browserPreferences.showTotalVideosChip.collectAsState()
  val showTotalDurationChip by browserPreferences.showTotalDurationChip.collectAsState()
  val showTotalSizeChip by browserPreferences.showTotalSizeChip.collectAsState()
  val showFolderPath by browserPreferences.showFolderPath.collectAsState()
  val showSizeChip by browserPreferences.showSizeChip.collectAsState()
  val showResolutionChip by browserPreferences.showResolutionChip.collectAsState()
  val showFramerateInResolution by browserPreferences.showFramerateInResolution.collectAsState()
  val showProgressBar by browserPreferences.showProgressBar.collectAsState()
  val unlimitedNameLines by appearancePreferences.unlimitedNameLines.collectAsState()
  val folderViewMode by browserPreferences.folderViewMode.collectAsState()

  // Dynamic dialog based on view mode
  val isInFolderView = folderViewMode == FolderViewMode.MediaStore

  SortDialog(
    isOpen = isOpen,
    onDismiss = onDismiss,
    title = "Sort & View Options",
    sortType = videoSortType.displayName,
    onSortTypeChange = { typeName ->
      VideoSortType.entries.find { it.displayName == typeName }?.let(onVideoSortTypeChange)
    },
    sortOrderAsc = videoSortOrder.isAscending,
    onSortOrderChange = { isAsc ->
      onVideoSortOrderChange(if (isAsc) SortOrder.Ascending else SortOrder.Descending)
    },
    types =
      listOf(
        VideoSortType.Title.displayName,
        VideoSortType.Duration.displayName,
        VideoSortType.Date.displayName,
        VideoSortType.Size.displayName,
      ),
    icons =
      listOf(
        Icons.Filled.Title,
        Icons.Filled.AccessTime,
        Icons.Filled.CalendarToday,
        Icons.Filled.SwapVert,
      ),
    getLabelForType = { type, _ ->
      when (type) {
        VideoSortType.Title.displayName -> Pair("A-Z", "Z-A")
        VideoSortType.Duration.displayName -> Pair("Shortest", "Longest")
        VideoSortType.Date.displayName -> Pair("Oldest", "Newest")
        VideoSortType.Size.displayName -> Pair("Smallest", "Biggest")
        else -> Pair("Asc", "Desc")
      }
    },
    showSortOptions = true,
    viewModeSelector =
      ViewModeSelector(
        label = "View Mode",
        firstOptionLabel = "Folder View",
        secondOptionLabel = "All Videos",
        firstOptionIcon = Icons.Filled.ViewModule,
        secondOptionIcon = Icons.Filled.VideoLibrary,
        isFirstOptionSelected = folderViewMode == FolderViewMode.MediaStore,
        onViewModeChange = { isFirstOption ->
          browserPreferences.folderViewMode.set(
            if (isFirstOption) FolderViewMode.MediaStore else FolderViewMode.AllVideos,
          )
        },
      ),
    visibilityToggles =
      buildList {
        add(
          VisibilityToggle(
            label = "Full Name",
            checked = unlimitedNameLines,
            onCheckedChange = { appearancePreferences.unlimitedNameLines.set(it) },
          )
        )
        if (isInFolderView) {
          add(
            VisibilityToggle(
              label = "Path",
              checked = showFolderPath,
              onCheckedChange = { browserPreferences.showFolderPath.set(it) },
            )
          )
          add(
            VisibilityToggle(
              label = "Total Videos",
              checked = showTotalVideosChip,
              onCheckedChange = { browserPreferences.showTotalVideosChip.set(it) },
            )
          )
          add(
            VisibilityToggle(
              label = "Total Duration",
              checked = showTotalDurationChip,
              onCheckedChange = { browserPreferences.showTotalDurationChip.set(it) },
            )
          )
          add(
            VisibilityToggle(
              label = "Folder Size",
              checked = showTotalSizeChip,
              onCheckedChange = { browserPreferences.showTotalSizeChip.set(it) },
            )
          )
        }
        add(
          VisibilityToggle(
            label = "Size",
            checked = showSizeChip,
            onCheckedChange = { browserPreferences.showSizeChip.set(it) },
          )
        )
        add(
          VisibilityToggle(
            label = "Resolution",
            checked = showResolutionChip,
            onCheckedChange = { browserPreferences.showResolutionChip.set(it) },
          )
        )
        add(
          VisibilityToggle(
            label = "Framerate",
            checked = showFramerateInResolution,
            onCheckedChange = { browserPreferences.showFramerateInResolution.set(it) },
          )
        )
        add(
          VisibilityToggle(
            label = "Progress Bar",
            checked = showProgressBar,
            onCheckedChange = { browserPreferences.showProgressBar.set(it) },
          )
        )
      },
  )
}
