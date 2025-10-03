package live.uwapps.mpvkt.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlaybackStateEntity(
  @PrimaryKey val mediaTitle: String,
  val lastPosition: Int, // in seconds
  val playbackSpeed: Double,
  val sid: Int,
  val subDelay: Int,
  val subSpeed: Double,
  val secondarySid: Int,
  val secondarySubDelay: Int,
  val aid: Int,
  val audioDelay: Int,
)
