package com.cleanplay.tv

import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

object Net {
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
}