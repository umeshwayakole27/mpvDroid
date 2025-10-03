package live.uwapps.mpvkt.ui.library.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import live.uwapps.mpvkt.domain.models.*

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LibraryContent(
  groupedVideos: Map<String, List<Video>>,
  viewMode: ViewMode,
  groupOption: GroupOption,
  selectedVideos: Set<String>,
  isDragSelecting: Boolean,
  onVideoClick: (Video) -> Unit,
  onVideoLongClick: (Video) -> Unit,
  onVideoHover: (Video) -> Unit,
  onToggleFavorite: (Video) -> Unit,
  initialScrollPosition: Pair<Int, Int> = Pair(0, 0),
  onSaveScrollPosition: (isGrid: Boolean, index: Int, offset: Int) -> Unit = { _, _, _ -> },
  modifier: Modifier = Modifier
) {
  // Memoize sections calculation to prevent recalculation on every recomposition
  val sections = remember(groupedVideos) {
    groupedVideos.entries
      .sortedWith(compareBy({ if (it.key.isEmpty()) 0 else 1 }, { it.key }))
      .map { it.key to it.value }
  }

  // Persist list/grid states across recompositions for smooth scrolling
  val listState = rememberLazyListState(
    initialFirstVisibleItemIndex = if (viewMode == ViewMode.LIST) initialScrollPosition.first else 0,
    initialFirstVisibleItemScrollOffset = if (viewMode == ViewMode.LIST) initialScrollPosition.second else 0
  )
  val gridState = rememberLazyGridState(
    initialFirstVisibleItemIndex = if (viewMode == ViewMode.GRID) initialScrollPosition.first else 0,
    initialFirstVisibleItemScrollOffset = if (viewMode == ViewMode.GRID) initialScrollPosition.second else 0
  )

  // Save scroll position when component is disposed or scroll state changes
  DisposableEffect(viewMode) {
    onDispose {
      when (viewMode) {
        ViewMode.LIST -> {
          onSaveScrollPosition(
            false,
            listState.firstVisibleItemIndex,
            listState.firstVisibleItemScrollOffset
          )
        }
        ViewMode.GRID -> {
          onSaveScrollPosition(
            true,
            gridState.firstVisibleItemIndex,
            gridState.firstVisibleItemScrollOffset
          )
        }
      }
    }
  }

  // Wrap callbacks in remember to make them stable and prevent unnecessary recompositions
  val stableOnVideoClick = remember { onVideoClick }
  val stableOnVideoLongClick = remember { onVideoLongClick }
  val stableOnVideoHover = remember { onVideoHover }
  val stableOnToggleFavorite = remember { onToggleFavorite }

  when (viewMode) {
    ViewMode.LIST -> LibraryList(
      sections = sections,
      groupOption = groupOption,
      selectedVideos = selectedVideos,
      isDragSelecting = isDragSelecting,
      onVideoClick = stableOnVideoClick,
      onVideoLongClick = stableOnVideoLongClick,
      onVideoHover = stableOnVideoHover,
      onToggleFavorite = stableOnToggleFavorite,
      state = listState,
      modifier = modifier
    )
    ViewMode.GRID -> LibraryGrid(
      sections = sections,
      groupOption = groupOption,
      selectedVideos = selectedVideos,
      isDragSelecting = isDragSelecting,
      onVideoClick = stableOnVideoClick,
      onVideoLongClick = stableOnVideoLongClick,
      onVideoHover = stableOnVideoHover,
      onToggleFavorite = stableOnToggleFavorite,
      state = gridState,
      modifier = modifier
    )
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryList(
  sections: List<Pair<String, List<Video>>>,
  groupOption: GroupOption,
  selectedVideos: Set<String>,
  isDragSelecting: Boolean,
  onVideoClick: (Video) -> Unit,
  onVideoLongClick: (Video) -> Unit,
  onVideoHover: (Video) -> Unit,
  onToggleFavorite: (Video) -> Unit,
  state: androidx.compose.foundation.lazy.LazyListState,
  modifier: Modifier = Modifier
) {
  LazyColumn(
    state = state,
    modifier = modifier,
    contentPadding = PaddingValues(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    sections.forEach { (groupName, videos) ->
      if (groupOption != GroupOption.NONE && groupName.isNotEmpty()) {
        item(
          key = "header-$groupName",
          contentType = "header"
        ) {
          Text(
            text = groupName,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp)
          )
        }
      }

      items(
        items = videos,
        key = { it.path },
        contentType = { "video" }
      ) { video ->
        VideoListItem(
          video = video,
          isSelected = selectedVideos.contains(video.path),
          isDragSelecting = isDragSelecting,
          onVideoClick = onVideoClick,
          onVideoLongClick = onVideoLongClick,
          onVideoHover = onVideoHover,
          onToggleFavorite = onToggleFavorite
        )
      }
    }
  }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LibraryGrid(
  sections: List<Pair<String, List<Video>>>,
  groupOption: GroupOption,
  selectedVideos: Set<String>,
  isDragSelecting: Boolean,
  onVideoClick: (Video) -> Unit,
  onVideoLongClick: (Video) -> Unit,
  onVideoHover: (Video) -> Unit,
  onToggleFavorite: (Video) -> Unit,
  state: androidx.compose.foundation.lazy.grid.LazyGridState,
  modifier: Modifier = Modifier
) {
  LazyVerticalGrid(
    columns = GridCells.Adaptive(160.dp),
    state = state,
    modifier = modifier.fillMaxSize(),
    contentPadding = PaddingValues(16.dp),
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    sections.forEach { (groupName, videos) ->
      if (groupOption != GroupOption.NONE && groupName.isNotEmpty()) {
        item(
          key = "header-$groupName",
          span = { GridItemSpan(maxLineSpan) },
          contentType = "header"
        ) {
          Text(
            text = groupName,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(vertical = 8.dp)
          )
        }
      }

      items(
        items = videos,
        key = { it.path },
        contentType = { "video" }
      ) { video ->
        VideoGridItem(
          video = video,
          isSelected = selectedVideos.contains(video.path),
          isDragSelecting = isDragSelecting,
          onVideoClick = onVideoClick,
          onVideoLongClick = onVideoLongClick,
          onVideoHover = onVideoHover,
          onToggleFavorite = onToggleFavorite
        )
      }
    }
  }
}
