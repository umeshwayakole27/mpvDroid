package live.uwapps.mpvkt.ui.preferences

import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.util.fastJoinToString
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import com.github.k1rakishou.fsaf.FileManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import live.uwapps.mpvkt.R
import live.uwapps.mpvkt.database.MpvKtDatabase
import live.uwapps.mpvkt.preferences.AdvancedPreferences
import live.uwapps.mpvkt.preferences.preference.collectAsState
import live.uwapps.mpvkt.presentation.Screen
import live.uwapps.mpvkt.presentation.components.ConfirmDialog
import live.uwapps.mpvkt.presentation.crash.CrashActivity
import live.uwapps.mpvkt.ui.utils.LocalBackStack
import me.zhanghai.compose.preference.Preference
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import me.zhanghai.compose.preference.TextFieldPreference
import me.zhanghai.compose.preference.TwoTargetIconButtonPreference
import org.koin.compose.koinInject
import java.io.File
import kotlin.io.path.deleteIfExists
import kotlin.io.path.outputStream
import kotlin.io.path.readLines

@Serializable
object AdvancedPreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val context = LocalContext.current
    val backStack = LocalBackStack.current
    val preferences = koinInject<AdvancedPreferences>()
    val fileManager = koinInject<FileManager>()
    val scope = rememberCoroutineScope()
    Scaffold(
      topBar = {
        TopAppBar(
          title = {
            Text(stringResource(R.string.pref_advanced))
          },
          navigationIcon = {
            IconButton(onClick = backStack::removeLastOrNull) {
              Icon(Icons.AutoMirrored.Default.ArrowBack, null)
            }
          },
        )
      },
    ) { padding ->
      ProvidePreferenceLocals {
        val locationPicker = rememberLauncherForActivityResult(
          ActivityResultContracts.OpenDocumentTree(),
        ) { uri ->
          if (uri == null) return@rememberLauncherForActivityResult

          val flags = Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
          context.contentResolver.takePersistableUriPermission(uri, flags)
          preferences.mpvConfStorageUri.set(uri.toString())
        }
        val mpvConfStorageLocation by preferences.mpvConfStorageUri.collectAsState()
        Column(
          Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding),
        ) {
          TwoTargetIconButtonPreference(
            title = { Text(stringResource(R.string.pref_advanced_mpv_conf_storage_location)) },
            summary = {
              if (mpvConfStorageLocation.isNotBlank()) {
                Text(getSimplifiedPathFromUri(mpvConfStorageLocation))
              }
            },
            onClick = { locationPicker.launch(null) },
            iconButtonIcon = { Icon(Icons.Default.Clear, null) },
            onIconButtonClick = { preferences.mpvConfStorageUri.delete() },
            iconButtonEnabled = mpvConfStorageLocation.isNotBlank(),
          )
          var mpvConf by remember { mutableStateOf(preferences.mpvConf.get()) }
          LaunchedEffect(true) {
            if (mpvConfStorageLocation.isBlank()) return@LaunchedEffect
            withContext(Dispatchers.IO) {
              val tempFile = kotlin.io.path.createTempFile()
              runCatching {
                val uri = DocumentFile.fromTreeUri(
                  context,
                  mpvConfStorageLocation.toUri(),
                )!!.findFile("mpv.conf")!!.uri
                context.contentResolver.openInputStream(uri)?.copyTo(tempFile.outputStream())
                preferences.mpvConf.set(tempFile.readLines().fastJoinToString("\n"))
              }
              tempFile.deleteIfExists()
            }
          }
          TextFieldPreference(
            value = mpvConf,
            onValueChange = { mpvConf = it },
            title = { Text(stringResource(R.string.pref_advanced_mpv_conf)) },
            textField = { value, onValueChange, onOk ->
              OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                maxLines = Int.MAX_VALUE,
                keyboardActions = KeyboardActions(onDone = { onOk() }),
              )
            },
            textToValue = {
              preferences.mpvConf.set(it)
              File(context.filesDir.path, "mpv.conf").writeText(it)
              if (mpvConfStorageLocation.isNotBlank()) {
                val tree = DocumentFile.fromTreeUri(context, mpvConfStorageLocation.toUri())!!
                val uri = if (tree.findFile("mpv.conf") == null) {
                  val conf = tree.createFile("text/plain", "mpv.conf")!!
                  conf.renameTo("mpv.conf")
                  conf.uri
                } else {
                  tree.findFile("mpv.conf")!!.uri
                }
                val out = context.contentResolver.openOutputStream(uri)
                out!!.write(it.toByteArray())
                out.flush()
                out.close()
              }
              it
            },
            summary = { if (mpvConf.isNotBlank()) Text(mpvConf.lines()[0]) },
          )
          var inputConf by remember { mutableStateOf(preferences.inputConf.get()) }
          LaunchedEffect(true) {
            if (mpvConfStorageLocation.isBlank()) return@LaunchedEffect
            withContext(Dispatchers.IO) {
              val tempFile = kotlin.io.path.createTempFile()
              runCatching {
                val uri = DocumentFile.fromTreeUri(
                  context,
                  mpvConfStorageLocation.toUri(),
                )!!.findFile("input.conf")!!.uri
                context.contentResolver.openInputStream(uri)?.copyTo(tempFile.outputStream())
                preferences.inputConf.set(tempFile.readLines().fastJoinToString("\n"))
              }
              tempFile.deleteIfExists()
            }
          }
          TextFieldPreference(
            value = inputConf,
            onValueChange = { inputConf = it },
            title = { Text(stringResource(R.string.pref_advanced_input_conf)) },
            textField = { value, onValueChange, onOk ->
              OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                maxLines = Int.MAX_VALUE,
                keyboardActions = KeyboardActions(onDone = { onOk() }),
              )
            },
            textToValue = {
              preferences.inputConf.set(it)
              File(context.filesDir.path, "input.conf").writeText(it)
              if (mpvConfStorageLocation.isNotBlank()) {
                val tree = DocumentFile.fromTreeUri(context, mpvConfStorageLocation.toUri())!!
                val uri = if (tree.findFile("input.conf") == null) {
                  val conf = tree.createFile("text/plain", "input.conf")!!
                  conf.renameTo("input.conf")
                  conf.uri
                } else {
                  tree.findFile("input.conf")!!.uri
                }
                val out = context.contentResolver.openOutputStream(uri)
                out!!.write(it.toByteArray())
                out.flush()
                out.close()
              }
              it
            },
            summary = { if (inputConf.isNotBlank()) Text(inputConf.lines()[0]) },
          )
          val activity = LocalActivity.current!!
          val clipboard = LocalClipboardManager.current
          Preference(
            title = { Text(stringResource(R.string.pref_advanced_dump_logs_title)) },
            summary = { Text(stringResource(R.string.pref_advanced_dump_logs_summary)) },
            onClick = {
              scope.launch(Dispatchers.IO) {
                val deviceInfo = CrashActivity.collectDeviceInfo()
                val logcat = CrashActivity.collectLogcat()

                clipboard.setText(AnnotatedString(CrashActivity.concatLogs(deviceInfo, null, logcat)))
                CrashActivity.shareLogs(deviceInfo, null, logcat, activity)
              }
            },
          )
          val verboseLogging by preferences.verboseLogging.collectAsState()
          SwitchPreference(
            value = verboseLogging,
            onValueChange = preferences.verboseLogging::set,
            title = { Text(stringResource(R.string.pref_advanced_verbose_logging_title)) },
            summary = { Text(stringResource(R.string.pref_advanced_verbose_logging_summary)) },
          )
          var isConfirmDialogShown by remember { mutableStateOf(false) }
          val mpvKtDatabase = koinInject<MpvKtDatabase>()
          Preference(
            title = { Text(stringResource(R.string.pref_advanced_clear_playback_history)) },
            onClick = { isConfirmDialogShown = true },
          )
          if (isConfirmDialogShown) {
            ConfirmDialog(
              stringResource(R.string.pref_advanced_clear_playback_history_confirm_title),
              stringResource(R.string.pref_advanced_clear_playback_history_confirm_subtitle),
              onConfirm = {
                scope.launch(Dispatchers.IO) { mpvKtDatabase.videoDataDao() }
                isConfirmDialogShown = false
                Toast.makeText(
                  context,
                  context.getString(R.string.pref_advanced_cleared_playback_history),
                  Toast.LENGTH_SHORT,
                ).show()
              },
              onCancel = { isConfirmDialogShown = false },
            )
          }
          Preference(
            title = { Text(text = stringResource(id = R.string.pref_advanced_clear_mpv_conf_cache)) },
            onClick = {
              fileManager.deleteContent(fileManager.fromPath(context.filesDir.path))
              Toast.makeText(
                context,
                context.getString(R.string.pref_advanced_cleared_mpv_conf_cache),
                Toast.LENGTH_SHORT,
              ).show()
            },
          )
          Preference(
            title = { Text(text = stringResource(id = R.string.pref_advanced_clear_fonts_cache)) },
            onClick = {
              fileManager.deleteContent(fileManager.fromPath(context.cacheDir.path + "/fonts"))
              Toast.makeText(
                context,
                context.getString(R.string.pref_advanced_cleared_fonts_cache),
                Toast.LENGTH_SHORT,
              ).show()
            },
          )
        }
      }
    }
  }
}

fun getSimplifiedPathFromUri(uri: String): String {
  return Environment.getExternalStorageDirectory().canonicalPath + "/" + Uri.decode(uri).substringAfterLast(":")
}
