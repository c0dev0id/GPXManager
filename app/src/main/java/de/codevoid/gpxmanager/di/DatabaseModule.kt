package de.codevoid.gpxmanager.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import de.codevoid.gpxmanager.data.db.AppDatabase
import de.codevoid.gpxmanager.data.db.dao.CategoryDao
import de.codevoid.gpxmanager.data.db.dao.FolderDao
import de.codevoid.gpxmanager.data.db.dao.GpxFileDao
import de.codevoid.gpxmanager.data.db.dao.LocationDao
import de.codevoid.gpxmanager.data.db.dao.PdfFileDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "gpxmanager.db")
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideFolderDao(db: AppDatabase): FolderDao = db.folderDao()

    @Provides
    fun provideGpxFileDao(db: AppDatabase): GpxFileDao = db.gpxFileDao()

    @Provides
    fun providePdfFileDao(db: AppDatabase): PdfFileDao = db.pdfFileDao()

    @Provides
    fun provideLocationDao(db: AppDatabase): LocationDao = db.locationDao()

    @Provides
    fun provideCategoryDao(db: AppDatabase): CategoryDao = db.categoryDao()
}
