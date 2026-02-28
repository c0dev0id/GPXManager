package de.codevoid.gpxmanager.data.file

import android.content.Context
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.InputStream
import java.util.UUID
import javax.inject.Inject

/**
 * Manages physical file operations for GPX and PDF files in internal storage.
 *
 * Files are stored in: <app_internal>/trips/<uuid>.<ext>
 * The UUID-based naming prevents collisions. Display names are stored in the database.
 */
class FileManager @Inject constructor(private val context: Context) {

    companion object {
        private const val TAG = "FileManager"
        private const val TRIPS_DIR = "trips"
    }

    private val tripsDir: File
        get() = File(context.filesDir, TRIPS_DIR).also { it.mkdirs() }

    /**
     * Copies a file from a content URI to internal storage.
     * Returns the generated file name on disk.
     */
    fun importFile(uri: Uri, extension: String): String? {
        return try {
            val fileName = "${UUID.randomUUID()}.$extension"
            val destFile = File(tripsDir, fileName)
            context.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            fileName
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import file", e)
            null
        }
    }

    /**
     * Opens an InputStream for a stored file. Caller must close it.
     */
    fun openFile(fileName: String): InputStream? {
        val file = File(tripsDir, fileName)
        return if (file.exists()) file.inputStream() else null
    }

    /**
     * Returns the File reference for a stored file.
     */
    fun getFile(fileName: String): File = File(tripsDir, fileName)

    /**
     * Exports a stored file to an external URI chosen by the user.
     */
    fun exportFile(fileName: String, destinationUri: Uri): Boolean {
        return try {
            val sourceFile = File(tripsDir, fileName)
            context.contentResolver.openOutputStream(destinationUri)?.use { output ->
                sourceFile.inputStream().use { input ->
                    input.copyTo(output)
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "Failed to export file: $fileName", e)
            false
        }
    }

    /**
     * Deletes a stored file from internal storage.
     */
    fun deleteFile(fileName: String): Boolean {
        return try {
            File(tripsDir, fileName).delete()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete file: $fileName", e)
            false
        }
    }

    /**
     * Copies a stored file, creating a new file with a new UUID name.
     * Returns the new file name, or null on failure.
     */
    fun copyFile(fileName: String): String? {
        return try {
            val extension = fileName.substringAfterLast('.', "")
            val newFileName = "${UUID.randomUUID()}.$extension"
            val source = File(tripsDir, fileName)
            val dest = File(tripsDir, newFileName)
            source.copyTo(dest)
            newFileName
        } catch (e: Exception) {
            Log.e(TAG, "Failed to copy file: $fileName", e)
            null
        }
    }
}
