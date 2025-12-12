package com.uw.mpvDroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideIn
import androidx.compose.animation.slideOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.uw.mpvDroid.preferences.AppearancePreferences
import com.uw.mpvDroid.preferences.preference.collectAsState
import com.uw.mpvDroid.presentation.Screen
import com.uw.mpvDroid.ui.browser.MainScreen
import com.uw.mpvDroid.ui.home.HomeScreen
import com.uw.mpvDroid.ui.theme.DarkMode
import com.uw.mpvDroid.ui.theme.MpvKtTheme
import com.uw.mpvDroid.ui.utils.LocalBackStack
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
  private val appearancePreferences by inject<AppearancePreferences>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent {
      val dark by appearancePreferences.darkMode.collectAsState()
      val isSystemInDarkTheme = isSystemInDarkTheme()
      enableEdgeToEdge(
        SystemBarStyle.auto(
          lightScrim = Color.White.toArgb(),
          darkScrim = Color.White.toArgb(),
        ) { dark == DarkMode.Dark || (dark == DarkMode.System && isSystemInDarkTheme) },
      )
      MpvKtTheme { Surface { Navigator() } }
    }
  }

  @Composable
  fun Navigator() {
    val backstack = rememberNavBackStack<Screen>(MainScreen)
    @Suppress("UNCHECKED_CAST")
    val typedBackstack = backstack as com.uw.mpvDroid.ui.utils.NavBackStack
    CompositionLocalProvider(LocalBackStack provides typedBackstack) {
      NavDisplay(
        backStack = backstack,
        onBack = { backstack.removeLastOrNull() },
        entryProvider = { route -> NavEntry(route) { (it as Screen).Content() } },
        popTransitionSpec = {
          fadeIn(animationSpec = tween(220)) +
            slideIn(animationSpec = tween(220)) { IntOffset(-it.width / 2, 0) } togetherWith
            fadeOut(animationSpec = tween(220)) +
            slideOut(animationSpec = tween(220)) { IntOffset(it.width / 2, 0) }
        },
        transitionSpec = {
          fadeIn(animationSpec = tween(220)) +
            slideIn(animationSpec = tween(220)) { IntOffset(it.width / 2, 0) } togetherWith
            fadeOut(animationSpec = tween(220)) +
            slideOut(animationSpec = tween(220)) { IntOffset(-it.width / 2, 0) }
        },
        predictivePopTransitionSpec = {
          (
            fadeIn(animationSpec = tween(220)) +
              scaleIn(
                animationSpec = tween(220, delayMillis = 30),
                initialScale = .9f,
                TransformOrigin(0f, .5f),
              ) +
              slideIn(animationSpec = tween(220)) { IntOffset(-it.width / 5, 0) }
            ).togetherWith(
            fadeOut(animationSpec = tween(220)) +
              scaleOut(animationSpec = tween(220, delayMillis = 30), targetScale = 1.05f, TransformOrigin(.5f, .5f)) +
              slideOut(animationSpec = tween(220)) { IntOffset(it.width / 2, 0) },
          )
        },
      )
    }
  }
}
