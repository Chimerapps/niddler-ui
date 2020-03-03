package com.chimerapps.niddler.ui.util.ext

fun <T> MutableList<T>.swap(index1: Int, index2: Int) {
    val item = this[index1]
    this[index1] = this[index2]
    this[index2] = item
}