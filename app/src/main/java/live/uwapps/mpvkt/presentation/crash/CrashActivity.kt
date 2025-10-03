package live.uwapps.mpvkt.presentation.crash

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.outlined.BugReport
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.coroutineScope
import `is`.xyz.mpv.Utils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import live.uwapps.mpvkt.BuildConfig
import live.uwapps.mpvkt.MainActivity
import live.uwapps.mpvkt.R
import live.uwapps.mpvkt.preferences.AppearancePreferences
import live.uwapps.mpvkt.preferences.preference.collectAsState
import live.uwapps.mpvkt.ui.theme.DarkMode
import live.uwapps.mpvkt.ui.theme.MpvKtTheme
import live.uwapps.mpvkt.ui.theme.spacing
import org.koin.android.ext.android.inject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

class CrashActivity : ComponentActivity() {

  private val clipboardManager by lazy { getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager }
  private lateinit var logcat: String
  private val appearancePreferences: AppearancePreferences by inject()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    lifecycle.coroutineScope.launch {
      logcat = collectLogcat()
    }
    setContent {
      val dark by appearancePreferences.darkMode.collectAsState()
      val isSystemInDarkTheme = isSystemInDarkTheme()
      enableEdgeToEdge(
        SystemBarStyle.auto(
          lightScrim = Color.White.toArgb(),
          darkScrim = Color.White.toArgb(),
        ) { dark == DarkMode.Dark || (dark == DarkMode.System && isSystemInDarkTheme) },
      )
      MpvKtTheme {
        CrashScreen(intent.getStringExtra("exception") ?: "")
      }
    }
  }

  companion object {
    suspend fun shareLogs(
      deviceInfo: String,
      exceptionString: String? = null,
      logcat: String,
      activity: Activity,
    ) {
      withContext(NonCancellable) {
        val file = File(activity.cacheDir, "mpvKt_logs.txt")
        if (file.exists()) file.delete()
        file.createNewFile()
        file.appendText(concatLogs(deviceInfo, exceptionString, logcat))
        val uri = FileProvider.getUriForFile(activity, BuildConfig.APPLICATION_ID + ".provider", file)
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.clipData = ClipData.newRawUri(null, uri)
        intent.type = "text/plain"
        intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
        activity.startActivity(
          Intent.createChooser(intent, activity.getString(R.string.crash_screen_share)),
        )
      }
    }

    fun concatLogs(
      deviceInfo: String,
      crashLogs: String? = null,
      logcat: String,
    ): String {
      return StringBuilder().apply {
        appendLine(deviceInfo)
        appendLine()
        if (!crashLogs.isNullOrBlank()) {
          appendLine("Exception:")
          appendLine(crashLogs)
          appendLine()
        }
        appendLine("Logcat:")
        appendLine(logcat)
      }.toString()
    }

    fun collectLogcat(): String {
      val process = Runtime.getRuntime()
      val reader = BufferedReader(InputStreamReader(process.exec("logcat -d").inputStream))
      val logcat = StringBuilder()
      // reader.lines() looks much nicer so why not use it on devices that support it?
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        reader.lines().forEach(logcat::appendLine)
      } else {
        reader.readLines().forEach(logcat::appendLine)
      }
      return logcat.toString()
    }

    fun collectDeviceInfo(): String {
      return """
      App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.GIT_SHA})
      Android version: ${Build.VERSION.RELEASE} (${Build.VERSION.SDK_INT})
      Device brand: ${Build.BRAND}
      Device manufacturer: ${Build.MANUFACTURER}
      Device model: ${Build.MODEL} (${Build.DEVICE})
      MPV version: ${Utils.VERSIONS.mpv}
      ffmpeg version: ${Utils.VERSIONS.ffmpeg}
      libplacebo version: ${Utils.VERSIONS.libPlacebo}
      """.trimIndent()
    }
  }

  @Composable
  fun CrashScreen(
    exceptionString: String,
    modifier: Modifier = Modifier,
  ) {
    val scope = rememberCoroutineScope()
    Scaffold(
      modifier = modifier.fillMaxSize(),
      bottomBar = {
        val borderColor = MaterialTheme.colorScheme.outline
        Column(
          Modifier
            .windowInsetsPadding(NavigationBarDefaults.windowInsets)
            .drawBehind {
              drawLine(
                borderColor,
                Offset.Zero,
                Offset(size.width, 0f),
                strokeWidth = Dp.Hairline.value,
              )
            }
            .padding(vertical = MaterialTheme.spacing.smaller, horizontal = MaterialTheme.spacing.medium),
          verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.extraSmall),
        ) {
          Row(
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.smaller),
          ) {
            Button(
              onClick = {
                scope.launch(Dispatchers.IO) {
                  shareLogs(collectDeviceInfo(), exceptionString, logcat, this@CrashActivity)
                }
              },
              modifier = Modifier.weight(1f),
            ) { Text(stringResource(R.string.crash_screen_share)) }
            FilledIconButton(
              onClick = {
                clipboardManager.setPrimaryClip(
                  ClipData.newPlainText(
                    null,
                    concatLogs(collectDeviceInfo(), exceptionString, logcat),
                  ),
                )
              },
            ) {
              Icon(Icons.Default.ContentCopy, null)
            }
          }
          OutlinedButton(
            onClick = {
              finish()
              startActivity(Intent(this@CrashActivity, MainActivity::class.java))
            },
            modifier = Modifier.fillMaxWidth(),
          ) {
            Text(stringResource(R.string.crash_screen_restart))
          }
        }
      },
    ) { paddingValues ->
      Column(
        modifier = Modifier
          .padding(paddingValues)
          .padding(horizontal = MaterialTheme.spacing.medium)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
      ) {
        Spacer(Modifier.height(paddingValues.calculateTopPadding()))
        Icon(
          Icons.Outlined.BugReport,
          null,
          modifier = Modifier.size(48.dp),
          tint = MaterialTheme.colorScheme.primary,
        )
        Text(
          stringResource(R.string.crash_screen_title),
          style = MaterialTheme.typography.headlineLarge,
        )
        Text(
          stringResource(R.string.crash_screen_subtitle, stringResource(R.string.app_name)),
          color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
          stringResource(R.string.crash_screen_logs_title),
          style = MaterialTheme.typography.headlineSmall,
        )
        LogsContainer(exceptionString)
        Text(
          "Logcat:",
          style = MaterialTheme.typography.headlineSmall,
        )
        LogsContainer(logcat)
        Spacer(Modifier.height(8.dp))
      }
    }
  }

  @Composable
  fun LogsContainer(
    logs: String,
    modifier: Modifier = Modifier,
  ) {
    LazyRow(
      modifier = modifier
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
      item {
        SelectionContainer {
          Text(
            text = logs,
            fontFamily = FontFamily.Monospace,
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(MaterialTheme.spacing.smaller),
          )
        }
      }
    }
  }
}
