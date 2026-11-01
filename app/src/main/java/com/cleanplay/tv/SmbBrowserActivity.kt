package com.cleanplay.tv

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.hierynomus.smbj.SMBClient
import com.hierynomus.smbj.auth.AuthenticationContext
import kotlin.concurrent.thread

class SmbBrowserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val listView = ListView(this)
        setContentView(listView)

        thread {
            try {
                val client = SMBClient()
                val connection = client.connect("192.168.1.100")
                val auth = AuthenticationContext("username", "password".toCharArray(), "domain")
                val session = connection.authenticate(auth)
                val share = session.connectShare("shared_folder") as com.hierynomus.smbj.share.DiskShare
                
                val fileList = share.list("*").map { it.fileName }
                
                runOnUiThread {
                    listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, fileList)
                    listView.setOnItemClickListener { _, _, position, _ ->
                        val selectedFile = fileList[position]
                        Toast.makeText(this, "เลือกไฟล์: $selectedFile", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this, "เชื่อมต่อ SMB ไม่สำเร็จ: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}