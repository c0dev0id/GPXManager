package de.codevoid.gpxmanager.data.repository

import de.codevoid.gpxmanager.data.db.dao.FolderDao
import de.codevoid.gpxmanager.data.db.dao.LocationDao
import de.codevoid.gpxmanager.data.db.entity.FolderEntity
import de.codevoid.gpxmanager.data.db.entity.LocationEntity
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

class LocationRepositoryTest {

    private lateinit var folderDao: FolderDao
    private lateinit var locationDao: LocationDao
    private lateinit var repository: LocationRepository

    @Before
    fun setup() {
        folderDao = mockk(relaxed = true)
        locationDao = mockk(relaxed = true)
        repository = LocationRepository(folderDao, locationDao)
    }

    @Test
    fun `getFolders returns location folders`() = runTest {
        val folders = listOf(
            FolderEntity(id = 1, name = "Europe", parentId = null, libraryType = FolderEntity.TYPE_LOCATION)
        )
        every { folderDao.getByParent(null, FolderEntity.TYPE_LOCATION) } returns flowOf(folders)

        println("=== Test: getFolders returns location folders ===")
        val result = repository.getFolders(null).first()
        println("Expected count: 1")
        println("Actual count: ${result.size}")

        assertEquals(1, result.size)
        assertEquals("Europe", result[0].name)
    }

    @Test
    fun `createLocation inserts and returns id`() = runTest {
        coEvery { locationDao.insert(any()) } returns 10L

        println("=== Test: createLocation inserts and returns id ===")
        val id = repository.createLocation("Home", null)
        println("Expected ID: 10")
        println("Actual ID: $id")

        assertEquals(10L, id)
        coVerify { locationDao.insert(match { it.name == "Home" && it.folderId == null }) }
    }

    @Test
    fun `renameLocation updates location name`() = runTest {
        val location = LocationEntity(id = 5, name = "Old Name", folderId = null)
        coEvery { locationDao.getById(5) } returns location

        println("=== Test: renameLocation updates name ===")
        repository.renameLocation(5, "New Name")

        coVerify { locationDao.update(match { it.id == 5L && it.name == "New Name" }) }
        println("Verified: update called with new name")
    }

    @Test
    fun `deleteLocation calls dao`() = runTest {
        println("=== Test: deleteLocation calls dao ===")
        repository.deleteLocation(7)

        coVerify { locationDao.deleteById(7) }
        println("Verified: deleteById(7) called")
    }

    @Test
    fun `moveLocation updates folder id`() = runTest {
        val location = LocationEntity(id = 3, name = "Test", folderId = 1)
        coEvery { locationDao.getById(3) } returns location

        println("=== Test: moveLocation updates folder id ===")
        repository.moveLocation(3, 5)

        coVerify { locationDao.update(match { it.id == 3L && it.folderId == 5L }) }
        println("Verified: update called with new folderId=5")
    }

    @Test
    fun `copyLocation creates duplicate with new id`() = runTest {
        val location = LocationEntity(id = 2, name = "Office", address = "Street 1", folderId = 1)
        coEvery { locationDao.getById(2) } returns location
        coEvery { locationDao.insert(any()) } returns 20L

        println("=== Test: copyLocation creates duplicate ===")
        repository.copyLocation(2, 3)

        coVerify {
            locationDao.insert(match {
                it.id == 0L && it.name == "Office" && it.folderId == 3L
            })
        }
        println("Verified: insert called with id=0, original name, new folderId=3")
    }
}
