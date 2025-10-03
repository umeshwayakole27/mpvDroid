package live.uwapps.mpvkt.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.serialization.Serializable
import live.uwapps.mpvkt.presentation.Screen
import live.uwapps.mpvkt.ui.library.LibraryScreen
import live.uwapps.mpvkt.ui.preferences.PreferencesScreen

@Serializable
object MainScreen : Screen {
  @Composable
  override fun Content() {
    var selectedTab by rememberSaveable { mutableStateOf(BottomTab.LIBRARY) }

    // Simple back handler - only handle system back button
    BackHandler(enabled = selectedTab == BottomTab.SETTINGS) {
      selectedTab = BottomTab.LIBRARY
    }

    Scaffold(
      containerColor = MaterialTheme.colorScheme.surface,
      bottomBar = {
        NavigationBar(
          modifier = Modifier.clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp)),
          containerColor = MaterialTheme.colorScheme.surfaceContainer,
          tonalElevation = 3.dp
        ) {
          BottomTab.entries.forEach { tab ->
            val selected = selectedTab == tab

            NavigationBarItem(
              selected = selected,
              onClick = { selectedTab = tab },
              icon = {
                when (tab) {
                  BottomTab.LIBRARY -> Icon(Icons.Default.VideoLibrary, contentDescription = "Library")
                  BottomTab.SETTINGS -> Icon(Icons.Default.Settings, contentDescription = "Settings")
                }
              },
              label = {
                Text(
                  when (tab) {
                    BottomTab.LIBRARY -> "Library"
                    BottomTab.SETTINGS -> "Settings"
                  }
                )
              },
              alwaysShowLabel = true
            )
          }
        }
      }
    ) { padding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(padding)
      ) {
        // Simple content switching without complex navigation
        when (selectedTab) {
          BottomTab.LIBRARY -> LibraryScreen.Content()
          BottomTab.SETTINGS -> PreferencesScreen.Content()
        }
      }
    }
  }
}

enum class BottomTab { LIBRARY, SETTINGS }
