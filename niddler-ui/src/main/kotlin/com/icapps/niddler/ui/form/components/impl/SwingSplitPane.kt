package com.icapps.niddler.ui.form.components.impl

import com.icapps.niddler.ui.form.components.SplitPane
import java.awt.Component
import javax.swing.JSplitPane

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
class SwingSplitPane : JSplitPane(), SplitPane {

    override val asComponent: Component
        get() = this

    override var left: Component
        get() = leftComponent
        set(value) {
            setLeftComponent(value)
        }

    override var right: Component
        get() = rightComponent
        set(value) {
            setRightComponent(value)
        }

    override var resizePriority: Double
        get() = resizeWeight
        set(value) {
            resizeWeight = value
        }

}