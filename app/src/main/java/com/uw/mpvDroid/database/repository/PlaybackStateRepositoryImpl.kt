package com.uw.mpvDroid.database.repository

import com.uw.mpvDroid.database.MpvDroidDatabase
import com.uw.mpvDroid.database.entities.PlaybackStateEntity
import com.uw.mpvDroid.domain.playbackstate.repository.PlaybackStateRepository

class PlaybackStateRepositoryImpl(
  private val database: MpvDroidDatabase
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
  
  override suspend fun updateMediaTitle(oldTitle: String, newTitle: String) {
    val existingState = database.videoDataDao().getVideoDataByTitle(oldTitle)
    if (existingState != null) {
      database.videoDataDao().upsert(existingState.copy(mediaTitle = newTitle))
      database.videoDataDao().deleteByTitle(oldTitle)
    }
  }
  
  override suspend fun deleteByTitle(title: String) {
    database.videoDataDao().deleteByTitle(title)
  }
}
