package com.chimerapps.niddler.ui.model.renderer.impl.plain

import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.model.renderer.textAreaRenderer
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import javax.swing.JComponent

object PlainBodyRenderer : BodyRenderer<ParsedNiddlerMessage> {

    override val supportsStructure: Boolean = false
    override val supportsPretty: Boolean = true
    override val supportsRaw: Boolean = true

    override fun structured(message: ParsedNiddlerMessage, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent {
        throw IllegalStateException("Structured not supported")
    }

    override fun pretty(message: ParsedNiddlerMessage, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent {
        return raw(message, reuseComponent, project, requestFocus)
    }

    override fun raw(message: ParsedNiddlerMessage, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent {
        return textAreaRenderer(message.message.getBodyAsString(message.bodyFormat.encoding) ?: "", reuseComponent, project, PlainTextFileType.INSTANCE, requestFocus)
    }

    override fun prettyText(bodyData: Any?): String = bodyData as? String ?: ""

}