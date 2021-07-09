package com.ttstd.xapkinstaller

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import com.google.gson.JsonParser
import org.zeroturnaround.zip.ZipUtil
import java.io.File


object AppUtils {
    private val TAG: String? = AppUtils::class.simpleName

    fun getFileInfo(filePath: String, context: Context): AppInfo? {
        if (filePath.endsWith(".apk")) {
            return getAPKInfo(filePath, context)
        } else if (filePath.endsWith(".xapk")) {
            return getXAPKInfo(filePath, context)
        } else {
            return null
        }
    }

    fun getAPKInfo(filePath: String, context: Context): AppInfo? {
        val packageManager = context.packageManager
        val pkgInfo: PackageInfo
        pkgInfo = packageManager.getPackageArchiveInfo(filePath, PackageManager.GET_ACTIVITIES)
        if (pkgInfo != null) {
            val appInfo = AppInfo()
            val applicationInfo: ApplicationInfo = pkgInfo.applicationInfo
            applicationInfo.sourceDir = filePath
            applicationInfo.publicSourceDir = filePath
            appInfo.icon = applicationInfo.loadIcon(packageManager)
            appInfo.label = applicationInfo.loadLabel(packageManager) as String
            appInfo.version = pkgInfo.versionName
            appInfo.pkg = applicationInfo.packageName
            appInfo.filename = filePath
            val file = File(filePath)
            appInfo.size = byteToMB(file.length())
            return appInfo
        } else {
            return null
        }
    }

    fun getXAPKInfo(filePath: String, context: Context): AppInfo? {
        val XAPKfile = File(filePath)
        if (XAPKfile.exists() || XAPKfile.isFile) {
            return XAPKinfo(XAPKfile)
        } else {
            return null
        }
    }

    private var unpackPath: String? = null
    private const val XAPKSize: Long = 0

    fun XAPKinfo(file: File): AppInfo? {
        //判断json文件是否存在
        if (ZipUtil.containsEntry(file, "manifest.json")) {
            val dirName: String = FileUtils.getFileNoExName(file.getAbsolutePath())
            unpackPath = file.getParentFile().getAbsolutePath() + File.separator + dirName
            val unpackDir = File(unpackPath)
            val bytes = ZipUtil.unpackEntry(file, "manifest.json")
            val jsonString = String(bytes)
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            val appInfo = AppInfo()
            appInfo.icon = BitmapDrawable(
                FileUtils.Bytes2Bimap(
                    ZipUtil.unpackEntry(
                        file,
                        jsonObject.get("icon").asString
                    )
                )
            )
            appInfo.label = jsonObject.get("name").asString
            appInfo.version = jsonObject.get("version_name").asString
            appInfo.pkg = jsonObject.get("package_name").asString
            appInfo.filename = file.absolutePath
            appInfo.size = byteToMB(jsonObject.get("total_size").asLong)
            return appInfo
        } else {
            ToastUtil.show("读取XAPK配置文件失败")
            return null
        }
    }


    //将字节数转化为MB
    fun byteToMB(size: Long): String {
        val kb: Long = 1024
        val mb = kb * 1024
        val gb = mb * 1024
        return if (size >= gb) {
            String.format("%.2f GB", size.toFloat() / gb)
        } else if (size >= mb) {
            val f = size.toFloat() / mb
            String.format(if (f > 100) "%.0f MB" else "%.2f MB", f)
        } else if (size > kb) {
            val f = size.toFloat() / kb
            String.format(if (f > 100) "%.0f KB" else "%.2f KB", f)
        } else {
            String.format("%d B", size)
        }
    }


}