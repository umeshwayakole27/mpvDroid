package com.uw.mpvDroid.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.uw.mpvDroid.database.converters.NetworkProtocolConverter
import com.uw.mpvDroid.database.dao.CustomButtonDao
import com.uw.mpvDroid.database.dao.ExternalSubtitleDao
import com.uw.mpvDroid.database.dao.NetworkConnectionDao
import com.uw.mpvDroid.database.dao.PlaybackStateDao
import com.uw.mpvDroid.database.dao.PlaylistDao
import com.uw.mpvDroid.database.dao.RecentlyPlayedDao
import com.uw.mpvDroid.database.dao.VideoMetadataDao
import com.uw.mpvDroid.database.entities.CustomButtonEntity
import com.uw.mpvDroid.database.entities.ExternalSubtitleEntity
import com.uw.mpvDroid.database.entities.PlaybackStateEntity
import com.uw.mpvDroid.database.entities.PlaylistEntity
import com.uw.mpvDroid.database.entities.PlaylistItemEntity
import com.uw.mpvDroid.database.entities.RecentlyPlayedEntity
import com.uw.mpvDroid.database.entities.VideoMetadataEntity
import com.uw.mpvDroid.domain.network.NetworkConnection

@Database(
  entities = [
    PlaybackStateEntity::class,
    CustomButtonEntity::class,
    RecentlyPlayedEntity::class,
    VideoMetadataEntity::class,
    NetworkConnection::class,
    PlaylistEntity::class,
    PlaylistItemEntity::class,
    ExternalSubtitleEntity::class,
  ],
  version = 8,
  exportSchema = true,
)
@TypeConverters(NetworkProtocolConverter::class)
abstract class MpvDroidDatabase : RoomDatabase() {
  abstract fun videoDataDao(): PlaybackStateDao
  abstract fun customButtonDao(): CustomButtonDao
  abstract fun recentlyPlayedDao(): RecentlyPlayedDao
  abstract fun videoMetadataDao(): VideoMetadataDao
  abstract fun networkConnectionDao(): NetworkConnectionDao
  abstract fun playlistDao(): PlaylistDao
  abstract fun externalSubtitleDao(): ExternalSubtitleDao
}
