package cn.wantu.apitest.util

import cn.wantu.apitest.ApiApp
import okhttp3.OkHttpClient

object UrlParseUtils {
    private lateinit var okHttpClient: OkHttpClient
    val userAgent =
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/72.0.3626.121 Safari/537.36"

    init {
        okHttpClient = ApiApp.okHttpClient()
    }

}