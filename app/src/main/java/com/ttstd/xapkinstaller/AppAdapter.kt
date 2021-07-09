package com.ttstd.xapkinstaller

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class AppAdapter(context: Context) : RecyclerView.Adapter<AppAdapter.AppHolder>() {
    private val TAG: String? = AppAdapter::class.simpleName
    var AppList: MutableList<AppInfo> = ArrayList()
    val mContext = context

    class AppHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        holder.iv_icon.setImageDrawable(info.icon)
        holder.tv_label.setText(info.label)
        holder.tv_version.setText(info.version)
        holder.tv_pkgname.setText(info.pkg)
        holder.tv_filepath.setText(info.filename)
        holder.tv_size.setText(info.size)
    }

    override fun getItemCount(): Int {
        return if (AppList.size == 0) 0 else AppList.size
//        return 10
    }

    fun setData(list: MutableList<AppInfo>) {
        this.AppList = list
        notifyDataSetChanged()
    }
}