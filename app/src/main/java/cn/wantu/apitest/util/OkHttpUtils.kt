package cn.wantu.apitest.util

import android.net.Uri
import cn.wantu.apitest.ApiApp
import okhttp3.FormBody
import okhttp3.Headers
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.RequestBody


object OkHttpUtils {
    private var client: OkHttpClient = ApiApp.okHttpClient()
    fun get(url: String) = GetBuilder(url)

    fun post(url: String) = PostBuilder(url)
    class GetBuilder(private val url: String) {
        private lateinit var _params: HashMap<String, String>
        private val _headers = Headers.Builder()

        fun excuteString() = excute().body?.string()

        fun excute() = client.newCall(build()).execute()

        private fun build() = okhttp3.Request.Builder()
            .url(appendParams())
            .headers(_headers.build())
            .build()

        fun param(k: String, v: String) {
            if (!::_params.isInitialized) {
                _params = LinkedHashMap()
            }
            _params[k] = v
        }

        fun params(params: HashMap<String, String>): GetBuilder {
            this._params = params
            return this
        }

        fun header(k: String, v: String): GetBuilder {
            _headers[k] = v
            return this
        }

        fun addHeader(k: String, v: String): GetBuilder {
            _headers.add(k, v)
            return this
        }

        fun headers(headers: Headers): GetBuilder {
            for ((name, value) in headers) {
                _headers[name] = value
            }
            return this
        }

        private fun appendParams(): String {
            if (!::_params.isInitialized) {
                return url
            }
            val builder = Uri.parse(url).buildUpon()
            for ((key, value) in _params) {
                builder.appendQueryParameter(key, value)
            }
            return builder.build().toString()
        }
    }
    class PostBuilder(private val url: String) {
        private val _headers = Headers.Builder()
        private var _requestBody: RequestBody? = null

        fun excuteString() = excute().body?.string()

        fun excute() = client.newCall(build()).execute()

        private fun build() = okhttp3.Request.Builder()
            .url(url)
            .post(_requestBody ?: throw IllegalStateException("RequestBody is required for POST requests."))
            .headers(_headers.build())
            .build()

        fun header(k: String, v: String): PostBuilder {
            _headers[k] = v
            return this
        }

        fun addHeader(k: String, v: String): PostBuilder {
            _headers.add(k, v)
            return this
        }

        fun headers(headers: Headers): PostBuilder {
            for ((name, value) in headers) {
                _headers[name] = value
            }
            return this
        }

        fun jsonBody(json: String): PostBuilder {
            _requestBody = RequestBody.create("application/json; charset=utf-8".toMediaType(), json)
            return this
        }

        fun formDataBody(params: Map<String, String>): PostBuilder {
            val formBodyBuilder = FormBody.Builder()
            params.forEach { (key, value) ->
                formBodyBuilder.add(key, value)
            }
            _requestBody = formBodyBuilder.build()
            return this
        }
    }
}