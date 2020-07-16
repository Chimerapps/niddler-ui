package com.chimerapps.niddler.ui.util.localization

import java.util.Locale
import java.util.ResourceBundle

object Localization {

    private val bundle = ResourceBundle.getBundle("/translations/translations", Locale.getDefault())

    fun getString(key: String): String {
        return bundle.getString(key)
    }

}