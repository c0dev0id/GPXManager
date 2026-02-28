package de.codevoid.gpxmanager.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.codevoid.gpxmanager.data.db.entity.GpxFileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GpxFileDao {

    @Query("SELECT * FROM gpx_files WHERE folder_id IS :folderId ORDER BY name ASC")
    fun getByFolder(folderId: Long?): Flow<List<GpxFileEntity>>

    @Query("SELECT * FROM gpx_files WHERE id = :id")
    suspend fun getById(id: Long): GpxFileEntity?

    @Insert
    suspend fun insert(gpxFile: GpxFileEntity): Long

    @Update
    suspend fun update(gpxFile: GpxFileEntity)

    @Query("DELETE FROM gpx_files WHERE id = :id")
    suspend fun deleteById(id: Long)
}
