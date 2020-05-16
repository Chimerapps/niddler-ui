package com.icapps.niddler.lib.utils

import java.util.LinkedHashMap

internal class LruCache<Key, Value>(val maxItems: Int,
                                    private val internalMap: MutableMap<Key, Value> = object : LinkedHashMap<Key, Value>() {
                                        override fun removeEldestEntry(eldest: MutableMap.MutableEntry<Key, Value>?): Boolean {
                                            return size > maxItems
                                        }
                                    }) : MutableMap<Key, Value> by internalMap