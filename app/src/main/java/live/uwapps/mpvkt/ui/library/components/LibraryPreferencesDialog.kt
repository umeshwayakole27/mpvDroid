package live.uwapps.mpvkt.ui.library.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.FilterList
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material.icons.outlined.ViewComfy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import live.uwapps.mpvkt.domain.models.*
import live.uwapps.mpvkt.presentation.library.LibraryViewModel
import live.uwapps.mpvkt.presentation.library.LibraryUiState

@Composable
fun LibraryPreferencesDialog(
  uiState: LibraryUiState,
  viewModel: LibraryViewModel,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  // State for nested dialogs
  var showSortDialog by remember { mutableStateOf(false) }
  var showFilterDialog by remember { mutableStateOf(false) }
  var showGroupDialog by remember { mutableStateOf(false) }
  var showFolderDialog by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = modifier
        .fillMaxWidth()
        .heightIn(max = 700.dp),
      shape = RoundedCornerShape(16.dp),
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier.padding(16.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Library Settings",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          IconButton(onClick = onDismiss) {
            Icon(
              Icons.Default.Cancel,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Settings Content
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          item {
            Text(
              text = "Default Settings",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(bottom = 8.dp)
            )
          }

          // Sort Option
          item {
            PreferenceItem(
              title = "Sort Order",
              summary = uiState.sortOption.displayName,
              icon = Icons.AutoMirrored.Outlined.Sort,
              onClick = { showSortDialog = true }
            )
          }

          // Filter Option
          item {
            PreferenceItem(
              title = "Filter",
              summary = uiState.filterOption.displayName,
              icon = Icons.Outlined.FilterList,
              onClick = { showFilterDialog = true }
            )
          }

          // Group Option
          item {
            PreferenceItem(
              title = "Grouping",
              summary = uiState.groupOption.displayName,
              icon = Icons.Outlined.Group,
              onClick = { showGroupDialog = true }
            )
          }

          // View Mode
          item {
            PreferenceItem(
              title = "View Mode",
              summary = if (uiState.viewMode == ViewMode.GRID) "Grid View" else "List View",
              icon = Icons.Outlined.ViewComfy,
              onClick = {
                viewModel.onViewModeChanged(
                  if (uiState.viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
                )
              }
            )
          }

          item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Actions",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.SemiBold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(vertical = 8.dp)
            )
          }

          // Folder Selection
          item {
            PreferenceItem(
              title = "Select Folder",
              summary = if (uiState.selectedFolderPath != null) {
                uiState.folders.find { it.path == uiState.selectedFolderPath }?.name ?: "Selected folder"
              } else {
                "All videos from device"
              },
              icon = Icons.Outlined.Folder,
              onClick = { showFolderDialog = true }
            )
          }

          // Scan Library
          item {
            PreferenceItem(
              title = "Scan for Videos",
              summary = "Search for new video files",
              icon = Icons.Outlined.Refresh,
              onClick = { viewModel.scanLibrary() }
            )
          }
        }
      }
    }
  }

  // Nested Dialogs
  if (showSortDialog) {
    SelectionDialog(
      title = "Choose Sort Order",
      options = SortOption.entries,
      currentSelection = uiState.sortOption,
      onOptionSelected = { option ->
        viewModel.onSortOptionChanged(option)
        showSortDialog = false
      },
      onDismiss = { showSortDialog = false },
      getDisplayName = { it.displayName }
    )
  }

  if (showFilterDialog) {
    SelectionDialog(
      title = "Choose Filter",
      options = FilterOption.entries,
      currentSelection = uiState.filterOption,
      onOptionSelected = { option ->
        viewModel.onFilterOptionChanged(option)
        showFilterDialog = false
      },
      onDismiss = { showFilterDialog = false },
      getDisplayName = { it.displayName }
    )
  }

  if (showGroupDialog) {
    SelectionDialog(
      title = "Choose Grouping",
      options = GroupOption.entries,
      currentSelection = uiState.groupOption,
      onOptionSelected = { option ->
        viewModel.onGroupOptionChanged(option)
        showGroupDialog = false
      },
      onDismiss = { showGroupDialog = false },
      getDisplayName = { it.displayName }
    )
  }

  if (showFolderDialog) {
    FolderSelectionDialog(
      folders = uiState.folders,
      selectedFolderPath = uiState.selectedFolderPath,
      onSelectFolder = { folderPath: String? ->
        viewModel.selectFolder(folderPath)
        showFolderDialog = false
      },
      onDismiss = { showFolderDialog = false }
    )
  }
}

@Composable
private fun PreferenceItem(
  title: String,
  summary: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  onClick: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    onClick = onClick,
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ),
    shape = RoundedCornerShape(12.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = Modifier.size(24.dp)
      )

      Spacer(modifier = Modifier.width(16.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Medium,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = summary,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Icon(
        Icons.Outlined.ChevronRight,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
      )
    }
  }
}

@Composable
private fun <T> SelectionDialog(
  title: String,
  options: List<T>,
  currentSelection: T,
  onOptionSelected: (T) -> Unit,
  onDismiss: () -> Unit,
  getDisplayName: (T) -> String,
  modifier: Modifier = Modifier
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title) },
    text = {
      LazyColumn {
        items(options.size) { index ->
          val option = options[index]
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(
              selected = currentSelection == option,
              onClick = { onOptionSelected(option) }
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = getDisplayName(option),
              style = MaterialTheme.typography.bodyMedium
            )
          }
        }
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
