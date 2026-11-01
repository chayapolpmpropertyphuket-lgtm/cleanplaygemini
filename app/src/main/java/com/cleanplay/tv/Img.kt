package com.cleanplay.tv

import android.content.Context
import java.io.File

object Img {
    lateinit var cacheDir: File
    fun init(ctx: Context) {
        cacheDir = File(ctx.cacheDir, "img_cache").apply { mkdirs() }
    }
}