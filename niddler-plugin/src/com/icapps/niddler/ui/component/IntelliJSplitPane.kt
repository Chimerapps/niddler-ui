package com.icapps.niddler.ui.component

import com.icapps.niddler.ui.form.components.SplitPane
import com.intellij.ui.JBSplitter
import java.awt.Component
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class IntelliJSplitPane : JBSplitter(), SplitPane {

    override val asComponent: Component
        get() = this

    override var left: Component
        get() = firstComponent
        set(value) {
            firstComponent = value as JComponent?
        }

    override var right: Component
        get() = secondComponent
        set(value) {
            secondComponent = value as JComponent?
        }

    override var resizePriority: Double
        get() {
            return -1.0
        }
        set(value) {
        }


}