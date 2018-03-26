package com.icapps.niddler.lib.utils

/**
 * @author nicolaverbeeck
 */
internal fun <T> Iterable<T>.split(block: (T) -> Boolean): Pair<List<T>, List<T>> {
    val left = mutableListOf<T>()
    val right = mutableListOf<T>()
    forEach {
        if (block(it))
            left += it
        else
            right += it
    }
    return left to right
}