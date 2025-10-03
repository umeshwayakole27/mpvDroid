package live.uwapps.mpvkt.database.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import live.uwapps.mpvkt.database.entities.VideoEntity

@Dao
interface VideoDao {
  @Query("SELECT * FROM videos ORDER BY dateAdded DESC")
  fun getAllVideos(): Flow<List<VideoEntity>>

  @Query("SELECT * FROM videos WHERE folder = :folderPath ORDER BY displayName ASC")
  fun getVideosByFolder(folderPath: String): Flow<List<VideoEntity>>

  @Query("SELECT * FROM videos WHERE isFavorite = 1 ORDER BY dateAdded DESC")
  fun getFavoriteVideos(): Flow<List<VideoEntity>>

  @Query("SELECT * FROM videos WHERE isWatched = 1 ORDER BY lastWatchedTime DESC")
  fun getWatchedVideos(): Flow<List<VideoEntity>>

  @Query("SELECT * FROM videos WHERE watchProgress > 0 AND watchProgress < 1 ORDER BY lastWatchedTime DESC")
  fun getPartiallyWatchedVideos(): Flow<List<VideoEntity>>

  @Query("SELECT * FROM videos WHERE title LIKE '%' || :query || '%' OR displayName LIKE '%' || :query || '%'")
  fun searchVideos(query: String): Flow<List<VideoEntity>>

  @Query("SELECT * FROM videos WHERE duration >= :minDuration AND duration <= :maxDuration")
  fun getVideosByDurationRange(minDuration: Long, maxDuration: Long): Flow<List<VideoEntity>>

  @Query("SELECT * FROM videos WHERE size >= :minSize AND size <= :maxSize")
  fun getVideosByFileSize(minSize: Long, maxSize: Long): Flow<List<VideoEntity>>

  @Query("SELECT * FROM videos WHERE mimeType = :mimeType")
  fun getVideosByFormat(mimeType: String): Flow<List<VideoEntity>>

  @Query("SELECT DISTINCT folderName FROM videos ORDER BY folderName ASC")
  fun getAllFolders(): Flow<List<String>>

  @Query("SELECT * FROM videos WHERE path = :path")
  suspend fun getVideoByPath(path: String): VideoEntity?

  @Query("SELECT * FROM videos WHERE folder IN (:folderPaths)")
  fun getVideosInFolders(folderPaths: Set<String>): Flow<List<VideoEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVideo(video: VideoEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertVideos(videos: List<VideoEntity>)

  @Update
  suspend fun updateVideo(video: VideoEntity)

  @Delete
  suspend fun deleteVideo(video: VideoEntity)

  @Query("DELETE FROM videos WHERE path = :path")
  suspend fun deleteVideoByPath(path: String)

  @Query("DELETE FROM videos")
  suspend fun deleteAllVideos()

  @Query("UPDATE videos SET isFavorite = :isFavorite WHERE path = :path")
  suspend fun updateFavoriteStatus(path: String, isFavorite: Boolean)

  @Query("UPDATE videos SET isWatched = :isWatched, lastWatchedTime = :timestamp WHERE path = :path")
  suspend fun updateWatchedStatus(path: String, isWatched: Boolean, timestamp: Long?)

  @Query("UPDATE videos SET watchProgress = :progress, lastWatchedTime = :timestamp WHERE path = :path")
  suspend fun updateWatchProgress(path: String, progress: Float, timestamp: Long)

  @Query("SELECT COUNT(*) FROM videos")
  suspend fun getVideoCount(): Int

  @Query("SELECT SUM(size) FROM videos")
  suspend fun getTotalSize(): Long?

  @Query("SELECT SUM(duration) FROM videos")
  suspend fun getTotalDuration(): Long?
}
