package de.codevoid.gpxmanager.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gpx_files",
    foreignKeys = [
        ForeignKey(
            entity = FolderEntity::class,
            parentColumns = ["id"],
            childColumns = ["folder_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("folder_id")]
)
data class GpxFileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "file_name") val fileName: String,
    @ColumnInfo(name = "folder_id") val folderId: Long?,
    val date: Long,
    @ColumnInfo(name = "route_count") val routeCount: Int,
    @ColumnInfo(name = "track_count") val trackCount: Int,
    @ColumnInfo(name = "waypoint_count") val waypointCount: Int,
    @ColumnInfo(name = "total_length_km") val totalLengthKm: Double,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)
