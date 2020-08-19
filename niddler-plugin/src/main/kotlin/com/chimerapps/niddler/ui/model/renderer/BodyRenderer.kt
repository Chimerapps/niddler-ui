package com.chimerapps.niddler.ui.model.renderer

import com.chimerapps.niddler.ui.model.renderer.impl.binary.BinaryBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.form.FormEncodedBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.html.HTMLBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.image.ImageBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.json.JsonBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.plain.PlainBodyRenderer
import com.chimerapps.niddler.ui.model.renderer.impl.xml.XMLBodyRenderer
import com.chimerapps.niddler.ui.util.ui.dispatchMain
import com.chimerapps.niddler.ui.util.ui.runWriteAction
import com.icapps.niddler.lib.model.BodyFormat
import com.icapps.niddler.lib.model.BodyFormatType
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.ide.highlighter.XmlFileType
import com.intellij.json.JsonFileType
import com.intellij.json.JsonLanguage
import com.intellij.json.editor.folding.JsonFoldingBuilder
import com.intellij.lang.xml.XMLLanguage
import com.intellij.lang.xml.XmlFoldingBuilder
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.FoldingModel
import com.intellij.openapi.editor.ex.DocumentEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.editor.impl.DocumentImpl
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileTypes.FileType
import com.intellij.openapi.fileTypes.UnknownFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiFileFactory
import com.intellij.ui.components.JBScrollPane
import java.util.IdentityHashMap
import javax.swing.JComponent


interface BodyRenderer<T : ParsedNiddlerMessage> {

    val supportsStructure: Boolean
    val supportsPretty: Boolean
    val supportsRaw: Boolean

    fun structured(message: T, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent
    fun pretty(message: T, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent
    fun raw(message: T, reuseComponent: JComponent?, project: Project, requestFocus: Boolean): JComponent

    fun prettyText(bodyData: Any?): String

}

//TODO extensions
fun bodyRendererForFormat(format: BodyFormat): BodyRenderer<ParsedNiddlerMessage>? {
    return bodyRendererForFormat(format.type)
}

//TODO extensions
fun bodyRendererForFormat(formatType: BodyFormatType): BodyRenderer<ParsedNiddlerMessage>? {
    return when (formatType) {
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

private val projectEditors = IdentityHashMap<Project, EditorImpl>()

internal fun textAreaRenderer(stringData: String, reuseComponent: JComponent?, project: Project, fileType: FileType?, requestFocus: Boolean): JComponent {
    val editor = projectEditors.getOrPut(project) {
        (EditorFactory.getInstance().createViewer(EditorFactory.getInstance().createDocument(""), project) as EditorImpl).also {
            Disposer.register(project, Disposable {
                projectEditors.remove(project)?.let { editor -> EditorFactory.getInstance().releaseEditor(editor) }
            })
        }
    }

    val document = editor.document
    (document as? DocumentImpl)?.setAcceptSlashR(true)
    runWriteAction {
        CommandProcessor.getInstance().executeCommand(project, Runnable {
            document.replaceString(0, document.textLength, stringData)
            editor.caretModel.moveToOffset(0)

            editor.foldingModel.runBatchFoldingOperation { editor.foldingModel.clearFoldRegions() }
            if (fileType != null) {
                buildCodeFolding(fileType, project, stringData, document, editor.foldingModel)
            }
            if (requestFocus) {
                dispatchMain {
                    editor.contentComponent.requestFocus()
                }
            }
        }, null, null, UndoConfirmationPolicy.DEFAULT, document)
    }

    editor.highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, fileType ?: UnknownFileType.INSTANCE)
    return editor.component
}

fun buildCodeFolding(fileType: FileType, project: Project, stringData: String, document: DocumentEx, foldingModel: FoldingModel) {
    when (fileType) {
        is JsonFileType -> {
            val psiFile = PsiFileFactory.getInstance(project).createFileFromText(JsonLanguage.INSTANCE, stringData)
            val regions = JsonFoldingBuilder().buildFoldRegions(psiFile.node, document)
            foldingModel.runBatchFoldingOperationDoNotCollapseCaret {
                regions.forEach { region ->
                    foldingModel.addFoldRegion(region.range.startOffset, region.range.endOffset, region.placeholderText ?: "")
                }
            }
        }
        is XmlFileType -> {
            val psiFile = PsiFileFactory.getInstance(project).createFileFromText(XMLLanguage.INSTANCE, stringData)
            val regions = XmlFoldingBuilder().buildFoldRegions(psiFile, document, true)
            foldingModel.runBatchFoldingOperationDoNotCollapseCaret {
                regions.forEach { region ->
                    foldingModel.addFoldRegion(region.range.startOffset, region.range.endOffset, region.placeholderText ?: "")
                }
            }
        }

    }
}

internal inline fun <reified T : JComponent> reuseOrNew(project: Project, key: String, reuseComponent: JComponent?, componentCreator: () -> T): Pair<JBScrollPane, T> {
    return if (reuseComponent is JBScrollPane && reuseComponent.componentCount != 0
            && reuseComponent.getComponent(0) is T && reuseComponent.getClientProperty(REUSE_COMPONENT_KEY) == key) {
        reuseComponent to reuseComponent.getComponent(0) as T
    } else {
        val component = componentCreator()
        JBScrollPane(component).also { it.putClientProperty(REUSE_COMPONENT_KEY, key) } to component
    }
}