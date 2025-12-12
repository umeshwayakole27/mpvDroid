package com.uw.mpvDroid.ui.utils

import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.uw.mpvDroid.presentation.Screen

typealias NavBackStack = SnapshotStateList<Screen>

val LocalBackStack: ProvidableCompositionLocal<NavBackStack> =
  compositionLocalOf { error("LocalBackStack not initialized!") }

val LocalHideBottomNav: ProvidableCompositionLocal<(Boolean) -> Unit> =
  compositionLocalOf { {} }

val LocalShowBottomNav: ProvidableCompositionLocal<(Boolean) -> Unit> =
  compositionLocalOf { {} }

// State for selection mode bottom bar
data class SelectionBottomBarState(
  val isVisible: Boolean = false,
  val onCopyClick: () -> Unit = {},
  val onMoveClick: () -> Unit = {},
  val onRenameClick: () -> Unit = {},
  val onDeleteClick: () -> Unit = {},
  val onAddToPlaylistClick: () -> Unit = {},
)

val LocalSelectionBottomBar: ProvidableCompositionLocal<(SelectionBottomBarState) -> Unit> =
  compositionLocalOf { {} }
