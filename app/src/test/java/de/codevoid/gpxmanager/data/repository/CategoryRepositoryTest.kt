package de.codevoid.gpxmanager.data.repository

import de.codevoid.gpxmanager.data.db.dao.CategoryDao
import de.codevoid.gpxmanager.data.db.entity.CategoryEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test

class CategoryRepositoryTest {

    private lateinit var categoryDao: CategoryDao
    private lateinit var repository: CategoryRepository

    @Before
    fun setup() {
        categoryDao = mockk(relaxed = true)
        repository = CategoryRepository(categoryDao)
    }

    @Test
    fun `getAll returns categories from dao`() = runTest {
        val categories = listOf(
            CategoryEntity(id = 1, name = "Restaurant"),
            CategoryEntity(id = 2, name = "Hotel")
        )
        every { categoryDao.getAll() } returns flowOf(categories)

        println("=== Test: getAll returns categories ===")
        val result = repository.getAll().first()
        println("Expected count: 2")
        println("Actual count: ${result.size}")
        println("Categories: ${result.map { it.name }}")

        assertEquals(2, result.size)
        assertEquals("Restaurant", result[0].name)
        assertEquals("Hotel", result[1].name)
    }

    @Test
    fun `create inserts category and returns id`() = runTest {
        coEvery { categoryDao.insert(any()) } returns 5L

        println("=== Test: create inserts category ===")
        val id = repository.create("Museum")
        println("Expected ID: 5")
        println("Actual ID: $id")

        assertEquals(5L, id)
        coVerify { categoryDao.insert(match { it.name == "Museum" }) }
    }

    @Test
    fun `getById returns category from dao`() = runTest {
        val category = CategoryEntity(id = 1, name = "Restaurant")
        coEvery { categoryDao.getById(1) } returns category

        println("=== Test: getById returns category ===")
        val result = repository.getById(1)
        println("Expected: Restaurant")
        println("Actual: ${result?.name}")

        assertNotNull(result)
        assertEquals("Restaurant", result?.name)
    }

    @Test
    fun `delete calls dao deleteById`() = runTest {
        println("=== Test: delete calls dao ===")
        repository.delete(3)

        coVerify { categoryDao.deleteById(3) }
        println("Verified: deleteById(3) called")
    }
}
