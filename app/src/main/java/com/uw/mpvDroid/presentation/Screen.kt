package com.uw.mpvDroid.presentation

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavKey

interface Screen : NavKey {
  @Composable
  fun Content()
}
