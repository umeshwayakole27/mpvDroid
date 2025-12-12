package com.uw.mpvDroid.ui.browser.networkstreaming

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Link
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FloatingActionButtonMenu
import androidx.compose.material3.FloatingActionButtonMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleFloatingActionButton
import androidx.compose.material3.ToggleFloatingActionButtonDefaults
import androidx.compose.material3.ToggleFloatingActionButtonDefaults.animateIcon
import androidx.compose.material3.animateFloatingActionButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.traversalIndex
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uw.mpvDroid.domain.network.NetworkConnection
import com.uw.mpvDroid.presentation.Screen
import com.uw.mpvDroid.ui.browser.components.BrowserTopBar
import com.uw.mpvDroid.ui.browser.cards.NetworkConnectionCard
import com.uw.mpvDroid.ui.browser.dialogs.AddConnectionSheet
import com.uw.mpvDroid.ui.browser.dialogs.EditConnectionSheet
import com.uw.mpvDroid.ui.browser.dialogs.StreamLinkDialog
import com.uw.mpvDroid.ui.browser.states.EmptyState
import com.uw.mpvDroid.ui.preferences.PreferencesScreen
import com.uw.mpvDroid.ui.utils.LocalBackStack
import com.uw.mpvDroid.utils.media.MediaUtils
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable

@Serializable
object NetworkStreamingScreen : Screen {
  
