package live.uwapps.mpvkt

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.VideoFrameDecoder
import live.uwapps.mpvkt.di.AppModule
import live.uwapps.mpvkt.di.DatabaseModule
import live.uwapps.mpvkt.di.PreferencesModule
import live.uwapps.mpvkt.di.FileManagerModule
import live.uwapps.mpvkt.di.ViewModelModule
import live.uwapps.mpvkt.presentation.crash.CrashActivity
import live.uwapps.mpvkt.presentation.crash.GlobalExceptionHandler
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application(), ImageLoaderFactory {
  override fun onCreate() {
    super.onCreate()
    Thread.setDefaultUncaughtExceptionHandler(GlobalExceptionHandler(applicationContext, CrashActivity::class.java))

    startKoin {
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

  override fun newImageLoader(): ImageLoader =
    ImageLoader.Builder(this)
      .components { add(VideoFrameDecoder.Factory()) }
      .build()
}
