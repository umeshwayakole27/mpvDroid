package live.uwapps.mpvkt.database

import androidx.room.Database
import androidx.room.RoomDatabase
import live.uwapps.mpvkt.database.dao.CustomButtonDao
import live.uwapps.mpvkt.database.dao.PlaybackStateDao
import live.uwapps.mpvkt.database.dao.VideoDao
import live.uwapps.mpvkt.database.dao.FolderDao
import live.uwapps.mpvkt.database.entities.CustomButtonEntity
import live.uwapps.mpvkt.database.entities.PlaybackStateEntity
import live.uwapps.mpvkt.database.entities.VideoEntity
import live.uwapps.mpvkt.database.entities.FolderEntity

@Database(
  entities = [
    PlaybackStateEntity::class, 
    CustomButtonEntity::class,
    VideoEntity::class,
    FolderEntity::class
  ], 
  version = 6
)
abstract class MpvKtDatabase : RoomDatabase() {
  abstract fun videoDataDao(): PlaybackStateDao
  abstract fun customButtonDao(): CustomButtonDao
  abstract fun videoDao(): VideoDao
  abstract fun folderDao(): FolderDao
}
