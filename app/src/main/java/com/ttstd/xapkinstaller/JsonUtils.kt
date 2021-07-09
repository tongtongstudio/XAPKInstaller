package com.ttstd.xapkinstaller

import java.io.*
import java.nio.charset.StandardCharsets

object JsonUtils {
    /**
     * 读取json文件，返回json串
     * @param fileName
     * @return
     */
    fun readJsonFile(fileName: String?): String? {
        val jsonStr: String
        return try {
            val jsonFile = File(fileName)
            val fileReader = FileReader(jsonFile)
            val reader: Reader =
                InputStreamReader(FileInputStream(jsonFile), StandardCharsets.UTF_8)
            var ch = 0
            val sb = StringBuffer()
            while (reader.read().also { ch = it } != -1) {
                sb.append(ch.toChar())
            }
            fileReader.close()
            reader.close()
            jsonStr = sb.toString()
            jsonStr
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }
}