package cn.wantu.apitest

import android.app.Application
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class ApiApp : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

    companion object {
        //情况二：声明延迟初始化属性
        private lateinit var instance: ApiApp
        private lateinit var okHttpClient: OkHttpClient
        fun instance() = instance
        fun okHttpClient() = okHttpClient
    }

}