package com.uw.mpvDroid.ui.browser.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog

@Composable
fun StreamLinkDialog(
  isOpen: Boolean,
  onDismiss: () -> Unit,
  onPlay: (String) -> Unit,
) {
  if (!isOpen) return

  val context = LocalContext.current
  var linkUrl by rememberSaveable { mutableStateOf("") }

  Dialog(onDismissRequest = onDismiss) {
    Card(
      shape = RoundedCornerShape(16.dp),
      modifier = Modifier.fillMaxWidth(),
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(24.dp),
      ) {
        Text(
          text = "Play Stream Link",
          style = MaterialTheme.typography.headlineSmall,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
          value = linkUrl,
          onValueChange = { linkUrl = it },
          label = { Text("Video URL") },
          placeholder = {
            Text(
              text = "https://example.com/video.mp4",
              color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          leadingIcon = {
            Icon(
              imageVector = Icons.Filled.Link,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
          },
          modifier = Modifier.fillMaxWidth(),
          singleLine = false,
          maxLines = 3,
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
          OutlinedButton(
            onClick = {
              val clipboardManager =
                context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
              val clipData = clipboardManager?.primaryClip
              if (clipData != null && clipData.itemCount > 0) {
                val text = clipData.getItemAt(0).text?.toString() ?: ""
                if (text.isNotBlank()) {
                  linkUrl = text
                }
              }
            },
            modifier = Modifier.weight(1f),
          ) {
            Icon(
              imageVector = Icons.Filled.ContentPaste,
              contentDescription = null,
              modifier = Modifier.padding(end = 8.dp),
            )
            Text("Paste")
          }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End),
        ) {
          TextButton(
            onClick = {
              linkUrl = ""
              onDismiss()
            },
          ) {
            Text("Cancel")
          }

          Button(
            onClick = {
              if (linkUrl.isNotBlank()) {
                onPlay(linkUrl)
                linkUrl = ""
                onDismiss()
              }
            },
            enabled = linkUrl.isNotBlank(),
          ) {
            Text("Play")
          }
        }
      }
    }
  }
}
