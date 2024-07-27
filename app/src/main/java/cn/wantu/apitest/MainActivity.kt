package cn.wantu.apitest

import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import androidx.activity.ComponentActivity
import com.google.gson.Gson
import okhttp3.Request
import okio.BufferedSink
import okio.IOException
import okio.buffer
import okio.sink
import java.io.File
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val app = application as ApiApp
        val client = app.okHttpClient
        thread {
            val request = Request.Builder()
                .url("https://docs.wty5.cn/App/ApiTestConfig.json")
                .build()
            val response = client.newCall(request).execute()
            val result = response.body?.string()
            val json = Gson().fromJson(result, ApiTestConfig::class.java)
            val pi = packageManager.getPackageInfo(packageName, 0)
            val currentVersion = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                pi.longVersionCode
            }else{
                pi.versionCode.toLong()
            }
            if (currentVersion < json.versionCode){
                Handler(mainLooper).post {
                    AlertDialog.Builder(this).setTitle("有新版本v${json.version}").setMessage(json.content)
                        .setPositiveButton("更新"){_,_->
                            thread {
                                try {
                                    // 更新
                                    val request2 = Request.Builder()
                                        .url(json.download)
                                        .build()
                                    client.newCall(request2).execute().use { response2 ->
                                        if (!response2.isSuccessful) throw IOException("Unexpected code $response2")
                                        // 获取文件名
                                        val fileName = "ApiTest_${json.version}.apk"
                                        // 创建目标文件
                                        val file = File(getExternalFilesDir(null), fileName)
                                        // 下载并保存文件
                                        response2.body?.source()?.let { source ->
                                            file.outputStream().use { output ->
                                                val sink: BufferedSink = output.sink().buffer()
                                                sink.writeAll(source)
                                                sink.close()
                                            }
                                        }
                                        // 下载完成
                                        println("File downloaded to: ${file.absolutePath}")
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                        .setNegativeButton("取消"){dialog,_->
                            dialog.dismiss()
                            gotoTestPage()
                        }
                        .setCancelable(false).show()
                }

            } else{
                // 无新版本
                gotoTestPage()
            }

        }

    }
    fun gotoTestPage(){

    }
    data class ApiTestConfig(val versionCode: Long, val version: String, val content: String, val download: String, val others: Any)
}
