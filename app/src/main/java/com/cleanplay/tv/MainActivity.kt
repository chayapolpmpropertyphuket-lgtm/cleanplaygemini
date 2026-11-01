package com.cleanplay.tv

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // เริ่มต้นระบบแคชรูปภาพ
        Img.init(this)

        val btnPlay = findViewById<Button>(R.id.btnPlay)
        btnPlay.setOnClickListener {
            Toast.makeText(this, "กำลังเปิดสตรีมมิ่ง...", Toast.LENGTH_SHORT).show()
        }
    }
}