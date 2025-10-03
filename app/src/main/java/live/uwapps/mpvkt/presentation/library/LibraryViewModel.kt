package live.uwapps.mpvkt.presentation.library

import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import live.uwapps.mpvkt.domain.library.*
import live.uwapps.mpvkt.domain.models.*
import live.uwapps.mpvkt.domain.scanner.VideoScanner
import live.uwapps.mpvkt.preferences.AppearancePreferences
import live.uwapps.mpvkt.preferences.LibraryPreferences

class LibraryViewModel(
  private val getVideosUseCase: GetVideosUseCase,
  private val groupVideosUseCase: GroupVideosUseCase,
  private val getFoldersUseCase: GetFoldersUseCase,
  private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
  private val updateWatchProgressUseCase: UpdateWatchProgressUseCase,
  private val getLibraryStatsUseCase: GetLibraryStatsUseCase,
  private val videoScanner: VideoScanner,
  private val appearancePreferences: AppearancePreferences,
  private val libraryPreferences: LibraryPreferences,
  private val deleteVideoUseCase: DeleteVideoUseCase
) : ViewModel() {

  private val _uiState = MutableStateFlow(LibraryUiState())
  val uiState: StateFlow<LibraryUiState> = _uiState.asStateFlow()

  // A separate flow for user-driven changes to avoid re-triggering database queries unnecessarily
  private val userPreferencesFlow = MutableStateFlow(UserPreferences())

  init {
    loadSavedPreferences()
    loadLibraryData()
  }

  private fun loadSavedPreferences() {
    // Load persisted preferences
    val savedSortOption = libraryPreferences.sortOption.get()
    val savedFilterOption = libraryPreferences.filterOption.get()
    val savedGroupOption = libraryPreferences.groupOption.get()
    val savedViewMode = libraryPreferences.viewMode.get()
    val savedFolderPath = libraryPreferences.selectedFolderPath.get().ifEmpty { null }

    // Initialize userPreferencesFlow with saved values
    userPreferencesFlow.value = UserPreferences(
      sortOption = savedSortOption,
      filterOption = savedFilterOption,
      groupOption = savedGroupOption,
      selectedFolderPath = savedFolderPath
    )

    // Initialize UI state with saved view mode
    _uiState.value = _uiState.value.copy(
      sortOption = savedSortOption,
      filterOption = savedFilterOption,
      groupOption = savedGroupOption,
      viewMode = savedViewMode,
      selectedFolderPath = savedFolderPath
    )
  }

  @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
  private fun loadLibraryData() {
    viewModelScope.launch {
      // Debounce rapid successive changes (e.g., typing in search) to avoid jank
      userPreferencesFlow
        .debounce(250)
        .flatMapLatest { prefs ->
          combine(
            getVideosUseCase(
              sortOption = prefs.sortOption,
              filterOption = prefs.filterOption,
              searchQuery = prefs.searchQuery.text
            ),
            getFoldersUseCase()
          ) { allVideos, folders ->
            // Always store the original, unfiltered list
            originalVideos = allVideos

            // Group the videos off the main thread
            val groupedVideos = withContext(Dispatchers.Default) {
              if (prefs.groupOption != GroupOption.NONE) {
                groupVideosUseCase(allVideos, prefs.groupOption)
              } else {
                mapOf("" to allVideos)
              }
            }

            // Return a new state object
            _uiState.value.copy(
              videos = allVideos,
              groupedVideos = groupedVideos,
              folders = folders,
              isLoading = false,
              sortOption = prefs.sortOption,
              filterOption = prefs.filterOption,
              groupOption = prefs.groupOption,
              searchQuery = prefs.searchQuery,
              selectedFolderPath = prefs.selectedFolderPath
            )
          }
        }.collect { newState ->
          _uiState.value = newState
        }
    }
  }

  fun onSearchQueryChanged(query: TextFieldValue) {
    // Immediately update the UI state so typing is smooth
    _uiState.value = _uiState.value.copy(searchQuery = query)
    // Also update the preferences flow for the debounced search
    userPreferencesFlow.value = userPreferencesFlow.value.copy(searchQuery = query)
  }

  fun clearSearch() {
    val emptyQuery = TextFieldValue("")
    // Immediately update the UI state
    _uiState.value = _uiState.value.copy(searchQuery = emptyQuery)
    // Also update the preferences flow
    userPreferencesFlow.value = userPreferencesFlow.value.copy(searchQuery = emptyQuery)
  }

  fun onSortOptionChanged(sortOption: SortOption) {
    userPreferencesFlow.value = userPreferencesFlow.value.copy(sortOption = sortOption)
    libraryPreferences.sortOption.set(sortOption)
  }

  fun onFilterOptionChanged(filterOption: FilterOption) {
    userPreferencesFlow.value = userPreferencesFlow.value.copy(filterOption = filterOption)
    libraryPreferences.filterOption.set(filterOption)
  }

  fun onGroupOptionChanged(groupOption: GroupOption) {
    userPreferencesFlow.value = userPreferencesFlow.value.copy(groupOption = groupOption)
    libraryPreferences.groupOption.set(groupOption)
  }

  fun onViewModeChanged(viewMode: ViewMode) {
    _uiState.value = _uiState.value.copy(viewMode = viewMode)
    libraryPreferences.viewMode.set(viewMode)
  }

  fun toggleFavorite(video: Video) {
    viewModelScope.launch {
      toggleFavoriteUseCase(video)
    }
  }

  fun updateWatchProgress(videoPath: String, progress: Float) {
    viewModelScope.launch {
      updateWatchProgressUseCase(videoPath, progress)
    }
  }

  fun selectFolder(folderPath: String?) {
    userPreferencesFlow.value = userPreferencesFlow.value.copy(selectedFolderPath = folderPath)
    libraryPreferences.selectedFolderPath.set(folderPath ?: "")
  }

  fun clearFolderSelection() {
    userPreferencesFlow.value = userPreferencesFlow.value.copy(selectedFolderPath = null)
    libraryPreferences.selectedFolderPath.set("")
  }

  fun deleteVideo(video: Video) {
    viewModelScope.launch {
      deleteVideoUseCase(video)
    }
  }

  fun toggleVideoSelection(video: Video) {
    val currentSelection = _uiState.value.selectedVideos
    _uiState.value = if (currentSelection.contains(video.path)) {
      _uiState.value.copy(selectedVideos = currentSelection - video.path)
    } else {
      _uiState.value.copy(selectedVideos = currentSelection + video.path)
    }
  }

  fun clearVideoSelection() {
    _uiState.value = _uiState.value.copy(selectedVideos = emptySet(), isDragSelecting = false)
  }

  fun startDragSelection(video: Video) {
    _uiState.value = _uiState.value.copy(
      isDragSelecting = true,
      selectedVideos = setOf(video.path)
    )
  }

  fun endDragSelection() {
    _uiState.value = _uiState.value.copy(isDragSelecting = false)
  }

  fun selectVideoOnDrag(video: Video) {
    if (_uiState.value.isDragSelecting) {
      val currentSelection = _uiState.value.selectedVideos
      if (!currentSelection.contains(video.path)) {
        _uiState.value = _uiState.value.copy(selectedVideos = currentSelection + video.path)
      }
    }
  }

  fun deleteSelectedVideos() {
    viewModelScope.launch {
      val videosToDelete = _uiState.value.videos.filter { it.path in _uiState.value.selectedVideos }
      videosToDelete.forEach { video ->
        deleteVideoUseCase(video)
      }
      clearVideoSelection()
    }
  }

  // Helper to get all videos from current state or reload if needed
  private fun getAllVideosFromState(): List<Video> {
    // Use original videos if available, otherwise current videos
    return if (originalVideos.isNotEmpty()) originalVideos else _uiState.value.videos
  }

  // Update loadLibraryData to store original videos
  private var originalVideos: List<Video> = emptyList()

  fun scanLibrary() {
    viewModelScope.launch {
      _uiState.value = _uiState.value.copy(isScanning = true, scanProgress = 0f)

      var lastUpdateTs = 0L
      var lastProgress = 0f

      videoScanner.scanAllVideos { processed, total ->
        val progress = if (total > 0) processed.toFloat() / total else 0f
        val now = System.currentTimeMillis()
        if (progress - lastProgress >= 0.01f || now - lastUpdateTs >= 100) {
          lastProgress = progress
          lastUpdateTs = now
          _uiState.value = _uiState.value.copy(scanProgress = progress)
        }
      }

      _uiState.value = _uiState.value.copy(isScanning = false, scanProgress = 1f)
      // Reset user preferences to trigger a full reload
      userPreferencesFlow.value = UserPreferences()
    }
  }

  fun saveScrollPosition(isGrid: Boolean, index: Int, offset: Int) {
    if (isGrid) {
      libraryPreferences.lastGridScrollIndex.set(index)
      libraryPreferences.lastGridScrollOffset.set(offset)
    } else {
      libraryPreferences.lastListScrollIndex.set(index)
      libraryPreferences.lastListScrollOffset.set(offset)
    }
  }

  fun getScrollPosition(isGrid: Boolean): Pair<Int, Int> {
    return if (isGrid) {
      Pair(
        libraryPreferences.lastGridScrollIndex.get(),
        libraryPreferences.lastGridScrollOffset.get()
      )
    } else {
      Pair(
        libraryPreferences.lastListScrollIndex.get(),
        libraryPreferences.lastListScrollOffset.get()
      )
    }
  }
}

data class LibraryUiState(
  val videos: List<Video> = emptyList(),
  val groupedVideos: Map<String, List<Video>> = emptyMap(),
  val folders: List<Folder> = emptyList(),
  val selectedFolderPath: String? = null,
  val selectedVideos: Set<String> = emptySet(),
  val isDragSelecting: Boolean = false,
  val searchQuery: TextFieldValue = TextFieldValue(""),
  val sortOption: SortOption = SortOption.DATE_ADDED_DESC,
  val filterOption: FilterOption = FilterOption.ALL,
  val groupOption: GroupOption = GroupOption.NONE,
  val viewMode: ViewMode = ViewMode.GRID,
  val isLoading: Boolean = true,
  val isScanning: Boolean = false,
  val scanProgress: Float = 0f,
  val libraryStats: LibraryStats? = null
)

// A new data class to hold all user-configurable preferences
private data class UserPreferences(
  val searchQuery: TextFieldValue = TextFieldValue(""),
  val sortOption: SortOption = SortOption.DATE_ADDED_DESC,
  val filterOption: FilterOption = FilterOption.ALL,
  val groupOption: GroupOption = GroupOption.NONE,
  val selectedFolderPath: String? = null
)
