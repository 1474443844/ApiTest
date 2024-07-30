package cn.wantu.apitest.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.io.File
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider

object ApkUtils {
    fun installApk(context: Context, apkFilePath: String) {
        val apkFile = File(apkFilePath)
        if (!apkFile.exists()) {
            // APK 文件不存在，处理错误
            return
        }

        val intent = Intent(Intent.ACTION_VIEW)
        val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apkFile).apply {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
        } else {
            Uri.fromFile(apkFile)
        }

        intent.setDataAndType(apkUri, "application/vnd.android.package-archive")
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        // 检查是否有权限安装未知来源的应用
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!context.packageManager.canRequestPackageInstalls()) {
                // 跳转到设置页面以请求安装未知应用的权限
                val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent.data = Uri.parse("package:${context.packageName}")
                context.startActivity(intent)
                return
            }
        }
        context.startActivity(intent)
    }
}