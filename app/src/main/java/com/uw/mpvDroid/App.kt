package com.uw.mpvDroid

import android.app.Application
import com.uw.mpvDroid.di.AppModule
import com.uw.mpvDroid.di.DatabaseModule
import com.uw.mpvDroid.di.FileManagerModule
import com.uw.mpvDroid.di.PreferencesModule
import com.uw.mpvDroid.di.ViewModelModule
import com.uw.mpvDroid.presentation.crash.CrashActivity
import com.uw.mpvDroid.presentation.crash.GlobalExceptionHandler
import org.koin.android.ext.koin.androidContext
import org.koin.androix.startup.KoinStartup
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.koinConfiguration

@OptIn(KoinExperimentalAPI::class)
class App : Application(), KoinStartup {
  override fun onCreate() {
    super.onCreate()
    Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(applicationContext, CrashActivity::class.java))
  }

  override fun onKoinStartup() = koinConfiguration {
    androidContext(this@App)
    modules(
      AppModule,
      PreferencesModule,
      DatabaseModule,
      FileManagerModule,
      ViewModelModule,
    )
  }
}
