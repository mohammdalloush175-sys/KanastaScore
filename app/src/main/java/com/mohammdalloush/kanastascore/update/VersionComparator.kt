package com.mohammdalloush.kanastascore.update

object VersionComparator {
    fun isNewer(candidate: String, current: String): Boolean = compare(candidate, current) > 0

    fun compare(first: String, second: String): Int {
        val left = parse(first)
        val right = parse(second)
        val maxSize = maxOf(left.numbers.size, right.numbers.size)
        repeat(maxSize) { index ->
            val leftPart = left.numbers.getOrElse(index) { 0 }
            val rightPart = right.numbers.getOrElse(index) { 0 }
            if (leftPart != rightPart) return leftPart.compareTo(rightPart)
        }

        if (left.preRelease == null && right.preRelease != null) return 1
        if (left.preRelease != null && right.preRelease == null) return -1
        return comparePreRelease(left.preRelease, right.preRelease)
    }

    private fun comparePreRelease(first: String?, second: String?): Int {
        if (first == null && second == null) return 0
        val leftParts = first.orEmpty().split('.', '-', '_').filter { it.isNotBlank() }
        val rightParts = second.orEmpty().split('.', '-', '_').filter { it.isNotBlank() }
        val maxSize = maxOf(leftParts.size, rightParts.size)
        repeat(maxSize) { index ->
            val left = leftParts.getOrNull(index) ?: return -1
            val right = rightParts.getOrNull(index) ?: return 1
            val leftNumber = left.toIntOrNull()
            val rightNumber = right.toIntOrNull()
            val result = when {
                leftNumber != null && rightNumber != null -> leftNumber.compareTo(rightNumber)
                leftNumber != null -> -1
                rightNumber != null -> 1
                else -> left.compareTo(right, ignoreCase = true)
            }
            if (result != 0) return result
        }
        return 0
    }

    private fun parse(raw: String): ParsedVersion {
        val cleaned = raw.trim().removePrefix("v").removePrefix("V")
        val main = cleaned.substringBefore('-')
        val preRelease = cleaned.substringAfter('-', missingDelimiterValue = "")
            .ifBlank { null }
        val numbers = main.split('.')
            .map { part -> part.takeWhile(Char::isDigit).toIntOrNull() ?: 0 }
        return ParsedVersion(numbers, preRelease)
    }

    private data class ParsedVersion(
        val numbers: List<Int>,
        val preRelease: String?,
    )
}
