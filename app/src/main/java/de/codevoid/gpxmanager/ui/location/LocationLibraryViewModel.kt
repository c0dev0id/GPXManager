package de.codevoid.gpxmanager.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.codevoid.gpxmanager.data.db.entity.FolderEntity
import de.codevoid.gpxmanager.data.db.entity.LocationEntity
import de.codevoid.gpxmanager.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationLibraryUiState(
    val folders: List<FolderEntity> = emptyList(),
    val locations: List<LocationEntity> = emptyList(),
    val currentFolder: FolderEntity? = null,
    val allFolders: List<FolderEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LocationLibraryViewModel @Inject constructor(
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _folderId = MutableStateFlow<Long?>(null)
    private val _currentFolder = MutableStateFlow<FolderEntity?>(null)
    private val _allFolders = MutableStateFlow<List<FolderEntity>>(emptyList())

    val uiState: StateFlow<LocationLibraryUiState> by lazy {
        combine(
            locationRepository.getFolders(_folderId.value),
            locationRepository.getLocations(_folderId.value),
            _currentFolder,
            _allFolders
        ) { folders, locations, currentFolder, allFolders ->
            LocationLibraryUiState(
                folders = folders,
                locations = locations,
                currentFolder = currentFolder,
                allFolders = allFolders,
                isLoading = false
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocationLibraryUiState())
    }

    fun setFolderId(folderId: Long?) {
        _folderId.value = folderId
        viewModelScope.launch {
            _currentFolder.value = folderId?.let { locationRepository.getFolder(it) }
        }
        loadAllFolders()
    }

    private fun loadAllFolders() {
        viewModelScope.launch {
            locationRepository.getFolders(null).collect { rootFolders ->
                _allFolders.value = rootFolders
            }
        }
    }

    fun createFolder(name: String) {
        viewModelScope.launch {
            locationRepository.createFolder(name, _folderId.value)
        }
    }

    fun createLocation(name: String, onCreated: (Long) -> Unit) {
        viewModelScope.launch {
            val id = locationRepository.createLocation(name, _folderId.value)
            onCreated(id)
        }
    }

    fun renameFolder(id: Long, newName: String) {
        viewModelScope.launch { locationRepository.renameFolder(id, newName) }
    }

    fun renameLocation(id: Long, newName: String) {
        viewModelScope.launch { locationRepository.renameLocation(id, newName) }
    }

    fun deleteFolder(id: Long) {
        viewModelScope.launch { locationRepository.deleteFolder(id) }
    }

    fun deleteLocation(id: Long) {
        viewModelScope.launch { locationRepository.deleteLocation(id) }
    }

    fun moveFolder(id: Long, targetParentId: Long?) {
        viewModelScope.launch { locationRepository.moveFolder(id, targetParentId) }
    }

    fun moveLocation(id: Long, targetFolderId: Long?) {
        viewModelScope.launch { locationRepository.moveLocation(id, targetFolderId) }
    }

    fun copyLocation(id: Long, targetFolderId: Long?) {
        viewModelScope.launch { locationRepository.copyLocation(id, targetFolderId) }
    }
}
