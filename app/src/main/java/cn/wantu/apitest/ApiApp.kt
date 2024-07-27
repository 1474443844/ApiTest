package cn.wantu.apitest

import android.app.Application
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

class ApiApp :Application(){
    private lateinit var _sApp: ApiApp
    private var sApp: ApiApp
        get() = _sApp
        private set(value) {
            _sApp = value
        }
    lateinit var okHttpClient: OkHttpClient
        private set
    override fun onCreate() {
        super.onCreate()
        sApp = this
        val logging = HttpLoggingInterceptor()
        logging.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient = OkHttpClient.Builder()
            .addInterceptor(logging)
            .build()
    }

}