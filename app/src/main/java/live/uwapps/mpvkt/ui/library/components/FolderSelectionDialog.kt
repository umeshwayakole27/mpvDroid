package live.uwapps.mpvkt.ui.library.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import live.uwapps.mpvkt.ui.theme.AnimationDurations
import live.uwapps.mpvkt.ui.theme.ExpressiveEasing

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderSelectionDialog(
  folders: List<Folder>,
  selectedFolderPath: String?,
  onSelectFolder: (String?) -> Unit,
  onDismiss: () -> Unit,
  modifier: Modifier = Modifier
) {
  Dialog(onDismissRequest = onDismiss) {
    Card(
      modifier = modifier
        .fillMaxWidth()
        .heightIn(max = 600.dp),
      shape = MaterialTheme.shapes.extraLarge,
      colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
      ),
      elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
      Column(
        modifier = Modifier.padding(20.dp)
      ) {
        // Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = "Select Folder",
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

        Spacer(modifier = Modifier.height(16.dp))

        // Show All Videos Option
        Card(
          modifier = Modifier.fillMaxWidth(),
          onClick = {
            onSelectFolder(null)
            onDismiss()
          },
          shape = MaterialTheme.shapes.medium,
          colors = CardDefaults.cardColors(
            containerColor = if (selectedFolderPath == null)
              MaterialTheme.colorScheme.primaryContainer
            else
              MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
          ),
          elevation = CardDefaults.cardElevation(
            defaultElevation = if (selectedFolderPath == null) 4.dp else 0.dp
          )
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = Icons.Default.VideoLibrary,
              contentDescription = null,
              tint = if (selectedFolderPath == null)
                MaterialTheme.colorScheme.primary
              else
                MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
              modifier = Modifier.weight(1f)
            ) {
              Text(
                text = "All Videos",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (selectedFolderPath == null) FontWeight.Bold else FontWeight.Medium,
                color = if (selectedFolderPath == null)
                  MaterialTheme.colorScheme.primary
                else
                  MaterialTheme.colorScheme.onSurface
              )
              Text(
                text = "Show all videos from device",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            if (selectedFolderPath == null) {
              Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Selected",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Folders list header
        Text(
          text = "Folders (${folders.size})",
          style = MaterialTheme.typography.titleSmall,
          fontWeight = FontWeight.SemiBold,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp)
        )

        // Folders list with smooth animations
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(
            items = folders,
            key = { it.path }
          ) { folder ->
            FolderItem(
              folder = folder,
              isSelected = selectedFolderPath == folder.path,
              onSelect = {
                onSelectFolder(folder.path)
                onDismiss()
              }
            )
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
  onSelect: () -> Unit,
  modifier: Modifier = Modifier
) {
  Card(
    modifier = modifier.fillMaxWidth(),
    onClick = onSelect,
    shape = MaterialTheme.shapes.medium,
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected)
        MaterialTheme.colorScheme.primaryContainer
      else
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ),
    elevation = CardDefaults.cardElevation(
      defaultElevation = if (isSelected) 4.dp else 0.dp
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(
        imageVector = Icons.Default.Folder,
        contentDescription = null,
        tint = if (isSelected)
          MaterialTheme.colorScheme.primary
        else
          MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.size(24.dp)
      )

      Spacer(modifier = Modifier.width(16.dp))

      Column(
        modifier = Modifier.weight(1f)
      ) {
        Text(
          text = folder.name,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
          color = if (isSelected)
            MaterialTheme.colorScheme.primary
          else
            MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Text(
          text = "${folder.videoCount} video${if (folder.videoCount != 1) "s" else ""}",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = folder.path,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
      }

      if (isSelected) {
        Icon(
          Icons.Default.CheckCircle,
          contentDescription = "Selected",
          tint = MaterialTheme.colorScheme.primary,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
