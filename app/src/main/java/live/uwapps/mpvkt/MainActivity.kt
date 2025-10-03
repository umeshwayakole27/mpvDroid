package live.uwapps.mpvkt

import android.app.Activity
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntOffset
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import live.uwapps.mpvkt.preferences.AppearancePreferences
import live.uwapps.mpvkt.preferences.preference.collectAsState
import live.uwapps.mpvkt.presentation.Screen
import live.uwapps.mpvkt.ui.main.MainScreen
import live.uwapps.mpvkt.ui.theme.DarkMode
import live.uwapps.mpvkt.ui.theme.MpvKtTheme
import live.uwapps.mpvkt.ui.utils.LocalBackStack
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
  private val appearancePreferences by inject<AppearancePreferences>()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
      onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
          // Predictive back progress logic
          if (shouldInterceptBackGesture()) {
            // Handle predictive back gesture
            performBackProgressAnimation()
          } else {
            isEnabled = false
            onBackPressedDispatcher.onBackPressed()
          }
        }
      })
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
      MpvKtTheme { Surface { Navigator() } }
    }
  }

  private fun shouldInterceptBackGesture(): Boolean {
    // Only intercept back gestures for specific navigation scenarios
    return false // Let the navigation system handle back gestures naturally
  }

  private fun performBackProgressAnimation() {
    // Predictive back animation is handled by the Compose navigation system
    // No additional implementation needed here
  }

  @Composable
  fun Navigator() {
    val backstack = rememberNavBackStack<Screen>(MainScreen)
    val activity = LocalContext.current as? Activity
    CompositionLocalProvider(LocalBackStack provides backstack) {
      NavDisplay(
        backStack = backstack,
        onBack = {
          val popped = backstack.removeLastOrNull()
          if (popped == null) activity?.finish()
        },
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
                TransformOrigin(-1f, .5f),
              )
            ).togetherWith(
            fadeOut(animationSpec = tween(220)) +
              scaleOut(animationSpec = tween(220, delayMillis = 30), targetScale = .9f, TransformOrigin(-1f, .5f)),
          )
        },
      )
    }
  }
}
