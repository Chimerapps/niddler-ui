package com.icapps.niddler.ui.form.components

import java.awt.Component

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
interface UIComponent {

    val asComponent: Component

    fun invalidate()

    fun repaint()

}