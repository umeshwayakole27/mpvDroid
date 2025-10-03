package live.uwapps.mpvkt.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "folders")
data class FolderEntity(
  @PrimaryKey val path: String,
  val name: String,
  val videoCount: Int = 0,
  val totalDuration: Long = 0L, // in milliseconds
  val totalSize: Long = 0L, // in bytes
  val lastScanned: Long = 0L,
  val isHidden: Boolean = false
)
