package live.uwapps.mpvkt.ui.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import live.uwapps.mpvkt.domain.models.*

@Composable
fun LibraryControls(
  sortOption: SortOption,
  filterOption: FilterOption,
  groupOption: GroupOption,
  viewMode: ViewMode,
  selectedFolders: Set<String>, // Added for folder filtering
  onSortOptionChanged: (SortOption) -> Unit,
  onFilterOptionChanged: (FilterOption) -> Unit,
  onGroupOptionChanged: (GroupOption) -> Unit,
  onViewModeChanged: (ViewMode) -> Unit,
  onFolderFilterClick: () -> Unit, // Added for folder filtering
  modifier: Modifier = Modifier
) {
  Column(modifier = modifier) {
    // Filter chips row
    LazyRow(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      contentPadding = PaddingValues(vertical = 8.dp)
    ) {
      items(FilterOption.entries) { filter ->
        FilterChip(
          selected = filterOption == filter,
          onClick = { onFilterOptionChanged(filter) },
          label = { Text(filter.displayName) }
        )
      }

      // Folder filter chip - show when folders are selected
      if (selectedFolders.isNotEmpty()) {
        item {
          FilterChip(
            selected = true,
            onClick = onFolderFilterClick,
            label = { Text("${selectedFolders.size} Folders") },
            leadingIcon = {
              Icon(
                Icons.Default.Folder,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
              )
            }
          )
        }
      }
    }

    // Control buttons row
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        SortDropdown(
          selectedSort = sortOption,
          onSortSelected = onSortOptionChanged
        )
        
        GroupDropdown(
          selectedGroup = groupOption,
          onGroupSelected = onGroupOptionChanged
        )

        // Folder filter button
        OutlinedButton(
          onClick = onFolderFilterClick,
          colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selectedFolders.isNotEmpty())
              MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            else
              MaterialTheme.colorScheme.surface,
            contentColor = if (selectedFolders.isNotEmpty())
              MaterialTheme.colorScheme.primary
            else
              MaterialTheme.colorScheme.onSurface
          )
        ) {
          Icon(
            Icons.Default.FolderOpen,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            if (selectedFolders.isNotEmpty()) "Folders (${selectedFolders.size})" else "Folders"
          )
        }
      }

      ViewModeToggle(
        viewMode = viewMode,
        onViewModeChanged = onViewModeChanged
      )
    }
  }
}

@Composable
private fun SortDropdown(
  selectedSort: SortOption,
  onSortSelected: (SortOption) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Box {
    OutlinedButton(
      onClick = { expanded = true }
    ) {
      Icon(Icons.Default.Sort, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Sort")
    }
    
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      SortOption.entries.forEach { option ->
        DropdownMenuItem(
          text = { Text(option.displayName) },
          onClick = {
            onSortSelected(option)
            expanded = false
          },
          leadingIcon = {
            if (selectedSort == option) {
              Icon(Icons.Default.Check, contentDescription = null)
            }
          }
        )
      }
    }
  }
}

@Composable
private fun GroupDropdown(
  selectedGroup: GroupOption,
  onGroupSelected: (GroupOption) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }

  Box {
    OutlinedButton(
      onClick = { expanded = true }
    ) {
      Icon(Icons.Default.Group, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Group")
    }
    
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false }
    ) {
      GroupOption.entries.forEach { option ->
        DropdownMenuItem(
          text = { Text(option.displayName) },
          onClick = {
            onGroupSelected(option)
            expanded = false
          },
          leadingIcon = {
            if (selectedGroup == option) {
              Icon(Icons.Default.Check, contentDescription = null)
            }
          }
        )
      }
    }
  }
}

@Composable
private fun ViewModeToggle(
  viewMode: ViewMode,
  onViewModeChanged: (ViewMode) -> Unit
) {
  IconButton(
    onClick = {
      onViewModeChanged(
        if (viewMode == ViewMode.GRID) ViewMode.LIST else ViewMode.GRID
      )
    }
  ) {
    Icon(
      imageVector = if (viewMode == ViewMode.GRID) Icons.Default.ViewList else Icons.Default.GridView,
      contentDescription = "Toggle view mode"
    )
  }
}
