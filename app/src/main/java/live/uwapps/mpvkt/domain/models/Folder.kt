package live.uwapps.mpvkt.domain.models

data class Folder(
  val path: String,
  val name: String,
  val videoCount: Int = 0,
  val totalDuration: Long = 0L,
  val totalSize: Long = 0L,
  val lastScanned: Long = 0L,
  val isHidden: Boolean = false
) {
  val formattedTotalDuration: String
    get() = formatDuration(totalDuration)

  val formattedTotalSize: String
    get() = formatFileSize(totalSize)

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
