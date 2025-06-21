package com.example.mediaplay.utils

import android.util.Log
import com.example.mediaplay.retrofit.M3UItem
import java.io.BufferedReader
import java.io.StringReader

object M3UParser {

    fun parse(m3uContent: String): List<M3UItem> {
        val itemList = mutableListOf<M3UItem>()
        var currentTitle: String? = null
        var currentImage: String? = null
        var currentGroup: String? = null

        val reader = BufferedReader(StringReader(m3uContent))
        var line: String?

        try {
            while (reader.readLine().also { line = it } != null) {
                val trimmedLine = line!!.trim()

                if (trimmedLine.startsWith("#EXTINF:", ignoreCase = true)) {
                    currentTitle = trimmedLine.substringAfter(",").trim()

                    // Extrair o logo (se existir)
                    val logoRegex = """tvg-logo="([^"]*)"""".toRegex()
                    currentImage = logoRegex.find(trimmedLine)?.groups?.get(1)?.value

                    // Extrair a categoria
                    val groupRegex = """group-title="([^"]*)"""".toRegex()
                    currentGroup = groupRegex.find(trimmedLine)?.groups?.get(1)?.value

                } else if (trimmedLine.startsWith("http", ignoreCase = true) && currentTitle != null) {
                    itemList.add(
                        M3UItem(
                            title = currentTitle,
                            url = trimmedLine,
                            imageUrl = currentImage,
                            groupTitle = currentGroup
                        )
                    )
                    currentTitle = null
                    currentImage = null
                    currentGroup = null
                }
            }
        } catch (e: Exception) {
            Log.e("M3U_DEBUG", "Erro ao fazer o parsing do M3U: ${e.localizedMessage}")
        } finally {
            reader.close()
        }

        Log.d("M3U_DEBUG", "Total de itens parseados: ${itemList.size}")
        return itemList
    }
}
