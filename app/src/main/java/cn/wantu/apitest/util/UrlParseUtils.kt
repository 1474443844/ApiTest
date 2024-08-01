package cn.wantu.apitest.util

import androidx.annotation.Keep
import com.google.gson.Gson
import java.net.URL

object UrlParseUtils {
    val userAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/127.0.0.0 Safari/537.36 Edg/127.0.0.0"

    fun lanzou(url: String, pwd: String = "") = Gson().fromJson<LanouApi>(
        OkHttpUtils.get("https://lanzou.uyclouds.com/v1/parseByUrl?type=old&url=$url&pwd=$pwd")
            .excuteString(), LanouApi::class.java
    ).url

    fun ilanzou(url: String, pwd: String = ""): String {
        val host = "https://${URL(url).host}/"
        val softInfo = OkHttpUtils.get(url).header("User-Agent", userAgent).excuteString()
        "<iframe.*?name=\"[\\s\\S]*?\"\\ssrc=\"/(.*?)\"".toRegex()
            .find(softInfo!!)?.groupValues?.get(1)?.let { link ->
            val signInfo =
                OkHttpUtils.get(host + link).header("User-Agent", userAgent).excuteString()
            "url\\s*:\\s*'/(.*?)'[\\s\\S]*'sign':'(.*?)'".toRegex()
                .find(signInfo!!)?.groupValues?.let {
                println(it)
                val downInfo = Gson().fromJson(
                    OkHttpUtils.post(host + it[1]).formDataBody(
                        mapOf(
                            "action" to "downprocess",
                            "signs" to "?ctdf",
                            "sign" to it[2]
                        )
                    ).header("User-Agent", userAgent).excuteString(), Lanzou::class.java
                )
                return "${downInfo.dom}/file/${downInfo.url}"
            }
        }
        return ""
    }

    @Keep
    data class LanouApi(val code: Int, val url: String)

    @Keep
    data class Lanzou(val zt: Int, val dom: String, val url: String)
}