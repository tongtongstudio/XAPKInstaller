package com.ttstd.xapkinstaller

import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class InstallActivity : AppCompatActivity() {
    private val TAG: String? = InstallActivity::class.simpleName

    companion object {
        val FILE_PATH: String = "FILE_PATH"
        val ACTION: String = BuildConfig.APPLICATION_ID + ".InstallActivity.ACTION"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_install)
        val filePath = intent.getStringArrayListExtra(FILE_PATH)
        if (!filePath.isNullOrEmpty()) {
            AppUtils.installAppatPie(this, filePath)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        val extras = intent!!.extras
        val status = intent.getIntExtra(
            PackageInstaller.EXTRA_STATUS,
            PackageInstaller.STATUS_FAILURE
        )
        val STATUS = intent.getStringExtra(PackageInstaller.EXTRA_STATUS)
        val PACKAGE_NAME = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
        val SESSION_ID = intent.getStringExtra(PackageInstaller.EXTRA_SESSION_ID)
        val STATUS_MESSAGE = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val LEGACY_STATUS = intent.getStringExtra("android.content.pm.extra.LEGACY_STATUS")
        when (status) {
            //系统签名可以不要重新startIntent
            PackageInstaller.STATUS_PENDING_USER_ACTION -> {
                val startIntent = extras!![Intent.EXTRA_INTENT] as Intent?
                startActivity(startIntent)
            }
            PackageInstaller.STATUS_SUCCESS -> {
                ToastUtil.show("安装成功")
                finish()
            }
            PackageInstaller.STATUS_FAILURE, PackageInstaller.STATUS_FAILURE_ABORTED, PackageInstaller.STATUS_FAILURE_BLOCKED, PackageInstaller.STATUS_FAILURE_CONFLICT, PackageInstaller.STATUS_FAILURE_INCOMPATIBLE, PackageInstaller.STATUS_FAILURE_INVALID, PackageInstaller.STATUS_FAILURE_STORAGE -> {
                ToastUtil.show("安装失败：$STATUS_MESSAGE")
                finish()
                Log.d(TAG, "Install failed! $STATUS , $STATUS_MESSAGE")
            }
            else -> {

            }
        }
    }
}