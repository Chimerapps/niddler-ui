package com.chimerapps.discovery.ui

import javax.swing.Icon

interface SessionIconProvider {

    fun iconForString(iconString: String): Icon?

}

open class DefaultSessionIconProvider : SessionIconProvider {

    override fun iconForString(iconString: String): Icon? {
        return when (iconString) {
            "android" -> IncludedLibIcons.Icons.android
            "apple" -> IncludedLibIcons.Icons.apple
            "dart" -> IncludedLibIcons.Icons.dart
            "flutter" -> IncludedLibIcons.Icons.flutter
            else -> null
        }
    }

}