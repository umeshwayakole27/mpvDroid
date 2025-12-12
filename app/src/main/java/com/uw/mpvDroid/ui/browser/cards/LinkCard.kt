package com.uw.mpvDroid.ui.browser.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun LinkCard(
  url: String,
  displayName: String,
  timestamp: Long,
  onClick: () -> Unit,
  onLongClick: () -> Unit = {},
  onCopy: () -> Unit,
  onShare: () -> Unit,
  isSelected: Boolean = false,
  modifier: Modifier = Modifier,
) {
  Card(
    modifier = modifier
      .fillMaxWidth()
      .padding(horizontal = 8.dp, vertical = 4.dp)
      .combinedClickable(
        onClick = onClick,
        onLongClick = onLongClick,
      ),
    shape = RoundedCornerShape(12.dp),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    colors = CardDefaults.cardColors(
      containerColor = if (isSelected) {
        MaterialTheme.colorScheme.primaryContainer
      } else {
        MaterialTheme.colorScheme.surfaceContainerLow
      },
    ),
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      // Link icon
      Icon(
        imageVector = Icons.Filled.Link,
        contentDescription = null,
        modifier = Modifier.size(48.dp),
        tint = MaterialTheme.colorScheme.primary,
      )

      Spacer(modifier = Modifier.width(16.dp))

      // Content
      Column(
        modifier = Modifier.weight(1f),
      ) {
        Text(
          text = displayName,
          style = MaterialTheme.typography.bodyLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )

        Text(
          text = url,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
        )

        // Timestamp
        Text(
          text = formatTimestamp(timestamp),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          modifier = Modifier.padding(top = 4.dp),
        )
      }

      // Action buttons
      Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = onCopy) {
          Icon(
            imageVector = Icons.Filled.ContentCopy,
            contentDescription = "Copy",
            tint = MaterialTheme.colorScheme.primary,
          )
        }

        IconButton(onClick = onShare) {
          Icon(
            imageVector = Icons.Filled.Share,
            contentDescription = "Share",
            tint = MaterialTheme.colorScheme.primary,
          )
        }

        IconButton(onClick = onClick) {
          Icon(
            imageVector = Icons.Filled.PlayArrow,
            contentDescription = "Play",
            tint = MaterialTheme.colorScheme.primary,
          )
        }
      }
    }
  }
}

private fun formatTimestamp(timestamp: Long): String {
  val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
  return sdf.format(Date(timestamp))
}
