package de.codevoid.gpxmanager.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import de.codevoid.gpxmanager.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAll(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getById(id: Long): CategoryEntity?

    @Insert
    suspend fun insert(category: CategoryEntity): Long

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: Long)
}
