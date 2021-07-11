package com.ttstd.xapkinstaller

import android.app.Application

/**
 * @Author TongTong
 * @Date 2021/7/11-1:26
 * @Email chrisSpringSmell@gmail.com
 */
class BaseApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        ToastUtil.init(this)
        AppUtils.init(this)
    }
}