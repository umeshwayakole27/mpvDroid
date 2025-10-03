package live.uwapps.mpvkt.database.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import live.uwapps.mpvkt.database.dao.VideoDao
import live.uwapps.mpvkt.database.dao.FolderDao
import live.uwapps.mpvkt.database.entities.VideoEntity
import live.uwapps.mpvkt.database.entities.FolderEntity
import live.uwapps.mpvkt.domain.models.Video
import live.uwapps.mpvkt.domain.models.Folder

class VideoLibraryRepository(
  private val videoDao: VideoDao,
  private val folderDao: FolderDao
) {
  fun getAllVideos(): Flow<List<Video>> =
    videoDao.getAllVideos().map { entities -> entities.map { it.toDomainModel() } }

  fun getVideosByFolder(folderPath: String): Flow<List<Video>> =
    videoDao.getVideosByFolder(folderPath).map { entities -> entities.map { it.toDomainModel() } }

  fun getFavoriteVideos(): Flow<List<Video>> =
    videoDao.getFavoriteVideos().map { entities -> entities.map { it.toDomainModel() } }

  fun getWatchedVideos(): Flow<List<Video>> =
    videoDao.getWatchedVideos().map { entities -> entities.map { it.toDomainModel() } }

  fun getPartiallyWatchedVideos(): Flow<List<Video>> =
    videoDao.getPartiallyWatchedVideos().map { entities -> entities.map { it.toDomainModel() } }

  fun getVideosInFolders(folderPaths: Set<String>): Flow<List<Video>> =
    videoDao.getVideosInFolders(folderPaths).map { entities -> entities.map { it.toDomainModel() } }

  fun searchVideos(query: String): Flow<List<Video>> =
    videoDao.searchVideos(query).map { entities -> entities.map { it.toDomainModel() } }

  fun getAllFolders(): Flow<List<Folder>> =
    folderDao.getAllFolders().map { entities -> entities.map { it.toDomainModel() } }

  suspend fun getVideoByPath(path: String): Video? =
    videoDao.getVideoByPath(path)?.toDomainModel()

  suspend fun insertVideo(video: Video) =
    videoDao.insertVideo(video.toEntity())

  suspend fun insertVideos(videos: List<Video>) =
    videoDao.insertVideos(videos.map { it.toEntity() })

  suspend fun updateVideo(video: Video) =
    videoDao.updateVideo(video.toEntity())

  suspend fun deleteVideo(video: Video) =
    videoDao.deleteVideo(video.toEntity())

  suspend fun updateFavoriteStatus(path: String, isFavorite: Boolean) =
    videoDao.updateFavoriteStatus(path, isFavorite)

  suspend fun updateWatchedStatus(path: String, isWatched: Boolean) =
    videoDao.updateWatchedStatus(path, isWatched, if (isWatched) System.currentTimeMillis() else null)

  suspend fun updateWatchProgress(path: String, progress: Float) =
    videoDao.updateWatchProgress(path, progress, System.currentTimeMillis())

  suspend fun insertFolder(folder: Folder) =
    folderDao.insertFolder(folder.toEntity())

  suspend fun insertFolders(folders: List<Folder>) =
    folderDao.insertFolders(folders.map { it.toEntity() })

  suspend fun getVideoCount(): Int = videoDao.getVideoCount()

  suspend fun getTotalSize(): Long = videoDao.getTotalSize() ?: 0L

  suspend fun getTotalDuration(): Long = videoDao.getTotalDuration() ?: 0L

  private fun VideoEntity.toDomainModel() = Video(
    path = path,
    title = title,
    displayName = displayName,
    duration = duration,
    size = size,
    dateAdded = dateAdded,
    dateModified = dateModified,
    mimeType = mimeType,
    resolution = resolution,
    thumbnail = thumbnail,
    folder = folder,
    folderName = folderName,
    format = format,
    aspectRatio = aspectRatio,
    frameRate = frameRate,
    bitrate = bitrate,
    hasSubtitles = hasSubtitles,
    isWatched = isWatched,
    isFavorite = isFavorite,
    lastWatchedTime = lastWatchedTime,
    watchProgress = watchProgress
  )

  private fun Video.toEntity() = VideoEntity(
    path = path,
    title = title,
    displayName = displayName,
    duration = duration,
    size = size,
    dateAdded = dateAdded,
    dateModified = dateModified,
    mimeType = mimeType,
    resolution = resolution,
    thumbnail = thumbnail,
    folder = folder,
    folderName = folderName,
    format = format,
    aspectRatio = aspectRatio,
    frameRate = frameRate,
    bitrate = bitrate,
    hasSubtitles = hasSubtitles,
    isWatched = isWatched,
    isFavorite = isFavorite,
    lastWatchedTime = lastWatchedTime,
    watchProgress = watchProgress
  )

  private fun FolderEntity.toDomainModel() = Folder(
    path = path,
    name = name,
    videoCount = videoCount,
    totalDuration = totalDuration,
    totalSize = totalSize,
    lastScanned = lastScanned,
    isHidden = isHidden
  )

  private fun Folder.toEntity() = FolderEntity(
    path = path,
    name = name,
    videoCount = videoCount,
    totalDuration = totalDuration,
    totalSize = totalSize,
    lastScanned = lastScanned,
    isHidden = isHidden
  )
}
