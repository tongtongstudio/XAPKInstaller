package com.ttstd.xapkinstaller

import android.graphics.drawable.Drawable
import java.io.Serializable

class AppInfo : Serializable {
    var icon: Drawable? = null
    var label: String? = null
    var version: String? = null
    var filename: String? = null
    var size: String? = null
    var pkg: String? = null
}