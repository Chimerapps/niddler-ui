package com.icapps.niddler.ui.form

import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.TabComponent
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
interface InterfaceFactory {

    fun createSplitPane(): SplitPane

    fun createTabComponent(): TabComponent

}