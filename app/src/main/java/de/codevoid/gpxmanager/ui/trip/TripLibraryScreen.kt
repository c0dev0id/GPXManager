package de.codevoid.gpxmanager.ui.trip

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Map
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.codevoid.gpxmanager.data.db.entity.GpxFileEntity
import de.codevoid.gpxmanager.data.db.entity.PdfFileEntity
import de.codevoid.gpxmanager.ui.common.ConfirmDialog
import de.codevoid.gpxmanager.ui.common.FolderCard
import de.codevoid.gpxmanager.ui.common.ItemCard
import de.codevoid.gpxmanager.ui.common.MoveDialog
import de.codevoid.gpxmanager.ui.common.TextInputDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripLibraryScreen(
    folderId: Long?,
    onNavigateToFolder: (Long) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: TripLibraryViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(folderId) {
        viewModel.setFolderId(folderId)
    }

    var showFabMenu by remember { mutableStateOf(false) }
    var showCreateFolderDialog by remember { mutableStateOf(false) }

    // Context menu state
    var selectedFolderId by remember { mutableStateOf<Long?>(null) }
    var selectedFolderName by remember { mutableStateOf("") }
    var selectedGpxFile by remember { mutableStateOf<GpxFileEntity?>(null) }
    var selectedPdfFile by remember { mutableStateOf<PdfFileEntity?>(null) }
    var showContextMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showRenameDialog by remember { mutableStateOf(false) }
    var showMoveDialog by remember { mutableStateOf(false) }
    var contextMenuType by remember { mutableStateOf("") } // "folder", "gpx", "pdf"

    // File picker for GPX upload
    val gpxPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
            cursor?.moveToFirst()
            val displayName = if (nameIndex >= 0) cursor?.getString(nameIndex) ?: "Untitled.gpx" else "Untitled.gpx"
            cursor?.close()
            viewModel.importGpxFile(it, displayName.removeSuffix(".gpx"))
        }
    }

    // File picker for PDF upload
    val pdfPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            val cursor = context.contentResolver.query(it, null, null, null, null)
            val nameIndex = cursor?.getColumnIndex(OpenableColumns.DISPLAY_NAME) ?: -1
            cursor?.moveToFirst()
            val displayName = if (nameIndex >= 0) cursor?.getString(nameIndex) ?: "Untitled.pdf" else "Untitled.pdf"
            cursor?.close()
            viewModel.importPdfFile(it, displayName.removeSuffix(".pdf"))
        }
    }

    // Export launcher
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("*/*")
    ) { uri ->
        // handled via callback chain
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.currentFolder?.name ?: "Trip Library",
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
                        text = { Text("Upload GPX") },
                        leadingIcon = { Icon(Icons.Filled.Map, contentDescription = null) },
                        onClick = {
                            showFabMenu = false
                            gpxPicker.launch(arrayOf("application/gpx+xml", "application/octet-stream", "*/*"))
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Upload PDF") },
                        leadingIcon = { Icon(Icons.Filled.Description, contentDescription = null) },
                        onClick = {
                            showFabMenu = false
                            pdfPicker.launch(arrayOf("application/pdf"))
                        }
                    )
                }
            }
        }
    ) { padding ->
        val isEmpty = uiState.folders.isEmpty() && uiState.gpxFiles.isEmpty() && uiState.pdfFiles.isEmpty()

        if (isEmpty && !uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No items yet. Tap + to add folders or upload files.",
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

                items(uiState.gpxFiles, key = { "gpx_${it.id}" }) { gpxFile ->
                    GpxFileCard(
                        gpxFile = gpxFile,
                        onLongClick = {
                            selectedGpxFile = gpxFile
                            contextMenuType = "gpx"
                            showContextMenu = true
                        }
                    )
                }

                items(uiState.pdfFiles, key = { "pdf_${it.id}" }) { pdfFile ->
                    PdfFileCard(
                        pdfFile = pdfFile,
                        onLongClick = {
                            selectedPdfFile = pdfFile
                            contextMenuType = "pdf"
                            showContextMenu = true
                        }
                    )
                }
            }
        }
    }

    // Context menu dialog
    if (showContextMenu) {
        val itemName = when (contextMenuType) {
            "folder" -> selectedFolderName
            "gpx" -> selectedGpxFile?.name ?: ""
            "pdf" -> selectedPdfFile?.name ?: ""
            else -> ""
        }
        ContextMenuDialog(
            itemName = itemName,
            showDownload = contextMenuType != "folder",
            onRename = { showContextMenu = false; showRenameDialog = true },
            onDelete = { showContextMenu = false; showDeleteDialog = true },
            onMove = { showContextMenu = false; showMoveDialog = true },
            onCopy = {
                showContextMenu = false
                when (contextMenuType) {
                    "gpx" -> selectedGpxFile?.let { viewModel.copyGpxFile(it.id, folderId) }
                    "pdf" -> selectedPdfFile?.let { viewModel.copyPdfFile(it.id, folderId) }
                }
            },
            onDismiss = { showContextMenu = false }
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

    if (showRenameDialog) {
        val currentName = when (contextMenuType) {
            "folder" -> selectedFolderName
            "gpx" -> selectedGpxFile?.name ?: ""
            "pdf" -> selectedPdfFile?.name ?: ""
            else -> ""
        }
        TextInputDialog(
            title = "Rename",
            label = "New name",
            initialValue = currentName,
            onConfirm = { newName ->
                when (contextMenuType) {
                    "folder" -> selectedFolderId?.let { viewModel.renameFolder(it, newName) }
                    "gpx" -> selectedGpxFile?.let { viewModel.renameGpxFile(it.id, newName) }
                    "pdf" -> selectedPdfFile?.let { viewModel.renamePdfFile(it.id, newName) }
                }
            },
            onDismiss = { showRenameDialog = false }
        )
    }

    if (showDeleteDialog) {
        val itemName = when (contextMenuType) {
            "folder" -> selectedFolderName
            "gpx" -> selectedGpxFile?.name ?: ""
            "pdf" -> selectedPdfFile?.name ?: ""
            else -> ""
        }
        ConfirmDialog(
            title = "Delete",
            message = "Are you sure you want to delete \"$itemName\"?",
            onConfirm = {
                when (contextMenuType) {
                    "folder" -> selectedFolderId?.let { viewModel.deleteFolder(it) }
                    "gpx" -> selectedGpxFile?.let { viewModel.deleteGpxFile(it.id) }
                    "pdf" -> selectedPdfFile?.let { viewModel.deletePdfFile(it.id) }
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
                    "gpx" -> selectedGpxFile?.let { viewModel.moveGpxFile(it.id, targetId) }
                    "pdf" -> selectedPdfFile?.let { viewModel.movePdfFile(it.id, targetId) }
                }
            },
            onDismiss = { showMoveDialog = false }
        )
    }
}

@Composable
private fun GpxFileCard(gpxFile: GpxFileEntity, onLongClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    ItemCard(
        onLongClick = onLongClick,
        preview = {
            Icon(
                imageVector = Icons.Filled.Map,
                contentDescription = "GPX File",
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
    ) {
        Column {
            Text(
                text = gpxFile.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = dateFormat.format(Date(gpxFile.date)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${gpxFile.routeCount} routes, ${gpxFile.trackCount} tracks, ${gpxFile.waypointCount} waypoints",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "%.2f km".format(gpxFile.totalLengthKm),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PdfFileCard(pdfFile: PdfFileEntity, onLongClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }

    ItemCard(
        onLongClick = onLongClick,
        preview = {
            Icon(
                imageVector = Icons.Filled.Description,
                contentDescription = "PDF File",
                modifier = Modifier.padding(12.dp),
                tint = MaterialTheme.colorScheme.tertiary
            )
        }
    ) {
        Column {
            Text(
                text = pdfFile.name,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = dateFormat.format(Date(pdfFile.uploadDate)),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "${pdfFile.pageCount} pages",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ContextMenuDialog(
    itemName: String,
    showDownload: Boolean,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onMove: () -> Unit,
    onCopy: () -> Unit,
    onDismiss: () -> Unit
) {
    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(itemName) },
        text = {
            Column {
                ContextMenuItem("Rename") { onRename() }
                ContextMenuItem("Move") { onMove() }
                ContextMenuItem("Copy") { onCopy() }
                ContextMenuItem("Delete") { onDelete() }
            }
        },
        confirmButton = {},
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ContextMenuItem(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        style = MaterialTheme.typography.bodyLarge
    )
}
