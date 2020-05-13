package com.chimerapps.niddler.ui.model.renderer

import com.chimerapps.niddler.ui.model.renderer.impl.binary.BinaryBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.form.FormEncodedBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.html.HTMLBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.image.ImageBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.json.JsonBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.plain.PlainBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.xml.XMLBodyRenderer
import com.chimerapps.niddler.ui.util.ui.runWriteAction
import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyFormatType
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.UnknownFileType
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import javax.swing.JComponent


interface BodyRenderer<T : ParsedNiddlerMessage> {

    val supportsStructure: Boolean
    val supportsPretty: Boolean
    val supportsRaw: Boolean

    fun structured(message: T, reuseComponent: JComponent?, project: Project): JComponent
    fun pretty(message: T, reuseComponent: JComponent?, project: Project): JComponent
    fun raw(message: T, reuseComponent: JComponent?, project: Project): JComponent

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
        BodyFormatType.FORMAT_HTML -> HTMLBodyRenderer
        else -> null
    }
}

private const val REUSE_COMPONENT_KEY = "niddler_reuse_component_key"
internal const val REUSE_KEY_MONOSPACED_TEXT = "monospaced_text"

internal fun textAreaRenderer(stringData: String, reuseComponent: JComponent?, project: Project, fileType: FileType?): JComponent {
    val editor = (reuseComponent?.getClientProperty(REUSE_KEY_MONOSPACED_TEXT) as? EditorImpl)
            ?: EditorFactory.getInstance().createViewer(EditorFactory.getInstance().createDocument(stringData), project) as EditorImpl

    val document = editor.document
    runWriteAction {
        CommandProcessor.getInstance().executeCommand(project, Runnable {
            document.replaceString(0, document.textLength, stringData)
            editor.caretModel.moveToOffset(0)
        }, null, null, UndoConfirmationPolicy.DEFAULT, document)
    }
    editor.highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, fileType ?: UnknownFileType.INSTANCE)

    return editor.component.also { it.putClientProperty(REUSE_KEY_MONOSPACED_TEXT, editor) }
}

internal inline fun <reified T : JComponent> reuseOrNew(key: String, reuseComponent: JComponent?, componentCreator: () -> T): Pair<JBScrollPane, T> {
    return if (reuseComponent is JBScrollPane && reuseComponent.componentCount != 0
            && reuseComponent.getComponent(0) is T && reuseComponent.getClientProperty(REUSE_COMPONENT_KEY) == key) {
        reuseComponent to reuseComponent.getComponent(0) as T
    } else {
        val component = componentCreator()
        JBScrollPane(component).also { it.putClientProperty(REUSE_COMPONENT_KEY, key) } to component
    }
}