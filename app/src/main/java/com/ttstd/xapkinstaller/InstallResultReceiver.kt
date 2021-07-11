package com.ttstd.xapkinstaller

import android.content.BroadcastReceiver
import android.content.Context
import androidx.annotation.RequiresApi
import android.os.Build
import android.content.Intent
import android.content.pm.PackageInstaller
import android.util.Log

class InstallResultReceiver : BroadcastReceiver() {
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    override fun onReceive(context: Context, intent: Intent) {
        // TODO: This method is called when the BroadcastReceiver is receiving
        // an Intent broadcast.
        //throw new UnsupportedOperationException("Not yet implemented");
        val status = intent.getIntExtra(
            PackageInstaller.EXTRA_STATUS,
            PackageInstaller.STATUS_FAILURE
        )
        if (status == PackageInstaller.STATUS_SUCCESS) {
            // success
            val PACKAGE_NAME = intent.getStringExtra("android.content.pm.extra.PACKAGE_NAME")
            Log.e(TAG, "APP Install Success!")
        } else {
            val msg = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
            Log.e(TAG, "onReceive: " + msg)
        }
        val STATUS = intent.getStringExtra(PackageInstaller.EXTRA_STATUS)
        val PACKAGE_NAME = intent.getStringExtra(PackageInstaller.EXTRA_PACKAGE_NAME)
        val SESSION_ID = intent.getStringExtra(PackageInstaller.EXTRA_SESSION_ID)
        val STATUS_MESSAGE = intent.getStringExtra(PackageInstaller.EXTRA_STATUS_MESSAGE)
        val LEGACY_STATUS = intent.getStringExtra("android.content.pm.extra.LEGACY_STATUS")
        //        Log.e("fht", STATUS);
//        Log.e("fht", PACKAGE_NAME);
//        Log.e("fht", SESSION_ID);
//        Log.e("fht", LEGACY_STATUS);
//        Log.e("fht", STATUS_MESSAGE);
        if (STATUS_MESSAGE != null && STATUS_MESSAGE == "INSTALL_SUCCEEDED") {
            ToastUtil.show(PACKAGE_NAME + "安装成功")
        }
    }

    companion object {
        private const val TAG = "InstallResultReceiver"
    }
}