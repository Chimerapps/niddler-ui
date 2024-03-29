package com.chimerapps.discovery.ui

import com.intellij.util.IconUtil
import java.util.Base64
import javax.swing.Icon
import javax.swing.ImageIcon

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

open class Base64SessionIconProvider(
    private val width: Float = 20.0f,
    private val height: Float = 20.0f,
) : SessionIconProvider {

    override fun iconForString(iconString: String): Icon? {
        try {
            val decoded = Base64.getDecoder().decode(iconString)
            val icon = ImageIcon(decoded)
            if (icon.image == null)
                return null

            return IconUtil.scale(icon, null, kotlin.math.min(width / icon.iconWidth, height / icon.iconHeight))
        } catch (e: Throwable) {
            return null
        }
    }

}

open class CompoundSessionIconProvider(vararg initialProviders: SessionIconProvider) : SessionIconProvider {

    private val providers = mutableListOf<SessionIconProvider>().also {
        it.addAll(initialProviders)
    }

    fun addProvider(sessionIconProvider: SessionIconProvider) {
        providers += sessionIconProvider
    }

    override fun iconForString(iconString: String): Icon? {
        providers.forEach { provider ->
            provider.iconForString(iconString)?.let { return it }
        }
        return null
    }

}