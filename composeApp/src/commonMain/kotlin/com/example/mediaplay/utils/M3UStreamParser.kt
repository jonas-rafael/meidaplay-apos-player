package com.example.mediaplay.utils

import com.example.mediaplay.models.MediaItem

object M3UStreamParser {

    private val TAG_REGEX = """([a-zA-Z0-9_-]+)="([^"]*)"""".toRegex()

    /**
     * Faz o parsing de um fluxo de linhas M3U de forma eficiente.
     */
    fun parse(lines: Sequence<String>): List<MediaItem> {
        val itemList = mutableListOf<MediaItem>()
        var currentMetadata: Map<String, String>? = null
        var currentTitle: String? = null

        lines.forEach { line ->
            val trimmedLine = line.trim()
            if (trimmedLine.isEmpty()) return@forEach

            when {
                trimmedLine.startsWith("#EXTINF:", ignoreCase = true) -> {
                    currentMetadata = TAG_REGEX.findAll(trimmedLine)
                        .associate { it.groupValues[1] to it.groupValues[2] }
                    currentTitle = trimmedLine.substringAfterLast(",").trim()
                }
                trimmedLine.startsWith("http", ignoreCase = true) -> {
                    if (currentTitle != null) {
                        val metadata = currentMetadata
                        val group = metadata?.get("group-title") ?: "Sem Grupo"
                        val groupLower = group.lowercase()
                        
                        val type = when {
                            groupLower.contains("filme") || groupLower.contains("movie") || groupLower.contains("cinema") -> "MOVIE"
                            groupLower.contains("serie") || groupLower.contains("episode") || groupLower.contains("temporada") -> "SERIES"
                            else -> "LIVE"
                        }

                        itemList.add(
                            MediaItem(
                                title = currentTitle!!,
                                url = trimmedLine,
                                imageUrl = metadata?.get("tvg-logo"),
                                groupTitle = group,
                                tvgId = metadata?.get("tvg-id"),
                                tvgName = metadata?.get("tvg-name"),
                                contentType = type
                            )
                        )
                    }
                    currentMetadata = null
                    currentTitle = null
                }
            }
        }

        return itemList
    }
}
