package live.uwapps.mpvkt.ui.library

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.serialization.Serializable
import live.uwapps.mpvkt.presentation.Screen
import live.uwapps.mpvkt.presentation.library.LibraryViewModel
import live.uwapps.mpvkt.ui.util.PlayerLauncher.playFile
import live.uwapps.mpvkt.ui.library.components.*
import live.uwapps.mpvkt.domain.models.Video
import live.uwapps.mpvkt.domain.models.ViewMode
import live.uwapps.mpvkt.ui.components.ExpressiveCircularProgressIndicator
import live.uwapps.mpvkt.ui.components.ExpressiveWavyLinearProgressIndicator
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object LibraryScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val viewModel: LibraryViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Library preferences dialog state
    var showLibraryPreferencesDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmDialog by rememberSaveable { mutableStateOf(false) }

    // Stable memoized callbacks to prevent recomposition
    val onVideoClick = remember(context, viewModel) {
      { video: Video ->
        // If in selection mode, toggle selection instead of playing
        if (uiState.selectedVideos.isNotEmpty()) {
          viewModel.toggleVideoSelection(video)
        } else {
          playFile(video.path, context)
        }
      }
    }

    val onToggleFavorite = remember(viewModel) {
      { video: Video -> viewModel.toggleFavorite(video) }
    }

    val onVideoLongClick = remember(viewModel) {
      { video: Video ->
        viewModel.startDragSelection(video)
      }
    }

    val onVideoHover = remember(viewModel) {
      { video: Video ->
        viewModel.selectVideoOnDrag(video)
      }
    }

    // File document picker - only keep this one
    val documentPicker = rememberLauncherForActivityResult(
      contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
      uri?.let { playFile(it.toString(), context) }
    }

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    // Handle back press when videos are selected
    BackHandler(enabled = uiState.selectedVideos.isNotEmpty()) {
      viewModel.clearVideoSelection()
    }

    // End drag selection when user lifts finger
    DisposableEffect(uiState.isDragSelecting) {
      onDispose {
        if (uiState.isDragSelecting) {
          viewModel.endDragSelection()
        }
      }
    }

    Scaffold(
      modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
      topBar = {
        // Calculate if all selected videos are favorited
        val selectedVideosList = uiState.videos.filter { it.path in uiState.selectedVideos }
        val allSelectedAreFavorited = selectedVideosList.isNotEmpty() &&
          selectedVideosList.all { it.isFavorite }

        LibraryTopBar(
          searchQuery = uiState.searchQuery,
          onSearchQueryChanged = viewModel::onSearchQueryChanged,
          onClearSearch = viewModel::clearSearch,
          onOpenFile = { documentPicker.launch(arrayOf("*/*")) },
          onLibraryPreferences = { showLibraryPreferencesDialog = true },
          selectedVideosCount = uiState.selectedVideos.size,
          allSelectedAreFavorited = allSelectedAreFavorited,
          onDeleteSelected = { showDeleteConfirmDialog = true },
          onToggleFavoriteSelected = {
            // Toggle favorite for all selected videos
            selectedVideosList.forEach { video ->
              viewModel.toggleFavorite(video)
            }
          },
          scrollBehavior = scrollBehavior
        )
      },
      snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
      Column(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues)
      ) {
        if (uiState.isScanning) {
          ScanningProgress(
            progress = uiState.scanProgress,
            modifier = Modifier.padding(16.dp)
          )
        }

        // Display content based on loading state and search results
        when {
          uiState.isLoading -> {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              ExpressiveCircularProgressIndicator()
            }
          }
          uiState.videos.isEmpty() -> {
            EmptyLibraryContent(
              onScanLibrary = viewModel::scanLibrary,
              isSearching = uiState.searchQuery.text.isNotEmpty(),
              modifier = Modifier.fillMaxSize()
            )
          }
          else -> {
            // Get initial scroll position from preferences
            val initialScrollPosition = remember(uiState.viewMode) {
              viewModel.getScrollPosition(uiState.viewMode == ViewMode.GRID)
            }

            // Use key to prevent recomposition when search query changes
            key(uiState.groupedVideos, uiState.viewMode, uiState.groupOption) {
              LibraryContent(
                groupedVideos = uiState.groupedVideos,
                viewMode = uiState.viewMode,
                groupOption = uiState.groupOption,
                selectedVideos = uiState.selectedVideos,
                isDragSelecting = uiState.isDragSelecting,
                onVideoClick = onVideoClick,
                onVideoLongClick = onVideoLongClick,
                onVideoHover = onVideoHover,
                onToggleFavorite = onToggleFavorite,
                initialScrollPosition = initialScrollPosition,
                onSaveScrollPosition = viewModel::saveScrollPosition,
                modifier = Modifier.fillMaxSize()
              )
            }
          }
        }

        // Library Preferences Dialog
        if (showLibraryPreferencesDialog) {
          LibraryPreferencesDialog(
            uiState = uiState,
            viewModel = viewModel,
            onDismiss = { showLibraryPreferencesDialog = false }
          )
        }

        // Delete Confirmation Dialog
        if (showDeleteConfirmDialog) {
          AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Videos") },
            text = {
              Text("Are you sure you want to delete ${uiState.selectedVideos.size} video(s)? This action cannot be undone.")
            },
            confirmButton = {
              Button(
                onClick = {
                  viewModel.deleteSelectedVideos()
                  showDeleteConfirmDialog = false
                },
                colors = ButtonDefaults.buttonColors(
                  containerColor = MaterialTheme.colorScheme.error
                )
              ) {
                Text("Delete")
              }
            },
            dismissButton = {
              TextButton(onClick = { showDeleteConfirmDialog = false }) {
                Text("Cancel")
              }
            }
          )
        }
      }
    }
  }
}

