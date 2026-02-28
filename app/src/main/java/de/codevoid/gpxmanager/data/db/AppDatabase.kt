package de.codevoid.gpxmanager.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import de.codevoid.gpxmanager.data.db.dao.CategoryDao
import de.codevoid.gpxmanager.data.db.dao.FolderDao
import de.codevoid.gpxmanager.data.db.dao.GpxFileDao
import de.codevoid.gpxmanager.data.db.dao.LocationDao
import de.codevoid.gpxmanager.data.db.dao.PdfFileDao
import de.codevoid.gpxmanager.data.db.entity.CategoryEntity
import de.codevoid.gpxmanager.data.db.entity.FolderEntity
import de.codevoid.gpxmanager.data.db.entity.GpxFileEntity
import de.codevoid.gpxmanager.data.db.entity.LocationEntity
import de.codevoid.gpxmanager.data.db.entity.PdfFileEntity

@Database(
    entities = [
        FolderEntity::class,
        GpxFileEntity::class,
        PdfFileEntity::class,
        LocationEntity::class,
        CategoryEntity::class
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun folderDao(): FolderDao
    abstract fun gpxFileDao(): GpxFileDao
    abstract fun pdfFileDao(): PdfFileDao
    abstract fun locationDao(): LocationDao
    abstract fun categoryDao(): CategoryDao
}
