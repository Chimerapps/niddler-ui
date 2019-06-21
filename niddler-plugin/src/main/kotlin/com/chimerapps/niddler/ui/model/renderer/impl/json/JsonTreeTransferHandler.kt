package com.chimerapps.niddler.ui.model.renderer.impl.json

import com.chimerapps.niddler.ui.model.renderer.impl.json.JsonNode
import com.google.gson.GsonBuilder
import com.intellij.util.ui.EmptyClipboardOwner
import java.awt.datatransfer.Clipboard
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.StringSelection
import javax.swing.JComponent
import javax.swing.JTree
import javax.swing.TransferHandler

internal class JsonTreeTransferHandler(private val delegate: TransferHandler) : TransferHandler() {

    override fun exportToClipboard(comp: JComponent, clip: Clipboard, action: Int) {
        if (comp !is JTree) {
            delegate.exportToClipboard(comp, clip, action)
            return
        }
        val node = (comp.selectionPath?.lastPathComponent as? JsonNode<*>)
        if (node == null) {
            delegate.exportToClipboard(comp, clip, action)
            return
        }
        val text = GsonBuilder().setPrettyPrinting().serializeNulls().create().toJson(node.jsonElement)
        clip.setContents(StringSelection(text), EmptyClipboardOwner.INSTANCE)
    }

    override fun getSourceActions(c: JComponent?): Int = COPY

    override fun canImport(support: TransferSupport?): Boolean = false

    override fun canImport(comp: JComponent?, transferFlavors: Array<out DataFlavor>?): Boolean = false

}