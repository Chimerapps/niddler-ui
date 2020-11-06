package com.chimerapps.niddler.ui.component.debug

import com.chimerapps.niddler.ui.model.renderer.bodyRendererForFormat
import com.chimerapps.niddler.ui.model.renderer.impl.html.HTMLBodyRenderer
import com.chimerapps.niddler.ui.util.ext.headerCase
import com.chimerapps.niddler.ui.util.ui.ensureMain
import com.chimerapps.niddler.ui.util.ui.runWriteAction
import com.icapps.niddler.lib.debugger.model.DebugMessage
import com.icapps.niddler.lib.debugger.model.DebugRequest
import com.icapps.niddler.lib.debugger.model.DebugResponse
import com.icapps.niddler.lib.debugger.model.breakpoint.DebugActionHandler
import com.icapps.niddler.lib.model.BodyFormatType
import com.icapps.niddler.lib.model.classifier.BodyClassifier
import com.icapps.niddler.lib.model.classifier.GuessingBodyParser
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
import java.awt.BorderLayout
import java.util.Base64
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Future
import javax.swing.JPanel

class DebugItemWindow(private val project: Project,
                      private val disposable: Disposable,
                      private val bodyClassifier: BodyClassifier) : JPanel(BorderLayout()), DebugActionHandler {

    private val editor: EditorImpl
    private val document: DocumentEx
    private lateinit var requestFuture: Future<DebugRequest>
    private lateinit var responseFuture: Future<DebugResponse>

    init {
        editor = (EditorFactory.getInstance().createViewer(EditorFactory.getInstance().createDocument(""), project) as EditorImpl).also { editor ->
            Disposer.register(disposable, {
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

        val stringBody = makeBodyString(debugRequest)

        val messageString = buildString {
            append(message)
            if (stringBody != null) append(stringBody)
        }
        initMessagePreamble(messageString)
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

        val stringBody = makeBodyString(debugResponse)

        val messageString = buildString {
            append(message)
            if (stringBody != null) append(stringBody)
        }
        initMessagePreamble(messageString)
        responseFuture = CompletableFuture()
    }

    private fun buildHeaders(into: StringBuilder, headers: Map<String, List<String>>) {
        into.append('\n')
        headers.forEach { (key, values) ->
            values.forEach { value -> into.append(key.headerCase()).append(": ").append(value).append('\n') }
        }
    }

    private fun initMessagePreamble(preamble: String) {
        runWriteAction {
            CommandProcessor.getInstance().executeCommand(project, {
                document.replaceString(0, document.textLength, preamble)
                editor.caretModel.moveToOffset(0)
            }, null, null, UndoConfirmationPolicy.DO_NOT_REQUEST_CONFIRMATION, document)
        }
    }

    override fun handleDebugRequest(request: DebugRequest): DebugRequest {
        val executionWaiter = CountDownLatch(1)
        ensureMain {
            setMessage(request)
            executionWaiter.countDown()
        }
        executionWaiter.await()
        return requestFuture.get()
    }

    override fun handleDebugResponse(request: DebugResponse): DebugResponse {
        val executionWaiter = CountDownLatch(1)
        ensureMain {
            setMessage(request)
            executionWaiter.countDown()
        }
        executionWaiter.await()
        return responseFuture.get()
    }

    private fun makeBodyString(message: DebugMessage): String? {
        val concreteBody = message.bodyMimeType?.let(bodyClassifier::classifyFormat)?.let { classifierResult ->
            when (classifierResult.format.type) {
                BodyFormatType.FORMAT_JSON,
                BodyFormatType.FORMAT_XML,
                BodyFormatType.FORMAT_PLAIN,
                BodyFormatType.FORMAT_HTML,
                BodyFormatType.FORMAT_EMPTY,
                BodyFormatType.FORMAT_FORM_ENCODED,
                BodyFormatType.FORMAT_BINARY -> {
                    GuessingBodyParser(classifierResult, message.encodedBody?.let { base64 -> Base64.getUrlDecoder().decode(base64) }).determineBodyType()
                }
                else -> null
            }
        }
        return concreteBody?.let { body ->
            body.data?.let { data ->
                bodyRendererForFormat(body.type)?.let { renderer ->
                    when {
                        renderer.supportsPretty || renderer is HTMLBodyRenderer -> renderer.prettyText(data)
                        else -> null
                    }
                }
            }
        }
    }

}