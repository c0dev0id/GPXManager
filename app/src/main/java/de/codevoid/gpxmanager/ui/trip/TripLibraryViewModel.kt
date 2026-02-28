package de.codevoid.gpxmanager.ui.trip

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.codevoid.gpxmanager.data.db.entity.FolderEntity
import de.codevoid.gpxmanager.data.db.entity.GpxFileEntity
import de.codevoid.gpxmanager.data.db.entity.PdfFileEntity
import de.codevoid.gpxmanager.data.repository.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TripLibraryUiState(
    val folders: List<FolderEntity> = emptyList(),
    val gpxFiles: List<GpxFileEntity> = emptyList(),
    val pdfFiles: List<PdfFileEntity> = emptyList(),
    val currentFolder: FolderEntity? = null,
    val allFolders: List<FolderEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class TripLibraryViewModel @Inject constructor(
    private val tripRepository: TripRepository
) : ViewModel() {

    private val _folderId = MutableStateFlow<Long?>(null)
    private val _currentFolder = MutableStateFlow<FolderEntity?>(null)
    private val _allFolders = MutableStateFlow<List<FolderEntity>>(emptyList())

    val uiState: StateFlow<TripLibraryUiState> by lazy {
        combine(
            tripRepository.getFolders(_folderId.value),
            tripRepository.getGpxFiles(_folderId.value),
            tripRepository.getPdfFiles(_folderId.value),
            _currentFolder,
            _allFolders
        ) { folders, gpxFiles, pdfFiles, currentFolder, allFolders ->
            TripLibraryUiState(
                folders = folders,
                gpxFiles = gpxFiles,
                pdfFiles = pdfFiles,
                currentFolder = currentFolder,
                allFolders = allFolders,
                isLoading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), TripLibraryUiState())
    }

    fun setFolderId(folderId: Long?) {
        _folderId.value = folderId
        viewModelScope.launch {
            _currentFolder.value = folderId?.let { tripRepository.getFolder(it) }
        }
        loadAllFolders()
    }

    private fun loadAllFolders() {
        viewModelScope.launch {
            tripRepository.getFolders(null).collect { rootFolders ->
                _allFolders.value = rootFolders
            }
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            tripRepository.createFolder(name, _folderId.value)
        }
    }

    fun renameFolder(id: Long, newName: String) {
        viewModelScope.launch { tripRepository.renameFolder(id, newName) }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch { tripRepository.deleteFolder(id) }
    }

    fun importGpxFile(uri: Uri, displayName: String) {
        viewModelScope.launch {
            tripRepository.importGpxFile(uri, displayName, _folderId.value)
        }
    }

    fun importPdfFile(uri: Uri, displayName: String) {
        viewModelScope.launch {
            tripRepository.importPdfFile(uri, displayName, _folderId.value)
        }
    }

    fun exportFile(fileName: String, destinationUri: Uri) {
        viewModelScope.launch { tripRepository.exportFile(fileName, destinationUri) }
    }

    fun renameGpxFile(id: Long, newName: String) {
        viewModelScope.launch { tripRepository.renameGpxFile(id, newName) }
    }

    fun renamePdfFile(id: Long, newName: String) {
        viewModelScope.launch { tripRepository.renamePdfFile(id, newName) }
    }

    fun deleteGpxFile(id: Long) {
        viewModelScope.launch { tripRepository.deleteGpxFile(id) }
    }

    fun deletePdfFile(id: Long) {
        viewModelScope.launch { tripRepository.deletePdfFile(id) }
    }

    fun moveGpxFile(id: Long, targetFolderId: Long?) {
        viewModelScope.launch { tripRepository.moveGpxFile(id, targetFolderId) }
    }

    fun movePdfFile(id: Long, targetFolderId: Long?) {
        viewModelScope.launch { tripRepository.movePdfFile(id, targetFolderId) }
    }

    fun moveFolder(id: Long, targetParentId: Long?) {
        viewModelScope.launch { tripRepository.moveFolder(id, targetParentId) }
    }

    fun copyGpxFile(id: Long, targetFolderId: Long?) {
        viewModelScope.launch { tripRepository.copyGpxFile(id, targetFolderId) }
    }

    fun copyPdfFile(id: Long, targetFolderId: Long?) {
        viewModelScope.launch { tripRepository.copyPdfFile(id, targetFolderId) }
    }
}
