package live.uwapps.mpvkt.ui.preferences

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.serialization.Serializable
import live.uwapps.mpvkt.presentation.Screen
import live.uwapps.mpvkt.presentation.library.LibraryViewModel
import live.uwapps.mpvkt.domain.models.*
import live.uwapps.mpvkt.ui.utils.LocalBackStack
import me.zhanghai.compose.preference.*
import org.koin.compose.viewmodel.koinViewModel

@Serializable
object LibraryPreferencesScreen : Screen {
  @OptIn(ExperimentalMaterial3Api::class)
  @Composable
  override fun Content() {
    val viewModel: LibraryViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // State for dropdown selections
    var showSortDialog by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }
    var showGroupDialog by remember { mutableStateOf(false) }

    Scaffold(
      topBar = {
        TopAppBar(
          title = { Text("Library Settings") }
        )
      }
    ) { padding ->
      ProvidePreferenceLocals {
        LazyColumn(
          modifier = Modifier
            .fillMaxSize()
            .padding(padding),
        ) {
          preferenceCategory(
            key = "library_defaults",
            title = { Text("Default Settings") }
          )

          // Default Sort Option - now functional
          preference(
            key = "library_sort_option",
            title = { Text("Default Sort Order") },
            summary = { Text("Current: ${uiState.sortOption.displayName}") },
            icon = { Icon(Icons.Outlined.Sort, null) },
            onClick = { showSortDialog = true }
          )

          // Default Filter Option - now functional
          preference(
            key = "library_filter_option",
            title = { Text("Default Filter") },
            summary = { Text("Current: ${uiState.filterOption.displayName}") },
            icon = { Icon(Icons.Outlined.FilterList, null) },
            onClick = { showFilterDialog = true }
          )

          // Default Group Option - now functional
          preference(
            key = "library_group_option",
            title = { Text("Default Grouping") },
            summary = { Text("Current: ${uiState.groupOption.displayName}") },
            icon = { Icon(Icons.Outlined.Group, null) },
            onClick = { showGroupDialog = true }
          )

          // Default View Mode - toggle functionality
          preference(
            key = "library_view_mode",
            title = { Text("Default View Mode") },
            summary = { Text("Current: ${if (uiState.viewMode == ViewMode.GRID) "Grid View" else "List View"}") },
            icon = { Icon(Icons.Outlined.ViewComfy, null) },
            onClick = { viewModel.onViewModeChanged(if (uiState.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID) }
          )

          preferenceCategory(
            key = "library_actions",
            title = { Text("Library Actions") }
          )

          // Scan Library Action
          preference(
            key = "scan_library",
            title = { Text("Scan for Videos") },
            summary = { Text("Search for new video files on device") },
            icon = { Icon(Icons.Outlined.Refresh, null) },
            onClick = { viewModel.scanLibrary() }
          )
        }
      }

      // Sort Options Dialog
      if (showSortDialog) {
        AlertDialog(
          onDismissRequest = { showSortDialog = false },
          title = { Text("Choose Sort Order") },
          text = {
            LazyColumn {
              items(SortOption.entries.size) { index ->
                val option = SortOption.entries[index]
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  RadioButton(
                    selected = uiState.sortOption == option,
                    onClick = {
                      viewModel.onSortOptionChanged(option)
                      showSortDialog = false
                    }
                  )
                  Text(
                    text = option.displayName,
                    modifier = Modifier
                      .weight(1f)
                      .padding(start = 8.dp)
                  )
                }
              }
            }
          },
          confirmButton = {
            TextButton(onClick = { showSortDialog = false }) {
              Text("Cancel")
            }
          }
        )
      }

      // Filter Options Dialog
      if (showFilterDialog) {
        AlertDialog(
          onDismissRequest = { showFilterDialog = false },
          title = { Text("Choose Filter") },
          text = {
            LazyColumn {
              items(FilterOption.entries.size) { index ->
                val option = FilterOption.entries[index]
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  RadioButton(
                    selected = uiState.filterOption == option,
                    onClick = {
                      viewModel.onFilterOptionChanged(option)
                      showFilterDialog = false
                    }
                  )
                  Text(
                    text = option.displayName,
                    modifier = Modifier
                      .weight(1f)
                      .padding(start = 8.dp)
                  )
                }
              }
            }
          },
          confirmButton = {
            TextButton(onClick = { showFilterDialog = false }) {
              Text("Cancel")
            }
          }
        )
      }

      // Group Options Dialog
      if (showGroupDialog) {
        AlertDialog(
          onDismissRequest = { showGroupDialog = false },
          title = { Text("Choose Grouping") },
          text = {
            LazyColumn {
              items(GroupOption.entries.size) { index ->
                val option = GroupOption.entries[index]
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  RadioButton(
                    selected = uiState.groupOption == option,
                    onClick = {
                      viewModel.onGroupOptionChanged(option)
                      showGroupDialog = false
                    }
                  )
                  Text(
                    text = option.displayName,
                    modifier = Modifier
                      .weight(1f)
                      .padding(start = 8.dp)
                  )
                }
              }
            }
          },
          confirmButton = {
            TextButton(onClick = { showGroupDialog = false }) {
              Text("Cancel")
            }
          }
        )
      }
    }
  }
}
