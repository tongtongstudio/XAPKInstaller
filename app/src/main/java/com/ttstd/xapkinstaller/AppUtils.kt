package com.ttstd.xapkinstaller

import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageInstaller
import android.content.pm.PackageInstaller.SessionParams
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.RemoteException
import android.text.TextUtils
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.zeroturnaround.zip.ZipUtil
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import kotlin.collections.HashMap

@SuppressLint("StaticFieldLeak")
object AppUtils {
    private val TAG: String? = AppUtils::class.simpleName
    private lateinit var mContext: Context

    fun init(context: Context) {
        mContext = context
    }

    /**
     * @param size Long 文件大小
     * @return String   格式化后的文本
     * 将字节数转化为MB
     */
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

    /**
     * @param filePath String   文件路径
     * @return AppInfo? 自定义的文件信息类
     */
    fun getFileInfo(filePath: String): AppInfo? {
        if (filePath.endsWith(".apk")) {
            return getAPKInfo(filePath)
        } else if (filePath.endsWith(".xapk")) {
            return getXAPKInfo(filePath)
        } else {
            return null
        }
    }

    /**
     * @param filePath String   文件路径
     * @return AppInfo? 获取apk文件的信息
     */
    fun getAPKInfo(filePath: String): AppInfo? {
        val packageManager = mContext.packageManager
        var pkgInfo: PackageInfo? = null
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
            appInfo.apkFile = true
            appInfo.filename = filePath
            val file = File(filePath)
            appInfo.size = byteToMB(file.length())
            return appInfo
        } else {
            return null
        }
    }

    /**
     * @param filePath String   文件路径
     * @return AppInfo? 获取xapk文件信息
     */
    fun getXAPKInfo(filePath: String): AppInfo? {
        val XAPKFile = File(filePath)
        if (XAPKFile.exists() || XAPKFile.isFile) {
            return XAPKinfo(XAPKFile)
        } else {
            return null
        }
    }

    /**
     * @param file File 文件
     * @return AppInfo? xapk文件信息
     */
    fun XAPKinfo(file: File): AppInfo? {
        //判断json文件是否存在，manifest.json默认存在的
        return if (ZipUtil.containsEntry(file, "manifest.json")) {
            //读取manifest.json 的byte[] 数据
            val bytes = ZipUtil.unpackEntry(file, "manifest.json")
            //转换为String
            val jsonString = String(bytes)
            //转换为JsonObject
            val jsonObject = JsonParser.parseString(jsonString).asJsonObject
            val appInfo = AppInfo()
            appInfo.icon = BitmapDrawable(
                FileUtils.Bytes2Bimap(
                    ZipUtil.unpackEntry(
                        file,
                        jsonObject["icon"].asString
                    )
                )
            )
            appInfo.label = jsonObject["name"].asString
            appInfo.version = jsonObject["version_name"].asString
            appInfo.pkg = jsonObject["package_name"].asString
            appInfo.filename = file.absolutePath
            appInfo.apkFile = false
            appInfo.size = byteToMB(jsonObject["total_size"].asLong)
            appInfo
        } else {
            ToastUtil.show("读取XAPK配置文件失败")
            null
        }
    }

    /**
     * @param path String
     * 安装apk兼容高版本
     */
    fun installAPK(path: String) {
        val intent = Intent(Intent.ACTION_VIEW)
        val apkFile = File(path)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            val uri: Uri = FileProvider.getUriForFile(
                mContext,
                mContext.getPackageName().toString() + ".fileprovider",
                apkFile
            )
            intent.setDataAndType(uri, "application/vnd.android.package-archive")
        } else {
            intent.setDataAndType(
                Uri.fromFile(apkFile),
                "application/vnd.android.package-archive"
            )
        }
        mContext.startActivity(intent)
    }

    /**
     * @param path String
     * 安装xapk文件
     */
    fun installXAPK(path: String) {
        val XAPKFile = File(path)
        if (XAPKFile.exists() || XAPKFile.isFile) {
            upzipXAPK(XAPKFile)
        } else {

        }
    }

    var unpackPath: String? = null
    var XAPKSize: Long = 0
    var packageName: String = ""

    /**
     * @param XAPKFile File
     * 异步解压文件 读取配置
     */
    fun upzipXAPK(XAPKFile: File) {
        Observable.create<String> { emitter ->
            if (ZipUtil.containsEntry(XAPKFile, "manifest.json")) {
                //判断json文件是否存在
                val dirName = FileUtils.getFileNoExName(XAPKFile.absolutePath)
                unpackPath = XAPKFile.parentFile.absolutePath + File.separator + dirName
                val unpackDir = File(unpackPath)
                ToastUtil.show("正在解压文件")
                ZipUtil.unpack(XAPKFile, unpackDir)
                val jsonString =
                    JsonUtils.readJsonFile(unpackDir.absolutePath + File.separator + "manifest.json")
                emitter.onNext(jsonString!!)
            } else {
                ToastUtil.show("读取XAPK配置文件失败")
                emitter.onComplete()
            }
        }.subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : Observer<String?> {
                override fun onSubscribe(d: Disposable) {}
                override fun onNext(t: String) {
                    val jsonObject = JsonParser.parseString(t).asJsonObject
                    XAPKSize = jsonObject["total_size"].asLong
                    packageName = jsonObject["package_name"].asString
                    //获取分裂的配置
                    val split_configs_em = jsonObject["split_configs"]
                    //获取OBB文件配置
                    val expansions_em = jsonObject["expansions"]
                    //split_configs对应的apk
                    val split_apks_em = jsonObject["split_apks"]
                    readConfig(split_configs_em, expansions_em, split_apks_em)
//                    Log.e(TAG, "installXAPK: " + t)
                }

                override fun onError(e: Throwable) {
                    Log.e(TAG, "onError: " + e.message)
                }

                override fun onComplete() {
                    Log.e(TAG, "onComplete: ")
                }

            })
    }

    /**
     * @param split_configs JsonElement?
     * @param expansions JsonElement?
     * @param split_apks JsonElement?
     */
    fun readConfig(
        split_configs: JsonElement?,
        expansions: JsonElement?,
        split_apks: JsonElement?
    ) {
        //没有split_configs只有单个apk,读取split_apks 的base字段的file文件
        //没有expansions 没有obb文件
        //split_apks应该是都会有的，包含一个base的json对象
        if (split_configs != null) {
            getSplitConfigs(split_configs.asJsonArray)
        } else {
            Log.e(TAG, "readConfig: " + "not found split_configs json data")
        }
        if (expansions != null) {
            readOBBConfig(expansions.asJsonArray)
        } else {
            Log.e(TAG, "readConfig: " + "not found expansions json data")
        }
        if (split_apks != null) {
            getSplitApks(split_apks.asJsonArray)
        } else {
            Log.e(TAG, "readConfig: " + "not found split_apks json data")
        }
    }

    var configList: List<String> = ArrayList()

    /**
     * 获取分裂的配置文件
     *
     * @param jsonArray
     */
    fun getSplitConfigs(jsonArray: JsonArray) {
        val type = object : TypeToken<List<String?>?>() {}.type
        val gson = Gson()
        configList = gson.fromJson(gson.toJson(jsonArray), type)
    }

    /**
     * 获取obb配置文件
     *
     * @param jsonArray
     */
    fun readOBBConfig(jsonArray: JsonArray) {
        if (TextUtils.isEmpty(unpackPath)) {
            Log.e(TAG, "readOBBConfig: " + "unpack directory is empty")
            return
        }
        val type = object : TypeToken<List<Expansions?>?>() {}.type
        val gson = Gson()
        val expansionsList: List<Expansions>? =
            gson.fromJson<List<Expansions>>(gson.toJson(jsonArray), type)
        if (expansionsList != null && expansionsList.size > 0) {
            for (expansions in expansionsList) {
                if (copyObbFile(expansions)) {
                    Log.e(TAG, "readOBBConfig: " + "success")
                } else {
                    Log.e(TAG, "readOBBConfig: " + "copy oob File failure")
                }
            }
        }
    }

    @SuppressLint("NewApi")
    fun copyObbFile(expansions: Expansions): Boolean {
        val install_location: String = expansions.install_location.toString()
        //install_location 不清楚是否还有其他定义
        val file: String = expansions.file.toString()
        val install_path: String = expansions.install_path.toString()
        return if (TextUtils.isEmpty(file)) {
            Log.e(TAG, "copyObbFile: " + "file path is empty")
            false
        } else {
            val localFile: File =
                File(unpackPath + File.separator + file)
            if (localFile.exists() && localFile.isFile) {
                val installFile =
                    File(Environment.getExternalStorageDirectory().absolutePath + File.separator + install_path)
                Log.e(TAG, "copyObbFile: " + "localFile: " + localFile.absolutePath)
                Log.e(TAG, "copyObbFile: " + "installFile: " + installFile.absolutePath)
                try {
                    val path = Paths.get(localFile.absolutePath)
                    Files.copy(path, FileOutputStream(installFile))
                    true
                } catch (e: IOException) {
                    Log.e(TAG, "copyObbFile: " + "IOException" + e.message)
                    e.printStackTrace()
                    false
                }
            } else {
                Log.e(TAG, "copyObbFile: " + "localFile: " + "File not exists")
                false
            }
        }
    }

    /**
     * 获取分裂的apk文件
     * @param jsonArray
     */
    fun getSplitApks(jsonArray: JsonArray) {
        val type = object : TypeToken<List<SplitApks>>() {}.type
        val gson = Gson()
        val splitApksList: List<SplitApks> =
            gson.fromJson(gson.toJson(jsonArray), type)
        val apkHashMap: HashMap<String, String> = HashMap()
        for (splitApks in splitApksList) {
            apkHashMap.put(splitApks.id.toString(), splitApks.file.toString())
        }
        getApkFilePath(configList, apkHashMap)
    }

    /**
     * @param configList
     * @param apkHashMap
     * configList 可以为空，为空解析split_apks对象中的base
     * apkList  应该不会为空
     */
    fun getApkFilePath(configList: List<String>?, apkHashMap: HashMap<String, String>) {
        val filePath: MutableList<String> = ArrayList()
        //应该直接获取SplitApks里面的file
        if (null != configList && configList.size > 0) {
            //split_configs不为空的情况
            filePath.add(
                unpackPath + File.separator + apkHashMap.get("base")
            )
            for (config in configList) {
                val file: String = apkHashMap.get(config).toString()
                filePath.add(unpackPath + File.separator + file)
            }
        } else {
            //split_configs为空的情况
            val file: String = apkHashMap.get("base").toString()
            filePath.add(file)
            Log.e(TAG, "installXAPK: base file = $file")
        }
        if (filePath.size != 0) {
            installApk(filePath)
        }
    }

    fun installApk(paths: List<String?>) {
        val intent = Intent(mContext, InstallActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putStringArrayListExtra(InstallActivity.FILE_PATH, paths as ArrayList<String>?)
        mContext.startActivity(intent)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun installAppatPie(context: Activity, apkFilePath: List<String?>) {
        val packageInstaller = context.packageManager.packageInstaller
        val sessionParams = SessionParams(SessionParams.MODE_FULL_INSTALL)
        sessionParams.setInstallLocation(0)
//        sessionParams.setSize(XAPKSize)
        val openSession =
            packageInstaller.openSession(packageInstaller.createSession(sessionParams))
        for (apkPath in apkFilePath) {
//            Log.e(TAG, "installAppatPie: " + apkPath)
            copyApkFile(openSession, apkPath)
        }
//        ToastUtil.show("正在安装应用")
        install(context, openSession)
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun copyApkFile(session: PackageInstaller.Session, apkFilePath: String?): Boolean {
        var success = false
        val apkFile = File(apkFilePath)
        try {
            val out =
                session.openWrite(FileUtils.getFileName(apkFilePath!!), 0, apkFile.length())
            val input = FileInputStream(apkFile)
            var read = 0
            val buffer = ByteArray(65536)
            while (true) {
                read = input.read(buffer)
                if (read == -1) {
                    session.fsync(out)
                    success = true
                    out.close()
                    input.close()
                    break
                }
                out.write(buffer, 0, read)
            }
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e("fht", "copyApkFile" + e.message)
        } catch (e: RuntimeException) {
            session.abandon()
        }
//        Log.e("fht", "copyApkFile success = $success")
        return success
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    fun install(context: Activity, session: PackageInstaller.Session) {
        try {
            val intent = Intent(context, InstallActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT
            )
            session.commit(pendingIntent.intentSender)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, "install: " + e.message)
        }
    }

//    private fun install( context: Context,packageInstaller: PackageInstaller, sessionId: Int) {
//        try {
//            val session = packageInstaller.openSession(sessionId)
//            val intent = Intent(context, InstallResultReceiver::class.java)
//            val pendingIntent = PendingIntent.getBroadcast(
//                context,
//                1, intent,
//                PendingIntent.FLAG_UPDATE_CURRENT
//            )
//            session.commit(pendingIntent.intentSender)
//        } catch (e: IOException) {
//            e.printStackTrace()
//            Log.e(TAG, "install: " + e.message)
//        }
//    }

}