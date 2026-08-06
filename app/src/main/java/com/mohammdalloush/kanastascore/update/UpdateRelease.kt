package com.mohammdalloush.kanastascore.update

data class UpdateRelease(
    val tagName: String,
    val version: String,
    val title: String,
    val notes: String,
    val publishedAt: String,
    val releaseUrl: String,
    val downloadUrl: String?,
    val prerelease: Boolean,
)

sealed interface UpdateCheckResult {
    data class UpdateAvailable(val release: UpdateRelease) : UpdateCheckResult
    data class UpToDate(val latestRelease: UpdateRelease?) : UpdateCheckResult
    data class Failure(val message: String) : UpdateCheckResult
}
