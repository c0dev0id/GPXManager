package de.codevoid.gpxmanager.data.repository

import android.net.Uri
import android.util.Log
import de.codevoid.gpxmanager.data.db.dao.FolderDao
import de.codevoid.gpxmanager.data.db.dao.GpxFileDao
import de.codevoid.gpxmanager.data.db.dao.PdfFileDao
import de.codevoid.gpxmanager.data.db.entity.FolderEntity
import de.codevoid.gpxmanager.data.db.entity.GpxFileEntity
import de.codevoid.gpxmanager.data.db.entity.PdfFileEntity
import de.codevoid.gpxmanager.data.file.FileManager
import de.codevoid.gpxmanager.data.file.GpxParser
import de.codevoid.gpxmanager.data.file.PdfUtil
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TripRepository @Inject constructor(
    private val folderDao: FolderDao,
    private val gpxFileDao: GpxFileDao,
    private val pdfFileDao: PdfFileDao,
    private val fileManager: FileManager,
    private val gpxParser: GpxParser
) {

    companion object {
        private const val TAG = "TripRepository"
    }

    fun getFolders(parentId: Long?): Flow<List<FolderEntity>> {
        return folderDao.getByParent(parentId, FolderEntity.TYPE_TRIP)
    }

    fun getGpxFiles(folderId: Long?): Flow<List<GpxFileEntity>> {
        return gpxFileDao.getByFolder(folderId)
    }

    fun getPdfFiles(folderId: Long?): Flow<List<PdfFileEntity>> {
        return pdfFileDao.getByFolder(folderId)
    }

    suspend fun getFolder(id: Long): FolderEntity? = folderDao.getById(id)

    suspend fun createFolder(name: String, parentId: Long?) {
        folderDao.insert(
            FolderEntity(
                name = name,
                parentId = parentId,
                libraryType = FolderEntity.TYPE_TRIP
            )
        )
    }

    suspend fun renameFolder(id: Long, newName: String) {
        folderDao.getById(id)?.let { folder ->
            folderDao.update(folder.copy(name = newName))
        }
    }

    suspend fun deleteFolder(id: Long) {
        folderDao.deleteById(id)
    }

    /**
     * Imports a GPX file from a content URI. Parses metadata and stores both
     * the physical file and database record.
     */
    suspend fun importGpxFile(uri: Uri, displayName: String, folderId: Long?): Boolean {
        val fileName = fileManager.importFile(uri, "gpx") ?: return false
        return try {
            val inputStream = fileManager.openFile(fileName) ?: return false
            val metadata = inputStream.use { gpxParser.parse(it) }
            gpxFileDao.insert(
                GpxFileEntity(
                    name = displayName,
                    fileName = fileName,
                    folderId = folderId,
                    date = metadata.date ?: System.currentTimeMillis(),
                    routeCount = metadata.routeCount,
                    trackCount = metadata.trackCount,
                    waypointCount = metadata.waypointCount,
                    totalLengthKm = metadata.totalLengthKm
                )
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import GPX file: $displayName", e)
            fileManager.deleteFile(fileName)
            false
        }
    }

    /**
     * Imports a PDF file from a content URI. Reads page count and stores both
     * the physical file and database record.
     */
    suspend fun importPdfFile(uri: Uri, displayName: String, folderId: Long?): Boolean {
        val fileName = fileManager.importFile(uri, "pdf") ?: return false
        return try {
            val file = fileManager.getFile(fileName)
            val pageCount = PdfUtil.getPageCount(file)
            pdfFileDao.insert(
                PdfFileEntity(
                    name = displayName,
                    fileName = fileName,
                    folderId = folderId,
                    uploadDate = System.currentTimeMillis(),
                    pageCount = pageCount
                )
            )
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import PDF file: $displayName", e)
            fileManager.deleteFile(fileName)
            false
        }
    }

    suspend fun exportFile(fileName: String, destinationUri: Uri): Boolean {
        return fileManager.exportFile(fileName, destinationUri)
    }

    suspend fun renameGpxFile(id: Long, newName: String) {
        gpxFileDao.getById(id)?.let { file ->
            gpxFileDao.update(file.copy(name = newName))
        }
    }

    suspend fun renamePdfFile(id: Long, newName: String) {
        pdfFileDao.getById(id)?.let { file ->
            pdfFileDao.update(file.copy(name = newName))
        }
    }

    suspend fun deleteGpxFile(id: Long) {
        gpxFileDao.getById(id)?.let { file ->
            fileManager.deleteFile(file.fileName)
            gpxFileDao.deleteById(id)
        }
    }

    suspend fun deletePdfFile(id: Long) {
        pdfFileDao.getById(id)?.let { file ->
            fileManager.deleteFile(file.fileName)
            pdfFileDao.deleteById(id)
        }
    }

    suspend fun moveGpxFile(id: Long, targetFolderId: Long?) {
        gpxFileDao.getById(id)?.let { file ->
            gpxFileDao.update(file.copy(folderId = targetFolderId))
        }
    }

    suspend fun movePdfFile(id: Long, targetFolderId: Long?) {
        pdfFileDao.getById(id)?.let { file ->
            pdfFileDao.update(file.copy(folderId = targetFolderId))
        }
    }

    suspend fun moveFolder(id: Long, targetParentId: Long?) {
        folderDao.getById(id)?.let { folder ->
            folderDao.update(folder.copy(parentId = targetParentId))
        }
    }

    suspend fun copyGpxFile(id: Long, targetFolderId: Long?) {
        gpxFileDao.getById(id)?.let { file ->
            val newFileName = fileManager.copyFile(file.fileName) ?: return
            gpxFileDao.insert(file.copy(id = 0, fileName = newFileName, folderId = targetFolderId))
        }
    }

    suspend fun copyPdfFile(id: Long, targetFolderId: Long?) {
        pdfFileDao.getById(id)?.let { file ->
            val newFileName = fileManager.copyFile(file.fileName) ?: return
            pdfFileDao.insert(file.copy(id = 0, fileName = newFileName, folderId = targetFolderId))
        }
    }
}
