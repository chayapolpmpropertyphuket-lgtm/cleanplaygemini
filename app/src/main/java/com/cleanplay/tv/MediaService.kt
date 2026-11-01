package com.cleanplay.tv

import okhttp3.Request
import org.json.JSONObject

data class MediaItemModel(
    val id: String,
    val title: String,
    val posterUrl: String,
    val streamUrl: String
)

object MediaService {

    // รองรับการแปลงไฟล์ W3U (JSON Format) หรือดึงจาก Server
    fun parseW3UFromJson(jsonString: String): List<MediaItemModel> {
        val list = mutableListOf<MediaItemModel>()
        try {
            val json = JSONObject(jsonString)
            val items = json.optJSONArray("items") ?: return emptyList()
            for (i in 0 until items.length()) {
                val obj = items.getJSONObject(i)
                val title = obj.optString("name", "ไม่มีชื่อ")
                val url = obj.optString("url", "")
                val logo = obj.optString("image", "")
                if (url.isNotEmpty()) {
                    list.add(MediaItemModel(i.toString(), title, logo, url))
                }
            }
        } catch (_: Exception) {}
        return list
    }

    // รองรับการดึง Jellyfin / Emby Library
    fun fetchJellyfinLibrary(serverUrl: String, apiKey: String, callback: (List<MediaItemModel>) -> Unit) {
        val url = "$serverUrl/Items?api_key=$apiKey&IncludeItemTypes=Movie,Episode&Recursive=true&Limit=30"
        Thread {
            try {
                val response = Net.client.newCall(Request.Builder().url(url).build()).execute()
                if (response.isSuccessful) {
                    val json = JSONObject(response.body?.string() ?: "{}")
                    val items = json.getJSONArray("Items")
                    val list = mutableListOf<MediaItemModel>()

                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val id = item.optString("Id")
                        val name = item.optString("Name")
                        val poster = "$serverUrl/Items/$id/Images/Primary?api_key=$apiKey"
                        val stream = "$serverUrl/Videos/$id/stream?static=true&api_key=$apiKey"
                        
                        list.add(MediaItemModel(id, name, poster, stream))
                    }
                    callback(list)
                } else {
                    callback(emptyList())
                }
            } catch (_: Exception) {
                callback(emptyList())
            }
        }.start()
    }
}