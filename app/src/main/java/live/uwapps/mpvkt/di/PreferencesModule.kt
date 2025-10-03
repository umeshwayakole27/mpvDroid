package live.uwapps.mpvkt.di

import live.uwapps.mpvkt.preferences.AdvancedPreferences
import live.uwapps.mpvkt.preferences.AppearancePreferences
import live.uwapps.mpvkt.preferences.AudioPreferences
import live.uwapps.mpvkt.preferences.DecoderPreferences
import live.uwapps.mpvkt.preferences.GesturePreferences
import live.uwapps.mpvkt.preferences.LibraryPreferences
import live.uwapps.mpvkt.preferences.PlayerPreferences
import live.uwapps.mpvkt.preferences.SubtitlesPreferences
import live.uwapps.mpvkt.preferences.preference.AndroidPreferenceStore
import live.uwapps.mpvkt.preferences.preference.PreferenceStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val PreferencesModule = module {
  single { AndroidPreferenceStore(androidContext()) }.bind(PreferenceStore::class)

  singleOf(::AppearancePreferences)
  singleOf(::PlayerPreferences)
  singleOf(::AudioPreferences)
  singleOf(::DecoderPreferences)
  singleOf(::GesturePreferences)
  singleOf(::SubtitlesPreferences)
  singleOf(::AdvancedPreferences)
  singleOf(::LibraryPreferences)
}
