package de.codevoid.gpxmanager.data.repository

import de.codevoid.gpxmanager.data.db.dao.FolderDao
import de.codevoid.gpxmanager.data.db.dao.LocationDao
import de.codevoid.gpxmanager.data.db.entity.FolderEntity
import de.codevoid.gpxmanager.data.db.entity.LocationEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationRepository @Inject constructor(
    private val folderDao: FolderDao,
    private val locationDao: LocationDao
) {

    fun getFolders(parentId: Long?): Flow<List<FolderEntity>> {
        return folderDao.getByParent(parentId, FolderEntity.TYPE_LOCATION)
    }

    fun getLocations(folderId: Long?): Flow<List<LocationEntity>> {
        return locationDao.getByFolder(folderId)
    }

    fun getLocationFlow(id: Long): Flow<LocationEntity?> {
        return locationDao.getByIdFlow(id)
    }

    suspend fun getFolder(id: Long): FolderEntity? = folderDao.getById(id)

    suspend fun getLocation(id: Long): LocationEntity? = locationDao.getById(id)

    suspend fun createFolder(name: String, parentId: Long?) {
        folderDao.insert(
            FolderEntity(
                name = name,
                parentId = parentId,
                libraryType = FolderEntity.TYPE_LOCATION
            )
        )
    }

    suspend fun createLocation(name: String, folderId: Long?): Long {
        return locationDao.insert(
            LocationEntity(name = name, folderId = folderId)
        )
    }

    suspend fun updateLocation(location: LocationEntity) {
        locationDao.update(location)
    }

    suspend fun renameFolder(id: Long, newName: String) {
        folderDao.getById(id)?.let { folder ->
            folderDao.update(folder.copy(name = newName))
        }
    }

    suspend fun renameLocation(id: Long, newName: String) {
        locationDao.getById(id)?.let { location ->
            locationDao.update(location.copy(name = newName))
        }
    }

    suspend fun deleteFolder(id: Long) {
        folderDao.deleteById(id)
    }

    suspend fun deleteLocation(id: Long) {
        locationDao.deleteById(id)
    }

    suspend fun moveFolder(id: Long, targetParentId: Long?) {
        folderDao.getById(id)?.let { folder ->
            folderDao.update(folder.copy(parentId = targetParentId))
        }
    }

    suspend fun moveLocation(id: Long, targetFolderId: Long?) {
        locationDao.getById(id)?.let { location ->
            locationDao.update(location.copy(folderId = targetFolderId))
        }
    }

    suspend fun copyLocation(id: Long, targetFolderId: Long?) {
        locationDao.getById(id)?.let { location ->
            locationDao.insert(
                location.copy(
                    id = 0,
                    folderId = targetFolderId,
                    createdAt = System.currentTimeMillis()
                )
            )
        }
    }
}
