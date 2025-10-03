package live.uwapps.mpvkt.domain.scanner

import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import live.uwapps.mpvkt.database.repository.VideoLibraryRepository
import live.uwapps.mpvkt.domain.models.Video
import live.uwapps.mpvkt.domain.models.Folder
import java.io.File

class VideoScanner(
  private val context: Context,
  private val repository: VideoLibraryRepository
) {
  suspend fun scanAllVideos(onProgress: (Int, Int) -> Unit = { _, _ -> }) {
    withContext(Dispatchers.IO) {
      val videos = mutableListOf<Video>()
      val folders = mutableMapOf<String, Folder>()
      
      val projection = arrayOf(
        MediaStore.Video.Media._ID,
        MediaStore.Video.Media.DISPLAY_NAME,
        MediaStore.Video.Media.TITLE,
        MediaStore.Video.Media.SIZE,
        MediaStore.Video.Media.DURATION,
        MediaStore.Video.Media.DATE_ADDED,
        MediaStore.Video.Media.DATE_MODIFIED,
        MediaStore.Video.Media.MIME_TYPE,
        MediaStore.Video.Media.WIDTH,
        MediaStore.Video.Media.HEIGHT,
        MediaStore.Video.Media.BUCKET_DISPLAY_NAME,
        MediaStore.Video.Media.RELATIVE_PATH
      )

      val cursor: Cursor? = context.contentResolver.query(
        MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
        projection,
        null,
        null,
        MediaStore.Video.Media.DATE_ADDED + " DESC"
      )

      cursor?.use { c ->
        val total = c.count
        var processed = 0
        
        while (c.moveToNext()) {
          try {
            val video = createVideoFromCursor(c)
            if (video != null) {
              videos.add(video)
              
              // Update folder information
              val folderPath = video.folder
              val existingFolder = folders[folderPath]
              if (existingFolder != null) {
                folders[folderPath] = existingFolder.copy(
                  videoCount = existingFolder.videoCount + 1,
                  totalDuration = existingFolder.totalDuration + video.duration,
                  totalSize = existingFolder.totalSize + video.size
                )
              } else {
                folders[folderPath] = Folder(
                  path = folderPath,
                  name = video.folderName,
                  videoCount = 1,
                  totalDuration = video.duration,
                  totalSize = video.size,
                  lastScanned = System.currentTimeMillis()
                )
              }
            }
          } catch (e: Exception) {
            // Log error but continue scanning
          }
          
          processed++
          onProgress(processed, total)
        }
      }

      // Save to database
      repository.insertVideos(videos)
      repository.insertFolders(folders.values.toList())
    }
  }

  private fun createVideoFromCursor(cursor: Cursor): Video? {
    return try {
      val idIndex = cursor.getColumnIndex(MediaStore.Video.Media._ID)
      if (idIndex < 0) return null
      val id = cursor.getLong(idIndex)
      val contentUri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

      val displayNameIndex = cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)
      val displayName = if (displayNameIndex >= 0) cursor.getString(displayNameIndex) ?: id.toString() else id.toString()

      val titleIndex = cursor.getColumnIndex(MediaStore.Video.Media.TITLE)
      val title = if (titleIndex >= 0) cursor.getString(titleIndex) ?: displayName else displayName
      
      val sizeIndex = cursor.getColumnIndex(MediaStore.Video.Media.SIZE)
      val size = if (sizeIndex >= 0) cursor.getLong(sizeIndex) else 0L

      val durationIndex = cursor.getColumnIndex(MediaStore.Video.Media.DURATION)
      val duration = if (durationIndex >= 0) cursor.getLong(durationIndex) else 0L
      
      val dateAddedIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATE_ADDED)
      val dateAdded = if (dateAddedIndex >= 0) cursor.getLong(dateAddedIndex) * 1000 else System.currentTimeMillis()

      val dateModifiedIndex = cursor.getColumnIndex(MediaStore.Video.Media.DATE_MODIFIED)
      val dateModified = if (dateModifiedIndex >= 0) cursor.getLong(dateModifiedIndex) * 1000 else System.currentTimeMillis()

      val mimeTypeIndex = cursor.getColumnIndex(MediaStore.Video.Media.MIME_TYPE)
      val mimeType = if (mimeTypeIndex >= 0) cursor.getString(mimeTypeIndex) ?: "" else ""
      
      val widthIndex = cursor.getColumnIndex(MediaStore.Video.Media.WIDTH)
      val heightIndex = cursor.getColumnIndex(MediaStore.Video.Media.HEIGHT)
      val width = if (widthIndex >= 0) cursor.getInt(widthIndex) else 0
      val height = if (heightIndex >= 0) cursor.getInt(heightIndex) else 0
      
      val resolution = if (width > 0 && height > 0) "${width}x${height}" else null
      val aspectRatio = if (width > 0 && height > 0) {
        val gcd = gcd(width, height)
        "${width / gcd}:${height / gcd}"
      } else null

      val bucketIndex = cursor.getColumnIndex(MediaStore.Video.Media.BUCKET_DISPLAY_NAME)
      val relativePathIndex = cursor.getColumnIndex(MediaStore.Video.Media.RELATIVE_PATH)
      val folderName = if (bucketIndex >= 0) cursor.getString(bucketIndex) ?: "" else ""
      val folder = if (relativePathIndex >= 0) cursor.getString(relativePathIndex) ?: folderName else folderName
      val format = displayName.substringAfterLast('.', missingDelimiterValue = "").lowercase()

      Video(
        path = contentUri.toString(),
        title = title,
        displayName = displayName,
        duration = duration,
        size = size,
        dateAdded = dateAdded,
        dateModified = dateModified,
        mimeType = mimeType,
        resolution = resolution,
        folder = folder,
        folderName = folderName,
        format = format,
        aspectRatio = aspectRatio
      )
    } catch (e: Exception) {
      null
    }
  }

  private fun gcd(a: Int, b: Int): Int {
    return if (b == 0) a else gcd(b, a % b)
  }

  suspend fun scanSingleVideo(filePath: String): Video? {
    return withContext(Dispatchers.IO) {
      val file = File(filePath)
      if (!file.exists()) return@withContext null

      try {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(filePath)
        
        val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
        val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
        val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
        val bitrate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_BITRATE)?.toLongOrNull()
        val frameRate = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)?.toFloatOrNull()
        
        retriever.release()

        val resolution = if (width > 0 && height > 0) "${width}x${height}" else null
        val aspectRatio = if (width > 0 && height > 0) {
          val gcd = gcd(width, height)
          "${width / gcd}:${height / gcd}"
        } else null

        val parentFile = file.parentFile
        val folder = parentFile?.absolutePath ?: ""
        val folderName = parentFile?.name ?: ""

        Video(
          path = filePath,
          title = file.nameWithoutExtension,
          displayName = file.name,
          duration = duration,
          size = file.length(),
          dateAdded = System.currentTimeMillis(),
          dateModified = file.lastModified(),
          mimeType = getMimeType(file.extension),
          resolution = resolution,
          folder = folder,
          folderName = folderName,
          format = file.extension.lowercase(),
          aspectRatio = aspectRatio,
          frameRate = frameRate,
          bitrate = bitrate
        )
      } catch (e: Exception) {
        null
      }
    }
  }

  private fun getMimeType(extension: String): String {
    return when (extension.lowercase()) {
      "mp4", "m4v" -> "video/mp4"
      "avi" -> "video/avi"
      "mkv" -> "video/mkv"
      "webm" -> "video/webm"
      "mov" -> "video/mov"
      "wmv" -> "video/wmv"
      "flv" -> "video/flv"
      "3gp" -> "video/3gp"
      "mpg", "mpeg" -> "video/mpeg"
      "ogv" -> "video/ogv"
      "ts" -> "video/ts"
      "vob" -> "video/vob"
      else -> "video/*"
    }
  }
}
