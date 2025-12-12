package com.uw.mpvDroid.ui.browser

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.core.tween
import com.uw.mpvDroid.ui.theme.MotionTokens
import com.uw.mpvDroid.ui.theme.MotionSpec
import com.uw.mpvDroid.ui.utils.NavigationType
import com.uw.mpvDroid.ui.utils.getNavigationType
import com.uw.mpvDroid.ui.components.navigation.ExpressiveNavigationBar
import com.uw.mpvDroid.ui.components.navigation.ExpressiveNavigationBarItem
import com.uw.mpvDroid.ui.components.navigation.ExpressiveNavigationRail
import com.uw.mpvDroid.ui.components.navigation.ExpressiveNavigationRailItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.PlaylistPlay
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.vector.ImageVector
import com.uw.mpvDroid.presentation.Screen
import com.uw.mpvDroid.ui.browser.folderlist.FolderListScreen
import com.uw.mpvDroid.ui.browser.networkstreaming.NetworkStreamingScreen
import com.uw.mpvDroid.ui.browser.playlist.PlaylistScreen
import com.uw.mpvDroid.ui.browser.recentlyplayed.RecentlyPlayedScreen
import com.uw.mpvDroid.ui.preferences.PreferencesScreen
import kotlinx.serialization.Serializable

@Serializable
object MainScreen : Screen {
  // Use a companion object to store state more persistently
  private var persistentSelectedTab: Int = 0

  @Composable
  override fun Content() {
    var selectedTab by remember {
      mutableIntStateOf(persistentSelectedTab)
    }

    // State to control bottom navigation bar visibility
    var hideBottomNav by remember { mutableStateOf(false) }
    var showBottomNavBar by remember { mutableStateOf(true) }
    
    // State for selection bottom bar
    var selectionBarState by remember { 
      mutableStateOf(com.uw.mpvDroid.ui.utils.SelectionBottomBarState())
    }
    
    // Get adaptive navigation type based on screen size
    val navigationType = getNavigationType()

    // Update persistent state whenever tab changes
    LaunchedEffect(selectedTab) {
      persistentSelectedTab = selectedTab
    }

    // Handle back button: navigate to Folders tab if not already there
    BackHandler(enabled = selectedTab != 0) {
      selectedTab = 0
    }

    val tabs = listOf(
      BottomNavItem(
        label = "Folders",
        selectedIcon = Icons.Filled.Folder,
        unselectedIcon = Icons.Outlined.Folder,
      ),
      BottomNavItem(
        label = "Recent",
        selectedIcon = Icons.Filled.History,
        unselectedIcon = Icons.Outlined.History,
      ),
      BottomNavItem(
        label = "Playlist",
        selectedIcon = Icons.Filled.PlaylistPlay,
        unselectedIcon = Icons.Outlined.PlaylistPlay,
      ),
      BottomNavItem(
        label = "Network",
        selectedIcon = Icons.Filled.Wifi,
        unselectedIcon = Icons.Outlined.Wifi,
      ),
      BottomNavItem(
        label = "Settings",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
      ),
    )

    // Use adaptive layout based on screen size
    when (navigationType) {
      NavigationType.BOTTOM_NAVIGATION -> {
        // Phone layout with bottom navigation
        Scaffold(
          modifier = Modifier.fillMaxSize(),
          bottomBar = {
            androidx.compose.animation.AnimatedVisibility(
              visible = showBottomNavBar,
              enter = androidx.compose.animation.slideInVertically(
                initialOffsetY = { it },
                animationSpec = MotionSpec.enter()
              ),
              exit = androidx.compose.animation.slideOutVertically(
                targetOffsetY = { it },
                animationSpec = MotionSpec.exit()
              )
            ) {
              androidx.compose.animation.Crossfade(
                targetState = selectionBarState.isVisible,
                animationSpec = MotionSpec.standard(),
                label = "bottomBarCrossfade"
              ) { showSelectionBar ->
                if (showSelectionBar) {
                  // Selection toolbar
                  com.uw.mpvDroid.ui.browser.components.BrowserBottomBar(
                    isSelectionMode = true,
                    onCopyClick = selectionBarState.onCopyClick,
                    onMoveClick = selectionBarState.onMoveClick,
                    onRenameClick = selectionBarState.onRenameClick,
                    onDeleteClick = selectionBarState.onDeleteClick,
                    onAddToPlaylistClick = selectionBarState.onAddToPlaylistClick,
                  )
                } else {
                  // Main navigation
                  ExpressiveNavigationBar {
                    tabs.forEachIndexed { index, item ->
                      ExpressiveNavigationBarItem(
                        icon = {
                          Icon(
                            imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                            contentDescription = item.label,
                          )
                        },
                        label = { Text(item.label) },
                        selected = selectedTab == index,
                        onClick = {
                          selectedTab = index
                        },
                      )
                    }
                  }
                }
              }
            }
          },
        ) { paddingValues ->
          ScreenContent(
            selectedTab = selectedTab,
            paddingValues = paddingValues,
            hideBottomNav = hideBottomNav,
            showBottomNavBar = showBottomNavBar,
            selectionBarState = selectionBarState,
            onHideBottomNavChange = { hide -> hideBottomNav = hide },
            onShowBottomNavBarChange = { show -> showBottomNavBar = show },
            onSelectionBarStateChange = { state -> selectionBarState = state }
          )
        }
      }
      
      NavigationType.NAVIGATION_RAIL -> {
        // Tablet layout with navigation rail
        Row(modifier = Modifier.fillMaxSize()) {
          ExpressiveNavigationRail {
            tabs.forEachIndexed { index, item ->
              ExpressiveNavigationRailItem(
                icon = {
                  Icon(
                    imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                  )
                },
                label = { Text(item.label) },
                selected = selectedTab == index,
                onClick = {
                  selectedTab = index
                },
              )
            }
          }
          
          ScreenContent(
            selectedTab = selectedTab,
            paddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
            hideBottomNav = hideBottomNav,
            showBottomNavBar = showBottomNavBar,
            selectionBarState = selectionBarState,
            onHideBottomNavChange = { hide -> hideBottomNav = hide },
            onShowBottomNavBarChange = { show -> showBottomNavBar = show },
            onSelectionBarStateChange = { state -> selectionBarState = state }
          )
        }
      }
      
      NavigationType.PERMANENT_NAVIGATION_DRAWER -> {
        // Large screen layout with permanent drawer (future implementation)
        // For now, use navigation rail
        Row(modifier = Modifier.fillMaxSize()) {
          ExpressiveNavigationRail {
            tabs.forEachIndexed { index, item ->
              ExpressiveNavigationRailItem(
                icon = {
                  Icon(
                    imageVector = if (selectedTab == index) item.selectedIcon else item.unselectedIcon,
                    contentDescription = item.label,
                  )
                },
                label = { Text(item.label) },
                selected = selectedTab == index,
                onClick = {
                  selectedTab = index
                },
              )
            }
          }
          
          ScreenContent(
            selectedTab = selectedTab,
            paddingValues = androidx.compose.foundation.layout.PaddingValues(0.dp),
            hideBottomNav = hideBottomNav,
            showBottomNavBar = showBottomNavBar,
            selectionBarState = selectionBarState,
            onHideBottomNavChange = { hide -> hideBottomNav = hide },
            onShowBottomNavBarChange = { show -> showBottomNavBar = show },
            onSelectionBarStateChange = { state -> selectionBarState = state }
          )
        }
      }
    }
  }
  
