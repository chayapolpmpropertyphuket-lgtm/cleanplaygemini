package com.cleanplay.tv

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class MediaItem(val title: String, val url: String)

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Img.init(this)

        // ปุ่มเล่นเดโมจาก Hero Banner
        findViewById<Button>(R.id.btnDemoPlay).setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("video_url", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            }
            startActivity(intent)
        }

        // ปุ่มเปิดหน้าจัดการไฟล์แลน (SMB)
        findViewById<Button>(R.id.btnFiles).setOnClickListener {
            startActivity(Intent(this, SmbBrowserActivity::class.java))
        }

        // รายการมีเดียตัวอย่างที่ไม่ใช่หน้าเปล่า แสดงผลเป็นการ์ดพร้อมคลิกเล่นได้จริง
        val sampleList = listOf(
            MediaItem("Big Buck Bunny (MP4)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            MediaItem("Elephant Dream (HD)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
            MediaItem("For Bigger Blazes", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
            MediaItem("Tears of Steel (Sci-Fi)", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerView.adapter = MediaAdapter(sampleList) { item ->
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("video_url", item.url)
            }
            startActivity(intent)
        }
    }
}

class MediaAdapter(private val items: List<MediaItem>, private val onClick: (MediaItem) -> Unit) :
    RecyclerView.Adapter<MediaAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}