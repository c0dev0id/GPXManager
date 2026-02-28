package de.codevoid.gpxmanager.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.codevoid.gpxmanager.data.db.entity.PdfFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PdfFileDao {

    @Query("SELECT * FROM pdf_files WHERE folder_id IS :folderId ORDER BY name ASC")
    fun getByFolder(folderId: Long?): Flow<List<PdfFileEntity>>

    @Query("SELECT * FROM pdf_files WHERE id = :id")
    suspend fun getById(id: Long): PdfFileEntity?

    @Insert
    suspend fun insert(pdfFile: PdfFileEntity): Long

    @Update
    suspend fun update(pdfFile: PdfFileEntity)

    @Query("DELETE FROM pdf_files WHERE id = :id")
    suspend fun deleteById(id: Long)
}
