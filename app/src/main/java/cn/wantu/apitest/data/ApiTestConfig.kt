package cn.wantu.apitest.data

import androidx.annotation.Keep

@Keep
data class ApiTestConfig(val versionCode: Long, val version: String, val content: String, val download: String, val others: Any)