  @Composable
  private fun ScreenContent(
    selectedTab: Int,
    paddingValues: androidx.compose.foundation.layout.PaddingValues,
    hideBottomNav: Boolean,
    showBottomNavBar: Boolean,
    selectionBarState: com.uw.mpvDroid.ui.utils.SelectionBottomBarState,
    onHideBottomNavChange: (Boolean) -> Unit,
    onShowBottomNavBarChange: (Boolean) -> Unit,
    onSelectionBarStateChange: (com.uw.mpvDroid.ui.utils.SelectionBottomBarState) -> Unit
  ) {
    // Pass bottom padding to each screen and provide controls
    androidx.compose.runtime.CompositionLocalProvider(
      com.uw.mpvDroid.ui.utils.LocalHideBottomNav provides onHideBottomNavChange,
      com.uw.mpvDroid.ui.utils.LocalShowBottomNav provides onShowBottomNavBarChange,
      com.uw.mpvDroid.ui.utils.LocalSelectionBottomBar provides onSelectionBarStateChange
    ) {
      // Fade-through transition for tab navigation
      AnimatedContent(
        targetState = selectedTab,
        transitionSpec = {
          fadeIn(
            animationSpec = tween(
              durationMillis = MotionTokens.DurationMedium2,
              delayMillis = MotionTokens.DurationShort1,
              easing = MotionTokens.EmphasizedDecelerate
            )
          ) togetherWith fadeOut(
            animationSpec = tween(
              durationMillis = MotionTokens.DurationShort2,
              easing = MotionTokens.EmphasizedAccelerate
            )
          )
        },
        label = "tabTransition",
        modifier = Modifier.fillMaxSize().padding(paddingValues)
      ) { tab ->
        when (tab) {
          0 -> FolderListScreen.Content()
          1 -> RecentlyPlayedScreen.Content()
          2 -> PlaylistScreen.Content()
          3 -> NetworkStreamingScreen.Content()
          4 -> PreferencesScreen.Content()
        }
      }
    }
  }

  private data class BottomNavItem(
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
  )
}
