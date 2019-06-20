package com.chimerapps.niddler.ui.model.renderer

import com.chimerapps.niddler.ui.model.renderer.impl.binary.BinaryBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.form.FormEncodedBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.image.ImageBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.json.JsonBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.plain.PlainBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.xml.XMLBodyRenderer
import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyFormatType
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBFont
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JTextArea

interface BodyRenderer<T : ParsedNiddlerMessage> {

    val supportsStructure: Boolean
    val supportsPretty: Boolean
    val supportsRaw: Boolean

    fun structured(message: T, reuseComponent: JComponent?): JComponent
    fun pretty(message: T, reuseComponent: JComponent?): JComponent
    fun raw(message: T, reuseComponent: JComponent?): JComponent

}

//TODO extensions
fun bodyRendererForFormat(format: BodyFormat): BodyRenderer<ParsedNiddlerMessage>? {
    return when (format.type) {
        BodyFormatType.FORMAT_JSON -> JsonBodyRenderer
        BodyFormatType.FORMAT_PLAIN -> PlainBodyRenderer
        BodyFormatType.FORMAT_IMAGE -> ImageBodyRenderer
        BodyFormatType.FORMAT_BINARY -> BinaryBodyRenderer
        BodyFormatType.FORMAT_FORM_ENCODED -> FormEncodedBodyRenderer
        BodyFormatType.FORMAT_XML -> XMLBodyRenderer
        else -> null
    }
}

internal fun textAreaRenderer(stringData: String, reuseComponent: JComponent?): JComponent {
    val component = reuseOrNew(reuseComponent) {
        JTextArea().also {
            it.isEditable = false
            it.font = JBFont.create(Font("Monospaced", 0, 10))
        }
    }

    val doc = component.second.document
    doc.remove(0, doc.length)
    doc.insertString(0, stringData, null)
    return component.first
}

internal inline fun <reified T : JComponent> reuseOrNew(reuseComponent: JComponent?, componentCreator: () -> T): Pair<JBScrollPane, T> {
    return if (reuseComponent is JBScrollPane && reuseComponent.componentCount != 0 && reuseComponent.getComponent(0) is T) {
        reuseComponent to reuseComponent.getComponent(0) as T
    } else {
        val component = componentCreator()
        JBScrollPane(component) to component
    }
}