package com.uw.mpvDroid.ui.preferences

import android.os.Build
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.serialization.Serializable
import com.uw.mpvDroid.R
import com.uw.mpvDroid.preferences.AppearancePreferences
import com.uw.mpvDroid.preferences.preference.collectAsState
import com.uw.mpvDroid.presentation.Screen
import com.uw.mpvDroid.presentation.preferences.MultiChoiceSegmentedButton
import com.uw.mpvDroid.ui.theme.DarkMode
import com.uw.mpvDroid.ui.utils.LocalBackStack
import me.zhanghai.compose.preference.PreferenceCategory
import me.zhanghai.compose.preference.ProvidePreferenceLocals
import me.zhanghai.compose.preference.SwitchPreference
import org.koin.compose.koinInject

@Serializable
object AppearancePreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val preferences = koinInject<AppearancePreferences>()
    val context = LocalContext.current
    val backstack = LocalBackStack.current
    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text(text = stringResource(R.string.pref_appearance_title)) },
          navigationIcon = {
            IconButton(onClick = backstack::removeLastOrNull) {
              Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
            }
          },
        )
      },
    ) { padding ->
      ProvidePreferenceLocals {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(padding),
        ) {
          PreferenceCategory(
            title = { Text(text = stringResource(id = R.string.pref_appearance_category_theme)) },
          )
          val darkMode by preferences.darkMode.collectAsState()
          MultiChoiceSegmentedButton(
            choices = DarkMode.entries.map { context.getString(it.titleRes) }.toImmutableList(),
            selectedIndices = persistentListOf(DarkMode.entries.indexOf(darkMode)),
            onClick = { preferences.darkMode.set(DarkMode.entries[it]) },
          )
          val materialYou by preferences.materialYou.collectAsState()
          val isMaterialYouAvailable = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
          SwitchPreference(
            value = materialYou,
            onValueChange = { preferences.materialYou.set(it) },
            title = { Text(text = stringResource(id = R.string.pref_appearance_material_you_title)) },
            summary = {
              Text(
                text = stringResource(
                  if (isMaterialYouAvailable) {
                    R.string.pref_appearance_material_you_summary
                  } else {
                    R.string.pref_appearance_material_you_summary_disabled
                  },
                ),
              )
            },
            enabled = isMaterialYouAvailable,
          )
          val amoledMode by preferences.amoledMode.collectAsState()
          SwitchPreference(
            value = amoledMode,
            onValueChange = { preferences.amoledMode.set(it) },
            title = { Text(text = stringResource(id = R.string.pref_appearance_amoled_mode_title)) },
            summary = { Text(text = stringResource(id = R.string.pref_appearance_amoled_mode_summary)) },
            enabled = darkMode == DarkMode.Dark || (darkMode == DarkMode.System && context.resources.configuration.uiMode and android.content.res.Configuration.UI_MODE_NIGHT_MASK == android.content.res.Configuration.UI_MODE_NIGHT_YES),
          )
        }
      }
    }
  }
}
