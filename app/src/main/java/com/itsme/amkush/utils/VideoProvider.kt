package com.itsme.amkush.utils

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import com.itsme.amkush.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class VideoProvider : ContentProvider() {

    override fun openFile(uri: Uri, mode: String): ParcelFileDescriptor? {
        // FIX #14: Safe null handling — previously used !! after ?. which would throw
        // NullPointerException if getExternalFilesDir returned null.
        val externalFilesDir = context?.getExternalFilesDir(null)?.absolutePath ?: return null

        val vcamsxFile = File(externalFilesDir, "copied_video.mp4")

        if (!vcamsxFile.exists()) {
            try {
                context?.resources?.openRawResource(R.raw.vcamsx)?.use { inputStream ->
                    FileOutputStream(vcamsxFile).use { fileOutputStream ->
                        inputStream.copyTo(fileOutputStream)
                    }
                }
            } catch (e: IOException) {
                e.printStackTrace()
                return null
            }
        }

        return ParcelFileDescriptor.open(vcamsxFile, ParcelFileDescriptor.MODE_READ_ONLY)
    }

    override fun onCreate(): Boolean = true

    // FIX #15: Removed dead legacy query() implementation that had:
    //  1. A crash: context?.getExternalFilesDir(null)!! would NPE when getExternalFilesDir is null
    //  2. A hardcoded path to a non-existent test file (advancedModeMovies/654e.../caibi_60.mp4)
    //  3. The result was never read by any live code path
    // Returns an empty cursor instead to satisfy the ContentProvider contract safely.
    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor = MatrixCursor(arrayOf("_id", "display_name", "size", "date_modified", "file"))

    // FIX #16: Removed unused extractContent() method — dead code that was never called
    // from any live code path and only added noise to the class.

    override fun getType(uri: Uri): String? = null
    override fun insert(uri: Uri, values: ContentValues?): Uri? = null
    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int = 0
    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int = 0
}
