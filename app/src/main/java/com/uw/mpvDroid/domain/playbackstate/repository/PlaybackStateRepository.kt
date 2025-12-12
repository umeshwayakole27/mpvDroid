package com.uw.mpvDroid.domain.playbackstate.repository

import com.uw.mpvDroid.database.entities.PlaybackStateEntity

interface PlaybackStateRepository {

  suspend fun upsert(playbackState: PlaybackStateEntity)

  suspend fun getVideoDataByTitle(mediaTitle: String): PlaybackStateEntity?

  suspend fun clearAllPlaybackStates()
  
  suspend fun updateMediaTitle(oldTitle: String, newTitle: String)
  
  suspend fun deleteByTitle(title: String)
}
