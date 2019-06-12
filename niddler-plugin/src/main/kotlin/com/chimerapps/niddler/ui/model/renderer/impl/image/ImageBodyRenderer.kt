package com.chimerapps.niddler.ui.model.renderer.impl.image

import com.chimerapps.niddler.ui.component.view.HexViewer
import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.model.renderer.reuseOrNew
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import java.awt.image.BufferedImage
import javax.swing.ImageIcon
import javax.swing.JComponent
import javax.swing.JLabel

object ImageBodyRenderer : BodyRenderer<ParsedNiddlerMessage> {

    override val supportsStructure: Boolean = false
    override val supportsPretty: Boolean = true
    override val supportsRaw: Boolean = true

    override fun structured(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        throw IllegalStateException("Structured not supported")
    }

    override fun pretty(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        val component = reuseOrNew(reuseComponent) { JLabel() }

        val label = component.second
        if (message.bodyData is BufferedImage)
            label.icon = ImageIcon(message.bodyData as BufferedImage)
        else
            label.icon = null

        return component.first
    }

    override fun raw(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        val component = reuseOrNew(reuseComponent) { HexViewer().also { it.postInit() } }
        component.second.setData(message.getBodyAsBytes)
        return component.first
    }
}