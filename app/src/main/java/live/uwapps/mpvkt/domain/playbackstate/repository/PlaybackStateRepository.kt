package live.uwapps.mpvkt.domain.playbackstate.repository

import live.uwapps.mpvkt.database.entities.PlaybackStateEntity

interface PlaybackStateRepository {

  suspend fun upsert(playbackState: PlaybackStateEntity)

  suspend fun getVideoDataByTitle(mediaTitle: String): PlaybackStateEntity?

  suspend fun clearAllPlaybackStates()
}
