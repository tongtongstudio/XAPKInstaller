package com.ttstd.xapkinstaller

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.AsyncTask
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import java.io.File
import java.util.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {
    private val TAG: String? = MainActivity::class.simpleName
    var permissions = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.INSTALL_PACKAGES,
        Manifest.permission.REQUEST_INSTALL_PACKAGES,
    )
    private val REQUEST_PERMISSION_CODE = 200
    lateinit var recyclerView: RecyclerView
    lateinit var swipeRefresh: SwipeRefreshLayout
    var adapter: AppAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkSelfPermission()
        initView()
        initData()
    }

    private fun checkSelfPermission() {
        val mPermissionList: MutableList<String> = java.util.ArrayList()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            for (s in permissions) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_DENIED
                ) {
                    mPermissionList.add(s)
                } else {
                    val scanTask = ScanTask()
                    scanTask.execute()
                }
            }
            if (mPermissionList.size > 0) { //????????????????????????????????????
                ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    REQUEST_PERMISSION_CODE
                )
            }
        } else {
            val scanTask = ScanTask()
            scanTask.execute()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE) {
            //?????????????????????????????????Switch?????????????????????
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val scanTask = ScanTask()
                scanTask.execute()
            } else if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this@MainActivity,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    )
                ) {
                    ToastUtil.show("????????????????????????????????????????????????")
                    checkSelfPermission()
                } else {
                    ToastUtil.show("??????????????????????????????????????????")
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    intent.data = Uri.parse("package:" + this.packageName)
                    startActivity(intent)
                }
                swipeRefresh.setRefreshing(false)
            }
        }
    }

    fun initView() {
        swipeRefresh = findViewById(R.id.swipeRefreshLayout)
        recyclerView = findViewById(R.id.recyclerView)
    }

    fun initData() {
        swipeRefresh.setOnRefreshListener {
            ScanTask().execute()
        }
        swipeRefresh.isRefreshing = true
        adapter = AppAdapter(this)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adapter
    }


    var filePaths = ArrayList<String>()

    inner class ScanTask : AsyncTask<Void, String, List<String>>() {
        override fun doInBackground(vararg params: Void?): List<String> {
            val s1 = System.currentTimeMillis()
            filePaths.clear()
            val fileList: MutableList<String> = java.util.ArrayList()
            val rootPath = Environment.getExternalStorageDirectory().path + File.separator
            val file = File(rootPath)
            if (file.exists()) {
                val list = LinkedList<File>()
                var files = file.listFiles()
                for (file2 in files) {
                    publishProgress(file2.absolutePath)
                    if (file2.isDirectory) {
                        list.add(file2)
                    } else {
                        if (isApkFile(file2.absolutePath)) {
                            fileList.add(file2.absolutePath)
                        }
                    }
                }
                var temp_file: File
                while (!list.isEmpty()) {
                    temp_file = list.removeFirst()
                    files = temp_file.listFiles()
                    for (file2 in files) {
                        publishProgress(file2.absolutePath)
                        if (file2.isDirectory) {
                            list.add(file2)
                        } else {
                            if (isApkFile(file2.absolutePath)) {
                                fileList.add(file2.absolutePath)
                            }
                        }
                    }
                }
            } else {
                Log.e("traverseFolder1", "???????????????!")
            }
            Log.e(
                "ScanTask",
                "doInBackground: " + "Scan time = " + (System.currentTimeMillis() - s1) + "ms"
            )
            return fileList
        }

        override fun onProgressUpdate(vararg values: String?) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: List<String>?) {
            super.onPostExecute(result)
            swipeRefresh.isRefreshing = false
            ReadInfoTask().execute(result)
        }
    }


    inner class ReadInfoTask : AsyncTask<List<String>, AppInfo, List<AppInfo>>() {
        override fun doInBackground(vararg p0: List<String>?): List<AppInfo> {
            val appInfoList: MutableList<AppInfo> = ArrayList()
            for (path in p0[0]!!) {
                val info: AppInfo? = AppUtils.getFileInfo(path)
                if (info != null) {
                    appInfoList.add(info)
                }
            }
            return appInfoList
        }

        override fun onProgressUpdate(vararg values: AppInfo?) {
            super.onProgressUpdate(*values)
        }

        override fun onPostExecute(result: List<AppInfo>?) {
            super.onPostExecute(result)
            adapter?.setData(result as MutableList<AppInfo>)
            adapter?.notifyDataSetChanged()
        }


    }

    fun isApkFile(file: String): Boolean {
        return  file.endsWith(".xapk")
//                ||file.endsWith(".apk")
    }

}