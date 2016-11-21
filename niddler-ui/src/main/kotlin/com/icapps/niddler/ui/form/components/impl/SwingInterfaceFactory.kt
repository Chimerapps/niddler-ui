package com.icapps.niddler.ui.form.components.impl

import com.icapps.niddler.ui.form.InterfaceFactory
import com.icapps.niddler.ui.form.components.SplitPane
import com.icapps.niddler.ui.form.components.TabComponent

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class SwingInterfaceFactory : InterfaceFactory {

    override fun createSplitPane(): SplitPane {
        return SwingSplitPane()
    }

    override fun createTabComponent(): TabComponent {
        return SwingTabComponent()
    }

}