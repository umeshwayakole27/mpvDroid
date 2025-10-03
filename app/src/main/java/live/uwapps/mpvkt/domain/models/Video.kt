package live.uwapps.mpvkt.domain.models

import androidx.compose.runtime.Immutable

@Immutable
data class Video(
  val path: String,
  val title: String,
  val displayName: String,
  val duration: Long,
  val size: Long,
  val dateAdded: Long,
  val dateModified: Long,
  val mimeType: String,
  val resolution: String? = null,
  val thumbnail: String? = null,
  val folder: String,
  val folderName: String,
  val format: String? = null,
  val aspectRatio: String? = null,
  val frameRate: Float? = null,
  val bitrate: Long? = null,
  val hasSubtitles: Boolean = false,
  val isWatched: Boolean = false,
  val isFavorite: Boolean = false,
  val lastWatchedTime: Long? = null,
  val watchProgress: Float = 0f
) {
  val formattedDuration: String
    get() = formatDuration(duration)

  val formattedSize: String
    get() = formatFileSize(size)

  val isPartiallyWatched: Boolean
    get() = watchProgress > 0f && watchProgress < 1f

  private fun formatDuration(durationMs: Long): String {
    val seconds = durationMs / 1000
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    return when {
      hours > 0 -> "%d:%02d:%02d".format(hours, minutes, secs)
      else -> "%d:%02d".format(minutes, secs)
    }
  }

  private fun formatFileSize(bytes: Long): String {
    val units = arrayOf("B", "KB", "MB", "GB", "TB")
    var size = bytes.toDouble()
    var unitIndex = 0

    while (size >= 1024 && unitIndex < units.size - 1) {
      size /= 1024
      unitIndex++
    }

    return "%.1f %s".format(size, units[unitIndex])
  }
}
