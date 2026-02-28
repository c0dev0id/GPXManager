# Developer Guide

## Software Stack

| Component | Technology | Version |
|---|---|---|
| Language | [Kotlin](https://kotlinlang.org/) | 2.1.0 |
| UI Framework | [Jetpack Compose](https://developer.android.com/develop/ui/compose) + [Material 3](https://m3.material.io/) | BOM 2024.12.01 |
| Navigation | [Navigation Compose](https://developer.android.com/guide/navigation/get-started) | 2.8.5 |
| Dependency Injection | [Hilt](https://dagger.dev/hilt/) | 2.54 |
| Database | [Room](https://developer.android.com/training/data-storage/room) | 2.7.1 |
| Build System | [Gradle](https://gradle.org/) with [AGP](https://developer.android.com/build) | Gradle 8.9, AGP 8.7.3 |
| Annotation Processing | [KSP](https://github.com/google/ksp) | 2.1.0-1.0.29 |
| Testing | [JUnit 4](https://junit.org/junit4/), [MockK](https://mockk.io/), [Turbine](https://github.com/cashapp/turbine) | -- |

**SDK Targets:** minSdk 34 (Android 14), targetSdk 35, compileSdk 35

## Project Structure

```
app/src/main/java/de/codevoid/gpxmanager/
  GpxManagerApp.kt              @HiltAndroidApp Application class
  MainActivity.kt               Single-activity Compose host, @AndroidEntryPoint

  navigation/
    Routes.kt                   Sealed class Screen defining all navigation routes
    AppNavigation.kt            NavHost setup with route-to-composable mappings

  data/
    db/
      AppDatabase.kt            Room database (version 1, 5 entities, exportSchema=true)
      entity/
        FolderEntity.kt         Shared folder table with library_type discriminator
        GpxFileEntity.kt        GPX file metadata (routes, tracks, waypoints, length)
        PdfFileEntity.kt        PDF file metadata (page count, upload date)
        LocationEntity.kt       Location with address, coordinates, category
        CategoryEntity.kt       Location category (unique name)
      dao/
        FolderDao.kt            Folder queries filtered by parent_id and library_type
        GpxFileDao.kt           GPX file queries filtered by folder_id
        PdfFileDao.kt           PDF file queries filtered by folder_id
        LocationDao.kt          Location queries with reactive Flow for detail view
        CategoryDao.kt          Category CRUD

    repository/
      TripRepository.kt         Folder, GPX, and PDF lifecycle (import, export, move, copy)
      LocationRepository.kt     Folder and location lifecycle (CRUD, move, copy)
      CategoryRepository.kt     Category CRUD

    file/
      GpxParser.kt              SAX-based GPX parser, extracts GpxMetadata
      PdfUtil.kt                Android PdfRenderer wrapper for page counting
      FileManager.kt            UUID-based file I/O in app-internal storage

  di/
    DatabaseModule.kt           Hilt module: AppDatabase + all DAOs
    RepositoryModule.kt         Hilt module: FileManager + GpxParser

  ui/
    theme/
      Color.kt                  Material 3 color scheme (green/teal palette)
      Type.kt                   Material 3 typography
      Theme.kt                  Dynamic color support, dark/light theme switching

    common/
      ItemCard.kt               Reusable 1/3 preview + 2/3 detail card
      FolderCard.kt             Folder icon card with long-press support
      ConfirmDialog.kt          Reusable confirmation dialog
      TextInputDialog.kt        Text input dialog with auto-focus
      MoveDialog.kt             Folder picker dialog for move/copy operations

    main/
      MainScreen.kt             Main menu: Trip Library, Locations, Settings

    trip/
      TripLibraryScreen.kt      Folder browser with GPX/PDF cards and FAB actions
      TripLibraryViewModel.kt   Combines folder, GPX, and PDF flows per folder

    location/
      LocationLibraryScreen.kt  Folder browser with location cards and FAB actions
      LocationLibraryViewModel.kt  Combines folder and location flows per folder
      LocationDetailScreen.kt   Location edit form (name, address, category, coordinates)
      LocationDetailViewModel.kt  Loads/saves location fields reactively

    settings/
      SettingsScreen.kt         Placeholder
```

## Key Files and Signatures

### GpxParser

Parses GPX XML files using Java's SAX parser. Returns a `GpxMetadata` data class.

```kotlin
class GpxParser {
    fun parse(inputStream: InputStream): GpxMetadata
}

data class GpxMetadata(
    val name: String?,
    val date: Long?,          // epoch millis from <metadata><time>
    val routeCount: Int,      // number of <rte> elements
    val trackCount: Int,      // number of <trk> elements
    val waypointCount: Int,   // number of <wpt> elements
    val totalLengthKm: Double // Haversine distance across all tracks and routes
)
```

SAX was chosen over `XmlPullParser` because `android.util.Xml` is not available in JVM unit tests, while `javax.xml.parsers.SAXParserFactory` works in both environments.

### FileManager

Handles physical file storage. Files are stored at `context.filesDir/trips/<uuid>.<ext>`.

```kotlin
class FileManager @Inject constructor(context: Context) {
    fun importFile(uri: Uri, extension: String): String?       // content URI -> internal, returns filename
    fun openFile(fileName: String): InputStream?               // open for reading
    fun getFile(fileName: String): File                        // get File reference
    fun exportFile(fileName: String, destinationUri: Uri): Boolean  // internal -> user-chosen location
    fun deleteFile(fileName: String): Boolean
    fun copyFile(fileName: String): String?                    // duplicate with new UUID
}
```

### TripRepository

Coordinates database and file operations for the trip library.

```kotlin
class TripRepository @Inject constructor(...) {
    fun getFolders(parentId: Long?): Flow<List<FolderEntity>>
    fun getGpxFiles(folderId: Long?): Flow<List<GpxFileEntity>>
    fun getPdfFiles(folderId: Long?): Flow<List<PdfFileEntity>>
    suspend fun createFolder(name: String, parentId: Long?)
    suspend fun importGpxFile(uri: Uri, displayName: String, folderId: Long?): Boolean
    suspend fun importPdfFile(uri: Uri, displayName: String, folderId: Long?): Boolean
    suspend fun exportFile(fileName: String, destinationUri: Uri): Boolean
    suspend fun renameGpxFile(id: Long, newName: String)
    suspend fun renamePdfFile(id: Long, newName: String)
    suspend fun deleteGpxFile(id: Long)              // deletes both DB record and physical file
    suspend fun deletePdfFile(id: Long)
    suspend fun moveGpxFile(id: Long, targetFolderId: Long?)
    suspend fun movePdfFile(id: Long, targetFolderId: Long?)
    suspend fun moveFolder(id: Long, targetParentId: Long?)
    suspend fun copyGpxFile(id: Long, targetFolderId: Long?)   // duplicates the physical file
    suspend fun copyPdfFile(id: Long, targetFolderId: Long?)
}
```

### LocationRepository

Manages folders and locations in the location library.

```kotlin
class LocationRepository @Inject constructor(...) {
    fun getFolders(parentId: Long?): Flow<List<FolderEntity>>
    fun getLocations(folderId: Long?): Flow<List<LocationEntity>>
    fun getLocationFlow(id: Long): Flow<LocationEntity?>       // reactive, for detail screen
    suspend fun createFolder(name: String, parentId: Long?)
    suspend fun createLocation(name: String, folderId: Long?): Long
    suspend fun updateLocation(location: LocationEntity)
    suspend fun renameFolder(id: Long, newName: String)
    suspend fun renameLocation(id: Long, newName: String)
    suspend fun deleteFolder(id: Long)
    suspend fun deleteLocation(id: Long)
    suspend fun moveFolder(id: Long, targetParentId: Long?)
    suspend fun moveLocation(id: Long, targetFolderId: Long?)
    suspend fun copyLocation(id: Long, targetFolderId: Long?)
}
```

## Building

```bash
./gradlew assembleDebug
```

The debug APK is output to `app/build/outputs/apk/debug/app-debug.apk`.

## Testing

```bash
./gradlew test
```

Unit tests are in `app/src/test/`. Test resources (sample GPX files) are in `app/src/test/resources/`.

### Test Coverage

- `GpxParserTest` -- Parser correctness: metadata extraction, element counting, Haversine distance, edge cases (empty files, waypoints-only).
- `CategoryRepositoryTest` -- Category CRUD operations with mocked DAO.
- `LocationRepositoryTest` -- Location and folder operations with mocked DAOs.

## Version Catalog

Dependencies are managed through `gradle/libs.versions.toml`. To update a dependency, change the version in the `[versions]` section and sync the project.
