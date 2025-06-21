package com.example.mediaplay.utils

import com.example.mediaplay.retrofit.M3UItem
object M3UParser {
    fun parse(m3uContent: String): List<M3UItem> {
        val lines = m3uContent.lines()
        val itemList = mutableListOf<M3UItem>()
        var currentTitle: String? = null
        var currentImage: String? = null
        var currentGroup: String? = null

        for (line in lines) {
            val trimmedLine = line.trim()
            if (trimmedLine.startsWith("#EXTINF:", ignoreCase = true)) {
                currentTitle = trimmedLine.substringAfter(",").trim()

                val logoRegex = """tvg-logo="([^"]*)"""".toRegex()
                val groupRegex = """group-title="([^"]*)"""".toRegex()

                currentImage = logoRegex.find(trimmedLine)?.groups?.get(1)?.value
                currentGroup = groupRegex.find(trimmedLine)?.groups?.get(1)?.value
            } else if (trimmedLine.startsWith("http", ignoreCase = true) && currentTitle != null) {
                itemList.add(M3UItem(
                    title = currentTitle,
                    url = trimmedLine,
                    imageUrl = currentImage,
                    groupTitle = currentGroup
                ))
                currentTitle = null
                currentImage = null
                currentGroup = null
            }
        }
        return itemList
    }
}
