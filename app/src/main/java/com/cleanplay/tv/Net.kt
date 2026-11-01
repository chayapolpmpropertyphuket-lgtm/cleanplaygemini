package com.cleanplay.tv

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

object Net {
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    fun text(url: String, headers: Map<String, String> = emptyMap(), method: String = "GET", body: String? = null): String {
        val reqB = body?.toRequestBody()
        val builder = Request.Builder().url(url).method(method, reqB)
        headers.forEach { (k, v) -> builder.header(k, v) }
        client.newCall(builder.build()).execute().use { resp ->
            if (!resp.isSuccessful) throw RuntimeException("HTTP ${resp.code}")
            return resp.body?.string() ?: ""
        }
    }
}