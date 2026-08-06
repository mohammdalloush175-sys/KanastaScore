package com.mohammdalloush.kanastascore.update

import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object GitHubUpdateChecker {
    const val REPOSITORY_URL = "https://github.com/mohammdalloush175-sys/KanastaScore"
    const val RELEASES_URL = "https://github.com/mohammdalloush175-sys/KanastaScore/releases"
    private const val API_URL = "https://api.github.com/repos/mohammdalloush175-sys/KanastaScore/releases?per_page=20"

    fun check(currentVersion: String): UpdateCheckResult {
        return try {
            val releases = fetchReleases()
            val latest = releases.maxWithOrNull { first, second ->
                VersionComparator.compare(first.version, second.version)
            }
            when {
                latest == null -> UpdateCheckResult.UpToDate(null)
                VersionComparator.isNewer(latest.version, currentVersion) -> {
                    UpdateCheckResult.UpdateAvailable(latest)
                }
                else -> UpdateCheckResult.UpToDate(latest)
            }
        } catch (error: Exception) {
            UpdateCheckResult.Failure(error.message ?: "تعذر الاتصال بخدمة تحديثات GitHub")
        }
    }

    private fun fetchReleases(): List<UpdateRelease> {
        val connection = (URL(API_URL).openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 12_000
            readTimeout = 15_000
            setRequestProperty("Accept", "application/vnd.github+json")
            setRequestProperty("X-GitHub-Api-Version", "2022-11-28")
            setRequestProperty("User-Agent", "KanastaScore-Android-Updater")
            useCaches = false
        }

        try {
            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader()?.use { it.readText() }.orEmpty()
            if (responseCode !in 200..299) {
                throw IllegalStateException("GitHub API HTTP $responseCode")
            }

            val json = JSONArray(body)
            return buildList {
                for (index in 0 until json.length()) {
                    val item = json.getJSONObject(index)
                    if (item.optBoolean("draft", false)) continue
                    val tagName = item.optString("tag_name").trim()
                    if (tagName.isBlank()) continue
                    val assets = item.optJSONArray("assets")
                    var apkUrl: String? = null
                    if (assets != null) {
                        for (assetIndex in 0 until assets.length()) {
                            val asset = assets.getJSONObject(assetIndex)
                            val assetName = asset.optString("name")
                            if (assetName.endsWith(".apk", ignoreCase = true)) {
                                apkUrl = asset.optString("browser_download_url").takeIf { it.isNotBlank() }
                                break
                            }
                        }
                    }
                    add(
                        UpdateRelease(
                            tagName = tagName,
                            version = tagName.removePrefix("v").removePrefix("V"),
                            title = item.optString("name").ifBlank { tagName },
                            notes = item.optString("body"),
                            publishedAt = item.optString("published_at"),
                            releaseUrl = item.optString("html_url").ifBlank { RELEASES_URL },
                            downloadUrl = apkUrl,
                            prerelease = item.optBoolean("prerelease", false),
                        ),
                    )
                }
            }
        } finally {
            connection.disconnect()
        }
    }
}
