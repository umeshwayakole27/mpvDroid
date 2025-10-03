package live.uwapps.mpvkt.di

import androidx.room.Room
import live.uwapps.mpvkt.database.Migrations
import live.uwapps.mpvkt.database.MpvKtDatabase
import live.uwapps.mpvkt.database.repository.CustomButtonRepositoryImpl
import live.uwapps.mpvkt.database.repository.PlaybackStateRepositoryImpl
import live.uwapps.mpvkt.database.repository.VideoLibraryRepository
import live.uwapps.mpvkt.domain.custombuttons.repository.CustomButtonRepository
import live.uwapps.mpvkt.domain.playbackstate.repository.PlaybackStateRepository
import live.uwapps.mpvkt.domain.library.*
import live.uwapps.mpvkt.domain.scanner.VideoScanner
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val DatabaseModule = module {
  single<MpvKtDatabase> {
    Room
      .databaseBuilder(androidContext(), MpvKtDatabase::class.java, "mpvKt.db")
      .addMigrations(migrations = Migrations)
      .build()
  }

  // Existing repositories
  singleOf(::CustomButtonRepositoryImpl).bind(CustomButtonRepository::class)
  singleOf(::PlaybackStateRepositoryImpl).bind(PlaybackStateRepository::class)

  // Video library components
  single { get<MpvKtDatabase>().videoDao() }
  single { get<MpvKtDatabase>().folderDao() }
  singleOf(::VideoLibraryRepository)
  singleOf(::VideoScanner)

  // Use cases
  singleOf(::GetVideosUseCase)
  singleOf(::GroupVideosUseCase)
  singleOf(::GetFoldersUseCase)
  singleOf(::ToggleFavoriteUseCase)
  singleOf(::UpdateWatchProgressUseCase)
  singleOf(::GetLibraryStatsUseCase)
  singleOf(::DeleteVideoUseCase)
}
