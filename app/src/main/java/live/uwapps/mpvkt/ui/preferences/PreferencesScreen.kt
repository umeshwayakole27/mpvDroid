package live.uwapps.mpvkt.ui.preferences

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Memory
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Subtitles
import androidx.compose.material.icons.outlined.Terminal
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import kotlinx.serialization.Serializable
import live.uwapps.mpvkt.R
import live.uwapps.mpvkt.presentation.Screen
import live.uwapps.mpvkt.ui.custombuttons.CustomButtonsScreen
import live.uwapps.mpvkt.ui.utils.LocalBackStack
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.preference

@Serializable
object PreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val backstack = LocalBackStack.current
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text(text = stringResource(R.string.pref_preferences)) },
        )
      },
    ) { padding ->
      ProvidePreferenceLocals {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        ) {
          preference(
            key = "library",
            title = { Text(text = "Library") },
            summary = { Text(text = "Sort, filter, and scan options") },
            icon = { Icon(Icons.Outlined.VideoLibrary, null) },
            onClick = { backstack.add(LibraryPreferencesScreen) },
          )
          preference(
            key = "appearance",
            title = { Text(text = stringResource(id = R.string.pref_appearance_title)) },
            summary = { Text(text = stringResource(id = R.string.pref_appearance_summary)) },
            icon = { Icon(Icons.Outlined.Palette, null) },
            onClick = { backstack.add(AppearancePreferencesScreen) },
          )
          preference(
            key = "player",
            title = { Text(text = stringResource(id = R.string.pref_player)) },
            summary = { Text(text = stringResource(id = R.string.pref_player_summary)) },
            icon = { Icon(Icons.Outlined.PlayCircle, null) },
            onClick = { backstack.add(PlayerPreferencesScreen) },
          )
          preference(
            key = "gesture",
            title = { Text(text = stringResource(id = R.string.pref_gesture)) },
            summary = { Text(text = stringResource(id = R.string.pref_gesture_summary)) },
            icon = { Icon(Icons.Outlined.Gesture, null) },
            onClick = { backstack.add(GesturePreferencesScreen) },
          )
          preference(
            key = "decoder",
            title = { Text(text = stringResource(id = R.string.pref_decoder)) },
            summary = { Text(text = stringResource(id = R.string.pref_decoder_summary)) },
            icon = { Icon(Icons.Outlined.Memory, null) },
            onClick = { backstack.add(DecoderPreferencesScreen) },
          )
          preference(
            key = "subtitles",
            title = { Text(text = stringResource(id = R.string.pref_subtitles)) },
            summary = { Text(text = stringResource(id = R.string.pref_subtitles_summary)) },
            icon = { Icon(Icons.Outlined.Subtitles, null) },
            onClick = { backstack.add(SubtitlesPreferencesScreen) },
          )
          preference(
            key = "audio",
            title = { Text(text = stringResource(id = R.string.pref_audio)) },
            summary = { Text(text = stringResource(id = R.string.pref_audio_summary)) },
            icon = { Icon(Icons.Outlined.Audiotrack, null) },
            onClick = { backstack.add(AudioPreferencesScreen) },
          )
          preference(
            key = "customButtons",
            title = { Text(text = stringResource(id = R.string.pref_custom_buttons_title)) },
            summary = { Text(text = stringResource(id = R.string.pref_custom_buttons_summary)) },
            icon = { Icon(Icons.Outlined.Terminal, null) },
            onClick = { backstack.add(CustomButtonsScreen) },
          )
          preference(
            key = "advanced",
            title = { Text(text = stringResource(R.string.pref_advanced)) },
            summary = { Text(text = stringResource(id = R.string.pref_advanced_summary)) },
            icon = { Icon(Icons.Outlined.Code, null) },
            onClick = { backstack.add(AdvancedPreferencesScreen) }
          )
          preference(
            key = "about",
            title = { Text(text = stringResource(id = R.string.pref_about_title)) },
            summary = { Text(text = stringResource(id = R.string.pref_about_summary)) },
            icon = { Icon(Icons.Outlined.Info, null) },
            onClick = { backstack.add(AboutScreen) },
          )
        }
      }
    }
  }
}
