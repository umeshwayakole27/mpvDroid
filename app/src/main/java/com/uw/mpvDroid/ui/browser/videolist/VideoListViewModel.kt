package com.uw.mpvDroid.ui.browser.videolist

import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.uw.mpvDroid.domain.media.model.Video
import com.uw.mpvDroid.domain.playbackstate.repository.PlaybackStateRepository
import com.uw.mpvDroid.repository.VideoRepository
import com.uw.mpvDroid.ui.browser.base.BaseBrowserViewModel
import com.uw.mpvDroid.utils.media.MediaLibraryEvents
import com.uw.mpvDroid.utils.media.MediaStoreObserver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

data class VideoWithPlaybackInfo(
  val video: Video,
  val timeRemaining: Long? = null, // in seconds
  val progressPercentage: Float? = null, // 0.0 to 1.0
)

class VideoListViewModel(
  application: Application,
  private val bucketId: String,
) : BaseBrowserViewModel(application),
  KoinComponent {
  private val playbackStateRepository: PlaybackStateRepository by inject()
  private val videoRepository: VideoRepository by inject()

  private val _videos = MutableStateFlow<List<Video>>(emptyList())
  val videos: StateFlow<List<Video>> = _videos.asStateFlow()

  private val _videosWithPlaybackInfo = MutableStateFlow<List<VideoWithPlaybackInfo>>(emptyList())
  val videosWithPlaybackInfo: StateFlow<List<VideoWithPlaybackInfo>> = _videosWithPlaybackInfo.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  // MediaStore observer for external changes
  private val mediaStoreObserver = MediaStoreObserver(application, viewModelScope)

  private val tag = "VideoListViewModel"

  init {
    loadVideos()
    // Start observing MediaStore for external changes
    viewModelScope.launch {
      mediaStoreObserver.startObserving()
    }
    // Listen for global media library changes and refresh this list when they occur
    viewModelScope.launch(Dispatchers.IO) {
      MediaLibraryEvents.changes.collectLatest {
        loadVideos()
      }
    }
  }

  override fun refresh() {
    loadVideos()
  }

  override fun onCleared() {
    super.onCleared()
    // Stop observing when ViewModel is destroyed
    viewModelScope.launch {
      mediaStoreObserver.stopObserving()
    }
  }

  private fun loadVideos() {
    viewModelScope.launch(Dispatchers.IO) {
      try {
        _isLoading.value = true

        // Load videos
        val videoList = videoRepository.getVideosInFolder(getApplication(), bucketId)

        if (videoList.isEmpty()) {
          Log.d(tag, "No videos found for bucket $bucketId - attempting media rescan")
          // Trigger a media scan to refresh MediaStore (non-blocking)
          triggerMediaScan()
          // Reduced delay for better responsiveness
          delay(300)
          val retryVideoList = videoRepository.getVideosInFolder(getApplication(), bucketId)
          _videos.value = retryVideoList
          // Show videos immediately without playback info
          _videosWithPlaybackInfo.value = retryVideoList.map { VideoWithPlaybackInfo(it) }
          _isLoading.value = false
          // Load playback info in background
          loadPlaybackInfoAsync(retryVideoList)
        } else {
          _videos.value = videoList
          // Show videos immediately without playback info
          _videosWithPlaybackInfo.value = videoList.map { VideoWithPlaybackInfo(it) }
          _isLoading.value = false
          // Load playback info in background
          loadPlaybackInfoAsync(videoList)
        }
      } catch (e: Exception) {
        Log.e(tag, "Error loading videos for bucket $bucketId", e)
        _videos.value = emptyList()
        _videosWithPlaybackInfo.value = emptyList()
        _isLoading.value = false
      }
    }
  }

  private suspend fun loadPlaybackInfoAsync(videos: List<Video>) {
    viewModelScope.launch(Dispatchers.IO) {
      val videosWithInfo =
        videos.map { video ->
          val playbackState = playbackStateRepository.getVideoDataByTitle(video.displayName)

          // Calculate watch progress (0.0 to 1.0)
          val progress = if (playbackState != null && video.duration > 0) {
            // Duration is in milliseconds, convert to seconds
            val durationSeconds = video.duration / 1000
            val timeRemaining = playbackState.timeRemaining.toLong()
            val watched = durationSeconds - timeRemaining
            val progressValue = (watched.toFloat() / durationSeconds.toFloat()).coerceIn(0f, 1f)

            // Only show progress for videos that are 1-99% complete
            if (progressValue in 0.01f..0.99f) progressValue else null
          } else {
            null
          }

          VideoWithPlaybackInfo(
            video = video,
            timeRemaining = playbackState?.timeRemaining?.toLong(),
            progressPercentage = progress,
          )
        }
      _videosWithPlaybackInfo.value = videosWithInfo
    }
  }

  private fun triggerMediaScan() {
    try {
      // Trigger a comprehensive media scan
      val externalStorage = android.os.Environment.getExternalStorageDirectory()

      android.media.MediaScannerConnection.scanFile(
        getApplication(),
        arrayOf(externalStorage.absolutePath),
        arrayOf("video/*"),
      ) { path, uri ->
        Log.d(tag, "Media scan completed for: $path -> $uri")
      }

      Log.d(tag, "Triggered media scan for: ${externalStorage.absolutePath}")
    } catch (e: Exception) {
      Log.e(tag, "Failed to trigger media scan", e)
    }
  }

  companion object {
    fun factory(
      application: Application,
      bucketId: String,
    ) = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T = VideoListViewModel(application, bucketId) as T
    }
  }
}
