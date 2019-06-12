package com.chimerapps.niddler.ui.model.renderer.impl.binary

import com.chimerapps.niddler.ui.component.view.HexViewer
import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.model.renderer.reuseOrNew
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import javax.swing.JComponent

object BinaryBodyRenderer : BodyRenderer<ParsedNiddlerMessage> {

    override val supportsStructure: Boolean = false
    override val supportsPretty: Boolean = false
    override val supportsRaw: Boolean = true

    override fun structured(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        throw IllegalStateException("Structured not support")
    }

    override fun pretty(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        throw IllegalStateException("Structured not support")
    }

    override fun raw(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        val component = reuseOrNew(reuseComponent) { HexViewer().also { it.postInit() } }
        component.second.setData(message.getBodyAsBytes)
        return component.first
    }
}