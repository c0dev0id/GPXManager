# Architecture

GPX Manager follows the MVVM (Model-View-ViewModel) architecture with a Repository pattern, as recommended by Android's modern app architecture guidelines.

## High-Level Overview

```
+-------------------+
|    UI Layer       |
| (Compose Screens) |
+--------+----------+
         |
+--------v----------+
|   ViewModel Layer  |
| (State + Actions)  |
+--------+-----------+
         |
+--------v-----------+
|  Repository Layer   |
| (Business Logic)    |
+---+--------+--------+
    |        |
+---v---+ +--v----------+
| Room  | | FileManager  |
| (DB)  | | (Storage)    |
+-------+ +--------------+
```

## Layers

### UI Layer

Jetpack Compose screens observe ViewModel state via `StateFlow` and `collectAsStateWithLifecycle`. Each screen is a composable function that receives navigation callbacks from the `NavHost`.

Screens:
- `MainScreen` -- Entry point with navigation to Trip Library, Locations, and Settings.
- `TripLibraryScreen` -- Browsable folder tree displaying GPX and PDF file cards.
- `LocationLibraryScreen` -- Browsable folder tree displaying location cards.
- `LocationDetailScreen` -- Edit form for a single location's properties.
- `SettingsScreen` -- Placeholder for future settings.

Shared components (`ui/common/`) provide reusable cards, dialogs, and input widgets.

### ViewModel Layer

Each screen has a dedicated `ViewModel` that holds UI state and exposes actions as suspend functions. ViewModels use constructor injection via Hilt and receive repository instances.

- `TripLibraryViewModel` -- Combines folder, GPX file, and PDF file flows for the current folder.
- `LocationLibraryViewModel` -- Combines folder and location flows for the current folder.
- `LocationDetailViewModel` -- Loads a single location and persists edits on each field change.

### Repository Layer

Repositories encapsulate data access and business logic. They coordinate between DAOs and the file system.

- `TripRepository` -- Manages folders (trip type), GPX files, and PDF files. Handles import (parse + store), export, rename, move, copy, and delete.
- `LocationRepository` -- Manages folders (location type) and location entries. CRUD plus move and copy.
- `CategoryRepository` -- Simple CRUD for location categories.

### Data Layer

**Room Database** (`AppDatabase`) with five tables:

```
+-------------------+       +-------------------+
|     folders       |       |    categories     |
|-------------------|       |-------------------|
| id (PK)          |       | id (PK)           |
| name             |       | name (UNIQUE)     |
| parent_id (FK)   |       +-------------------+
| library_type     |               |
| created_at       |               |
+---+---+---------+               |
    |   |                          |
    |   +------------------+       |
    |                      |       |
+---v---------------+ +----v------v--------+
|    gpx_files      | |    locations       |
|-------------------| |--------------------|
| id (PK)          | | id (PK)            |
| name             | | name               |
| file_name        | | address            |
| folder_id (FK)   | | longitude          |
| date             | | latitude           |
| route_count      | | category_id (FK)   |
| track_count      | | folder_id (FK)     |
| waypoint_count   | | created_at         |
| total_length_km  | +--------------------+
| created_at       |
+-------------------+
+-------------------+
|    pdf_files      |
|-------------------|
| id (PK)          |
| name             |
| file_name        |
| folder_id (FK)   |
| upload_date      |
| page_count       |
| created_at       |
+-------------------+
```

The `folders` table is shared between both libraries, distinguished by the `library_type` column (`TRIP` or `LOCATION`). Folders support nesting via the self-referencing `parent_id` foreign key.

**File Storage** (`FileManager`) stores physical GPX and PDF files in the app's internal storage directory at `files/trips/<uuid>.<ext>`. UUID-based naming prevents filename collisions. The human-readable display name is kept in the database only.

### Dependency Injection

Hilt provides dependency injection across the application:

- `DatabaseModule` -- Provides the `AppDatabase` singleton and all DAO instances.
- `RepositoryModule` -- Provides `FileManager` and `GpxParser` instances.
- Repositories use `@Inject` constructor injection directly.

## Navigation

Jetpack Navigation Compose manages screen transitions through a single-activity architecture. Routes are defined as a sealed class (`Screen`) with typed parameters for folder IDs and location IDs.

## GPX Parsing

The `GpxParser` uses Java's built-in SAX parser (`javax.xml.parsers.SAXParserFactory`) to stream GPX XML files. It extracts:
- Metadata name and date
- Route, track, and waypoint counts
- Total path length via Haversine distance calculation

The SAX approach was chosen for memory efficiency (streaming, no DOM tree) and JVM test compatibility.
