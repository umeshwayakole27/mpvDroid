package com.uw.mpvDroid.database.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.uw.mpvDroid.database.entities.PlaybackStateEntity

@Dao
interface PlaybackStateDao {
  @Upsert
  suspend fun upsert(playbackStateEntity: PlaybackStateEntity)

  @Query("SELECT * FROM PlaybackStateEntity WHERE mediaTitle = :mediaTitle LIMIT 1")
  suspend fun getVideoDataByTitle(mediaTitle: String): PlaybackStateEntity?

  @Query("DELETE FROM PlaybackStateEntity")
  suspend fun clearAllPlaybackStates()
  
  @Query("DELETE FROM PlaybackStateEntity WHERE mediaTitle = :title")
  suspend fun deleteByTitle(title: String)
}
