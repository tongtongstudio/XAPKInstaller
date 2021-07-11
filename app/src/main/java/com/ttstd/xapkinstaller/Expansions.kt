package com.ttstd.xapkinstaller

import java.io.Serializable

class Expansions : Serializable {
    //安装地址
    var file: String? = null
    //包内文件地址
    var install_location: String? = null
    //安装位置
    var install_path: String? = null
}