package com.cleanplay.tv

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: PosterAdapter
    private val mediaList = mutableListOf<MediaItemModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Img.init(this)

        // โฟกัสเริ่มต้นที่ปุ่มเล่นเดโม
        val btnDemoPlay = findViewById<Button>(R.id.btnDemoPlay)
        btnDemoPlay?.requestFocus()

        btnDemoPlay?.setOnClickListener {
            playVideo("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4")
        }

        // ปุ่มเปิดหน้า SMB แลด
        findViewById<Button>(R.id.btnFiles)?.setOnClickListener {
            startActivity(Intent(this, SmbBrowserActivity::class.java))
        }

        // ปุ่มเปิด Dialog เพิ่มลิงก์ W3U / M3U / MP4
        findViewById<Button>(R.id.btnAddUrl)?.setOnClickListener {
            showAddUrlDialog()
        }

        // ปุ่มตั้งค่าเชื่อมต่อ Jellyfin / Emby Server
        findViewById<Button>(R.id.btnServerConfig)?.setOnClickListener {
            showServerConfigDialog()
        }

        // ตั้งค่า RecyclerView แนวนอน
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        adapter = PosterAdapter(mediaList) { item ->
            playVideo(item.streamUrl)
        }
        recyclerView.adapter = adapter

        // โหลดข้อมูลที่บันทึกไว้ในเครื่อง
        loadSavedMedia()
    }

    private fun playVideo(url: String) {
        val intent = Intent(this, PlayerActivity::class.java).apply {
            putExtra("video_url", url)
        }
        startActivity(intent)
    }

    private fun showAddUrlDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }
        
        val inputTitle = EditText(this).apply { hint = "ชื่อรายการ / ช่อง" }
        val inputUrl = EditText(this).apply { hint = "ลิงก์สตรีม (MP4, HLS, M3U8, W3U)" }
        
        container.addView(inputTitle)
        container.addView(inputUrl)

        AlertDialog.Builder(this)
            .setTitle("เพิ่มลิงก์มีเดียใหม่")
            .setView(container)
            .setPositiveButton("บันทึก") { _, _ ->
                val title = inputTitle.text.toString().trim()
                val url = inputUrl.text.toString().trim()
                if (title.isNotEmpty() && url.isNotEmpty()) {
                    mediaList.add(MediaItemModel(System.currentTimeMillis().toString(), title, "", url))
                    adapter.notifyDataSetChanged()
                    saveMediaList()
                    Toast.makeText(this, "เพิ่มรายการสำเร็จ!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "กรุณากรอกข้อมูลให้ครบ", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun showServerConfigDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(40, 20, 40, 20)
        }
        
        val inputServer = EditText(this).apply { hint = "Server URL (เช่น http://192.168.1.50:8096)" }
        val inputKey = EditText(this).apply { hint = "API Key / Access Token" }
        
        container.addView(inputServer)
        container.addView(inputKey)

        AlertDialog.Builder(this)
            .setTitle("เชื่อมต่อ Jellyfin / Emby")
            .setView(container)
            .setPositiveButton("เชื่อมต่อ") { _, _ ->
                val server = inputServer.text.toString().trim()
                val key = inputKey.text.toString().trim()
                if (server.isNotEmpty() && key.isNotEmpty()) {
                    Toast.makeText(this, "กำลังดึงข้อมูลจากเซิร์ฟเวอร์...", Toast.LENGTH_SHORT).show()
                    MediaService.fetchJellyfinLibrary(server, key) { fetchedList ->
                        runOnUiThread {
                            if (fetchedList.isNotEmpty()) {
                                mediaList.addAll(fetchedList)
                                adapter.notifyDataSetChanged()
                                Toast.makeText(this, "โหลดสำเร็จ ${fetchedList.size} รายการ", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(this, "ไม่พบข้อมูล หรือเชื่อมต่อไม่สำเร็จ", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("ยกเลิก", null)
            .show()
    }

    private fun saveMediaList() {
        val prefs = getSharedPreferences("CleanPlayPrefs", Context.MODE_PRIVATE)
        val data = mediaList.joinToString("|||") { "${it.title}===${it.streamUrl}" }
        prefs.edit().putString("saved_media", data).apply()
    }

    private fun loadSavedMedia() {
        val prefs = getSharedPreferences("CleanPlayPrefs", Context.MODE_PRIVATE)
        val saved = prefs.getString("saved_media", "") ?: ""
        
        mediaList.clear()
        if (saved.isNotEmpty()) {
            saved.split("|||").forEach { itemStr ->
                val parts = itemStr.split("===")
                if (parts.size == 2) {
                    mediaList.add(MediaItemModel(System.currentTimeMillis().toString(), parts[0], "", parts[1]))
                }
            }
        }

        // ค่าเริ่มต้นถ้ายังไม่มีรายการ
        if (mediaList.isEmpty()) {
            mediaList.add(MediaItemModel("1", "Big Buck Bunny", "", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
            mediaList.add(MediaItemModel("2", "Tears of Steel", "", "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/TearsOfSteel.mp4"))
        }
        adapter.notifyDataSetChanged()
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