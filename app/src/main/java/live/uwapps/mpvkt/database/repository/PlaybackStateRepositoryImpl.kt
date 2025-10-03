package live.uwapps.mpvkt.database.repository

import live.uwapps.mpvkt.database.MpvKtDatabase
import live.uwapps.mpvkt.database.entities.PlaybackStateEntity
import live.uwapps.mpvkt.domain.playbackstate.repository.PlaybackStateRepository

class PlaybackStateRepositoryImpl(
  private val database: MpvKtDatabase
) : PlaybackStateRepository {
  override suspend fun upsert(playbackState: PlaybackStateEntity) {
    database.videoDataDao().upsert(playbackState)
  }

  override suspend fun getVideoDataByTitle(mediaTitle: String): PlaybackStateEntity? {
    return database.videoDataDao().getVideoDataByTitle(mediaTitle)
  }

  override suspend fun clearAllPlaybackStates() {
    database.videoDataDao().clearAllPlaybackStates()
  }
}
