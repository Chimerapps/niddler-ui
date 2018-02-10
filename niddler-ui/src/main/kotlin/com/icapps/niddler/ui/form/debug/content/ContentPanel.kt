package com.icapps.niddler.ui.form.debug.content

/**
 * @author nicolaverbeeck
 */
interface ContentPanel {

    var enableListener: ((enabled: Boolean) -> Unit)?

    fun apply(isEnabled: Boolean)

    fun updateEnabledFlag(enabled: Boolean)

}