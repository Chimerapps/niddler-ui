package com.chimerapps.niddler.ui.model.renderer.impl.html

import com.chimerapps.niddler.ui.model.renderer.BodyRenderer
import com.chimerapps.niddler.ui.model.renderer.textAreaRenderer
import com.icapps.niddler.lib.model.ParsedNiddlerMessage
import com.intellij.ui.components.JBScrollPane
import javafx.application.Platform
import javafx.embed.swing.JFXPanel
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.web.WebView
import javax.swing.JComponent
import javax.swing.JPanel


object HTMLBodyRenderer : BodyRenderer<ParsedNiddlerMessage> {

    override val supportsStructure: Boolean = false
    override val supportsPretty: Boolean = true
    override val supportsRaw: Boolean = true

    override fun structured(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        throw IllegalStateException("Structured not supported")
    }

    override fun pretty(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        //Dont't reuse webviews. Who likes this anyway...

        val fxPanel = JFXPanel()
        val component = JPanel()
        component.add(fxPanel)

        Platform.setImplicitExit(false)
        Platform.runLater {
            val root = Group()
            val scene = Scene(root)

            val webview = WebView()
            webview.engine.loadContent(message.getBodyAsString(message.bodyFormat.encoding))

            root.children.add(webview)
            fxPanel.scene = scene
        }
        return JBScrollPane(component)
    }

    override fun raw(message: ParsedNiddlerMessage, reuseComponent: JComponent?): JComponent {
        return textAreaRenderer(message.getBodyAsString(message.bodyFormat.encoding) ?: "", reuseComponent)
    }
}