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

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Img.init(this)

        // กำหนดโฟกัสเริ่มต้นให้รีโมททีวีสามารถกดใช้งานได้ทันที
        val btnDemoPlay = findViewById<Button>(R.id.btnDemoPlay)
        btnDemoPlay?.requestFocus()

        btnDemoPlay?.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java).apply {
                putExtra("video_url", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
            }
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnFiles)?.setOnClickListener {
            startActivity(Intent(this, SmbBrowserActivity::class.java))
        }

        // รายการมีเดียตัวอย่าง (รองรับการขยายผลเชื่อมต่อ W3U / M3U / Jellyfin)
        val sampleList = listOf(
            MediaItemModel("1", "Big Buck Bunny (MP4)", "", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"),
            MediaItemModel("2", "Elephant Dream (HD)", "", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4"),
            MediaItemModel("3", "For Bigger Blazes", "", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4"),
            MediaItemModel("4", "Tears of Steel (Sci-Fi)", "", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"),
            MediaItemModel("5", "Subaru Outback Sports", "", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/SubaruOutbackSeeTheWorld.mp4")
        )

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        if (recyclerView != null) {
            recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
            recyclerView.adapter = PosterAdapter(sampleList) { item ->
                val intent = Intent(this, PlayerActivity::class.java).apply {
                    putExtra("video_url", item.streamUrl)
                }
                startActivity(intent)
            }
        }
    }
}

class PosterAdapter(private val items: List<MediaItemModel>, private val onClick: (MediaItemModel) -> Unit) :
    RecyclerView.Adapter<PosterAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvTitle: TextView = view.findViewById(R.id.tvPosterTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_poster, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvTitle.text = item.title
        holder.itemView.setOnClickListener { onClick(item) }
    }

    override fun getItemCount() = items.size
}