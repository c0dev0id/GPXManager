package de.codevoid.gpxmanager.data.file

import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Log
import java.io.File

object PdfUtil {

    private const val TAG = "PdfUtil"

    /**
     * Returns the number of pages in a PDF file.
     * Returns 0 if the file cannot be read or is not a valid PDF.
     */
    fun getPageCount(file: File): Int {
        return try {
            val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val renderer = PdfRenderer(fd)
            val count = renderer.pageCount
            renderer.close()
            fd.close()
            count
        } catch (e: Exception) {
            Log.e(TAG, "Failed to read PDF page count: ${file.name}", e)
            0
        }
    }
}
