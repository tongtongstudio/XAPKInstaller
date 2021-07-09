package com.ttstd.xapkinstaller

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.widget.Toast


@SuppressLint("StaticFieldLeak")
object ToastUtil {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var toast: Toast? = null
    private var mContext: Context? = null

    fun init(context: Context?) {
        mContext = context
        toast = Toast.makeText(mContext, "", Toast.LENGTH_SHORT)
    }

    private var time1 = 0L
    private var time2 = 0L

    fun show(msg: String?) {
        mainHandler.post {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                time2 = System.currentTimeMillis()
                if (time2 - time1 > 3499) {
                    showToast(mContext, msg, Toast.LENGTH_LONG)
                    //                        Log.e("fht", "LENGTH_LONG");
                    time1 = time2
                }
            } else {
                if (toast != null) {
                    toast!!.setText(msg)
                    toast!!.show()
                }
            }
        }
    }

    private var mToast: Toast? = null

    //android 8.0以后限制
    //https://www.jianshu.com/p/d9813ad03d59
    //https://www.jianshu.com/p/050ce052b873
    fun showToast(context: Context?, text: String?, duration: Int) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            Toast.makeText(context, text, duration).show()
        } else {
            if (mToast == null) {
                mToast = Toast.makeText(context, text, duration)
            } else {
                mToast!!.setText(text)
                mToast!!.duration = duration
            }
            mToast!!.show()
        }
    }
}