  @OptIn(ExperimentalMaterial3ExpressiveApi::class)
  @Composable
  override fun Content() {
    val backstack = LocalBackStack.current
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val viewModel: NetworkStreamingViewModel =
      viewModel(factory = NetworkStreamingViewModel.factory(context.applicationContext as android.app.Application))

    val connections by viewModel.connections.collectAsState()
    val connectionStatuses by viewModel.connectionStatuses.collectAsState()
    var showAddSheet by remember { mutableStateOf(false) }
    var showStreamLinkDialog by remember { mutableStateOf(false) }
    var editingConnection by remember { mutableStateOf<NetworkConnection?>(null) }
    var fabExpanded by rememberSaveable { mutableStateOf(false) }

    // LazyList state for scroll tracking
    val listState = rememberLazyListState()
    val focusRequester = remember { FocusRequester() }

    // Track scroll position to determine FAB visibility
    var previousIndex by remember { mutableIntStateOf(0) }
    var previousScrollOffset by remember { mutableIntStateOf(0) }
    var isFabVisible by remember { mutableStateOf(true) }

    // Update FAB visibility based on scroll direction
    LaunchedEffect(listState.firstVisibleItemIndex, listState.firstVisibleItemScrollOffset) {
      val currentIndex = listState.firstVisibleItemIndex
      val currentScrollOffset = listState.firstVisibleItemScrollOffset

      // Always show at top
      if (currentIndex == 0 && currentScrollOffset == 0) {
        isFabVisible = true
      } else {
        // Calculate if scrolling down or up
        val isScrollingDown = if (currentIndex != previousIndex) {
          currentIndex > previousIndex
        } else {
          currentScrollOffset > previousScrollOffset
        }

        // Hide when scrolling down, show when scrolling up
        isFabVisible = !isScrollingDown
      }

      previousIndex = currentIndex
      previousScrollOffset = currentScrollOffset
    }

    // Auto-collapse menu when scrolling
    LaunchedEffect(listState.isScrollInProgress) {
      if (fabExpanded && listState.isScrollInProgress) {
        fabExpanded = false
      }
    }

    androidx.activity.compose.BackHandler(enabled = fabExpanded) {
      fabExpanded = false
    }

    Scaffold(
      topBar = {
        BrowserTopBar(
          title = "Network",
          isInSelectionMode = false,
          selectedCount = 0,
          totalCount = 0,
          onBackClick = null, // No back button for network screen (root tab)
          onCancelSelection = { },
          onSortClick = null,
          // Search functionality disabled for production
          onSearchClick = null,
          onDeleteClick = null,
          onRenameClick = null,
          isSingleSelection = false,
          onInfoClick = null,
          onShareClick = null,
          onPlayClick = null,
          onSelectAll = null,
          onInvertSelection = null,
          onDeselectAll = null,
        )
      },
      floatingActionButton = {
        Box(modifier = Modifier.padding(bottom = 8.dp)) {
          FloatingActionButtonMenu(
            expanded = fabExpanded,
            button = {
              ToggleFloatingActionButton(
                modifier = Modifier
                  .semantics {
                    traversalIndex = -1f
                    stateDescription = if (fabExpanded) "Expanded" else "Collapsed"
                    contentDescription = "Toggle menu"
                  }
                  .animateFloatingActionButton(
                    visible = isFabVisible,
                    alignment = Alignment.BottomEnd,
                  )
                  .focusRequester(focusRequester),
                checked = fabExpanded,
                onCheckedChange = { fabExpanded = !fabExpanded },
                containerSize = ToggleFloatingActionButtonDefaults.containerSizeMedium(),
              ) {
                val icon by remember {
                  derivedStateOf {
                    if (checkedProgress > 0.5f) Icons.Filled.Close else Icons.Filled.Add
                  }
                }
                Icon(
                  painter = rememberVectorPainter(icon),
                  contentDescription = null,
                  modifier = Modifier.animateIcon(
                    checkedProgress = { checkedProgress },
                    size = ToggleFloatingActionButtonDefaults.iconSize(
                      initialSize = 40.dp,
                      finalSize = 24.dp,
                    ),
                  ),
                )
              }
            },
          ) {
            FloatingActionButtonMenuItem(
              onClick = {
                fabExpanded = false
                showAddSheet = true
              },
              icon = { Icon(Icons.Filled.Add, contentDescription = null) },
              text = { Text("Add Connection") },
            )
            FloatingActionButtonMenuItem(
              onClick = {
                fabExpanded = false
                showStreamLinkDialog = true
              },
              icon = { Icon(Icons.Filled.Link, contentDescription = null) },
              text = { Text("Stream Link") },
            )
          }
        }
      },
    ) { padding ->
      LazyColumn(
        state = listState,
        modifier = Modifier
          .fillMaxSize()
          .padding(padding),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
      ) {
          // Local Network header
          item {
            Text(
              text = "Local Network",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.padding(vertical = 8.dp),
            )
          }

          // Show empty state or connection list
          if (connections.isEmpty()) {
            item {
              Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                colors = CardDefaults.cardColors(
                  containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                ),
              ) {
                Column(
                  modifier = Modifier.padding(24.dp),
                  horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                  Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                  )
                  Spacer(modifier = Modifier.height(16.dp))
                  Text(
                    text = "No network connections",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface, // a
                  )
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "Add SMB, FTP, or WebDAV connections to browse network files",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                  )
                }
              }
            }
          } else {
            items(connections, key = { it.id }) { connection ->
              val status = connectionStatuses[connection.id]
              NetworkConnectionCard(
                connection = connection,
                onConnect = { conn ->
                  viewModel.connect(conn)
                },
                onDisconnect = { conn -> viewModel.disconnect(conn) },
                onEdit = { conn -> editingConnection = conn },
                onDelete = { conn -> viewModel.deleteConnection(conn) },
                onBrowse = { conn ->
                  // Navigate to browser screen if connected
                  if (status?.isConnected == true) {
                    backstack.add(
                      NetworkBrowserScreen(
                        connectionId = conn.id,
                        connectionName = conn.name,
                        currentPath = conn.path,
                      ),
                    )
                  }
                },
                onAutoConnectChange = { conn, autoConnect ->
                  viewModel.updateConnection(conn.copy(autoConnect = autoConnect))
                },
                isConnected = status?.isConnected ?: false,
                isConnecting = status?.isConnecting ?: false,
                error = status?.error,
                modifier = Modifier.padding(bottom = 16.dp),
              )
            }
          }
        }

      // Add Connection Sheet
      AddConnectionSheet(
        isOpen = showAddSheet,
        onDismiss = { showAddSheet = false },
        onSave = { connection ->
          viewModel.addConnection(connection)
          showAddSheet = false
        },
      )

      // Stream Link Dialog
      StreamLinkDialog(
        isOpen = showStreamLinkDialog,
        onDismiss = { showStreamLinkDialog = false },
        onPlay = { url ->
          MediaUtils.playFile(url, context, "network_stream")
        },
      )

      // Edit Connection Sheet
      editingConnection?.let { connection ->
        EditConnectionSheet(
          connection = connection,
          isOpen = true,
          onDismiss = { editingConnection = null },
          onSave = { updatedConnection ->
            viewModel.updateConnection(updatedConnection)
            editingConnection = null
          },
        )
      }
    }
  }
}
