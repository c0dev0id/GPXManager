package de.codevoid.gpxmanager.ui.location

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.codevoid.gpxmanager.data.db.entity.LocationEntity
import de.codevoid.gpxmanager.ui.common.ConfirmDialog
import de.codevoid.gpxmanager.ui.common.FolderCard
import de.codevoid.gpxmanager.ui.common.ItemCard
import de.codevoid.gpxmanager.ui.common.MoveDialog
import de.codevoid.gpxmanager.ui.common.TextInputDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationLibraryScreen(
    folderId: Long?,
    onNavigateToFolder: (Long) -> Unit,
    onNavigateToLocation: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: LocationLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(folderId) {
        viewModel.setFolderId(folderId)
    }

    var showFabMenu by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }
    var showCreateLocationDialog by remember { mutableStateOf(false) }

    // Context menu state
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }
    var selectedFolderName by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf<LocationEntity?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var contextMenuType by remember { mutableStateOf("") } // "folder" or "location"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.currentFolder?.name ?: "Locations",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        },
        floatingActionButton = {
            Box {
                FloatingActionButton(onClick = { showFabMenu = true }) {
                    Icon(Icons.Filled.Add, contentDescription = "Add")
                }
                DropdownMenu(
                    expanded = showFabMenu,
                    onDismissRequest = { showFabMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("New Folder") },
                        leadingIcon = { Icon(Icons.Filled.CreateNewFolder, contentDescription = null) },
                        onClick = {
                            showFabMenu = false
                            showCreateFolderDialog = true
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("New Location") },
                        leadingIcon = { Icon(Icons.Filled.LocationOn, contentDescription = null) },
                        onClick = {
                            showFabMenu = false
                            showCreateLocationDialog = true
                        }
                    )
                }
            }
        }
    ) { padding ->
        val isEmpty = uiState.folders.isEmpty() && uiState.locations.isEmpty()

        if (isEmpty && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No items yet. Tap + to add folders or locations.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.folders, key = { "folder_${it.id}" }) { folder ->
                    FolderCard(
                        name = folder.name,
                        onClick = { onNavigateToFolder(folder.id) },
                        onLongClick = {
                            selectedFolderId = folder.id
                            selectedFolderName = folder.name
                            contextMenuType = "folder"
                            showContextMenu = true
                        }
                    )
                }

                items(uiState.locations, key = { "loc_${it.id}" }) { location ->
                    LocationCard(
                        location = location,
                        onClick = { onNavigateToLocation(location.id) },
                        onLongClick = {
                            selectedLocation = location
                            contextMenuType = "location"
                            showContextMenu = true
                        }
                    )
                }
            }
        }
    }

    // Context menu
    if (showContextMenu) {
        val itemName = when (contextMenuType) {
            "folder" -> selectedFolderName
            "location" -> selectedLocation?.name ?: ""
            else -> ""
        }
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showContextMenu = false },
            title = { Text(itemName) },
            text = {
                Column {
                    Text(
                        text = "Rename",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showContextMenu = false; showRenameDialog = true }
                            .padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = "Move",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showContextMenu = false; showMoveDialog = true }
                            .padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (contextMenuType == "location") {
                        Text(
                            text = "Copy",
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showContextMenu = false
                                    selectedLocation?.let { viewModel.copyLocation(it.id, folderId) }
                                }
                                .padding(vertical = 12.dp),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Text(
                        text = "Delete",
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showContextMenu = false; showDeleteDialog = true }
                            .padding(vertical = 12.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                androidx.compose.material3.TextButton(onClick = { showContextMenu = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showCreateFolderDialog) {
        TextInputDialog(
            title = "New Folder",
            label = "Folder name",
            onConfirm = { viewModel.createFolder(it) },
            onDismiss = { showCreateFolderDialog = false }
        )
    }

    if (showCreateLocationDialog) {
        TextInputDialog(
            title = "New Location",
            label = "Location name",
            onConfirm = { name ->
                viewModel.createLocation(name) { id ->
                    onNavigateToLocation(id)
                }
            },
            onDismiss = { showCreateLocationDialog = false }
        )
    }

    if (showRenameDialog) {
        val currentName = when (contextMenuType) {
            "folder" -> selectedFolderName
            "location" -> selectedLocation?.name ?: ""
            else -> ""
        }
        TextInputDialog(
            title = "Rename",
            label = "New name",
            initialValue = currentName,
            onConfirm = { newName ->
                when (contextMenuType) {
                    "folder" -> selectedFolderId?.let { viewModel.renameFolder(it, newName) }
                    "location" -> selectedLocation?.let { viewModel.renameLocation(it.id, newName) }
                }
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showDeleteDialog) {
        val itemName = when (contextMenuType) {
            "folder" -> selectedFolderName
            "location" -> selectedLocation?.name ?: ""
            else -> ""
        }
        ConfirmDialog(
            title = "Delete",
            message = "Are you sure you want to delete \"$itemName\"?",
            onConfirm = {
                when (contextMenuType) {
                    "folder" -> selectedFolderId?.let { viewModel.deleteFolder(it) }
                    "location" -> selectedLocation?.let { viewModel.deleteLocation(it.id) }
                }
            },
            onDismiss = { showDeleteDialog = false }
        )
    }

    if (showMoveDialog) {
        MoveDialog(
            title = "Move to...",
            folders = uiState.allFolders,
            onSelectFolder = { targetId ->
                when (contextMenuType) {
                    "folder" -> selectedFolderId?.let { viewModel.moveFolder(it, targetId) }
                    "location" -> selectedLocation?.let { viewModel.moveLocation(it.id, targetId) }
                }
            },
            onDismiss = { showMoveDialog = false }
        )
    }
}

@Composable
private fun LocationCard(
    location: LocationEntity,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    ItemCard(
        onClick = onClick,
        onLongClick = onLongClick,
        preview = {
            Icon(
                imageVector = Icons.Filled.LocationOn,
                contentDescription = "Location",
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.secondary
            )
        }
    ) {
        Column {
            Text(
                text = location.name,
                style = MaterialTheme.typography.titleMedium
            )
            if (location.address.isNotEmpty()) {
                Text(
                    text = location.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
