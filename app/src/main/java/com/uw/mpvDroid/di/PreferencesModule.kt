package com.uw.mpvDroid.di

import com.uw.mpvDroid.preferences.AdvancedPreferences
import com.uw.mpvDroid.preferences.AppearancePreferences
import com.uw.mpvDroid.preferences.AudioPreferences
import com.uw.mpvDroid.preferences.BrowserPreferences
import com.uw.mpvDroid.preferences.DecoderPreferences
import com.uw.mpvDroid.preferences.FoldersPreferences
import com.uw.mpvDroid.preferences.GesturePreferences
import com.uw.mpvDroid.preferences.PlayerPreferences
import com.uw.mpvDroid.preferences.SubtitlesPreferences
import com.uw.mpvDroid.preferences.SystemPreferences
import com.uw.mpvDroid.preferences.preference.AndroidPreferenceStore
import com.uw.mpvDroid.preferences.preference.PreferenceStore
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val PreferencesModule =
  module {
    single { AndroidPreferenceStore(androidContext()) }.bind(PreferenceStore::class)

    single { AppearancePreferences(get()) }
    singleOf(::PlayerPreferences)
    singleOf(::GesturePreferences)
    singleOf(::DecoderPreferences)
    singleOf(::SubtitlesPreferences)
    singleOf(::AudioPreferences)
    singleOf(::AdvancedPreferences)
    singleOf(::BrowserPreferences)
    singleOf(::FoldersPreferences)
    singleOf(::SystemPreferences)
  }
