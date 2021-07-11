package com.ttstd.xapkinstaller

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView


class AppAdapter(context: Activity) : RecyclerView.Adapter<AppAdapter.AppHolder>() {
    private val TAG: String? = AppAdapter::class.simpleName
    var AppList: MutableList<AppInfo> = ArrayList()
    val mContext = context

    class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val iv_root: ConstraintLayout = itemView.findViewById(R.id.root)
        var iv_icon: ImageView = itemView.findViewById(R.id.iv_icon)
        var tv_label: TextView = itemView.findViewById(R.id.tv_label)
        var tv_version: TextView = itemView.findViewById(R.id.tv_version)
        var tv_filepath: TextView = itemView.findViewById(R.id.tv_filename)
        var tv_size: TextView = itemView.findViewById(R.id.tv_size)
        var tv_pkgname: TextView = itemView.findViewById(R.id.tv_pkgname)
        var bt_install: Button = itemView.findViewById(R.id.bt_install)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppHolder {
        return AppHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_appinfo, parent, false)
        )
    }

    override fun onBindViewHolder(holder: AppHolder, position: Int) {
        val info: AppInfo = AppList.get(position)
        holder.iv_root.setOnClickListener {

        }
        holder.iv_icon.setImageDrawable(info.icon)
        holder.tv_label.setText(info.label)
        holder.tv_version.setText(info.version)
        holder.tv_pkgname.setText(info.pkg)
        holder.tv_filepath.setText(info.filename)
        holder.tv_size.setText(info.size)
        holder.bt_install.setOnClickListener {
            if (info.apkFile) {
                AppUtils.installAPK(info.filename.toString())
            } else {
                val builder = AlertDialog.Builder(mContext)
                    .setTitle("安装XAPK")
                    .setMessage("如果XAPK文件过大需要一段时间解压，是否安装？")
                    .setIcon(info.icon)
                    .setCancelable(false)
                    .setNegativeButton("取消", { dialogInterface, i ->
//                        dialogInterface.dismiss()
                    })
                    .setPositiveButton("安装", { dialogInterface, i ->
//                        val intent = Intent(mContext, InstallActivity::class.java)
//                        intent.putExtra(InstallActivity.FILE_PATH, info.filename.toString())
//                        mContext.startActivity(intent)
                        AppUtils.installXAPK(info.filename.toString())
                    })
                builder.show()
            }
        }
    }

    override fun getItemCount(): Int {
        return if (AppList.size == 0) 0 else AppList.size
//        return 10
    }

    fun setData(list: MutableList<AppInfo>) {
        this.AppList = list
    }
}