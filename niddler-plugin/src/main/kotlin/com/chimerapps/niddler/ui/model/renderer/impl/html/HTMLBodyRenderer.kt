package com.chimerapps.niddler.ui.model.renderer.impl.html

import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.model.renderer.textAreaRenderer
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import javax.swing.JComponent


object HTMLBodyRenderer : BodyRenderer<ParsedNiddlerMessage> {

    override val supportsStructure: Boolean = false
    override val supportsPretty: Boolean = false
    override val supportsRaw: Boolean = true

    override fun structured(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        throw IllegalStateException("Structured not supported")
    }

    override fun pretty(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        throw IllegalStateException("Pretty not supported")
    }

    override fun raw(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        return textAreaRenderer(message.getBodyAsString(message.bodyFormat.encoding) ?: "", reuseComponent)
    }
}