package com.chimerapps.niddler.ui.component.debug

import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.breakpoint.DebugActionHandler
import com.intellij.openapi.Disposable
import com.intellij.openapi.command.CommandProcessor
import com.intellij.openapi.command.UndoConfirmationPolicy
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.ex.DocumentEx
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileTypes.PlainTextFileType
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import org.jetbrains.io.response
import java.awt.BorderLayout
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import javax.swing.JPanel

class DebugItemWindow(private val project: Project) : JPanel(BorderLayout()), DebugActionHandler {

    private val editor: EditorImpl
    private val document: DocumentEx
    private lateinit var requestFuture: Future<DebugRequest>
    private lateinit var responseFuture: Future<DebugResponse>

    init {
        editor = (EditorFactory.getInstance().createViewer(EditorFactory.getInstance().createDocument(""), project) as EditorImpl).also { editor ->
            Disposer.register(project, Disposable {
                EditorFactory.getInstance().releaseEditor(editor)
            })
        }
        document = editor.document
        editor.highlighter = EditorHighlighterFactory.getInstance().createEditorHighlighter(project, PlainTextFileType.INSTANCE)

        add(editor.component, BorderLayout.CENTER)
    }

    fun setMessage(debugRequest: DebugRequest) {
        val message = buildString {
            append(debugRequest.method)
            append(' ')
            append(debugRequest.url)
            append('\n')
            debugRequest.headers?.let { buildHeaders(this, it) }
            append('\n')
        }
        initMessagePreamble(message)
        requestFuture = CompletableFuture()
    }

    fun setMessage(debugResponse: DebugResponse) {
        val message = buildString {
            append(debugResponse.code)
            append(' ')
            append(debugResponse.message)
            append('\n')
            debugResponse.headers?.let { buildHeaders(this, it) }
            append('\n')
        }
        initMessagePreamble(message)
        responseFuture = CompletableFuture()
    }

    private fun buildHeaders(into: StringBuilder, headers: Map<String, List<String>>) {
        into.append('\n');
        headers.forEach { (key, values) ->
            values.forEach { value -> into.append(key).append(':').append(value).append('\n') }
        }
    }

    private fun initMessagePreamble(preamble: String) {
        CommandProcessor.getInstance().executeCommand(project, Runnable {
            document.replaceString(0, document.textLength, preamble)
            editor.caretModel.moveToOffset(0)
        }, null, null, UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION, document)
    }

    override fun handleDebugRequest(request: DebugRequest): DebugRequest {
        setMessage(request)
        return requestFuture.get()
    }

    override fun handleDebugResponse(request: DebugResponse): DebugResponse {
        setMessage(request)
        return responseFuture.get()
    }

}