package live.uwapps.mpvkt.domain.library

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.Dispatchers
import live.uwapps.mpvkt.database.repository.VideoLibraryRepository
import live.uwapps.mpvkt.domain.models.FilterOption
import live.uwapps.mpvkt.domain.models.Folder
import live.uwapps.mpvkt.domain.models.GroupOption
import live.uwapps.mpvkt.domain.models.SortOption
import live.uwapps.mpvkt.domain.models.Video
import me.xdrop.fuzzywuzzy.FuzzySearch
import live.uwapps.mpvkt.util.SearchText
import java.io.File

class GetVideosUseCase(
  private val repository: VideoLibraryRepository,
) {
  operator fun invoke(
    sortOption: SortOption = SortOption.DATE_ADDED_DESC,
    filterOption: FilterOption = FilterOption.ALL,
    searchQuery: String = "",
    folderPaths: Set<String> = emptySet(),
  ): Flow<List<Video>> {
    val baseFlow = when (filterOption) {
      FilterOption.ALL -> if (folderPaths.isEmpty()) {
        repository.getAllVideos()
      } else {
        repository.getVideosInFolders(folderPaths)
      }
      FilterOption.FAVORITES -> repository.getFavoriteVideos()
      FilterOption.WATCHED -> repository.getWatchedVideos()
      FilterOption.UNWATCHED -> repository.getAllVideos().map { videos ->
        videos.filter { !it.isWatched }
      }
      FilterOption.PARTIALLY_WATCHED -> repository.getPartiallyWatchedVideos()
      FilterOption.RECENT -> repository.getAllVideos().map { videos ->
        val weekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
        videos.filter { it.dateAdded > weekAgo }
      }
    }

    return baseFlow
      .map { videos ->
        val qn = SearchText.normalize(searchQuery)
        val filteredVideos = if (qn.isNotBlank()) {
          videos.map { video ->
            val titleN = SearchText.normalize(video.title)
            val displayNameN = SearchText.normalize(video.displayName)
            val folderNameN = SearchText.normalize(video.folderName)

            val titleScore = FuzzySearch.partialRatio(qn, titleN)
            val displayNameScore = FuzzySearch.partialRatio(qn, displayNameN)
            val folderScore = FuzzySearch.partialRatio(qn, folderNameN)
            val maxScore = maxOf(titleScore, displayNameScore, folderScore)
            video to maxScore
          }
            .filter { it.second >= 75 } // Threshold for matching
            .sortedByDescending { it.second }
            .map { it.first }
        } else {
          videos
        }

        // Apply sorting
        when (sortOption) {
          SortOption.NAME_ASC -> filteredVideos.sortedBy { it.displayName }
          SortOption.NAME_DESC -> filteredVideos.sortedByDescending { it.displayName }
          SortOption.DATE_ADDED_ASC -> filteredVideos.sortedBy { it.dateAdded }
          SortOption.DATE_ADDED_DESC -> filteredVideos.sortedByDescending { it.dateAdded }
          SortOption.DATE_MODIFIED_ASC -> filteredVideos.sortedBy { it.dateModified }
          SortOption.DATE_MODIFIED_DESC -> filteredVideos.sortedByDescending { it.dateModified }
          SortOption.SIZE_ASC -> filteredVideos.sortedBy { it.size }
          SortOption.SIZE_DESC -> filteredVideos.sortedByDescending { it.size }
          SortOption.DURATION_ASC -> filteredVideos.sortedBy { it.duration }
          SortOption.DURATION_DESC -> filteredVideos.sortedByDescending { it.duration }
          SortOption.LAST_WATCHED_ASC -> filteredVideos.sortedBy { it.lastWatchedTime ?: 0 }
          SortOption.LAST_WATCHED_DESC -> filteredVideos.sortedByDescending { it.lastWatchedTime ?: 0 }
        }
      }
      .flowOn(Dispatchers.Default)
  }
}

class GroupVideosUseCase {
  operator fun invoke(videos: List<Video>, groupOption: GroupOption): Map<String, List<Video>> {
    return when (groupOption) {
      GroupOption.NONE -> mapOf("" to videos)
      GroupOption.FOLDER -> videos.groupBy { it.folderName }
      GroupOption.FORMAT -> videos.groupBy { it.format?.uppercase() ?: "Unknown" }
      GroupOption.DURATION -> videos.groupBy {
        when {
          it.duration < 10 * 60 * 1000 -> "Short (< 10 min)"
          it.duration < 30 * 60 * 1000 -> "Medium (10-30 min)"
          it.duration < 60 * 60 * 1000 -> "Long (30-60 min)"
          it.duration < 120 * 60 * 1000 -> "Very Long (1-2 hours)"
          else -> "Movie (> 2 hours)"
        }
      }
      GroupOption.SIZE -> videos.groupBy {
        when {
          it.size < 100 * 1024 * 1024 -> "Small (< 100 MB)"
          it.size < 500 * 1024 * 1024 -> "Medium (100-500 MB)"
          it.size < 1024 * 1024 * 1024 -> "Large (500 MB - 1 GB)"
          else -> "Very Large (> 1 GB)"
        }
      }
      GroupOption.DATE_ADDED -> videos.groupBy {
        val now = System.currentTimeMillis()
        val dayMs = 24 * 60 * 60 * 1000
        when {
          it.dateAdded > now - dayMs -> "Today"
          it.dateAdded > now - 7 * dayMs -> "This Week"
          it.dateAdded > now - 30 * dayMs -> "This Month"
          it.dateAdded > now - 365 * dayMs -> "This Year"
          else -> "Older"
        }
      }
    }
  }
}

class GetFoldersUseCase(
  private val repository: VideoLibraryRepository
) {
  operator fun invoke(): Flow<List<Folder>> = repository.getAllFolders()
}

class ToggleFavoriteUseCase(
  private val repository: VideoLibraryRepository
) {
  suspend operator fun invoke(video: Video) {
    repository.updateFavoriteStatus(video.path, !video.isFavorite)
  }
}

class UpdateWatchProgressUseCase(
  private val repository: VideoLibraryRepository
) {
  suspend operator fun invoke(videoPath: String, progress: Float) {
    repository.updateWatchProgress(videoPath, progress)

    // Mark as watched if progress is above 90%
    if (progress >= 0.9f) {
      repository.updateWatchedStatus(videoPath, true)
    }
  }
}

class GetLibraryStatsUseCase(
  private val repository: VideoLibraryRepository
) {
  suspend operator fun invoke(): LibraryStats {
    return LibraryStats(
      totalVideos = repository.getVideoCount(),
      totalSize = repository.getTotalSize(),
      totalDuration = repository.getTotalDuration()
    )
  }
}

class DeleteVideoUseCase(
  private val repository: VideoLibraryRepository
) {
  suspend operator fun invoke(video: Video): Boolean {
    return try {
      // Delete the physical file
      val file = File(video.path)
      val fileDeleted = if (file.exists()) {
        file.delete()
      } else {
        true // File already doesn't exist
      }

      // Delete from database
      repository.deleteVideo(video)

      fileDeleted
    } catch (e: Exception) {
      e.printStackTrace()
      false
    }
  }
}

data class LibraryStats(
  val totalVideos: Int,
  val totalSize: Long,
  val totalDuration: Long
)
