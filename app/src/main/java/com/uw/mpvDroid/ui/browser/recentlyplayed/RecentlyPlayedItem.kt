package com.uw.mpvDroid.ui.browser.recentlyplayed

import com.uw.mpvDroid.database.entities.PlaylistEntity
import com.uw.mpvDroid.domain.media.model.Video

sealed class RecentlyPlayedItem {
  abstract val timestamp: Long

  data class VideoItem(
    val video: Video,
    override val timestamp: Long,
  ) : RecentlyPlayedItem()

  data class PlaylistItem(
    val playlist: PlaylistEntity,
    val videoCount: Int,
    val mostRecentVideoPath: String,
    override val timestamp: Long,
  ) : RecentlyPlayedItem()

  data class LinkItem(
    val url: String,
    val displayName: String,
    override val timestamp: Long,
  ) : RecentlyPlayedItem()
}
