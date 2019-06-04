package com.chimerapps.niddler.ui.model

import com.intellij.ide.util.PropertiesComponent

object AppPreferences {

    private const val NIDDLER_PREFIX = "com.chimerapps.niddler."

    private val properties = PropertiesComponent.getInstance()

    fun get(key: String, default: Int): Int = properties.getInt("$NIDDLER_PREFIX$key", default)
    fun put(key: String, value: Int, default: Int = Int.MIN_VALUE) = properties.setValue("$NIDDLER_PREFIX$key", value, default)

    fun get(key: String, default: Float): Float = properties.getFloat("$NIDDLER_PREFIX$key", default)
    fun put(key: String, value: Float, default: Float = Float.MIN_VALUE) = properties.setValue("$NIDDLER_PREFIX$key", value, default)

    fun get(key: String, default: String): String = properties.getValue("$NIDDLER_PREFIX$key", default)
    fun put(key: String, value: String, default: String = "") = properties.setValue("$NIDDLER_PREFIX$key", value, default)

    fun get(key: String, default: Boolean): Boolean = properties.getBoolean("$NIDDLER_PREFIX$key", default)
    fun put(key: String, value: Boolean, default: Boolean = false) = properties.setValue("$NIDDLER_PREFIX$key", value, default)

}