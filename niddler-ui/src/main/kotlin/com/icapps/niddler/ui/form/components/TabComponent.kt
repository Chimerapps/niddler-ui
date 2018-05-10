package com.icapps.niddler.ui.form.components

import java.awt.Component
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
interface TabComponent : UIComponent {

    fun addTab(title: String, component: JComponent)

    val numTabs: Int
    val currentTab: Int

    operator fun get(index: Int): Component

}