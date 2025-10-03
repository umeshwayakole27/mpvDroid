package live.uwapps.mpvkt.ui.utils

import java.io.File
import live.uwapps.mpvkt.util.SearchText

data class SearchResult(
    val file: File,
    val score: Double
)

object FileSearchUtils {
    private val videoExtensions = setOf(
        "mp4", "mkv", "avi", "mov", "flv", "wmv", "webm", "m4v", "3gp", "ogv",
        "mpg", "mpeg", "ts", "m2ts", "mts", "vob", "asf", "rm", "rmvb", "divx"
    )

    fun searchVideos(directory: File, query: String): List<File> {
        val qn = SearchText.normalize(query)
        if (qn.isBlank()) return emptyList()

        val results = mutableListOf<SearchResult>()

        directory.walkTopDown().forEach { file ->
            if (file.isFile && file.extension.lowercase() in videoExtensions) {
                val nameN = SearchText.normalize(file.nameWithoutExtension)
                val score = calculateFuzzyScore(nameN, qn)
                if (score > 0.0) {
                    results.add(SearchResult(file, score))
                }
            }
        }

        return results
            .sortedByDescending { it.score }
            .map { it.file }
    }

    private fun calculateFuzzyScore(fileNameN: String, queryN: String): Double {
        // Exact match gets highest score
        if (fileNameN == queryN) return 100.0

        // Contains query as substring gets high score
        if (fileNameN.contains(queryN)) {
            val containsScore = 50.0 + (queryN.length.toDouble() / fileNameN.length) * 25.0
            return containsScore
        }

        // Fuzzy matching - check if all characters from query exist in order
        var queryIndex = 0
        var matchCount = 0

        for (char in fileNameN) {
            if (queryIndex < queryN.length && char == queryN[queryIndex]) {
                queryIndex++
                matchCount++
            }
        }

        if (matchCount == queryN.length) {
            // All query characters found in order
            return (matchCount.toDouble() / fileNameN.length) * 30.0
        }

        // Partial character matching
        val queryChars = queryN.toSet()
        val fileChars = fileNameN.toSet()
        val commonChars = queryChars.intersect(fileChars).size

        if (commonChars > 0) {
            return (commonChars.toDouble() / queryChars.size) * 15.0
        }

        return 0.0
    }
}
