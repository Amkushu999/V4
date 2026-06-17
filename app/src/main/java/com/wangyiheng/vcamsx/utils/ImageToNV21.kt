package com.wangyiheng.vcamsx.utils

import android.graphics.Bitmap
import android.graphics.ImageFormat

object ImageToNV21 {
    
    fun convertToNV21(bitmap: Bitmap): ByteArray {
        val width = bitmap.width
        val height = bitmap.height
        val frameSize = width * height
        val yuv = ByteArray(frameSize * 3 / 2)
        
        val pixels = IntArray(frameSize)
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height)
        
        var index = 0
        
        for (i in 0 until frameSize) {
            val pixel = pixels[i]
            val r = (pixel shr 16) and 0xFF
            val g = (pixel shr 8) and 0xFF
            val b = pixel and 0xFF
            
            val y = ((66 * r + 129 * g + 25 * b + 128) shr 8) + 16
            yuv[index++] = when {
                y < 0 -> 0.toByte()
                y > 255 -> 255.toByte()
                else -> y.toByte()
            }
        }
        
        for (j in 0 until height step 2) {
            for (i in 0 until width step 2) {
                val pixel1 = pixels[j * width + i]
                val pixel2 = pixels[j * width + i + 1]
                val pixel3 = pixels[(j + 1) * width + i]
                val pixel4 = pixels[(j + 1) * width + i + 1]
                
                val r = (((pixel1 shr 16) and 0xFF) + ((pixel2 shr 16) and 0xFF) + 
                         ((pixel3 shr 16) and 0xFF) + ((pixel4 shr 16) and 0xFF)) / 4
                val g = (((pixel1 shr 8) and 0xFF) + ((pixel2 shr 8) and 0xFF) + 
                         ((pixel3 shr 8) and 0xFF) + ((pixel4 shr 8) and 0xFF)) / 4
                val b = ((pixel1 and 0xFF) + (pixel2 and 0xFF) + 
                         (pixel3 and 0xFF) + (pixel4 and 0xFF)) / 4
                
                val u = ((-38 * r - 74 * g + 112 * b + 128) shr 8) + 128
                val v = ((112 * r - 94 * g - 18 * b + 128) shr 8) + 128
                
                yuv[index++] = when {
                    v < 0 -> 0.toByte()
                    v > 255 -> 255.toByte()
                    else -> v.toByte()
                }
                yuv[index++] = when {
                    u < 0 -> 0.toByte()
                    u > 255 -> 255.toByte()
                    else -> u.toByte()
                }
            }
        }
        return yuv
    }
}