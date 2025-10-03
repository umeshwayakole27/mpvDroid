package live.uwapps.mpvkt.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "videos")
data class VideoEntity(
  @PrimaryKey val path: String,
  val title: String,
  val displayName: String,
  val duration: Long, // in milliseconds
  val size: Long, // in bytes
  val dateAdded: Long,
  val dateModified: Long,
  val mimeType: String,
  val resolution: String?, // e.g., "1920x1080"
  val thumbnail: String?, // path to thumbnail or null
  val folder: String, // parent folder path
  val folderName: String, // parent folder display name
  val isVideo: Boolean = true,
  val format: String?, // file extension
  val aspectRatio: String?, // e.g., "16:9"
  val frameRate: Float?, // frames per second
  val bitrate: Long?, // in bps
  val hasSubtitles: Boolean = false,
  val isWatched: Boolean = false,
  val isFavorite: Boolean = false,
  val lastWatchedTime: Long? = null,
  val watchProgress: Float = 0f // 0.0 to 1.0
)
