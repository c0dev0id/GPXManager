package de.codevoid.gpxmanager.data.repository

import de.codevoid.gpxmanager.data.db.dao.CategoryDao
import de.codevoid.gpxmanager.data.db.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CategoryRepository @Inject constructor(
    private val categoryDao: CategoryDao
) {

    fun getAll(): Flow<List<CategoryEntity>> = categoryDao.getAll()

    suspend fun getById(id: Long): CategoryEntity? = categoryDao.getById(id)

    suspend fun create(name: String): Long = categoryDao.insert(CategoryEntity(name = name))

    suspend fun delete(id: Long) = categoryDao.deleteById(id)
}
