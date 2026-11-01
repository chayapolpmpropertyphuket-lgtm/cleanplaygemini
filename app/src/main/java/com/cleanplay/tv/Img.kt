package com.cleanplay.tv

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

object Img {
    private val memoryCache = object : android.util.LruCache<String, Bitmap>(
        (Runtime.getRuntime().maxMemory() / 6 / 1024).toInt()
    ) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int = bitmap.byteCount / 1024
    }
    
    private val pool = ThreadPoolExecutor(
        3, 6, 30, TimeUnit.SECONDS,
        LinkedBlockingDeque(),
        { r -> Thread(r, "ImgPool").apply { priority = Thread.MIN_PRIORITY } }
    )
    
    private val handler = Handler(Looper.getMainLooper())
    lateinit var cacheDir: File

    fun init(ctx: Context) {
        cacheDir = File(ctx.cacheDir, "img_cache").apply { mkdirs() }
    }
}