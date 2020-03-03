package com.chimerapps.niddler.ui.util.ext

fun String.trimToNull(): String? {
    val trimmed = trim()
    if (trimmed.isEmpty()) return null
    return trimmed
}

fun String.headerCase(): String {
    return split('-').map { it.capitalize() }.joinToString("-")
}