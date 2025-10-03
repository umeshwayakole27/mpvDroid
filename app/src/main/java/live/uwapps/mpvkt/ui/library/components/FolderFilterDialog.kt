package live.uwapps.mpvkt.ui.library.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import live.uwapps.mpvkt.domain.models.Folder
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderFilterDialog(
  folders: List<Folder>,
  selectedFolders: Set<String>,
  onApply: (Set<String>) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  var currentSelectedFolders by remember { mutableStateOf(selectedFolders) }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = modifier
        .fillMaxWidth()
        .heightIn(max = 600.dp),
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
            text = "Filter by Folders",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
          )

          IconButton(onClick = onDismiss) {
            Icon(
              Icons.Default.Close,
              contentDescription = "Close",
              tint = MaterialTheme.colorScheme.onSurface
            )
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Selection controls
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          OutlinedButton(
            onClick = { currentSelectedFolders = folders.map { it.path }.toSet() },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = MaterialTheme.colorScheme.primary
            )
          ) {
            Icon(
              Icons.Default.SelectAll,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Select All")
          }

          OutlinedButton(
            onClick = { currentSelectedFolders = emptySet() },
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.outlinedButtonColors(
              contentColor = MaterialTheme.colorScheme.secondary
            )
          ) {
            Icon(
              Icons.Default.Clear,
              contentDescription = null,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text("Clear All")
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Folder selection info
        if (currentSelectedFolders.isNotEmpty()) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
              containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(8.dp)
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                Icons.Default.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "${currentSelectedFolders.size} of ${folders.size} folders selected",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            }
          }
          Spacer(modifier = Modifier.height(8.dp))
        }

        // Folders list
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
          items(folders) { folder ->
            FolderItem(
              folder = folder,
              isSelected = currentSelectedFolders.contains(folder.path),
              onToggle = {
                currentSelectedFolders = if (currentSelectedFolders.contains(folder.path)) {
                  currentSelectedFolders - folder.path
                } else {
                  currentSelectedFolders + folder.path
                }
              }
            )
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Action buttons
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.End,
          verticalAlignment = Alignment.CenterVertically
        ) {
          TextButton(onClick = onDismiss) {
            Text("Cancel")
          }
          Spacer(modifier = Modifier.width(8.dp))
          Button(
            onClick = {
              onApply(currentSelectedFolders)
              onDismiss()
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary
            )
          ) {
            Text("Apply Filter")
          }
        }
      }
    }
  }
}

@Composable
private fun FolderItem(
  folder: Folder,
  isSelected: Boolean,
  onToggle: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier
      .fillMaxWidth(),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
      else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ),
    shape = RoundedCornerShape(8.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Checkbox(
        checked = isSelected,
        onCheckedChange = { onToggle() },
        colors = CheckboxDefaults.colors(
          checkedColor = MaterialTheme.colorScheme.primary
        )
      )

      Spacer(modifier = Modifier.width(12.dp))

      Icon(
        Icons.Default.Folder,
        contentDescription = null,
        tint = if (isSelected)
          MaterialTheme.colorScheme.primary
        else
          MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(20.dp)
      )

      Spacer(modifier = Modifier.width(12.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = File(folder.path).name,
          style = MaterialTheme.typography.bodyMedium,
          fontWeight = if (isSelected) FontWeight.Medium else FontWeight.Normal,
          color = if (isSelected)
            MaterialTheme.colorScheme.onPrimaryContainer
          else
            MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        Text(
          text = folder.path,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )

        if (folder.videoCount > 0) {
          Text(
            text = "${folder.videoCount} videos",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary
          )
        }
      }
    }
  }
}
