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

    fun load(url: String?, target: ImageView, fallback: Int = 0) {
        if (url.isNullOrEmpty()) { 
            if (fallback != 0) target.setImageResource(fallback) else target.setImageDrawable(null)
            return 
        }
        target.tag = url
        memoryCache.get(url)?.let { target.setImageBitmap(it); return }
        if (fallback != 0) target.setImageResource(fallback)

        pool.execute {
            val bmp = fetchBitmap(url)
            if (bmp != null) {
                memoryCache.put(url, bmp)
                handler.post {
                    if (target.tag == url) {
                        target.setImageBitmap(bmp)
                        target.alpha = 0f
                        target.animate().alpha(1f).setDuration(150).start()
                    }
                }
            }
        }
    }

    private fun fetchBitmap(url: String): Bitmap? {
        val hash = md5(url)
        val file = File(cacheDir, hash)
        if (!file.exists() || file.length() == 0L) {
            try {
                Net.client.newCall(Request.Builder().url(url).build()).execute().use { resp ->
                    if (resp.isSuccessful) {
                        resp.body?.byteStream()?.use { input ->
                            file.outputStream().use { output -> input.copyTo(output) }
                        }
                    }
                }
            } catch (e: Exception) { return null }
        }
        return BitmapFactory.decodeFile(file.absolutePath)
    }

    private fun md5(s: String): String {
        val digest = MessageDigest.getInstance("MD5")
        return digest.digest(s.toByteArray()).joinToString("") { "%02x".format(it) }
    }
}