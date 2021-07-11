package com.ttstd.xapkinstaller

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File


object FileUtils {
    fun getFileNoExName(filePath: String): String {
        val fileNameEx = filePath.substring(filePath.lastIndexOf(File.separator) + 1)
        val fileName = fileNameEx.substring(0, fileNameEx.lastIndexOf("."))
        return if (fileName.indexOf(".") == -1) fileNameEx else fileName
    }

    fun getFileName(path:String):String{
        val fileNameEx = path.substring(path.lastIndexOf(File.separator) + 1)
        return fileNameEx
    }


    fun Bytes2Bimap(b: ByteArray): Bitmap? {
        return if (b.size != 0) {
            BitmapFactory.decodeByteArray(b, 0, b.size)
        } else {
            null
        }
    }


}