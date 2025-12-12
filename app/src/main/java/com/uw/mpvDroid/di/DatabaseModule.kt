package com.uw.mpvDroid.di

import androidx.room.Room
import androidx.room.RoomDatabase
import com.uw.mpvDroid.database.MpvDroidDatabase
import com.uw.mpvDroid.database.repository.PlaybackStateRepositoryImpl
import com.uw.mpvDroid.database.repository.PlaylistRepository
import com.uw.mpvDroid.database.repository.RecentlyPlayedRepositoryImpl
import com.uw.mpvDroid.domain.playbackstate.repository.PlaybackStateRepository
import com.uw.mpvDroid.domain.recentlyplayed.repository.RecentlyPlayedRepository
import com.uw.mpvDroid.domain.subtitle.repository.ExternalSubtitleRepository
import com.uw.mpvDroid.domain.thumbnail.ThumbnailRepository
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val DatabaseModule =
  module {
    // Provide kotlinx.serialization Json as a singleton (used by PlayerViewModel)
    single<Json> {
      Json {
        isLenient = true
        ignoreUnknownKeys = true
      }
    }

    single<MpvDroidDatabase> {
      val context = androidContext()
      Room
        .databaseBuilder(context, MpvDroidDatabase::class.java, "mpvdroid.db")
        .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
        .fallbackToDestructiveMigration()
        .build()
    }

    singleOf(::PlaybackStateRepositoryImpl).bind(PlaybackStateRepository::class)

    single<RecentlyPlayedRepository> {
      RecentlyPlayedRepositoryImpl(get<MpvDroidDatabase>().recentlyPlayedDao())
    }

    single<ExternalSubtitleRepository> {
      ExternalSubtitleRepository(
        context = androidContext(),
        dao = get<MpvDroidDatabase>().externalSubtitleDao(),
      )
    }

    single { ThumbnailRepository(androidContext()) }

    single {
      com.uw.mpvDroid.database.repository.VideoMetadataCacheRepository(
        context = androidContext(),
        dao = get<MpvDroidDatabase>().videoMetadataDao(),
      )
    }

    single {
      com.uw.mpvDroid.repository.VideoRepository(
        metadataCache = get(),
      )
    }

    single {
      get<MpvDroidDatabase>().networkConnectionDao()
    }

    single {
      com.uw.mpvDroid.repository.NetworkRepository(
        dao = get(),
      )
    }

    single {
      PlaylistRepository(
        playlistDao = get<MpvDroidDatabase>().playlistDao(),
      )
    }
  }
