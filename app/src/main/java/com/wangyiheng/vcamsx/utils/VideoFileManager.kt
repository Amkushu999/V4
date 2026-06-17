package com.wangyiheng.vcamsx.utils

import android.content.Context
import android.net.Uri
import com.wangyiheng.vcamsx.config.Config
import com.wangyiheng.vcamsx.interfaces.IVideoFileManager
import java.io.File

/**
 * Handles all video file operations
 */
class VideoFileManager(private val context: Context) : IVideoFileManager {
    
    override fun copyVideoToAppDir(videoUri: Uri): File {
        val outputDir = context.getExternalFilesDir(null)?.absolutePath 
            ?: throw IllegalStateException("Cannot access external files directory")
        
        val outputFile = File(outputDir, Config.VIDEO_FILE_NAME)
        
        context.contentResolver.openInputStream(videoUri)?.use { input ->
            outputFile.outputStream().use { fileOut ->
                input.copyTo(fileOut)
            }
        } ?: throw IllegalArgumentException("Cannot open input stream from URI")
        
        return outputFile
    }
    
    override fun getVideoPath(): String {
        val outputDir = context.getExternalFilesDir(null)?.absolutePath 
            ?: throw IllegalStateException("Cannot access external files directory")
        return "$outputDir/${Config.VIDEO_FILE_NAME}"
    }
    
    override fun videoExists(): Boolean {
        val path = getVideoPath()
        return File(path).exists()
    }

    /**
     * 🆕 NEW: Deletes the saved video file from internal storage.
     * Used when the user switches from Video to Image injection.
     */
    override fun deleteSavedVideo() {
        val path = getVideoPath()
        val file = File(path)
        if (file.exists()) {
            file.delete()
        }
    }
}