package de.codevoid.gpxmanager.ui.location

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.codevoid.gpxmanager.data.db.entity.CategoryEntity
import de.codevoid.gpxmanager.data.db.entity.LocationEntity
import de.codevoid.gpxmanager.data.repository.CategoryRepository
import de.codevoid.gpxmanager.data.repository.LocationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class LocationDetailUiState(
    val location: LocationEntity? = null,
    val categories: List<CategoryEntity> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class LocationDetailViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _location = MutableStateFlow<LocationEntity?>(null)

    val uiState: StateFlow<LocationDetailUiState> by lazy {
        combine(
            _location,
            categoryRepository.getAll()
        ) { location, categories ->
            LocationDetailUiState(
                location = location,
                categories = categories,
                isLoading = location == null
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), LocationDetailUiState())
    }

    fun loadLocation(locationId: Long) {
        viewModelScope.launch {
            locationRepository.getLocationFlow(locationId).collect { location ->
                _location.value = location
            }
        }
    }

    fun updateName(name: String) {
        _location.value?.let { loc ->
            val updated = loc.copy(name = name)
            _location.value = updated
            viewModelScope.launch { locationRepository.updateLocation(updated) }
        }
    }

    fun updateAddress(address: String) {
        _location.value?.let { loc ->
            val updated = loc.copy(address = address)
            _location.value = updated
            viewModelScope.launch { locationRepository.updateLocation(updated) }
        }
    }

    fun updateCategory(categoryId: Long?) {
        _location.value?.let { loc ->
            val updated = loc.copy(categoryId = categoryId)
            _location.value = updated
            viewModelScope.launch { locationRepository.updateLocation(updated) }
        }
    }

    fun updateCoordinates(longitude: Double?, latitude: Double?) {
        _location.value?.let { loc ->
            val updated = loc.copy(longitude = longitude, latitude = latitude)
            _location.value = updated
            viewModelScope.launch { locationRepository.updateLocation(updated) }
        }
    }

    fun createCategory(name: String) {
        viewModelScope.launch {
            val id = categoryRepository.create(name)
            updateCategory(id)
        }
    }
}
