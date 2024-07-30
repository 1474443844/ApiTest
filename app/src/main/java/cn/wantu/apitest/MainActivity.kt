package cn.wantu.apitest

import android.app.AlertDialog
import android.app.ProgressDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import cn.wantu.apitest.data.ApiTestConfig
import cn.wantu.apitest.util.ApkUtils
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
                val json = Gson().fromJson(result, ApiTestConfig::class.java)
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
                                    withContext(Dispatchers.Main) {
                                        progressDialog.setMessage("Downloading $fileName...")
                                        progressDialog.show()
                                    }
                                    // 更新
                                    val request2 = Request.Builder()
                                        .url(json.download)
                                        .build()
                                    client.newCall(request2).execute().use { response2 ->
                                        if (!response2.isSuccessful) throw IOException("Unexpected code $response2")
                                        // 创建目标文件
                                        val file = File(getExternalFilesDir(null), fileName)
                                        // 下载并保存文件
                                        response2.body?.let { body ->
                                            val contentLength = body.contentLength()
                                            body.source().let { source ->
                                                file.outputStream().use { output ->
                                                    val sink = output.sink().buffer()
                                                    val buffer = ByteArray(8192)
                                                    var totalBytesRead: Long = 0
                                                    var bytesRead: Int
                                                    while (source.read(buffer)
                                                            .also { bytesRead = it } != -1
                                                    ) {
                                                        sink.write(buffer, 0, bytesRead)
                                                        totalBytesRead += bytesRead
                                                        withContext(Dispatchers.Main){
                                                            progressDialog.progress =
                                                                (100 * totalBytesRead / contentLength).toInt()
                                                        }
                                                    }
                                                    sink.close()
                                                }
                                            }
                                        }
                                        withContext(Dispatchers.Main) {
                                            progressDialog.dismiss()
                                            AlertDialog.Builder(this@MainActivity)
                                                .setTitle("下载成功")
                                                .setMessage("File downloaded to: ${file.absolutePath}")
                                                .setPositiveButton("安装") { _, _ ->
                                                    ApkUtils.installApk(this@MainActivity, file.absolutePath)
                                                }.show()

                                        }
                                        // 下载完成
                                        println("File downloaded to: ${file.absolutePath}")
                                    }

                                }
                            }
                            .setNegativeButton("取消") { dialog, _ ->
                                dialog.dismiss()
                                gotoTestPage()
                            }
                            .setCancelable(false).show()
                    }
                }
            }
        }
//        thread {
////            val request = Request.Builder()
////                .url("https://docs.wty5.cn/App/ApiTestConfig.json")
////                .build()
////            val response = client.newCall(request).execute()
////            val result = response.body?.string()
////            val json = Gson().fromJson(result, ApiTestConfig::class.java)
////            val pi = packageManager.getPackageInfo(packageName, 0)
////            val currentVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
////                pi.longVersionCode
////            }else{
////                pi.versionCode.toLong()
////            }
////            if (currentVersion < json.versionCode){
////                handler.post {
//                    AlertDialog.Builder(this).setTitle("有新版本v${json.version}").setMessage(json.content)
//                        .setPositiveButton("更新"){_,_->
//                            thread {
//                                handler.post {
//                                    progressDialog.show()
//                                }
//                                try {
//                                    // 更新
//                                    val request2 = Request.Builder()
//                                        .url(json.download)
//                                        .build()
//                                    client.newCall(request2).execute().use { response2 ->
//                                        if (!response2.isSuccessful) throw IOException("Unexpected code $response2")
//                                        // 获取文件名
//                                        val fileName = "ApiTest_${json.version}.apk"
//                                        // 创建目标文件
//                                        val file = File(getExternalFilesDir(null), fileName)
//                                        // 下载并保存文件
//                                        response2.body?.let { body ->
//                                            val contentLength = body.contentLength()
//                                            body.source().let { source ->
//                                                file.outputStream().use { output ->
//                                                    val sink = output.sink().buffer()
//                                                    val buffer = ByteArray(8192)
//                                                    var totalBytesRead: Long = 0
//                                                    var bytesRead: Int
//                                                    while (source.read(buffer).also { bytesRead = it } != -1) {
//                                                        sink.write(buffer, 0, bytesRead)
//                                                        totalBytesRead += bytesRead
//                                                        handler.post {
//                                                            progressDialog.progress = (100*totalBytesRead/contentLength).toInt()
//                                                        }
//                                                    }
//                                                    sink.close()
//                                                }
//                                            }
//                                        }
//                                        handler.post {
//                                            progressDialog.dismiss()
//                                            AlertDialog.Builder(this).setTitle("下载成功")
//                                                .setMessage("File downloaded to: ${file.absolutePath}")
//                                                .setPositiveButton("安装"){ _, _ ->
//
//                                                }.show()
//
//                                        }
//                                        // 下载完成
//                                        println("File downloaded to: ${file.absolutePath}")
//                                    }
//                                } catch (e: Exception) {
//                                    e.printStackTrace()
//                                }
//                            }
//                        }
//                        .setNegativeButton("取消"){dialog,_->
//                            dialog.dismiss()
//                            gotoTestPage()
//                        }
//                        .setCancelable(false).show()
//                }
//
//            } else{
//                // 无新版本
//                gotoTestPage()
//            }

        //}

    }

    private fun gotoTestPage() {

    }
}