@Composable
private fun EmptyLibraryContent(
  onScanLibrary: () -> Unit,
  isSearching: Boolean,
  modifier: Modifier = Modifier
) {
  Box(
    modifier = modifier.padding(16.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = if (isSearching) Icons.Default.SearchOff else Icons.Default.VideoLibrary,
        contentDescription = null,
        modifier = Modifier.size(64.dp),
        tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
      )
      Spacer(modifier = Modifier.height(16.dp))
      Text(
        text = if (isSearching) "No videos found" else "Library is empty",
        style = MaterialTheme.typography.headlineSmall
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = if (isSearching) "Your search returned no results." else "Scan your device to find videos.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      if (!isSearching) {
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onScanLibrary) {
          Icon(Icons.Default.Refresh, contentDescription = null)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Scan Library")
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryTopBar(
  searchQuery: TextFieldValue,
  onSearchQueryChanged: (TextFieldValue) -> Unit,
  onClearSearch: () -> Unit,
  onOpenFile: () -> Unit,
  onLibraryPreferences: () -> Unit,
  selectedVideosCount: Int,
  allSelectedAreFavorited: Boolean,
  onDeleteSelected: () -> Unit,
  onToggleFavoriteSelected: () -> Unit,
  modifier: Modifier = Modifier,
  scrollBehavior: TopAppBarScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
) {
  var isSearching by rememberSaveable { mutableStateOf(searchQuery.text.isNotEmpty()) }

  // Keep active when text is non-empty (so user won't lose the field while editing)
  LaunchedEffect(searchQuery.text) {
    if (searchQuery.text.isNotEmpty()) isSearching = true
  }

  // Handle back press to exit search instead of leaving the screen
  BackHandler(enabled = isSearching) {
    onClearSearch()
    isSearching = false
  }

  // Stable callbacks
  val onSearchClick = remember { { isSearching = true } }
  val onBackClick = remember {
    {
      onClearSearch()
      isSearching = false
    }
  }

  Surface(
    modifier = modifier.fillMaxWidth(),
    shape = RoundedCornerShape(
      bottomStart = 24.dp,
      bottomEnd = 24.dp
    ),
    tonalElevation = 3.dp,
    shadowElevation = 3.dp
  ) {
    LargeTopAppBar(
      title = {
        if (isSearching) {
          SearchBarContent(
            searchQuery = searchQuery,
            onSearchQueryChanged = onSearchQueryChanged,
            onBackClick = onBackClick
          )
        } else if (selectedVideosCount > 0) {
          Text("$selectedVideosCount selected")
        } else {
          Text("Video Library")
        }
      },
      actions = {
        // When videos are selected, show only delete and favorite buttons
        if (selectedVideosCount > 0) {
          IconButton(onClick = onToggleFavoriteSelected) {
            Icon(
              imageVector = if (allSelectedAreFavorited) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Toggle favorites"
            )
          }
          IconButton(onClick = onDeleteSelected) {
            Icon(Icons.Default.Delete, contentDescription = "Delete selected videos")
          }
        } else {
          // Normal actions when no selection
          if (!isSearching) {
            IconButton(onClick = onSearchClick) {
              Icon(Icons.Default.Search, contentDescription = "Search")
            }
          }
          IconButton(onClick = onOpenFile) {
            Icon(Icons.Default.FileOpen, contentDescription = "Open file")
          }
          IconButton(onClick = onLibraryPreferences) {
            Icon(Icons.Default.VideoSettings, contentDescription = "Library preferences")
          }
        }
      },
      scrollBehavior = scrollBehavior,
      colors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface
      )
    )
  }
}

@Composable
private fun SearchBarContent(
  searchQuery: TextFieldValue,
  onSearchQueryChanged: (TextFieldValue) -> Unit,
  onBackClick: () -> Unit
) {
  val focusRequester = remember { FocusRequester() }
  val keyboard = LocalSoftwareKeyboardController.current

  // Focus the field when first shown
  LaunchedEffect(Unit) {
    focusRequester.requestFocus()
    keyboard?.show()
  }

  // Stable callback for clearing text
  val onClearText = remember {
    { onSearchQueryChanged(TextFieldValue("")) }
  }

  // Memoize colors to prevent recreation
  val textFieldColors = remember {
    @Composable {
      TextFieldDefaults.colors(
        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
        focusedTextColor = MaterialTheme.colorScheme.onSurface,
        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
      )
    }
  }

  Box(
    modifier = Modifier
      .fillMaxWidth()
      .animateContentSize(animationSpec = spring())
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth()
        .clip(MaterialTheme.shapes.extraLarge),
      tonalElevation = 3.dp,
      shadowElevation = 3.dp,
      shape = MaterialTheme.shapes.extraLarge,
      color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
      TextField(
        value = searchQuery,
        onValueChange = onSearchQueryChanged,
        placeholder = {
          Text("Search videos…")
        },
        leadingIcon = {
          IconButton(onClick = onBackClick) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        trailingIcon = {
          if (searchQuery.text.isNotEmpty()) {
            IconButton(onClick = onClearText) {
              Icon(Icons.Default.Clear, contentDescription = "Clear text")
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .focusRequester(focusRequester),
        colors = textFieldColors(),
        textStyle = MaterialTheme.typography.bodyLarge,
        singleLine = true,
        shape = MaterialTheme.shapes.extraLarge
      )
    }
  }
}

@Composable
private fun ScanningProgress(
  progress: Float,
  modifier: Modifier = Modifier
) {
  Card(modifier = modifier.fillMaxWidth()) {
    Column(
      modifier = Modifier.padding(16.dp)
    ) {
      Text(
        text = "Scanning for videos...",
        style = MaterialTheme.typography.titleMedium
      )
      Spacer(modifier = Modifier.height(12.dp))
      ExpressiveWavyLinearProgressIndicator(
        progress = progress,
        modifier = Modifier.fillMaxWidth()
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "${(progress * 100).toInt()}% complete",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}
