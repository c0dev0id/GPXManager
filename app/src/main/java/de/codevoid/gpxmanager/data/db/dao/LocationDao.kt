package de.codevoid.gpxmanager.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import de.codevoid.gpxmanager.data.db.entity.LocationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface LocationDao {

    @Query("SELECT * FROM locations WHERE folder_id IS :folderId ORDER BY name ASC")
    fun getByFolder(folderId: Long?): Flow<List<LocationEntity>>

    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getById(id: Long): LocationEntity?

    @Query("SELECT * FROM locations WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<LocationEntity?>

    @Insert
    suspend fun insert(location: LocationEntity): Long

    @Update
    suspend fun update(location: LocationEntity)

    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun deleteById(id: Long)
}
