package live.uwapps.mpvkt.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import live.uwapps.mpvkt.database.entities.FolderEntity

@Dao
interface FolderDao {
  @Query("SELECT * FROM folders ORDER BY name ASC")
  fun getAllFolders(): Flow<List<FolderEntity>>

  @Query("SELECT * FROM folders WHERE isHidden = 0 ORDER BY name ASC")
  fun getVisibleFolders(): Flow<List<FolderEntity>>

  @Query("SELECT * FROM folders WHERE path = :path")
  suspend fun getFolderByPath(path: String): FolderEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFolder(folder: FolderEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertFolders(folders: List<FolderEntity>)

  @Update
  suspend fun updateFolder(folder: FolderEntity)

  @Delete
  suspend fun deleteFolder(folder: FolderEntity)

  @Query("DELETE FROM folders WHERE path = :path")
  suspend fun deleteFolderByPath(path: String)

  @Query("UPDATE folders SET isHidden = :isHidden WHERE path = :path")
  suspend fun updateFolderVisibility(path: String, isHidden: Boolean)

  @Query("UPDATE folders SET videoCount = :count, totalDuration = :duration, totalSize = :size, lastScanned = :timestamp WHERE path = :path")
  suspend fun updateFolderStats(path: String, count: Int, duration: Long, size: Long, timestamp: Long)
}
