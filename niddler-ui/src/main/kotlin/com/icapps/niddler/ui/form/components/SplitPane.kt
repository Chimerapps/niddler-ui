package com.icapps.niddler.ui.form.components

import java.awt.Component
import javax.swing.JComponent

/**
 * @author Nicola Verbeeck
 * @date 21/11/16.
 */
interface SplitPane : UIComponent {

    var left: Component
    var right: Component

    var resizePriority: Double

}