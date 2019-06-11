package com.chimerapps.niddler.ui.model.renderer

import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import javax.swing.JComponent

interface BodyRenderer<T : ParsedNiddlerMessage> {

    val supportsStructure: Boolean
    val supportsPretty: Boolean
    val supportsRaw: Boolean

    fun structured(message: T, reuseComponent: JComponent?): JComponent
    fun pretty(message: T, reuseComponent: JComponent?): JComponent
    fun raw(message: T, reuseComponent: JComponent?): JComponent

}