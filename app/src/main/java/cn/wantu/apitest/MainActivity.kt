package cn.wantu.apitest

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import cn.wantu.apitest.data.ApiTestConfig
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Request
import okio.IOException
import okio.buffer
import okio.sink
import java.io.File

class MainActivity : ComponentActivity() {
    companion object {
        lateinit var json: ApiTestConfig
    }

    private val getUnknownAppSourcesPermission =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                startActivity(newInstallIntent())
            } else {
                Toast.makeText(this, "请求权限失败", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val client = ApiApp.okHttpClient()
        val progressDialog = ProgressDialog(this)
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDialog.setCancelable(false)
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        coroutineScope.launch {
            withContext(Dispatchers.Default) {
                val request = Request.Builder()
                    .url("https://docs.wty5.cn/App/ApiTestConfig.json")
                    .build()
                val response = client.newCall(request).execute()
                val result = response.body?.string()
                json = Gson().fromJson(result, ApiTestConfig::class.java)
                val pi = packageManager.getPackageInfo(packageName, 0)
                val currentVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    pi.longVersionCode
                } else {
                    pi.versionCode.toLong()
                }
                if (currentVersion < json.versionCode) {
                    coroutineScope.launch(Dispatchers.Main) {
                        AlertDialog.Builder(this@MainActivity).setTitle("有新版本v${json.version}")
                            .setMessage(json.content)
                            .setPositiveButton("更新") { _, _ ->
                                coroutineScope.launch(Dispatchers.Default) {
                                    val fileName = "ApiTest_${json.version}.apk"
                                    // 创建目标文件
                                    val file = File(getExternalFilesDir(null), fileName)
                                    withContext(Dispatchers.Main) {
                                        progressDialog.setMessage("Downloading $fileName...")
                                        progressDialog.show()
                                    }
                                    val request2 = Request.Builder()
                                        .url(json.download)
                                        .build()
                                    // 下载并显示进度
                                    client.newCall(request2).execute().use { response2 ->
                                        if (!response2.isSuccessful) throw IOException("Unexpected code $response2")
                                        // 下载并保存文件
                                        response2.body?.let { body ->
                                            val contentLength = body.contentLength()
                                            body.source().let { source ->
                                                file.outputStream().use { output ->
                                                    source.skip(json.skip.toLong())
                                                    val sink = output.sink().buffer()
                                                    val buffer = ByteArray(8192)
                                                    var totalBytesRead: Long = 0
                                                    var bytesRead: Int
                                                    while (source.read(buffer)
                                                            .also { bytesRead = it } != -1
                                                    ) {
                                                        sink.write(buffer, 0, bytesRead)
                                                        totalBytesRead += bytesRead
                                                        withContext(Dispatchers.Main) {
                                                            progressDialog.progress =
                                                                (100 * totalBytesRead / contentLength).toInt()
                                                        }
                                                    }
                                                    sink.close()
                                                }
                                            }
                                        }
                                        // 下载完成
                                        withContext(Dispatchers.Main) {
                                            progressDialog.dismiss()
                                            AlertDialog.Builder(this@MainActivity)
                                                .setTitle("下载成功")
                                                .setMessage("File downloaded to: ${file.absolutePath}")
                                                .setPositiveButton("安装") { _, _ ->
                                                    installApk()
                                                }.show()
                                        }
                                    }
                                }
                            }
                            .setNegativeButton("取消") { dialog, _ ->
                                dialog.dismiss()
                                gotoTestPage()
                            }
                            .setCancelable(false).show()
                    }
                } else {
                    gotoTestPage()
                }
            }
        }
    }

    private fun gotoTestPage() {

    }

    private fun newInstallIntent() = Intent(Intent.ACTION_VIEW).apply {
        val file = File(getExternalFilesDir(null), "ApiTest_${json.version}.apk")
        val apkUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(this@MainActivity, "${packageName}.fileprovider", file)
                .apply {
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
        } else {
            Uri.fromFile(file)
        }
        setDataAndType(apkUri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }

    private fun installApk() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!packageManager.canRequestPackageInstalls()) {
                // 跳转到设置页面以请求安装未知应用的权限
                val intent1 = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                intent1.data = Uri.parse("package:${packageName}")
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
                    startActivityForResult(intent1, -1)
                } else {
                    getUnknownAppSourcesPermission.launch(intent1)
                    return
                }
            }
        }
        startActivity(newInstallIntent())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            startActivity(newInstallIntent())
        } else {
            Toast.makeText(this, "请求权限失败", Toast.LENGTH_SHORT).show()
        }
    }
}
